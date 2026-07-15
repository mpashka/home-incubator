import { queryFromUrl } from "../util.js";
import {
  kindForCount,
  scrapeCards,
  type DomExtraction,
  type DomExtractor,
} from "./types.js";

/**
 * Ozon DOM fallback. Product tiles on search/category pages are anchors to
 * `/product/...`. Selectors WILL drift with Ozon redesigns — this is the backup
 * path; the network hook (searchResultsV2 widget) is primary.
 */
const LINK_SELECTOR = 'a[href*="/product/"]';

export const ozonDom: DomExtractor = {
  marketplace: "ozon",

  extract(): DomExtraction | null {
    const items = scrapeCards(LINK_SELECTOR);
    if (items.length === 0) return null;

    const isProductPage = /^\/product\//.test(location.pathname);
    return {
      kind: kindForCount(items.length, isProductPage),
      searchQuery: queryFromUrl(location.href),
      items,
      // Prefer images tied to cards; fall back to a broad DOM sweep is unnecessary
      // here since scrapeCards already carries per-item images.
      images: items.map((i) => i.image).filter((x): x is string => !!x),
    };
  },
};
