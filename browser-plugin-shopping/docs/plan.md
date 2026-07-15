# Work plan

Living checklist. `[x]` done · `[~]` partial · `[ ]` todo.

## Done — foundation (iterations 1–2)

- [x] Extension collector: MV3, TS, esbuild; network hook (MAIN world) + DOM fallback.
- [x] Ozon + Yandex Market adapters (network capture; content-based Ozon listing detection).
- [x] Durable outbox → `POST /api/ingest`; idempotent JSONB persistence.
- [x] Wishlist + template search-query generation (options page).
- [x] Viewer read API (`GET /api/captures`) + Vue list (raw captures).
- [x] LLM normalization — pluggable provider (default Yandex), structured output.
- [x] Image matching — DCT pHash + union-find clustering (`matchGroupId`).
- [x] `PayloadTrimmer` — YM `baobabPayload` extraction (2 MB → ~7 KB), unblocks context limit.
- [x] Toolbar badge indicator (captured count, sync-health colour, search/product tooltip).

## Done — MVP launch (verified live on real data)

- [x] Local run: Postgres dev db, JDK 21, Yandex creds; backend on :8080.
- [x] Extension loaded in real Chrome; anti-bot avoided in the user's session.
- [x] Collection verified: YM search (rich, images), Ozon search (via scroll pagination),
      Ozon product, image extraction (extensionless CDN URLs).
- [x] Normalization verified: browse → capture → grouped models with per-seller price
      comparison + "from China" flag (e.g. Xiaomi Pad 8, Honor Pad 10).
- [x] Fixes found live: image-download User-Agent (CDN 403), `Extension context invalidated`
      guard, Ozon SSR/scroll capture, `page/json/v2` unconditional capture.

## In progress / next

- [x] **(1) Tighten model dedup** — models keyed by the normalized canonical title for BOTH
      marketplaces (SoC/screen dropped — LLM-variable; `modelId` dropped — Yandex assigns a
      distinct one per config → over-splits). Groups one tablet across sellers, China-vs-local,
      AND Ozon-vs-Yandex. Verified: 0 title splits; cross-marketplace models appear (Xiaomi Pad 8
      = 59 offers across both). Residual: "Xiaomi Pad 8" vs "Xiaomi Mi Pad 8" still split by the
      LLM's inconsistent brand wording — future prompt/normalization tweak.
- [x] **(2) Ozon normalization** — no extractor needed. Verified both Ozon DOM (visible cards)
      and Ozon network (`page/json/v2`, double-encoded widget states, ≤ ~260 KB) normalize
      cleanly as-is: real tablets, prices, Ozon URLs, china flag (Honor MagicPad 2, Xiaomi Pad
      8 Pro, Lenovo Legion Y700, …). Ozon payloads fit the context; the LLM parses the encoded
      states. Remaining Ozon work is just draining captures.
- [ ] **(3) Drain backlog + raise the viewer** ← current. Normalize the full capture backlog
      (run batches or enable the scheduled job); build the Vue model view over `GET /api/models`
      (price comparison, china flag, photos, match groups).

## Backlog

- [ ] Wire `matchGroupId` into the model view / offer grouping across marketplaces.
- [ ] YM/Ozon product-detail capture (SSR — extract embedded state) — low priority.
- [ ] Capture applied filters (Snapdragon-8 facet) into `searchQuery`, not just base text.
- [ ] Scheduled normalization + match jobs (flags exist, off by default).
- [ ] Agent-optimizer: rank offers by price/quality over the collected dataset.
- [ ] Harden adapters against marketplace schema drift; add regression fixtures.
