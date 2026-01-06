# Структура файлов fe_mobile_flutter

- `lib/`
  - `main.dart` — Основная точка входа, домашняя страница, интеграция UI для сканирования QR и синхронизации.
  - `qr_scanner_page.dart` — UI сканера QR кода. Сканирует QR код и отправляет URL на backend для обработки. Backend асинхронно получает данные с tax.gov.me.
  - `local_db.dart` — Определение базы данных Drift с обновленной схемой v3:
    - `Users` - пользователи приложения
    - `Shops` - сети магазинов (с тегами)
    - `ShopPoints` - конкретные точки продаж (с адресами, tax_id)
    - `Categories` - категории товаров
    - `Warranties` - гарантии на товары
    - `Receipts` - чеки (с userId, shopPointId, posTime, tags)
    - `ReceiptRaws` - сырые QR коды (с статусами: pending/processing/completed/failed)
    - `ReceiptItems` - позиции чеков (с userId, tags)
  - `sync_service.dart` — Логика синхронизации с бэкендом. Опрашивает статус обработки QR кодов и загружает готовые данные.
  - `api_models.dart` — (TODO) Модели для взаимодействия с backend API.
- `pubspec.yaml` — Зависимости проекта (Flutter, Drift, HTTP, сканер QR кодов, shared_preferences и т.д.).
- Файлы, сгенерированные `build_runner` (например, `local_db.g.dart`) — Сгенерированы Drift для доступа к базе данных.

## Изменения в схеме БД (v2 → v3)

- **Receipts**: добавлено `userId`, `shopId` → `shopPointId`, `date` → `posTime`, добавлено `tags`
- **ReceiptItems**: добавлено `tags`
- **Shops**: удалено `address`, `phone`, добавлено `tags`
- **Новая таблица ShopPoints**: точки продаж с полной информацией о локации
- **Новая таблица ReceiptRaws**: сырые QR коды с отслеживанием статуса обработки

Другие стандартные каталоги Flutter (android/, ios/, web/ и т.д.) присутствуют для поддержки платформ.
 