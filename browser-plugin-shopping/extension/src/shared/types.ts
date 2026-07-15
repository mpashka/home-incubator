/**
 * Shared contracts between the injected hook, content script, background worker,
 * side panel, and (eventually) the Quarkus `/ingest` backend.
 *
 * Phase 1 keeps everything raw: we do NOT normalize on the client. The extension
 * only captures what the marketplace itself already loaded, plus enough context
 * (url, search query, images) for the backend LLM to normalize and de-duplicate
 * later.
 */

export type Marketplace = "ozon" | "yandex_market";

/** What kind of page produced the capture. */
export type CaptureKind = "search" | "product" | "unknown";

/** Where the payload came from. Network is preferred; DOM is the fallback. */
export type CaptureSource = "network" | "dom";

/**
 * A single raw capture, as shipped from a page to the background worker and then
 * to the backend. `payload` is intentionally `unknown` — the backend owns
 * interpretation. We keep it verbatim so re-parsing is possible when marketplace
 * schemas change.
 */
export interface RawCapture {
  /** Stable client-side id (uuid) used for de-dup and idempotent ingest. */
  id: string;
  marketplace: Marketplace;
  kind: CaptureKind;
  /** Full page URL the capture was observed on. */
  pageUrl: string;
  /** For network captures: the request URL that returned the payload. */
  requestUrl?: string;
  /** Best-effort search query associated with the capture (from URL or DOM). */
  searchQuery?: string;
  /** Epoch millis when captured on the client. */
  capturedAt: number;
  source: CaptureSource;
  /** Verbatim marketplace payload (parsed JSON for network, extracted object for DOM). */
  payload: unknown;
  /** Absolute image URLs referenced by the capture (downloaded by the backend). */
  images: string[];
}

/** A lightweight index row kept in IndexedDB for the side-panel list. */
export interface CaptureSummary {
  id: string;
  marketplace: Marketplace;
  kind: CaptureKind;
  searchQuery?: string;
  capturedAt: number;
  /** How many product-like items the adapter counted in the payload. */
  itemCount: number;
  synced: boolean;
}
