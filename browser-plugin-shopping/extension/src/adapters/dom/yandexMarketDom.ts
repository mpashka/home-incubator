import { queryFromUrl } from "../util.js";
import {
  kindForCount,
  scrapeCards,
  type DomExtraction,
  type DomExtractor,
} from "./types.js";

/**
 * Yandex Market DOM fallback. Product tiles link to `/product--<slug>/<id>` or
 * `/product/<id>`. Backup path only; the network resolver is primary. Selectors
 * drift with Market redesigns.
 */
const LINK_SELECTOR = 'a[href*="/product"]';

export const yandexMarketDom: DomExtractor = {
  marketplace: "yandex_market",

  extract(): DomExtraction | null {
    const items = scrapeCards(LINK_SELECTOR);
    if (items.length === 0) return null;

    const isProductPage = /\/product(--|\/)/.test(location.pathname);
    return {
      kind: kindForCount(items.length, isProductPage),
      searchQuery: queryFromUrl(location.href),
      items,
      images: items.map((i) => i.image).filter((x): x is string => !!x),
    };
  },
};
