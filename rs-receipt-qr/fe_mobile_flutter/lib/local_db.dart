import 'package:drift/drift.dart';
import 'package:drift_flutter/drift_flutter.dart';

part 'local_db.g.dart';

class Users extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get firstName => text().nullable()();
  TextColumn get lastName => text().nullable()();
}

class Shops extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get name => text()();
  TextColumn get tags => text().nullable()();
}

class ShopPoints extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get shopId => integer().nullable().references(Shops, #id)();
  IntColumn get taxId => integer()();
  TextColumn get name => text()();
  TextColumn get locationName => text().nullable()();
  TextColumn get address => text().nullable()();
  TextColumn get city => text().nullable()();
  TextColumn get cityUnit => text().nullable()();
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

class Receipts extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get userId => integer().references(Users, #id)();
  IntColumn get shopPointId => integer().nullable().references(ShopPoints, #id)();
  DateTimeColumn get posTime => dateTime()();
  RealColumn get total => real()();
  TextColumn get imagePath => text().nullable()();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
  TextColumn get tags => text().nullable()();
}

class ReceiptRaws extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get userId => integer().references(Users, #id)();
  TextColumn get url => text()();
  IntColumn get receiptId => integer().nullable().references(Receipts, #id)();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
  TextColumn get status => text().withDefault(const Constant('pending'))();
}

class ReceiptItems extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get userId => integer().references(Users, #id)();
  IntColumn get receiptId => integer().references(Receipts, #id)();
  TextColumn get name => text()();
  IntColumn get categoryId => integer().nullable().references(Categories, #id)();
  RealColumn get price => real()();
  IntColumn get quantity => integer()();
  IntColumn get warrantyId => integer().nullable().references(Warranties, #id)();
  TextColumn get tags => text().nullable()();
}

@DriftDatabase(tables: [Users, Shops, ShopPoints, Categories, Warranties, Receipts, ReceiptRaws, ReceiptItems])
class LocalDatabase extends _$LocalDatabase {
  LocalDatabase() : super(driftDatabase(name: 'app.db'));

  @override
  int get schemaVersion => 3;

  @override
  MigrationStrategy get migration => MigrationStrategy(
    onUpgrade: (migrator, from, to) async {
      if (from < 3) {
        // Drop all old data and recreate schema
        await migrator.deleteTable('receipt_items');
        await migrator.deleteTable('receipts');
        await migrator.deleteTable('shops');

        // Create new tables
        await migrator.createTable(shopPoints);
        await migrator.createTable(receiptRaws);
        await migrator.createTable(shops);
        await migrator.createTable(receipts);
        await migrator.createTable(receiptItems);
      }
    },
  );
}
 