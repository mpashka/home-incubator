// Types
export interface PurchaseItem {
  id: number;
  receiptId: number;
  name: string;
  categoryId?: number;
  price: number;
  quantity: number;
  warrantyId?: number;
  tags?: string[];
}

export interface Receipt {
  id: number;
  shopId?: number;
  date: string;
  total: number;
  imagePath?: string;
  createdAt: string;
  items?: PurchaseItem[];
  tags?: string[];
}

// API base URL
const API_BASE = 'http://localhost:8080/api'; // Change as needed

// Fetch all receipts
export async function fetchReceipts(): Promise<Receipt[]> {
  const res = await fetch(`${API_BASE}/receipts`);
  if (!res.ok) throw new Error('Failed to fetch receipts');
  return await res.json();
}

// Fetch purchase items for a receipt
export async function fetchPurchaseItems(receiptId: number): Promise<PurchaseItem[]> {
  const res = await fetch(`${API_BASE}/purchase-items/by-receipt/${receiptId}`);
  if (!res.ok) throw new Error('Failed to fetch purchase items');
  return await res.json();
}

// Add a new purchase item
export async function addPurchaseItem(item: Omit<PurchaseItem, 'id'>): Promise<void> {
  await fetch(`${API_BASE}/purchase-items`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(item),
  });
}

// Add a new receipt
export async function addReceipt(receipt: Omit<Receipt, 'id'>): Promise<void> {
  await fetch(`${API_BASE}/receipts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(receipt),
  });
}

// Update tags for a receipt or purchase item
export async function updateTags(
  type: 'receipt' | 'purchase-item',
  id: number,
  tags: string[],
): Promise<void> {
  await fetch(`${API_BASE}/${type === 'receipt' ? 'receipts' : 'purchase-items'}/${id}/tags`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ tags }),
  });
}

// The backend must support:
// - GET /api/receipts (list receipts)
// - GET /api/purchase-items/by-receipt/:receiptId (list items for a receipt)
// - POST /api/receipts (add receipt)
// - POST /api/purchase-items (add purchase item)
// - PUT /api/receipts/:id/tags (update receipt tags)
// - PUT /api/purchase-items/:id/tags (update purchase item tags)
// Receipts and purchase items should support a 'tags' field (array of strings) 