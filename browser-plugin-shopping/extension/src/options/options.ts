/**
 * Options page: edit the wishlist and preview generated search queries. Each
 * query links to Ozon and Yandex Market search pages; opening a link runs the
 * search, which the content-script collector captures automatically.
 */
import {
  generateQueries,
  getWishlist,
  searchUrl,
  setWishlist,
  type Wishlist,
} from "../shared/wishlist.js";

const $ = <T extends HTMLElement>(id: string) => document.getElementById(id) as T;

const category = $<HTMLInputElement>("category");
const keywords = $<HTMLInputElement>("keywords");
const attributes = $<HTMLTextAreaElement>("attributes");
const list = $<HTMLDivElement>("list");
const saved = $<HTMLSpanElement>("saved");

const splitCsv = (s: string) => s.split(",").map((x) => x.trim()).filter(Boolean);

/** Read the form into a Wishlist. */
function readForm(): Wishlist {
  return {
    category: category.value.trim(),
    keywords: splitCsv(keywords.value),
    attributes: attributes.value
      .split("\n")
      .map(splitCsv)
      .filter((g) => g.length > 0),
  };
}

function renderQueries(w: Wishlist): void {
  list.replaceChildren();
  for (const q of generateQueries(w)) {
    const row = document.createElement("div");
    row.className = "q";

    const text = document.createElement("span");
    text.className = "text";
    text.textContent = q;
    row.append(text);

    for (const [mp, label] of [
      ["ozon", "Ozon"],
      ["yandex_market", "Я.Маркет"],
    ] as const) {
      const a = document.createElement("a");
      a.href = searchUrl(mp, q);
      a.target = "_blank";
      a.rel = "noopener";
      a.textContent = label;
      row.append(a);
    }
    list.append(row);
  }
}

function fillForm(w: Wishlist): void {
  category.value = w.category;
  keywords.value = w.keywords.join(", ");
  attributes.value = w.attributes.map((g) => g.join(", ")).join("\n");
}

// Live preview as the user types.
for (const el of [category, keywords, attributes]) {
  el.addEventListener("input", () => renderQueries(readForm()));
}

$("save").addEventListener("click", async () => {
  const w = readForm();
  await setWishlist(w);
  saved.textContent = "saved ✓";
  setTimeout(() => (saved.textContent = ""), 1500);
});

void (async () => {
  const w = await getWishlist();
  fillForm(w);
  renderQueries(w);
})();
