import 'package:flutter/material.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import 'local_db.dart';
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:drift/drift.dart' show Value;
import 'qr_scanner_invoice_models.dart';

class QrScannerPage extends StatefulWidget {
  final void Function(String) onScanned;
  const QrScannerPage({super.key, required this.onScanned});

  @override
  State<QrScannerPage> createState() => _QrScannerPageState();
}

class _QrScannerPageState extends State<QrScannerPage> {
  final MobileScannerController controller = MobileScannerController(
    detectionSpeed: DetectionSpeed.noDuplicates,
  );
  bool _isProcessing = false;
  String? _message;

  @override
  Future<void> dispose() async {
    super.dispose();
    await controller.dispose();
  }

  Map<String, String?> extractParamsFromQrUrl(String url) {
    final uri = Uri.parse(url);
    return {
      'iic': uri.queryParameters['iic'],
      'crtd': uri.queryParameters['crtd'],
      'tin': uri.queryParameters['tin'],
    };
  }

  Future<Map<String, dynamic>> fetchInvoiceResource({
    required String iic,
    required String crtd,
    required String tin,
  }) async {
    final response = await http.post(
      Uri.parse('https://mapr.tax.gov.me/ic/api/verifyInvoice'),
      body: {
        'iic': iic,
        'dateTimeCreated': crtd,
        'tin': tin,
      },
    );
    if (response.statusCode == 200) {
      return Map<String, dynamic>.from(jsonDecode(response.body));
    } else {
      throw Exception('Failed to fetch invoice: ${response.statusCode}');
    }
  }

  Future<void> saveInvoiceToDb(InvoiceResource invoice, LocalDatabase db) async {
    final receiptId = await db.into(db.receipts).insert(
      ReceiptsCompanion.insert(
        userId: 1, // TODO: get actual user ID
        shopPointId: const Value(null),
        posTime: DateTime.parse(invoice.dateTimeCreated),
        total: invoice.totalPrice,
        imagePath: const Value(null),
        createdAt: Value(DateTime.now()),
      ),
    );
    for (final item in invoice.items) {
      await db.into(db.receiptItems).insert(
        ReceiptItemsCompanion.insert(
          userId: 1, // TODO: get actual user ID
          receiptId: receiptId,
          name: item.name,
          categoryId: const Value(null),
          price: item.priceAfterVat,
          quantity: item.quantity.toInt(),
          warrantyId: const Value(null),
        ),
      );
    }
  }

  Future<void> _handleScan(String qrUrl) async {
    setState(() {
      _isProcessing = true;
      _message = null;
    });
    try {
      final params = extractParamsFromQrUrl(qrUrl);
      final iic = params['iic'];
      final crtd = params['crtd'];
      final tin = params['tin'];
      if (iic == null || crtd == null || tin == null) {
        throw Exception('Missing required QR parameters');
      }
      final json = await fetchInvoiceResource(iic: iic, crtd: crtd, tin: tin);
      final invoice = InvoiceResource.fromJson(json);
      final db = LocalDatabase();
      await saveInvoiceToDb(invoice, db);
      setState(() {
        _message = 'Invoice saved successfully!';
      });
    } catch (e) {
      setState(() {
        _message = 'Error: $e';
      });
    } finally {
      setState(() {
        _isProcessing = false;
      });
    }
  }

  Future<void> _onDetect(BarcodeCapture capture) async {
    if (_isProcessing) return;

    final barcodes = capture.barcodes;
    if (barcodes.isEmpty) return;

    final code = barcodes.first.rawValue;
    if (code == null || code.isEmpty) return;

    await controller.stop();
    await _handleScan(code);
    await controller.start();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Scan QR Code')),
      body: Stack(
        children: [
          MobileScanner(
            controller: controller,
            onDetect: _onDetect,
          ),
          if (_isProcessing)
            Container(
              color: Colors.black54,
              child: const Center(child: CircularProgressIndicator()),
            ),
          if (_message != null)
            Align(
              alignment: Alignment.bottomCenter,
              child: Container(
                color: _message!.startsWith('Error') ? Colors.red : Colors.green,
                padding: const EdgeInsets.all(16),
                child: Text(_message!, style: const TextStyle(color: Colors.white)),
              ),
            ),
        ],
      ),
    );
  }
} 