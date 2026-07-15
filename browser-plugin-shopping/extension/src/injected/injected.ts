/**
 * MAIN-world hook. Runs in the page's own JS context (same realm as the site),
 * so it can wrap `fetch` and `XMLHttpRequest` before the marketplace app code
 * uses them. It captures response bodies for the marketplace's own internal API
 * calls and forwards the raw text to the ISOLATED content script via
 * window.postMessage.
 *
 * This file must stay dependency-light and side-effect-safe: it executes on
 * every marketplace page at document_start.
 */
import { PAGE_MSG, type PageNetworkMessage } from "../shared/messages.js";
import type { Marketplace } from "../shared/types.js";

/** URL substrings that identify the internal data APIs we care about. */
const INTEREST: Record<Marketplace, RegExp> = {
  // Ozon: page/json/v2 (search/category/product) + widget/json/v2 (scroll pagination,
  // e.g. widgetStateId=searchResultsV2-…). The first screen is SSR-embedded, so on Ozon
  // XHR data mostly arrives when scrolling / loading more.
  ozon: /\/(entrypoint-api|composer-api)\.bx\/(page|widget)\/json\/v2/i,
  // Yandex Market internal resolver / API endpoints returning product JSON.
  yandex_market: /\/api\/resolve|\/api\/v1\/.*resolve|\/resolve\.json/i,
};

function detectMarketplace(): Marketplace | null {
  const h = location.hostname;
  if (h.endsWith("ozon.ru")) return "ozon";
  if (h.endsWith("market.yandex.ru")) return "yandex_market";
  return null;
}

const marketplace = detectMarketplace();

function isInteresting(url: string): boolean {
  return marketplace !== null && INTEREST[marketplace].test(url);
}

/**
 * DIAGNOSTIC: candidate data endpoints we might want to capture. Logged (not
 * captured) so we can discover the real product-detail / Ozon URLs from the page
 * console and then tune INTEREST. Remove once adapters are dialed in.
 */
const DIAG = /\/api\/|resolve|\.bx\/|graphql|\/gateway\/|\/models\/|composer/i;
function diag(url: string): void {
  if (marketplace && !isInteresting(url) && DIAG.test(url)) {
    console.info(`[shopping-collector] DIAG candidate ${marketplace}: ${url}`);
  }
}

function forward(requestUrl: string, bodyText: string): void {
  if (!marketplace || !bodyText) return;
  console.info(`[shopping-collector] network hit ${marketplace}: ${requestUrl}`);
  const msg: PageNetworkMessage = {
    source: PAGE_MSG,
    marketplace,
    requestUrl,
    bodyText,
  };
  // Same-origin only; content script filters by our namespace.
  window.postMessage(msg, location.origin);
}

if (marketplace) {
  // --- fetch ---
  const origFetch = window.fetch;
  window.fetch = async function (...args: Parameters<typeof fetch>) {
    const res = await origFetch.apply(this, args);
    try {
      const url = typeof args[0] === "string" ? args[0] : (args[0] as Request | URL).toString();
      diag(url);
      if (isInteresting(url)) {
        // Clone so we never disturb the site's own consumption of the body.
        res.clone().text().then((t) => forward(url, t)).catch(() => {});
      }
    } catch {
      /* never break the page */
    }
    return res;
  };

  // --- XMLHttpRequest ---
  const OrigXHR = window.XMLHttpRequest;
  const openOrig = OrigXHR.prototype.open;
  const sendOrig = OrigXHR.prototype.send;

  OrigXHR.prototype.open = function (
    this: XMLHttpRequest & { __url?: string },
    method: string,
    url: string | URL,
    ...rest: unknown[]
  ) {
    this.__url = url.toString();
    // @ts-expect-error variadic passthrough to native open
    return openOrig.call(this, method, url, ...rest);
  };

  OrigXHR.prototype.send = function (
    this: XMLHttpRequest & { __url?: string },
    ...args: unknown[]
  ) {
    this.addEventListener("load", () => {
      try {
        const url = this.__url ?? "";
        diag(url);
        if (isInteresting(url) && typeof this.responseText === "string") {
          forward(url, this.responseText);
        }
      } catch {
        /* ignore */
      }
    });
    // @ts-expect-error variadic passthrough to native send
    return sendOrig.apply(this, args);
  };
}
