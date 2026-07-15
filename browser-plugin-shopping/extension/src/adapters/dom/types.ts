import type { CaptureKind, Marketplace } from "../../shared/types.js";

/**
 * DOM fallback extraction. Used only when the network hook misses (SSR-only data,
 * schema change). Selectors are inherently fragile, so extractors stay shallow
 * and defensive: they never throw, and return `null` when they find nothing
 * usable so the caller can stay quiet.
 */

export interface DomItem {
  /** Stable-ish key within a page (usually the product path). */
  id: string;
  title: string;
  url: string;
  priceText?: string;
  image?: string;
}

export interface DomExtraction {
  kind: CaptureKind;
  searchQuery?: string;
  items: DomItem[];
  images: string[];
}

export interface DomExtractor {
  marketplace: Marketplace;
  /** Read the current document. Returns null when nothing product-like is found. */
  extract(): DomExtraction | null;
}

const PRICE_RE = /(\d[\d  \s]{0,12}\d|\d)\s*₽/;

/** Absolute URL from a possibly-relative href. */
function absUrl(href: string): string {
  try {
    return new URL(href, location.origin).toString();
  } catch {
    return href;
  }
}

/** Best image src for an <img>, tolerating lazy-loading attributes. */
function imgSrc(img: HTMLImageElement | null): string | undefined {
  if (!img) return undefined;
  const src =
    img.currentSrc ||
    img.getAttribute("src") ||
    img.getAttribute("data-src") ||
    img.srcset?.split(",")[0]?.trim().split(" ")[0];
  return src && /^https?:/.test(src) ? src : undefined;
}

/** Walk up from an anchor to the smallest container holding an image and a price. */
function tileOf(anchor: Element): Element {
  let el: Element = anchor;
  for (let i = 0; i < 6; i++) {
    const p: HTMLElement | null = el.parentElement;
    if (!p) break;
    if (p.querySelector("img") && PRICE_RE.test(p.textContent ?? "")) return p;
    el = p;
  }
  return anchor.closest("article") ?? anchor.parentElement ?? anchor;
}

/**
 * Generic product-card scraper: collects anchors matching `linkSelector`,
 * deduplicates by product path, and pulls title / price / image from each tile.
 */
export function scrapeCards(linkSelector: string): DomItem[] {
  const byId = new Map<string, DomItem>();
  const anchors = Array.from(document.querySelectorAll<HTMLAnchorElement>(linkSelector));

  for (const a of anchors) {
    const href = a.getAttribute("href");
    if (!href) continue;
    const url = absUrl(href);
    let id: string;
    try {
      id = new URL(url).pathname;
    } catch {
      id = url;
    }
    if (byId.has(id)) continue;

    const tile = tileOf(a);
    const img = tile.querySelector<HTMLImageElement>("img");
    const priceText = (tile.textContent ?? "").match(PRICE_RE)?.[0]?.replace(/\s+/g, " ").trim();
    const title =
      (a.getAttribute("title") ||
        a.textContent?.trim() ||
        img?.getAttribute("alt") ||
        "")
        .replace(/\s+/g, " ")
        .trim()
        .slice(0, 200);

    if (!title && !priceText) continue; // pure navigation link, not a product card

    byId.set(id, { id, title, url, priceText, image: imgSrc(img) });
  }

  return [...byId.values()];
}

/** Cheap stable hash (djb2) over item ids, to detect "nothing changed". */
export function hashItems(items: DomItem[]): string {
  const key = items.map((i) => i.id).sort().join("|");
  let h = 5381;
  for (let i = 0; i < key.length; i++) h = ((h << 5) + h) ^ key.charCodeAt(i);
  return (h >>> 0).toString(36);
}

export function kindForCount(count: number, isProductPage: boolean): CaptureKind {
  if (count > 1) return "search";
  if (isProductPage) return "product";
  return "unknown";
}
