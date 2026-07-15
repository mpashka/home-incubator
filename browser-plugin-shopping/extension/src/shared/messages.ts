import type { Marketplace, RawCapture } from "./types.js";

/**
 * window.postMessage envelope used ONLY between the MAIN-world injected hook and
 * the ISOLATED-world content script. Namespaced so we can ignore unrelated
 * page messages. This crosses into untrusted page context, so it carries raw
 * primitives only.
 */
export const PAGE_MSG = "__SHOPPING_COLLECTOR__" as const;

export interface PageNetworkMessage {
  source: typeof PAGE_MSG;
  marketplace: Marketplace;
  /** Request URL that produced the body. */
  requestUrl: string;
  /** Response body as text; content script parses/routes it. */
  bodyText: string;
}

export function isPageNetworkMessage(data: unknown): data is PageNetworkMessage {
  return (
    typeof data === "object" &&
    data !== null &&
    (data as { source?: unknown }).source === PAGE_MSG
  );
}

/**
 * chrome.runtime messages between content script / popup and the background
 * service worker.
 */
export type RuntimeMessage =
  | { type: "CAPTURE"; capture: RawCapture }
  /** Ask the worker to try draining the outbox now (e.g. after settings change). */
  | { type: "FLUSH" };
