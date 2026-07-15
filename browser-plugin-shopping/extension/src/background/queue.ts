import type { RawCapture } from "../shared/types.js";
import { getSettings, getStats, updateStats } from "./config.js";

/**
 * A tiny durable outbox. Captures are appended to chrome.storage.local and
 * flushed to the backend `/ingest` endpoint. On failure they stay queued and are
 * retried on the next capture or alarm tick — the service worker can be killed at
 * any moment, so nothing lives only in memory.
 */
const QUEUE_KEY = "outbox";
const MAX_QUEUE = 500;

async function readQueue(): Promise<RawCapture[]> {
  const stored = await chrome.storage.local.get(QUEUE_KEY);
  return (stored[QUEUE_KEY] as RawCapture[] | undefined) ?? [];
}

async function writeQueue(items: RawCapture[]): Promise<void> {
  await chrome.storage.local.set({ [QUEUE_KEY]: items.slice(-MAX_QUEUE) });
}

/** De-dup guard: skip captures whose id we already have queued. Returns true if enqueued. */
export async function enqueue(capture: RawCapture): Promise<boolean> {
  const q = await readQueue();
  if (q.some((c) => c.id === capture.id)) return false;
  q.push(capture);
  await writeQueue(q);
  const stats = await getStats();
  const byKind = capture.kind === "product" ? { product: stats.product + 1 } : { search: stats.search + 1 };
  await updateStats({
    captured: stats.captured + 1,
    queued: q.length,
    ...byKind,
    lastMarketplace: capture.marketplace,
    lastKind: capture.kind,
    lastAt: capture.capturedAt,
  });
  return true;
}

let flushing = false;

/** Attempt to POST all queued captures. Safe to call concurrently. */
export async function flush(): Promise<void> {
  if (flushing) return;
  flushing = true;
  try {
    const settings = await getSettings();
    if (!settings.enabled) return;

    let q = await readQueue();
    while (q.length > 0) {
      const batch = q.slice(0, 20);
      try {
        const res = await fetch(`${settings.backendUrl}/api/ingest`, {
          method: "POST",
          headers: { "content-type": "application/json" },
          body: JSON.stringify({ captures: batch }),
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
      } catch (err) {
        await updateStats({ lastError: String(err), queued: q.length });
        return; // keep the batch queued; retry later
      }
      q = q.slice(batch.length);
      await writeQueue(q);
      const stats = await getStats();
      await updateStats({
        synced: stats.synced + batch.length,
        queued: q.length,
        lastError: undefined,
      });
    }
  } finally {
    flushing = false;
  }
}
