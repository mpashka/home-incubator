class InvoiceItem {
  final String name;
  final String code;
  final String unit;
  final double quantity;
  final double priceAfterVat;
  InvoiceItem({required this.name, required this.code, required this.unit, required this.quantity, required this.priceAfterVat});
  factory InvoiceItem.fromJson(Map<String, dynamic> json) => InvoiceItem(
    name: json['name'],
    code: json['code'],
    unit: json['unit'],
    quantity: (json['quantity'] as num).toDouble(),
    priceAfterVat: (json['priceAfterVat'] as num).toDouble(),
  );
}

class InvoiceResource {
  final String iic;
  final double totalPrice;
  final String dateTimeCreated;
  final String issuerTaxNumber;
  final List<InvoiceItem> items;
  InvoiceResource({required this.iic, required this.totalPrice, required this.dateTimeCreated, required this.issuerTaxNumber, required this.items});
  factory InvoiceResource.fromJson(Map<String, dynamic> json) => InvoiceResource(
    iic: json['iic'],
    totalPrice: (json['totalPrice'] as num).toDouble(),
    dateTimeCreated: json['dateTimeCreated'],
    issuerTaxNumber: json['issuerTaxNumber'],
    items: (json['items'] as List).map((item) => InvoiceItem.fromJson(item)).toList(),
  );
} 