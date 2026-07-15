import type { Marketplace } from "../../shared/types.js";
import { ozonDom } from "./ozonDom.js";
import type { DomExtractor } from "./types.js";
import { yandexMarketDom } from "./yandexMarketDom.js";

const DOM_EXTRACTORS: Record<Marketplace, DomExtractor> = {
  ozon: ozonDom,
  yandex_market: yandexMarketDom,
};

export function domExtractorFor(marketplace: Marketplace): DomExtractor {
  return DOM_EXTRACTORS[marketplace];
}

export function detectMarketplace(): Marketplace | null {
  const h = location.hostname;
  if (h.endsWith("ozon.ru")) return "ozon";
  if (h.endsWith("market.yandex.ru")) return "yandex_market";
  return null;
}
