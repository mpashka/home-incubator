import type { AdapterInput, AdapterResult, MarketplaceAdapter } from "./types.js";
import { collectImageUrls, parseMaybeJson, queryFromUrl } from "./util.js";

/**
 * Ozon adapter.
 *
 * The internal API `entrypoint-api.bx/page/json/v2?url=<path>` returns a JSON
 * object whose `widgetStates` map keys are `"<widgetName>-<id>-<slot>"` and whose
 * values are JSON-encoded strings. We match widget names by PREFIX because the
 * suffix ids change per request.
 *
 * Phase 1: we only detect kind, count items, and collect image urls. The full
 * payload is shipped verbatim for backend normalization.
 */
const PRODUCT_WIDGET = /^(webProductHeading|webPrice|webCharacteristics|webGallery)/;
/** Arrays that hold product cards across Ozon grid widgets (search, category, shelves). */
const ITEM_ARRAYS = ["items", "tiles", "products"];

function widgetStates(payload: unknown): Record<string, unknown> {
  const p = payload as { widgetStates?: Record<string, unknown> } | null;
  return p && typeof p === "object" && p.widgetStates ? p.widgetStates : {};
}

export const ozonAdapter: MarketplaceAdapter = {
  marketplace: "ozon",

  matches(input: AdapterInput): boolean {
    return Object.keys(widgetStates(input.payload)).length > 0;
  },

  extract(input: AdapterInput): AdapterResult {
    const states = widgetStates(input.payload);
    const keys = Object.keys(states);

    // Ozon double-encodes: each widget state is a JSON string. Decode them so item
    // arrays and image URLs (buried inside) become visible.
    const decoded = keys.map((k) => parseMaybeJson(states[k]));

    // Count product-card arrays across the DECODED states (best-effort, for the summary).
    let itemCount = 0;
    for (const state of decoded) {
      if (state && typeof state === "object") {
        for (const arr of ITEM_ARRAYS) {
          const v = (state as Record<string, unknown>)[arr];
          if (Array.isArray(v)) itemCount += v.length;
        }
      }
    }

    const url = input.requestUrl ?? "";
    // `page/json/v2` is a full page (search/category/product) — always worth capturing,
    // even when the item array is nested somewhere we didn't count. `widget/json/v2`
    // (orderInfo, menus, …) is only kept if it actually carries product arrays.
    const isPage = /page\/json\/v2/.test(url);
    const isProduct = keys.some((k) => PRODUCT_WIDGET.test(k)) || /\/product\//.test(url);
    const isListing = isPage || itemCount > 0;

    console.info(
      `[shopping-collector] ozon widgets: ${keys.map((k) => k.split("-")[0]).join(",")} items=${itemCount} page=${isPage}`,
    );

    const searchQuery = queryFromUrl(input.pageUrl) ?? queryFromUrl(input.requestUrl);

    return {
      kind: isProduct ? "product" : isListing ? "search" : "unknown",
      searchQuery: isListing && !isProduct ? searchQuery : undefined,
      images: collectImageUrls(decoded),
      itemCount: isProduct ? 1 : Math.max(itemCount, isListing ? 1 : 0),
    };
  },
};
