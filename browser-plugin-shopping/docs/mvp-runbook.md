# MVP runbook

Local end-to-end launch. The backend + full pipeline (ingest → Yandex normalization →
models → image match) is **verified live** on this machine; the browser side is manual.

## Prerequisites (already set up on this machine)

- **PostgreSQL** running on `localhost:5432` with a dev db/role:
  ```bash
  sudo -u postgres psql -c "CREATE ROLE shopping LOGIN PASSWORD 'shopping'"
  sudo -u postgres createdb -O shopping shopping
  ```
  The backend's `%dev` profile points at `jdbc:postgresql://localhost:5432/shopping`.
- **JDK 21** at `~/.jdks/temurin-21` (Gradle 8.10.2 can't run on the default JDK 25).
- **Yandex creds** in the environment: `YANDEX_AI_TOKEN`, `YANDEX_FOLDER_ID` (in `~/.profile`).

## 1. Backend

```bash
cd backend
export JAVA_HOME=~/.jdks/temurin-21
source ~/.profile              # YANDEX_AI_TOKEN / YANDEX_FOLDER_ID
./gradlew quarkusDev           # http://localhost:8080
```
Sanity: `curl http://localhost:8080/api/ingest/count` → a number. Provider log line:
`LLM provider: yandex`.

## 2. Extension (Chrome)

```bash
cd extension && npm install && npm run build   # → extension/dist
```
- `chrome://extensions` → enable **Developer mode** → **Load unpacked** → pick `extension/dist`.
- Click the toolbar icon → set **Backend URL** = `http://localhost:8080`, tick **enabled**.
- Popup → **Wishlist…** → set e.g. category `планшет`, attributes `11 дюймов, 12 дюймов`
  / `snapdragon 8`. Click a generated **Ozon** / **Я.Маркет** link.

## 3. Collect

Browse the opened search and open a few product cards. Watch:
- popup counters (**captured** / **synced**), and
- `curl http://localhost:8080/api/ingest/count` (should grow),
- `curl http://localhost:8080/api/captures` (what arrived).

## 4. Normalize + match

```bash
curl -X POST http://localhost:8080/api/normalize/run   # Yandex extracts offers
curl http://localhost:8080/api/models                  # ProductModel → Variant → Offer
curl -X POST http://localhost:8080/api/match/run       # hash photos, cluster variants
```

Optional viewer: `cd viewer && npm install && npm run dev` → http://localhost:5173.

## Debugging live capture (the #1 risk)

Marketplace API endpoints and DOM selectors are best-effort and **not yet tuned against live
Ozon/Yandex Market**. If **captured** stays 0:

1. On the marketplace tab, open DevTools → **Console**: the MAIN-world hook logs
   `[shopping-collector] network hit …` for intercepted API calls, and the content script
   logs `[shopping-collector] capture …` when it sends one.
2. `chrome://extensions` → the extension's **service worker** → *Inspect* → console/network
   for ingest POSTs and errors.
3. If the network hook never logs, the API URL patterns in `injected.ts` (`INTEREST`) need
   updating to the current endpoints; the DOM fallback (`adapters/dom/*` selectors) should
   still fire on scroll — check its `capture dom/…` log.
4. Grab a failing request URL or a sample captured payload (`/api/captures/{id}`) and tune the
   adapter regex/selectors to match.

## Reset

```bash
PGPASSWORD=shopping psql -h 127.0.0.1 -U shopping -d shopping \
  -c "truncate price_history, variant_image, offer, variant, product_model, raw_capture restart identity cascade;"
```
