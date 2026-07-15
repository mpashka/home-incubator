/**
 * Background service worker. Receives captures from content scripts, queues them
 * durably, and flushes to the Quarkus backend. Also drives the toolbar badge so the
 * user can see collection working at a glance.
 */
import type { RuntimeMessage } from "../shared/messages.js";
import { getStats } from "./config.js";
import { enqueue, flush } from "./queue.js";

/** Reflect capture/sync state on the toolbar icon badge + tooltip. */
async function renderBadge(): Promise<void> {
  const s = await getStats();
  const text = s.captured === 0 ? "" : s.captured > 999 ? "999+" : String(s.captured);
  // green = synced clean · orange = queued (backend unreachable) · red = last sync errored
  const color = s.lastError ? "#dc2626" : s.queued > 0 ? "#f59e0b" : "#16a34a";

  const last =
    s.lastKind && s.lastMarketplace
      ? `\nLast: ${s.lastMarketplace} ${s.lastKind}`
      : "";
  const err = s.lastError ? `\n⚠ sync: ${s.lastError}` : "";
  const title =
    `Shopping Collector\n` +
    `Search pages: ${s.search}   Product cards: ${s.product}\n` +
    `Synced ${s.synced}, queued ${s.queued}` +
    last +
    err;

  try {
    await chrome.action.setBadgeBackgroundColor({ color });
    await chrome.action.setBadgeText({ text });
    await chrome.action.setTitle({ title });
  } catch {
    /* action API unavailable during teardown */
  }
}

// Re-render whenever stats change (capture enqueued, flush synced, error set).
chrome.storage.onChanged.addListener((changes, area) => {
  if (area === "local" && changes.stats) void renderBadge();
});

// Restore the badge when the worker (re)starts.
chrome.runtime.onStartup.addListener(() => void renderBadge());
chrome.runtime.onInstalled.addListener(() => {
  chrome.alarms.create("flush", { periodInMinutes: 1 });
  void renderBadge();
});

chrome.alarms.onAlarm.addListener((a) => {
  if (a.name === "flush") void flush();
});

chrome.runtime.onMessage.addListener((message: RuntimeMessage, _sender, sendResponse) => {
  if (message.type === "CAPTURE") {
    (async () => {
      await enqueue(message.capture);
      void flush();
      sendResponse({ ok: true });
    })();
    return true; // async response
  }
  if (message.type === "FLUSH") {
    void flush();
    sendResponse({ ok: true });
    return false;
  }
  return false;
});

void renderBadge();
