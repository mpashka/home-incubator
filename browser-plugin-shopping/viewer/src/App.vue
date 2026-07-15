<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import type { CapturePage, CaptureSummary } from "./types";

const items = ref<CaptureSummary[]>([]);
const total = ref(0);
const page = ref(0);
const size = 50;
const marketplace = ref("");
const kind = ref("");
const q = ref("");
const error = ref("");
const loading = ref(true);

const pageCount = computed(() => Math.max(1, Math.ceil(total.value / size)));

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  const params = new URLSearchParams({ page: String(page.value), size: String(size) });
  if (marketplace.value) params.set("marketplace", marketplace.value);
  if (kind.value) params.set("kind", kind.value);
  if (q.value.trim()) params.set("q", q.value.trim());
  try {
    const res = await fetch(`/api/captures?${params}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = (await res.json()) as CapturePage;
    items.value = data.items;
    total.value = data.total;
  } catch (e) {
    error.value = String(e);
  } finally {
    loading.value = false;
  }
}

// Reload when filters change (reset to first page).
watch([marketplace, kind], () => {
  page.value = 0;
  void load();
});

const fmtDate = (ms: number) => new Date(ms).toLocaleString();

onMounted(load);
</script>

<template>
  <main style="font: 14px/1.5 system-ui, sans-serif; max-width: 1000px; margin: 24px auto; padding: 0 16px">
    <h1>Collected captures <small style="color: #71717a">({{ total }})</small></h1>

    <div style="display: flex; gap: 8px; flex-wrap: wrap; margin: 12px 0">
      <select v-model="marketplace">
        <option value="">all marketplaces</option>
        <option value="ozon">Ozon</option>
        <option value="yandex_market">Yandex Market</option>
      </select>
      <select v-model="kind">
        <option value="">all kinds</option>
        <option value="search">search</option>
        <option value="product">product</option>
        <option value="unknown">unknown</option>
      </select>
      <input v-model="q" placeholder="query contains…" @keyup.enter="page = 0; load()" />
      <button @click="page = 0; load()">Search</button>
    </div>

    <p v-if="loading">Loading…</p>
    <p v-else-if="error" style="color: #b00020">
      Backend not reachable ({{ error }}). Run <code>./gradlew quarkusDev</code> in <code>backend/</code>.
    </p>
    <p v-else-if="items.length === 0">
      No captures yet. Browse Ozon / Yandex Market with the extension enabled.
    </p>

    <table v-else style="width: 100%; border-collapse: collapse">
      <thead>
        <tr style="text-align: left; border-bottom: 1px solid #e4e4e7">
          <th></th><th>marketplace</th><th>kind</th><th>query</th><th>imgs</th><th>captured</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="c in items" :key="c.id" style="border-bottom: 1px solid #f4f4f5">
          <td style="width: 56px; padding: 6px 8px">
            <img v-if="c.thumbnail" :src="c.thumbnail" width="44" height="44" style="object-fit: cover; border-radius: 6px" />
          </td>
          <td>{{ c.marketplace }}</td>
          <td>{{ c.kind }}</td>
          <td><a :href="c.pageUrl" target="_blank" rel="noopener">{{ c.searchQuery || "—" }}</a></td>
          <td>{{ c.imageCount }}</td>
          <td style="color: #71717a; white-space: nowrap">{{ fmtDate(c.capturedAt) }}</td>
        </tr>
      </tbody>
    </table>

    <div v-if="!loading && !error && total > size" style="display: flex; gap: 12px; align-items: center; margin-top: 14px">
      <button :disabled="page <= 0" @click="page--; load()">Prev</button>
      <span>page {{ page + 1 }} / {{ pageCount }}</span>
      <button :disabled="page + 1 >= pageCount" @click="page++; load()">Next</button>
    </div>
  </main>
</template>
