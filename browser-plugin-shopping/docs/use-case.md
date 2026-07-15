# Use case

## Problem

I want to buy a tablet with the best price/quality ratio. Concretely my wishlist is:

- screen larger than **11"**;
- **Snapdragon 8-series** SoC;
- best **price / quality** trade-off.

Buying in **Serbia** is expensive and impractical: VAT, customs, poor logistics, a small
local market. Prices for the **same** tablet in **Russia** vary widely — there are many
offers "slow, shipped from China", global firmware, sometimes almost **half the price**
of the local equivalent.

I tried pointing an AI agent at the marketplaces to search for me, but it does poorly:
marketplaces actively protect their data from scraping (anti-bot, dynamic rendering,
obfuscated APIs), so a headless agent can't reliably read prices and offers.

## Idea

Do the data collection from **inside the browser**, in my own authenticated session,
where the marketplace has already rendered/loaded the data. A **Chrome MV3 extension**
observes the pages I browse and captures:

- products, prices (including the "green"/card price where present);
- the **search query** that produced them;
- **photos**, product name, and the **varying attributes** — e.g. the same tablet exists
  in different **colors** and **memory** configurations, each a distinct variant;
- delivery context (notably "shipped from China" vs local, global firmware, etc.).

As I browse a marketplace, the extension accumulates **actual, current prices and offers
tailored to my wishlist**. The extension can also **generate search queries** to widen
coverage. Then I run an **AI agent over my own collected data** to pick the optimum —
instead of fighting anti-bot protection live.

## Why a browser extension (not a headless agent)

- Runs in the real, authenticated session → sees the data the site already loaded.
- Not blocked by anti-bot / Cloudflare the way an external scraper is.
- Captures the marketplace's **own internal API responses** (network interception),
  which is far more robust than DOM scraping and survives layout redesigns.

## Scope — iteration 1

- Marketplaces: **Yandex Market**, **Ozon**.
- Extension: Chrome **Manifest V3**, TypeScript, bundled with esbuild.
  Pure **collector** — no data-browsing UI. It captures and ships to the backend.
- Extraction: **intercept fetch/XHR** responses of the marketplace's internal APIs
  (MAIN-world hook), with **DOM parsing as a fallback**.
- Backend: **Quarkus** (Java/Kotlin). Receives raw captures at `/api/ingest`.
  An **LLM normalizes** raw payloads into a structured model and de-duplicates offers,
  **including image-based matching** of variants across marketplaces.
- Viewer: a **separate Vue/TypeScript** app on top of the backend API to browse the
  collected models, variants, offers, and price history.

## Data model (target)

```
ProductModel            e.g. "Redmi Pad Pro 12.1"
└── Variant             color + RAM/ROM, own photos
    └── Offer           marketplace, seller, price, "green" price,
        │               delivery origin (China/local), global firmware, captured_at
        └── PriceHistory  per-offer price points over time
```

The extension ships **raw** captures; the backend owns normalization so that re-parsing
is possible when marketplace schemas change.

## Non-goals (for now)

- No affiliate-link injection or "find cheaper" redirects (unlike many existing extensions).
- No live scraping outside the user's own browsing session.
- No cross-user data collection.
