/** Raw-capture read models served by the backend (phase 3). */
export interface CaptureSummary {
  id: string;
  marketplace: "ozon" | "yandex_market";
  kind: "search" | "product" | "unknown";
  searchQuery?: string;
  pageUrl: string;
  capturedAt: number;
  imageCount: number;
  thumbnail?: string;
}

export interface CapturePage {
  items: CaptureSummary[];
  total: number;
  page: number;
  size: number;
}

/**
 * Normalized read models (phase 5, LLM). Kept here so the viewer can grow into
 * them; not served yet.
 */
export interface Offer {
  id: string;
  marketplace: "ozon" | "yandex_market";
  seller?: string;
  price: number;
  greenPrice?: number;
  currency: string;
  deliveryFromChina?: boolean;
  globalFirmware?: boolean;
  url: string;
  capturedAt: number;
}

export interface Variant {
  id: string;
  color?: string;
  ram?: number;
  storage?: number;
  images: string[];
  offers: Offer[];
}

export interface ProductModel {
  id: string;
  title: string;
  screenInches?: number;
  soc?: string;
  variants: Variant[];
}
