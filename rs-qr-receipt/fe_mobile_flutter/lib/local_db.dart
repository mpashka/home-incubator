import 'package:drift/drift.dart';
import 'package:drift_flutter/drift_flutter.dart';

part 'local_db.g.dart';

class Receipts extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get shopId => integer().nullable().references(Shops, #id)();
  DateTimeColumn get date => dateTime()();
  RealColumn get total => real()();
  TextColumn get imagePath => text().nullable()();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
}

class Shops extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get name => text()();
  TextColumn get address => text().nullable()();
  TextColumn get phone => text().nullable()();
}

class Categories extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get name => text().unique()();
}

class Warranties extends Table {
  IntColumn get id => integer().autoIncrement()();
  DateTimeColumn get purchaseDate => dateTime()();
  IntColumn get periodMonths => integer()();
  TextColumn get status => text().withDefault(const Constant('active'))();
  TextColumn get notes => text().nullable()();
}

class PurchaseItems extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get receiptId => integer().references(Receipts, #id)();
  TextColumn get name => text()();
  IntColumn get categoryId => integer().nullable().references(Categories, #id)();
  RealColumn get price => real()();
  IntColumn get quantity => integer()();
  IntColumn get warrantyId => integer().nullable().references(Warranties, #id)();
}

@DriftDatabase(tables: [Receipts, Shops, Categories, PurchaseItems, Warranties])
class LocalDatabase extends _$LocalDatabase {
  LocalDatabase() : super(FlutterQueryExecutor.inDatabaseFolder(path: 'app.db'));

  @override
  int get schemaVersion => 1;
} 