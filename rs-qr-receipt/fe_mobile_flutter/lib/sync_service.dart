import 'dart:convert';
import 'package:http/http.dart' as http;
import 'local_db.dart';

// Extensions for JSON serialization
extension ReceiptToJson on Receipt {
  Map<String, dynamic> toJson() => {
    'id': id,
    'shopId': shopId,
    'date': date.toIso8601String(),
    'total': total,
    'imagePath': imagePath,
    'createdAt': createdAt.toIso8601String(),
  };
}

extension PurchaseItemToJson on PurchaseItem {
  Map<String, dynamic> toJson() => {
    'id': id,
    'receiptId': receiptId,
    'name': name,
    'categoryId': categoryId,
    'price': price,
    'quantity': quantity,
    'warrantyId': warrantyId,
  };
}

class ReceiptWithItems {
  final Receipt receipt;
  final List<PurchaseItem> items;

  ReceiptWithItems({required this.receipt, required this.items});

  Map<String, dynamic> toJson() => {
    'receipt': receipt.toJson(),
    'items': items.map((e) => e.toJson()).toList(),
  };
}

class SyncService {
  final LocalDatabase db;
  final String backendUrl;

  SyncService({required this.db, required this.backendUrl});

  Future<void> sendAllReceiptsWithItems() async {
    final receipts = await db.select(db.receipts).get();
    for (final receipt in receipts) {
      final items = await (db.select(db.purchaseItems)..where((tbl) => tbl.receiptId.equals(receipt.id))).get();
      final payload = ReceiptWithItems(receipt: receipt, items: items).toJson();
      final response = await http.post(
        Uri.parse('$backendUrl/api/receipts/with-items'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(payload),
      );
      if (response.statusCode == 200 || response.statusCode == 204) {
        // Optionally mark as synced
        print('Receipt ${receipt.id} sent successfully!');
      } else {
        print('Failed to send receipt ${receipt.id}: ${response.statusCode} ${response.body}');
        // Optionally handle retry or error
      }
    }
  }
} 