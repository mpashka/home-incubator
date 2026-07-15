import type { CaptureKind, Marketplace, RawCapture } from "../shared/types.js";

/** Context passed to an adapter for a single observed network body. */
export interface AdapterInput {
  marketplace: Marketplace;
  requestUrl: string;
  pageUrl: string;
  /** Already-parsed JSON payload (adapters do not re-parse text). */
  payload: unknown;
}

/**
 * Shallow, drift-tolerant extraction. Adapters MUST NOT throw; on any doubt they
 * return the capture with `payload` intact and best-effort metadata. The heavy
 * normalization lives on the backend LLM.
 */
export interface AdapterResult {
  kind: CaptureKind;
  searchQuery?: string;
  images: string[];
  /** Number of product-like items detected, for the side-panel summary. */
  itemCount: number;
}

export interface MarketplaceAdapter {
  marketplace: Marketplace;
  /** True if this body is worth keeping (e.g. actually contains product data). */
  matches(input: AdapterInput): boolean;
  extract(input: AdapterInput): AdapterResult;
}

/** Helper: build the final RawCapture from an adapter result. */
export function toCapture(
  input: AdapterInput,
  result: AdapterResult,
  id: string,
): RawCapture {
  return {
    id,
    marketplace: input.marketplace,
    kind: result.kind,
    pageUrl: input.pageUrl,
    requestUrl: input.requestUrl,
    searchQuery: result.searchQuery,
    capturedAt: Date.now(),
    source: "network",
    payload: input.payload,
    images: result.images,
  };
}
