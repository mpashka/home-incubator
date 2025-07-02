<template>
  <div>
    <h2>Receipts</h2>
    <div class="filters">
      <input v-model="filterText" placeholder="Filter by tag/type..." />
      <button @click="saveFilter">Save Filter</button>
    </div>
    <div class="totals">Total Purchases: {{ totalPurchases }}</div>
    <button @click="showAddReceipt = true">Add Receipt</button>
    <div v-for="receipt in filteredReceipts" :key="receipt.id" class="receipt">
      <div>
        <strong>Date:</strong> {{ receipt.date }} | <strong>Total:</strong> {{ receipt.total }}
        <span v-if="receipt.tags">Tags: <input v-model="receipt.tagsString" @change="updateReceiptTags(receipt)" placeholder="Comma separated tags" /></span>
      </div>
      <div>
        <button @click="showAddPurchase(receipt.id)">Add Purchase</button>
      </div>
      <ul>
        <li v-for="item in receipt.items" :key="item.id">
          {{ item.name }} ({{ item.quantity }}) - {{ item.price }}
          <span v-if="item.tags">Tags: <input v-model="item.tagsString" @change="updatePurchaseTags(item)" placeholder="Comma separated tags" /></span>
        </li>
      </ul>
    </div>
    <div v-if="showAddReceipt">
      <h3>Add Receipt</h3>
      <form @submit.prevent="addReceipt">
        <input v-model="newReceipt.date" placeholder="Date (YYYY-MM-DD)" required />
        <input v-model.number="newReceipt.total" placeholder="Total" required />
        <button type="submit">Add</button>
        <button @click="showAddReceipt = false">Cancel</button>
      </form>
    </div>
    <div v-if="showAddPurchaseId">
      <h3>Add Purchase</h3>
      <form @submit.prevent="addPurchase">
        <input v-model="newPurchase.name" placeholder="Name" required />
        <input v-model.number="newPurchase.price" placeholder="Price" required />
        <input v-model.number="newPurchase.quantity" placeholder="Quantity" required />
        <button type="submit">Add</button>
        <button @click="showAddPurchaseId = null">Cancel</button>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed } from 'vue';
import { fetchReceipts, fetchPurchaseItems, addReceipt, addPurchaseItem, updateTags, Receipt, PurchaseItem } from '../api';

export default defineComponent({
  setup() {
    const receipts = ref<Receipt[]>([]);
    const filterText = ref('');
    const savedFilter = ref('');
    const showAddReceipt = ref(false);
    const showAddPurchaseId = ref<number|null>(null);
    const newReceipt = ref({ date: '', total: 0 });
    const newPurchase = ref({ name: '', price: 0, quantity: 1 });

    const loadReceipts = async () => {
      const data = await fetchReceipts();
      // Fetch items for each receipt
      for (const r of data) {
        r.items = await fetchPurchaseItems(r.id);
        // For tag editing UI
        r.tagsString = (r.tags || []).join(', ');
        r.items.forEach(i => i.tagsString = (i.tags || []).join(', '));
      }
      receipts.value = data;
    };

    onMounted(loadReceipts);

    const filteredReceipts = computed(() => {
      if (!filterText.value) return receipts.value;
      const f = filterText.value.toLowerCase();
      return receipts.value.filter(r =>
        (r.tags && r.tags.some(t => t.toLowerCase().includes(f))) ||
        (r.items && r.items.some(i => i.tags && i.tags.some(t => t.toLowerCase().includes(f))))
      );
    });

    const totalPurchases = computed(() => {
      return receipts.value.reduce((sum, r) => sum + (r.items?.reduce((s, i) => s + i.price * i.quantity, 0) || 0), 0);
    });

    const saveFilter = () => {
      savedFilter.value = filterText.value;
      localStorage.setItem('receiptFilter', filterText.value);
    };

    onMounted(() => {
      const saved = localStorage.getItem('receiptFilter');
      if (saved) filterText.value = saved;
    });

    const updateReceiptTags = async (receipt: any) => {
      receipt.tags = receipt.tagsString.split(',').map((t: string) => t.trim()).filter((t: string) => t);
      await updateTags('receipt', receipt.id, receipt.tags);
    };
    const updatePurchaseTags = async (item: any) => {
      item.tags = item.tagsString.split(',').map((t: string) => t.trim()).filter((t: string) => t);
      await updateTags('purchase-item', item.id, item.tags);
    };

    const addReceipt = async () => {
      await addReceipt({ ...newReceipt.value, createdAt: new Date().toISOString() });
      showAddReceipt.value = false;
      newReceipt.value = { date: '', total: 0 };
      await loadReceipts();
    };
    const showAddPurchase = (receiptId: number) => {
      showAddPurchaseId.value = receiptId;
    };
    const addPurchase = async () => {
      if (showAddPurchaseId.value) {
        await addPurchaseItem({ ...newPurchase.value, receiptId: showAddPurchaseId.value });
        showAddPurchaseId.value = null;
        newPurchase.value = { name: '', price: 0, quantity: 1 };
        await loadReceipts();
      }
    };

    return {
      receipts,
      filterText,
      filteredReceipts,
      totalPurchases,
      saveFilter,
      showAddReceipt,
      newReceipt,
      addReceipt,
      showAddPurchaseId,
      showAddPurchase,
      newPurchase,
      addPurchase,
      updateReceiptTags,
      updatePurchaseTags,
    };
  },
});
</script>

<style scoped>
.receipt {
  border: 1px solid #ccc;
  margin: 1em 0;
  padding: 1em;
}
.filters {
  margin-bottom: 1em;
}
.totals {
  font-weight: bold;
  margin-bottom: 1em;
}
</style> 