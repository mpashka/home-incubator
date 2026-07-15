# browser-plugin-shopping — agent context

Context for AI agents working on this project. Read this first.

## What this is

A personal tool to **collect real marketplace tablet offers (Ozon, Yandex Market) and
turn the messy, ungrouped search feeds into a clean, comparable dataset** (model → variant →
offer + price + "from China" flag), so an AI agent can later pick the best price/quality
option. Live scraping fails against marketplace anti-bot; a **browser extension** running in
the user's own authenticated session sidesteps that. Full motivation: `docs/use-case.md`.

User goal (concrete): buy a tablet — screen > 11", Snapdragon 8-series SoC, best price/quality.
RF prices for the same tablet vary widely; many cheap "slow, from China / global firmware"
listings exist. The tool surfaces and normalizes those.

## Architecture

```
Chrome MV3 extension (collector)                Quarkus backend                    Vue viewer
├─ injected.ts  MAIN world                      ├─ POST /api/ingest  (raw, JSONB)  (reads API)
│   hooks fetch/XHR → raw marketplace JSON       ├─ GET  /api/captures  (paginated)
├─ content.ts   ISOLATED                         ├─ POST /api/normalize/run
│   parses, runs adapter, DOM fallback           │    PayloadTrimmer → LLM (Yandex/Anthropic)
├─ service-worker.ts  durable outbox + badge     │    → ProductModel→Variant→Offer→PriceHistory
└─ popup / options (wishlist)                    ├─ GET  /api/models  (normalized tree)
                                                 └─ POST /api/match/run  (pHash photo clustering)
```

- **Extension ships RAW captures verbatim** (`RawCapture`: marketplace, kind, pageUrl,
  searchQuery, source network|dom, payload, images). The backend owns interpretation, so
  re-parsing survives marketplace schema drift.
- **LLM provider is pluggable** (`shopping.llm.provider`, default **`yandex`** — the user has a
  Yandex AI Studio quota). `OfferExtractor` interface; `YandexOfferExtractor` (OpenAI-compatible
  Chat Completions, `response_format: json_object`, parsed via Jackson) and
  `AnthropicOfferExtractor` (Claude structured output). See `docs/architecture.md`.
- **PayloadTrimmer is essential**: raw YM search payloads are ~2 MB / 750K tokens — over the
  model's 262K context. YM product cards live at `collections.widgets[].product.baobabPayload`
  (title, price, marketSku, modelId, isCrossBorder) + `productPayload` (specs, gallery). The
  trimmer extracts just those → ~7 KB. Ozon payloads (≤ ~200 KB) pass through.
- **Image matching (phase 6)**: `ImageHash` (DCT pHash), variants clustered by near-identical
  photos (Hamming ≤ threshold) into `matchGroupId`.

## Repo layout

| Dir | What | Build |
|---|---|---|
| `extension/` | MV3 collector (TypeScript) | `npm i && npm run build` → `extension/dist`, esbuild (`build.mjs`) |
| `backend/`   | Quarkus 3.15, Java 21 | `./gradlew quarkusDev` (needs JDK 17–21) |
| `viewer/`    | Vue 3 + TS (Vite) | `npm i && npm run dev` |
| `docs/`      | use-case, architecture, mvp-runbook, plan | — |

## How to run (this machine)

Full runbook: **`docs/mvp-runbook.md`**. Essentials:
- **Postgres 18** already runs on `localhost:5432`; dev db+role `shopping`/`shopping`
  (backend `%dev` profile wires the JDBC url — an explicit url disables Dev Services, no Docker).
- **JDK 21** at `~/.jdks/temurin-21` — Gradle 8.10.2 cannot run on the default JDK 25
  (`/opt/java/jdk-21` symlink is broken). Run backend with `JAVA_HOME=~/.jdks/temurin-21`.
- **Yandex creds** `YANDEX_AI_TOKEN` / `YANDEX_FOLDER_ID` are in `~/.profile` (env).
- Backend: `cd backend && JAVA_HOME=~/.jdks/temurin-21 ./gradlew quarkusDev` → http://localhost:8080
- Extension: `cd extension && npm run build`, then `chrome://extensions` → Developer mode →
  Load unpacked → `extension/dist`. Defaults: backend `http://localhost:8080`, collection on.
- After a code change: reload the extension (↻) **and hard-reload the marketplace tab
  (Ctrl+Shift+R)** — old content scripts persist in open tabs otherwise.

## Verify a change (no unit tests yet)

- Extension: `npm run typecheck` + `npm run build`. Pure logic (adapters, pHash, query gen) is
  verifiable offline with node/jsdom.
- Backend: `JAVA_HOME=~/.jdks/temurin-21 ./gradlew compileJava`. End-to-end: POST a capture to
  `/api/ingest`, `POST /api/normalize/run`, inspect `GET /api/models`. Reset:
  `psql ... -c "truncate price_history, variant_image, offer, variant, product_model, raw_capture restart identity cascade;"`

## Gotchas

- **Marketplace endpoints/DOM are reverse-engineered and fragile.** Ozon search is SSR on first
  load; product data arrives via `entrypoint-api.bx/page/json/v2` only on scroll. YM uses
  `/api/resolve` with a normalized entity graph. Selectors/regex (`injected.ts` INTEREST,
  `adapters/*`) may need re-tuning when the sites change. Diagnostic logs:
  `[shopping-collector] …` in the page console; DIAG lines show candidate un-captured endpoints.
- **Context limit**: never send a full raw YM payload to the LLM — always via `PayloadTrimmer`.
- **YM product-detail pages are SSR** — the `/api/resolve` we capture there is near-empty; product
  detail isn't captured. The search feed carries the offers, so this is low priority.
- **Model dedup** (`ProductModel.dedupKey = title|screen|soc`) is LLM-variable — same model can
  split when the LLM guesses SoC/screen/casing differently. Improve via normalized title + YM
  `modelId` (already extracted by the trimmer, not yet used as a key).

## State & plan

See **`docs/plan.md`** for what's done and what's next (kept up to date as work proceeds).
Milestone reached: full pipeline works end-to-end on real data — browse → capture → normalize →
grouped models with per-seller price comparison + "from China" flag.
