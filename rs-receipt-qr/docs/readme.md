Приложение, которое позволяет сканировать чеки, сохранять информацию о покупках и позволяет
анализировать расходы и находить чеки для гарантийного обслуживания.

## Модули

### Backend
- **be_quarkus** - бэкенд приложение на Java + Quarkus
  - Документация: `@/docs/be_quarkus/overview.md`
  - REST API для чеков и QR кодов
  - Асинхронная обработка QR кодов с tax.gov.me
  - PostgreSQL + MyBatis

### Frontend
- **fe_web_vue** - веб-приложение на TypeScript + Vue + Vue Material
  - Аналитика расходов, фильтрация, поиск

- **fe_mobile_flutter** - мобильное приложение на Flutter
  - Документация: `@/docs/fe_mobile_flutter/`
  - Сканирование QR кодов чеков
  - Offline работа с локальной БД (Drift)
  - Синхронизация с backend

## Документация

### API
- `@/docs/api/receipt-raw.md` - API для работы с QR кодами
- Полное описание REST endpoints в `@/docs/be_quarkus/overview.md`

### База данных
- `@/docs/er_diagram.puml` - ER диаграмма базы данных
- `@/be_quarkus/src/main/sql/schema.sql` - SQL схема

### QR коды чеков
- `@/docs/qr_crna_gora/qr-code-crna-gora.md` - информация о QR коде в Черногории
- `@/docs/qr_srbija/qr-code-srbija.md` - информация о QR коде в Сербии

## Архитектура

**Поток обработки QR кода:**
1. Flutter сканирует QR код чека
2. Отправляет URL на backend (`POST /api/receipt-raw`)
3. Backend сохраняет в `receipt_raw` со статусом `pending`
4. QrProcessingScheduler обрабатывает QR асинхронно (каждые 30 сек)
5. Backend получает данные с tax.gov.me API
6. Flutter периодически проверяет статус и загружает готовые данные

**Подробнее:** см. `@/docs/be_quarkus/overview.md` и `@/docs/fe_mobile_flutter/usage.md`

