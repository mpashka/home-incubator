/** Persisted extension settings, edited from the popup. */
export interface Settings {
  /** Base URL of the Quarkus backend, e.g. http://localhost:8080 */
  backendUrl: string;
  /** Master on/off for collection + ingest. */
  enabled: boolean;
}

const DEFAULTS: Settings = {
  backendUrl: "http://localhost:8080",
  enabled: true,
};

export async function getSettings(): Promise<Settings> {
  const stored = await chrome.storage.local.get("settings");
  return { ...DEFAULTS, ...(stored.settings as Partial<Settings> | undefined) };
}

export async function setSettings(patch: Partial<Settings>): Promise<Settings> {
  const next = { ...(await getSettings()), ...patch };
  await chrome.storage.local.set({ settings: next });
  return next;
}

/** Rolling counters shown in the popup and on the toolbar badge. */
export interface Stats {
  captured: number;
  synced: number;
  queued: number;
  /** Captures by page kind, for the badge tooltip ("search recognized" / "cards captured"). */
  search: number;
  product: number;
  lastError?: string;
  lastMarketplace?: string;
  lastKind?: string;
  /** Epoch millis of the most recent capture. */
  lastAt?: number;
}

export async function getStats(): Promise<Stats> {
  const stored = await chrome.storage.local.get("stats");
  return {
    captured: 0, synced: 0, queued: 0, search: 0, product: 0,
    ...(stored.stats as Partial<Stats> | undefined),
  };
}

export async function updateStats(patch: Partial<Stats>): Promise<void> {
  const next = { ...(await getStats()), ...patch };
  await chrome.storage.local.set({ stats: next });
}
