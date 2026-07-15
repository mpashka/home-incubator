/**
 * Small, defensive helpers shared by adapters. Everything here tolerates
 * unknown/changing marketplace shapes.
 */

/**
 * Marketplace image CDNs whose URLs carry NO file extension (so an extension
 * regex alone misses them): Yandex `avatars.mds.yandex.net/get-mpic/…/orig`,
 * Ozon `ir.ozone.ru/…/wc1000/…`.
 */
const IMG_HOST = /avatars\.mds\.yandex\.net|\/get-mpic\/|ir\.ozone\.ru|cdn\d*\.ozone\.ru|\/multimedia-/i;

/** Recursively collect string values that look like image URLs (by extension OR CDN host). */
export function collectImageUrls(node: unknown, out = new Set<string>()): string[] {
  const isImgUrl = (s: string) =>
    /^(https?:)?\/\//.test(s) &&
    (/\.(jpe?g|png|webp|avif)(\?|$)/i.test(s) || IMG_HOST.test(s));

  const norm = (s: string) => (s.startsWith("//") ? "https:" + s : s);

  const walk = (n: unknown) => {
    if (typeof n === "string") {
      if (isImgUrl(n)) out.add(norm(n));
      return;
    }
    if (Array.isArray(n)) {
      n.forEach(walk);
      return;
    }
    if (n && typeof n === "object") {
      for (const v of Object.values(n as Record<string, unknown>)) walk(v);
    }
  };

  walk(node);
  return [...out];
}

/** Extract a search query from a marketplace URL (text=…, from …). */
export function queryFromUrl(rawUrl: string): string | undefined {
  try {
    const u = new URL(rawUrl, "https://x");
    return (
      u.searchParams.get("text") ??
      u.searchParams.get("query") ??
      u.searchParams.get("search") ??
      undefined
    );
  } catch {
    return undefined;
  }
}

/**
 * Ozon frequently double-encodes: a widget `state` is a JSON string inside JSON.
 * Parse it lazily and safely.
 */
export function parseMaybeJson(value: unknown): unknown {
  if (typeof value !== "string") return value;
  try {
    return JSON.parse(value);
  } catch {
    return value;
  }
}
