# browser-plugin-shopping

Collect actual marketplace offers (Ozon, Yandex Market) while browsing, in your own
authenticated session, then run an AI agent over your own data to pick the best
price/quality option. See **[docs/use-case.md](docs/use-case.md)** for the full motivation
and **[docs/architecture.md](docs/architecture.md)** for the design.

## Layout

| Dir          | What                                                                 | Status |
|--------------|----------------------------------------------------------------------|--------|
| `extension/` | Chrome MV3 collector (TypeScript, esbuild). Captures → backend.       | works (iter. 1) |
| `backend/`   | Quarkus API. `/api/ingest` accepts raw captures; LLM normalize later. | stub |
| `viewer/`    | Vue/TS app to browse collected models, variants, offers, prices.      | stub |

## Extension — build & load

```bash
cd extension
npm install
npm run build        # → extension/dist
```

Then in Chrome: `chrome://extensions` → enable Developer mode → **Load unpacked** →
select `extension/dist`. Open the popup to set the backend URL and toggle collection.
Browse Ozon / Yandex Market; captures are queued and POSTed to `/api/ingest`.

`npm run watch` for incremental rebuilds; `npm run typecheck` for types only.

## Backend

Persists raw captures verbatim (JSONB), idempotent by client `id`, and normalizes them
into a comparable model with an LLM.

- `POST /api/ingest` — accept `{captures: RawCapture[]}` (from the extension outbox).
- `GET /api/ingest/count` — stored capture total.
- `GET /api/captures` — paginated raw captures (viewer list).
- `POST /api/normalize/run` — run one LLM normalization batch now.
- `GET /api/models` — normalized `ProductModel → Variant → Offer` tree (+ `matchGroupId`).
- `POST /api/match/run` — hash pending variant photos and recompute match groups.

**Normalization (phase 5):** `CaptureNormalizer` turns a raw payload into tablet offers via a
pluggable `OfferExtractor`, then `NormalizationService` upserts them into model/variant/offer
+ price history (dedup keys). Runs on a schedule when `shopping.normalize.enabled=true`;
`POST /api/normalize/run` triggers it on demand regardless.

**LLM provider (`shopping.llm.provider`, default `yandex`):**

- **`yandex`** — Yandex AI Studio OpenAI-compatible Chat Completions
  (`https://ai.api.cloud.yandex.net/v1`), model `qwen3.6-35b-a3b/latest`, `response_format:
  json_object` parsed client-side (schema mode is unstable on Yandex). Reads `YANDEX_AI_TOKEN`
  + `YANDEX_FOLDER_ID` from the env (model addressed as `gpt://<folder>/<model>`).
- **`anthropic`** — Claude (`claude-opus-4-8`) with schema-enforced structured output. Needs
  `ANTHROPIC_API_KEY` (or an `ant auth login` profile).

Switch provider/model/creds in `application.properties` (`shopping.llm.*`).

```bash
cd backend
./gradlew quarkusDev   # http://localhost:8080
```

Requirements:
- **JDK 17–21 to *run* Gradle 8.10.2** (Gradle can't run on JDK 25). Compilation targets
  Java 21. Set `JAVA_HOME` to a 21 JDK if your default is newer.
- **Docker** for `quarkusDev`/tests: Quarkus Dev Services auto-starts a throwaway
  PostgreSQL. Without Docker, supply a real datasource via config (see
  `application.properties`).

Verified: `./gradlew compileJava` succeeds (Quarkus 3.15.1). Runtime ingest not yet
exercised here (no container runtime in the build sandbox).

## Viewer

Vue 3 + TS app. Lists stored captures from `GET /api/captures` (paginated, filter by
marketplace / kind / query, thumbnails). Detail (`/api/captures/{id}`) returns the verbatim
payload. Will grow into the normalized model view once phase 5 exists.

```bash
cd viewer
npm install
npm run dev            # http://localhost:5173, proxies /api → :8080
npm run build          # type-check (vue-tsc) + production build
```

## Wishlist & query generation

Open the extension **options page** (popup → "Wishlist…") to describe what you want
(category, keywords, attribute groups). The extension generates search queries as the
cartesian product of the attribute groups and links each to Ozon / Yandex Market search.
Opening a query runs the search, which the collector captures automatically. Pure
client-side (no backend/LLM). See `extension/src/shared/wishlist.ts`.

## Roadmap (by increasing complexity)

1. **Wishlist + search-query generation** — done (extension, template-based).
2. **Persist raw captures** on the backend (idempotent by id) — done (compile-verified).
3. **Viewer read API** — list captures/models — done (compile/build-verified).
4. **DOM fallback** extractor in the content script — done (jsdom-verified).
5. **LLM normalization** — RawCapture → model/variant/offer, de-dup — done (compile-verified).
6. **Image-based variant matching** — perceptual hash (pHash) — done (pHash verified;
   union-find clustering compile-verified). Semantic CLIP embeddings are a future upgrade
   for genuinely different photos.
