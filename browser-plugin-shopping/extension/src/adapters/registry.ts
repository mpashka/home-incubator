import type { Marketplace } from "../shared/types.js";
import { ozonAdapter } from "./ozon.js";
import type { MarketplaceAdapter } from "./types.js";
import { yandexMarketAdapter } from "./yandexMarket.js";

const ADAPTERS: Record<Marketplace, MarketplaceAdapter> = {
  ozon: ozonAdapter,
  yandex_market: yandexMarketAdapter,
};

export function adapterFor(marketplace: Marketplace): MarketplaceAdapter {
  return ADAPTERS[marketplace];
}
