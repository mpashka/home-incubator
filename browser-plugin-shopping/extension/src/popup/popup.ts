/**
 * Minimal popup: toggle collection, set the backend URL, and watch counters.
 * There is intentionally no data browsing here — collected results are viewed in
 * the separate Vue app on top of the backend.
 */
import { getSettings, getStats, setSettings } from "../background/config.js";
import type { RuntimeMessage } from "../shared/messages.js";

const $ = <T extends HTMLElement>(id: string) => document.getElementById(id) as T;

const enabled = $<HTMLInputElement>("enabled");
const backendUrl = $<HTMLInputElement>("backendUrl");
const flushBtn = $<HTMLButtonElement>("flush");

async function renderStats(): Promise<void> {
  const s = await getStats();
  $("captured").textContent = String(s.captured);
  $("synced").textContent = String(s.synced);
  $("queued").textContent = String(s.queued);
  $("search").textContent = String(s.search);
  $("product").textContent = String(s.product);
  $("err").textContent = s.lastError ?? "";
}

async function init(): Promise<void> {
  const settings = await getSettings();
  enabled.checked = settings.enabled;
  backendUrl.value = settings.backendUrl;
  await renderStats();
}

enabled.addEventListener("change", () => setSettings({ enabled: enabled.checked }));
backendUrl.addEventListener("change", () => setSettings({ backendUrl: backendUrl.value.trim() }));
flushBtn.addEventListener("click", () => {
  const msg: RuntimeMessage = { type: "FLUSH" };
  chrome.runtime.sendMessage(msg).catch(() => {});
});

$("wishlist").addEventListener("click", (e) => {
  e.preventDefault();
  chrome.runtime.openOptionsPage();
});

// Live-update counters while the popup is open.
chrome.storage.onChanged.addListener((changes) => {
  if (changes.stats) void renderStats();
});

void init();
