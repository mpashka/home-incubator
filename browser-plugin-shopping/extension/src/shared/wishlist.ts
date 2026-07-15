import type { Marketplace } from "./types.js";

/**
 * A wishlist describes what the user wants to buy in template form, so the
 * extension can generate concrete search queries (and thus search URLs) without
 * a backend or LLM.
 *
 * Generation is the cartesian product of the attribute groups: one term is
 * picked from each group and combined with the category + keywords. Example:
 *   category:  "планшет"
 *   keywords:  ["global"]
 *   attributes: [["11 дюймов", "12 дюймов"], ["snapdragon 8"]]
 * →  "планшет global 11 дюймов snapdragon 8"
 *    "планшет global 12 дюймов snapdragon 8"
 */
export interface Wishlist {
  category: string;
  keywords: string[];
  /** Each inner array is one dimension; queries take one term from each. */
  attributes: string[][];
}

export const DEFAULT_WISHLIST: Wishlist = {
  category: "планшет",
  keywords: [],
  attributes: [
    ["11 дюймов", "12 дюймов"],
    ["snapdragon 8"],
  ],
};

const STORAGE_KEY = "wishlist";

export async function getWishlist(): Promise<Wishlist> {
  const stored = await chrome.storage.local.get(STORAGE_KEY);
  const w = stored[STORAGE_KEY] as Partial<Wishlist> | undefined;
  return {
    category: w?.category ?? DEFAULT_WISHLIST.category,
    keywords: w?.keywords ?? DEFAULT_WISHLIST.keywords,
    attributes: w?.attributes ?? DEFAULT_WISHLIST.attributes,
  };
}

export async function setWishlist(w: Wishlist): Promise<void> {
  await chrome.storage.local.set({ [STORAGE_KEY]: w });
}

/** Cartesian product of the attribute groups; [] groups collapse to no term. */
function combos(groups: string[][]): string[][] {
  return groups
    .filter((g) => g.length > 0)
    .reduce<string[][]>(
      (acc, group) => acc.flatMap((prefix) => group.map((term) => [...prefix, term])),
      [[]],
    );
}

/** Build the list of concrete query strings from a wishlist. Deduplicated. */
export function generateQueries(w: Wishlist): string[] {
  const base = [w.category, ...w.keywords].filter(Boolean);
  const queries = combos(w.attributes).map((combo) =>
    [...base, ...combo]
      .join(" ")
      .replace(/\s+/g, " ")
      .trim(),
  );
  return [...new Set(queries.filter(Boolean))];
}

/** Marketplace search URL for a query. Opening it triggers the collector. */
export function searchUrl(marketplace: Marketplace, query: string): string {
  const q = encodeURIComponent(query);
  switch (marketplace) {
    case "ozon":
      return `https://www.ozon.ru/search/?text=${q}`;
    case "yandex_market":
      return `https://market.yandex.ru/search?text=${q}`;
  }
}
