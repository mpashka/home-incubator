import type { AdapterInput, AdapterResult, MarketplaceAdapter } from "./types.js";
import { collectImageUrls, queryFromUrl } from "./util.js";

/**
 * Yandex Market adapter.
 *
 * Market's internal `/api/resolve` returns a batch of "collections" and
 * "results". Schemas are less publicly documented than Ozon's and change often,
 * so phase 1 stays deliberately shallow: detect search vs product by URL/heuristics,
 * count offer-like entries, collect images, and ship the payload verbatim.
 */

function looksLikeSearch(pageUrl: string, requestUrl: string): boolean {
  return /\/search(\?|\/|$)/.test(pageUrl) || /text=/.test(requestUrl) || /text=/.test(pageUrl);
}

function looksLikeProduct(pageUrl: string): boolean {
  return /\/product(-|\/)/.test(pageUrl) || /\/card\//.test(pageUrl);
}

/** Best-effort count of product/offer entries anywhere in the payload. */
function countItems(payload: unknown): number {
  const collections = (payload as { collections?: Record<string, unknown> } | null)?.collections;
  if (!collections || typeof collections !== "object") return 0;
  let n = 0;
  for (const key of ["product", "offer", "sku", "products", "offers"]) {
    const c = (collections as Record<string, unknown>)[key];
    if (c && typeof c === "object") n += Object.keys(c as Record<string, unknown>).length;
  }
  return n;
}

export const yandexMarketAdapter: MarketplaceAdapter = {
  marketplace: "yandex_market",

  matches(input: AdapterInput): boolean {
    const p = input.payload as { collections?: unknown; results?: unknown } | null;
    return !!p && typeof p === "object" && ("collections" in p || "results" in p);
  },

  extract(input: AdapterInput): AdapterResult {
    const isSearch = looksLikeSearch(input.pageUrl, input.requestUrl);
    const isProduct = !isSearch && looksLikeProduct(input.pageUrl);
    const itemCount = countItems(input.payload);

    return {
      kind: isSearch ? "search" : isProduct ? "product" : "unknown",
      searchQuery: isSearch
        ? queryFromUrl(input.pageUrl) ?? queryFromUrl(input.requestUrl)
        : undefined,
      images: collectImageUrls(input.payload),
      itemCount: isProduct ? Math.max(1, itemCount) : itemCount,
    };
  },
};
