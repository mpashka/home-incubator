/** Normalized model tree served by GET /api/models. */
export interface Offer {
  marketplace: "ozon" | "yandex_market";
  seller?: string;
  price?: number;
  greenPrice?: number;
  currency?: string;
  deliveryFromChina?: boolean;
  globalFirmware?: boolean;
  url?: string;
}

export interface Variant {
  color?: string;
  ramGb?: number;
  storageGb?: number;
  matchGroupId?: number;
  image?: string;
  offers: Offer[];
}

export interface ProductModel {
  id: number;
  title?: string;
  screenInches?: number;
  soc?: string;
  variants: Variant[];
}
