<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import type { Offer, ProductModel, Variant } from "./types";

const models = ref<ProductModel[]>([]);
const error = ref("");
const loading = ref(true);
const expanded = reactive<Record<number, boolean>>({});

const f = reactive({
  query: "",
  soc: "",
  minScreen: 0,
  marketplace: "" as "" | "ozon" | "yandex_market" | "both",
  sort: "offers" as "offers" | "priceAsc" | "priceDesc",
});

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  try {
    const res = await fetch("/api/models");
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    models.value = (await res.json()) as ProductModel[];
  } catch (e) {
    error.value = String(e);
  } finally {
    loading.value = false;
  }
}
onMounted(load);

const preset = () => {
  f.soc = "Snapdragon 8";
  f.minScreen = 11;
  f.marketplace = "";
  f.sort = "offers";
};

// Derived per-model stats.
function offersOf(m: ProductModel): Offer[] {
  return m.variants.flatMap((v) => v.offers);
}
function pricesOf(m: ProductModel): number[] {
  return offersOf(m)
    .map((o) => o.price)
    .filter((p): p is number => typeof p === "number" && p > 0)
    .sort((a, b) => a - b);
}
function marketsOf(m: ProductModel): Set<string> {
  return new Set(offersOf(m).map((o) => o.marketplace));
}
function chinaOf(m: ProductModel): number {
  return offersOf(m).filter((o) => o.deliveryFromChina).length;
}
function imageOf(m: ProductModel): string | undefined {
  return m.variants.find((v) => v.image)?.image;
}
const fmt = (n?: number) => (typeof n === "number" ? n.toLocaleString("ru-RU") : "—");

const view = computed(() => {
  let list = models.value.filter((m) => {
    if (f.query && !(m.title ?? "").toLowerCase().includes(f.query.toLowerCase())) return false;
    if (f.soc && !(m.soc ?? "").toLowerCase().includes(f.soc.toLowerCase())) return false;
    if (f.minScreen && !(m.screenInches && m.screenInches >= f.minScreen)) return false;
    const mk = marketsOf(m);
    if (f.marketplace === "both" && mk.size < 2) return false;
    if ((f.marketplace === "ozon" || f.marketplace === "yandex_market") && !mk.has(f.marketplace))
      return false;
    return true;
  });
  list = list.map((m) => m); // shallow copy for sort
  const min = (m: ProductModel) => pricesOf(m)[0] ?? Infinity;
  list.sort((a, b) => {
    if (f.sort === "offers") return offersOf(b).length - offersOf(a).length;
    if (f.sort === "priceAsc") return min(a) - min(b);
    return (pricesOf(b).slice(-1)[0] ?? 0) - (pricesOf(a).slice(-1)[0] ?? 0);
  });
  return list;
});

const totalOffers = computed(() =>
  models.value.reduce((n, m) => n + offersOf(m).length, 0),
);

const mkLabel = (mk: string) => (mk === "ozon" ? "Ozon" : "Я.Маркет");
const sortedVariants = (m: ProductModel): Variant[] =>
  [...m.variants].sort((a, b) => (a.offers[0]?.price ?? 0) - (b.offers[0]?.price ?? 0));
</script>

<template>
  <main class="wrap">
    <h1>Планшеты <small>{{ models.length }} моделей · {{ totalOffers }} предложений</small></h1>

    <div class="filters">
      <input v-model="f.query" placeholder="поиск по названию…" />
      <input v-model="f.soc" placeholder="процессор (напр. Snapdragon 8)" />
      <label>экран ≥
        <input type="number" step="0.1" v-model.number="f.minScreen" style="width: 4em" /> "</label>
      <select v-model="f.marketplace">
        <option value="">оба маркета</option>
        <option value="ozon">только Ozon</option>
        <option value="yandex_market">только Я.Маркет</option>
        <option value="both">есть на обоих</option>
      </select>
      <select v-model="f.sort">
        <option value="offers">по числу предложений</option>
        <option value="priceAsc">сначала дешёвые</option>
        <option value="priceDesc">сначала дорогие</option>
      </select>
      <button class="preset" @click="preset">Snapdragon 8, &gt;11"</button>
    </div>

    <p v-if="loading">Загрузка…</p>
    <p v-else-if="error" class="err">
      Бэкенд недоступен ({{ error }}). Запусти <code>./gradlew quarkusDev</code>.
    </p>
    <p v-else-if="view.length === 0">Ничего не найдено под фильтры.</p>

    <div v-for="m in view" :key="m.id" class="card">
      <div class="head" @click="expanded[m.id] = !expanded[m.id]">
        <img v-if="imageOf(m)" :src="imageOf(m)" class="thumb" loading="lazy" />
        <div v-else class="thumb ph" />
        <div class="info">
          <div class="title">{{ m.title || "?" }}</div>
          <div class="meta">
            <span v-if="m.soc">{{ m.soc }}</span>
            <span v-if="m.screenInches">· {{ m.screenInches }}"</span>
          </div>
          <div class="badges">
            <span class="b offers">{{ offersOf(m).length }} предл.</span>
            <span class="b price" v-if="pricesOf(m).length">
              {{ fmt(pricesOf(m)[0]) }}–{{ fmt(pricesOf(m).slice(-1)[0]) }} ₽
            </span>
            <span v-for="mk in [...marketsOf(m)]" :key="mk" class="b mk">{{ mkLabel(mk) }}</span>
            <span v-if="marketsOf(m).size > 1" class="b cross">2 маркета</span>
            <span v-if="chinaOf(m)" class="b china">🇨🇳 {{ chinaOf(m) }}</span>
          </div>
        </div>
        <div class="chev">{{ expanded[m.id] ? "▲" : "▼" }}</div>
      </div>

      <div v-if="expanded[m.id]" class="offers">
        <div v-for="(v, vi) in sortedVariants(m)" :key="vi" class="variant">
          <div class="vhead">
            <img v-if="v.image" :src="v.image" class="vthumb" loading="lazy" />
            <span>{{ [v.color, (v.ramGb || v.storageGb) ? `${v.ramGb ?? "?"}/${v.storageGb ?? "?"} ГБ` : ""].filter(Boolean).join(" · ") || "вариант" }}</span>
            <span v-if="v.matchGroupId" class="b group" title="группа по фото">📷 #{{ v.matchGroupId }}</span>
          </div>
          <table>
            <tr v-for="(o, oi) in [...v.offers].sort((a,b)=>(a.price??0)-(b.price??0))" :key="oi">
              <td><span class="b mk">{{ mkLabel(o.marketplace) }}</span></td>
              <td class="seller">{{ o.seller || "—" }}</td>
              <td class="p">
                <b>{{ fmt(o.greenPrice ?? o.price) }} ₽</b>
                <s v-if="o.greenPrice && o.price && o.greenPrice < o.price">{{ fmt(o.price) }}</s>
              </td>
              <td>
                <span v-if="o.deliveryFromChina" class="b china">из Китая</span>
                <span v-else class="b local">локально</span>
                <span v-if="o.globalFirmware" class="b glob">global</span>
              </td>
              <td><a v-if="o.url" :href="o.url" target="_blank" rel="noopener">открыть ↗</a></td>
            </tr>
          </table>
        </div>
      </div>
    </div>
  </main>
</template>

<style>
body { margin: 0; background: #fafafa; }
.wrap { font: 14px/1.45 system-ui, sans-serif; max-width: 960px; margin: 0 auto; padding: 16px; color: #18181b; }
h1 { font-size: 20px; } h1 small { color: #71717a; font-size: 13px; font-weight: 400; }
.filters { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin: 12px 0 18px; position: sticky; top: 0; background: #fafafa; padding: 8px 0; z-index: 2; }
.filters input, .filters select { padding: 6px 8px; border: 1px solid #d4d4d8; border-radius: 6px; font: inherit; }
.preset { padding: 6px 12px; border: 1px solid #2563eb; background: #eff6ff; color: #1d4ed8; border-radius: 6px; cursor: pointer; }
.err { color: #b00020; }
.card { background: #fff; border: 1px solid #e4e4e7; border-radius: 10px; margin-bottom: 10px; overflow: hidden; }
.head { display: flex; gap: 12px; align-items: center; padding: 10px 12px; cursor: pointer; }
.head:hover { background: #f4f4f5; }
.thumb { width: 56px; height: 56px; object-fit: contain; border-radius: 8px; background: #f4f4f5; flex: none; }
.thumb.ph { }
.info { flex: 1; min-width: 0; }
.title { font-weight: 600; }
.meta { color: #52525b; font-size: 13px; }
.badges { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 5px; }
.b { font-size: 12px; padding: 2px 7px; border-radius: 10px; background: #f4f4f5; color: #3f3f46; white-space: nowrap; }
.b.offers { background: #ecfdf5; color: #047857; }
.b.price { background: #eff6ff; color: #1d4ed8; font-variant-numeric: tabular-nums; }
.b.cross { background: #fef3c7; color: #92400e; }
.b.china { background: #fee2e2; color: #b91c1c; }
.b.local { background: #f0fdf4; color: #15803d; }
.b.glob { background: #ede9fe; color: #6d28d9; }
.b.group { background: #e0f2fe; color: #0369a1; }
.chev { color: #a1a1aa; }
.offers { border-top: 1px solid #f0f0f0; padding: 6px 12px 12px; }
.variant { margin-top: 10px; }
.vhead { display: flex; align-items: center; gap: 8px; font-weight: 500; margin-bottom: 4px; }
.vthumb { width: 28px; height: 28px; object-fit: contain; border-radius: 5px; background: #f4f4f5; }
table { width: 100%; border-collapse: collapse; }
td { padding: 4px 6px; border-bottom: 1px solid #f4f4f5; vertical-align: middle; }
.p { font-variant-numeric: tabular-nums; white-space: nowrap; }
.p s { color: #a1a1aa; margin-left: 6px; font-size: 12px; }
.seller { color: #52525b; max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
a { color: #2563eb; text-decoration: none; }
</style>
