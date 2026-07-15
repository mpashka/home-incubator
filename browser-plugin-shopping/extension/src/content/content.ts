/**
 * ISOLATED-world content script. Bridges the page (MAIN-world hook) to the
 * extension background worker, and runs a DOM-scraping fallback when the network
 * hook misses.
 *
 * No UI: viewing collected data is a separate Vue app on top of the backend.
 */
import { adapterFor } from "../adapters/registry.js";
import { toCapture, type AdapterInput } from "../adapters/types.js";
import { detectMarketplace, domExtractorFor } from "../adapters/dom/registry.js";
import { hashItems } from "../adapters/dom/types.js";
import { isPageNetworkMessage } from "../shared/messages.js";
import type { RuntimeMessage } from "../shared/messages.js";
import type { RawCapture } from "../shared/types.js";

const isTopFrame = window.top === window;

/**
 * True once the extension was reloaded/updated while this page kept its old content
 * script — any chrome.* call then throws "Extension context invalidated". We detect it
 * and go quiet instead of spamming uncaught errors.
 */
let contextAlive = true;
function extensionGone(): boolean {
  try {
    return !chrome.runtime?.id;
  } catch {
    return true;
  }
}

/** URLs for which the network hook already produced a capture. */
const networkSeenUrls = new Set<string>();
/** Last DOM-scrape signature per URL, to avoid re-sending unchanged results. */
const lastDomHash = new Map<string, string>();

function send(capture: RawCapture): void {
  if (!contextAlive || extensionGone()) {
    contextAlive = false;
    return; // stale content script after an extension reload — stay quiet
  }
  // Lightweight visibility for tuning against live pages (personal tool).
  console.info(
    `[shopping-collector] capture ${capture.source}/${capture.kind} ` +
      `mp=${capture.marketplace} imgs=${capture.images.length} url=${capture.pageUrl}`,
  );
  const msg: RuntimeMessage = { type: "CAPTURE", capture };
  try {
    // sendMessage can throw SYNCHRONOUSLY ("Extension context invalidated"), so a bare
    // .catch() isn't enough — guard with try/catch too.
    chrome.runtime.sendMessage(msg).catch(() => {
      /* background may be asleep; it will be woken on next event */
    });
  } catch {
    contextAlive = false;
  }
}

// --- Network path (primary) -------------------------------------------------

window.addEventListener("message", (event) => {
  if (event.source !== window) return;
  const data = event.data;
  if (!isPageNetworkMessage(data)) return;

  let payload: unknown;
  try {
    payload = JSON.parse(data.bodyText);
  } catch {
    return;
  }

  const input: AdapterInput = {
    marketplace: data.marketplace,
    requestUrl: data.requestUrl,
    pageUrl: location.href,
    payload,
  };

  const adapter = adapterFor(data.marketplace);
  if (!adapter.matches(input)) return;

  const result = adapter.extract(input);
  if (result.kind === "unknown" && result.itemCount === 0) return;

  // Network covered this page — suppress the DOM fallback for it.
  networkSeenUrls.add(location.href);
  send(toCapture(input, result, crypto.randomUUID()));
});

// --- DOM fallback (secondary) ----------------------------------------------

const marketplace = detectMarketplace();

/** Scrape the DOM iff the network hook produced nothing for this URL. */
function runDomFallback(): void {
  if (!marketplace) return;
  const url = location.href;
  if (networkSeenUrls.has(url)) return; // network already covered it

  const extraction = domExtractorFor(marketplace).extract();
  if (!extraction || extraction.items.length === 0) return;
  if (extraction.kind === "unknown") return;

  const sig = hashItems(extraction.items);
  if (lastDomHash.get(url) === sig) return; // nothing new since last scrape
  lastDomHash.set(url, sig);

  send({
    id: crypto.randomUUID(),
    marketplace,
    kind: extraction.kind,
    pageUrl: url,
    searchQuery: extraction.searchQuery,
    capturedAt: Date.now(),
    source: "dom",
    payload: { items: extraction.items },
    images: extraction.images,
  });
}

// Debounce: marketplaces mutate the DOM continuously (lazy load, filters).
let timer: number | undefined;
function scheduleDomFallback(): void {
  if (timer !== undefined) clearTimeout(timer);
  timer = setTimeout(runDomFallback, 1500) as unknown as number;
}

if (isTopFrame && marketplace) {
  // React to SPA content changes…
  new MutationObserver(scheduleDomFallback).observe(document.documentElement, {
    childList: true,
    subtree: true,
  });
  // …and to scroll-driven lazy loading.
  window.addEventListener("scroll", scheduleDomFallback, { passive: true });
  // Safety net in case observers miss the settle.
  window.addEventListener("load", scheduleDomFallback);
  scheduleDomFallback();
}
