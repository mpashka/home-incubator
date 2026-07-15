# Architecture

See [use-case.md](./use-case.md) for the problem statement.

## Components

```
┌────────────────────── Chrome MV3 extension (collector) ──────────────────────┐
│ injected.ts   MAIN world  — hooks fetch/XHR, forwards raw API bodies          │
│      │ window.postMessage (namespaced, same-origin)                           │
│ content.ts    ISOLATED    — parses JSON, runs marketplace adapter (shallow)   │
│      │ chrome.runtime.sendMessage {CAPTURE}                                    │
│ service-worker.ts         — durable outbox queue, POST /api/ingest, retry      │
│ popup                     — enable toggle, backend URL, counters (no browsing) │
└───────────────────────────────────────────────────────────────────────────────┘
                                   │ HTTP POST /api/ingest { captures: RawCapture[] }
                                   ▼
┌────────────────────────── Quarkus backend ──────────────────────────┐
│ /api/ingest        store raw captures verbatim (idempotent by id)     │
│ LLM normalizer     RawCapture → ProductModel / Variant / Offer        │
│ image matcher      variant de-dup across marketplaces via embeddings   │
│ /api/models …      read API for the viewer                            │
│ DB                 models → variants → offers → price history          │
└───────────────────────────────────────────────────────────────────────┘
                                   │ HTTP (read API)
                                   ▼
                    Vue/TS viewer  — browse models, compare offers, price history
```

## Why these boundaries

- **MAIN vs ISOLATED world.** The hook must run in the page's realm to wrap the site's
  `fetch`/`XHR`. The content script stays isolated (its own module scope, `chrome.*`
  access). They talk over `window.postMessage` with a namespaced envelope.
- **Adapters are shallow.** They only detect page kind, count items, collect image URLs,
  and pull the search query. Full normalization is the backend LLM's job, so the client
  stays robust to schema drift and ships payloads **verbatim** for re-parsing later.
- **Durable outbox.** The MV3 service worker can be killed at any time, so captures live
  in `chrome.storage.local` and flush with retry (on capture + 1-min alarm).

## Marketplace endpoints (reverse-engineered)

- **Ozon:** `www.ozon.ru/api/entrypoint-api.bx/page/json/v2?url=<path>` (and the
  `api.ozon.ru/composer-api.bx/...` equivalent). Response has a `widgetStates` map keyed
  `"<widget>-<id>-<slot>"`; match by **prefix** (`searchResultsV2`, `webPrice`, …). Widget
  values are **JSON strings inside JSON** (double-decode).
- **Yandex Market:** internal `/api/resolve`-style endpoints returning
  `collections`/`results` JSON. Less documented, more volatile → DOM fallback matters more.

> These are undocumented internal APIs; they change without notice. That is exactly why we
> keep payloads raw and normalize server-side.

## Extraction fallback (implemented)

If the network hook misses (schema change, SSR-only data), a DOM parser in the content
script extracts product cards by selector as a `source: "dom"` capture
(`adapters/dom/`). It is a true *fallback*: it runs only when no network capture was
produced for the current URL. A debounced scheduler (MutationObserver + scroll + load)
re-scrapes as the SPA mutates, and a djb2 signature over item ids suppresses re-sending
unchanged results. Selectors are deliberately fragile-tolerant (`scrapeCards` walks up to
the tile holding an image + a `₽` price) — the network path stays primary.

## Normalization & matching (backend, implemented)

- **LLM normalization (phase 5):** `CaptureNormalizer` calls Claude (`claude-opus-4-8`) with
  structured output to extract tablet offers from a raw payload; `NormalizationService`
  upserts them into `ProductModel → Variant → Offer → PriceHistory` with per-level dedup
  keys. The LLM call runs outside any DB transaction.
- **Image matching (phase 6):** each `Variant` accrues photo URLs (`VariantImage`), reduced
  to a 64-bit **perceptual hash** (DCT pHash, pure Java). A union-find pass clusters variants
  whose photos are within a Hamming-distance threshold into `matchGroupId`s — catching the
  same physical tablet across marketplaces even when names differ. pHash nails reused
  manufacturer photos cheaply; semantic CLIP embeddings are the upgrade for different shots.
