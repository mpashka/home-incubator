# Backend Quarkus - Описание компонента

## Общая информация

**Технологический стек:**
- Java 11+
- Quarkus Framework - современный Java-фреймворк для облачных и микросервисных приложений
- PostgreSQL - реляционная база данных
- MyBatis - framework для персистентности данных (ORM)
- JAX-RS / Jakarta REST - для RESTful API

**Назначение:**
Backend-сервис для управления чеками, обработки QR кодов сербских/черногорских чеков и предоставления API для мобильного и веб-приложений.

---

## Архитектура

### Основные компоненты

```
be_quarkus/
├── src/main/java/com/receipt/
│   ├── Entity классы (Receipt, Shop, ShopPoint, etc.)
│   ├── Mapper интерфейсы (MyBatis)
│   ├── Resource классы (REST controllers)
│   ├── Service классы (бизнес-логика)
│   └── dto/ (Data Transfer Objects)
└── src/main/resources/
    └── application.properties (конфигурация)
```

### Слои приложения

1. **Entity Layer** - модели данных
   - `User.java` - пользователи
   - `Shop.java` - сети магазинов
   - `ShopPoint.java` - точки продаж
   - `Receipt.java` - чеки
   - `ReceiptRaw.java` - сырые QR коды
   - `ReceiptItem.java` - позиции чеков
   - `Category.java` - категории товаров
   - `Warranty.java` - гарантии

2. **Persistence Layer** - MyBatis mappers
   - `ReceiptMapper.java` - CRUD для чеков
   - `ReceiptRawMapper.java` - CRUD для сырых QR
   - `ShopPointMapper.java` - CRUD для точек продаж
   - `ShopMapper.java` - CRUD для магазинов
   - `ReceiptItemMapper.java` - CRUD для позиций

3. **Service Layer** - бизнес-логика
   - `QrProcessingService.java` - обработка QR кодов
   - `QrProcessingScheduler.java` - планировщик асинхронной обработки

4. **API Layer** - REST endpoints
   - `ReceiptResource.java` - API для чеков
   - `ReceiptRawResource.java` - API для QR кодов
   - `ReceiptItemResource.java` - API для позиций

5. **DTO Layer** - модели для внешних API
   - `dto/InvoiceResource.java` - чек от tax.gov.me
   - `dto/InvoiceItem.java` - позиция чека от tax.gov.me

---

## Основные функции

### 1. Асинхронная обработка QR кодов

**Процесс:**
1. Клиент отправляет QR URL через `POST /api/receipt-raw`
2. Backend создает запись в `receipt_raw` со статусом `pending`
3. `QrProcessingScheduler` каждые 30 секунд проверяет pending QR коды
4. `QrProcessingService` для каждого QR:
   - Парсит URL (параметры: iic, crtd, tin)
   - Вызывает tax.gov.me API
   - Создает/находит ShopPoint по tax_id
   - Создает Receipt и ReceiptItem записи
   - Обновляет статус на `completed` или `failed`

**Компоненты:**
- `QrProcessingService.java` - логика обработки
- `QrProcessingScheduler.java` - планировщик (@Scheduled every 30s)
- `ReceiptRawResource.java` - REST API

### 2. CRUD операции для чеков

**Endpoints:**
- `GET /api/receipts` - список всех чеков
- `GET /api/receipts/{id}` - получение чека по ID
- `POST /api/receipts` - создание чека
- `POST /api/receipts/with-items` - создание чека с позициями
- `PUT /api/receipts/{id}` - обновление чека
- `DELETE /api/receipts/{id}` - удаление чека
- `PUT /api/receipts/{id}/tags` - обновление тегов

**Компонент:** `ReceiptResource.java`

### 3. Работа с позициями чеков

**Endpoints:**
- `GET /api/receipt-items/by-receipt/{receiptId}` - позиции по чеку
- `GET /api/receipt-items/by-user/{userId}` - позиции по пользователю
- `GET /api/receipt-items/{id}` - получение позиции
- `POST /api/receipt-items` - создание позиции
- `PUT /api/receipt-items/{id}` - обновление позиции
- `DELETE /api/receipt-items/{id}` - удаление позиции
- `PUT /api/receipt-items/{id}/tags` - обновление тегов

**Компонент:** `ReceiptItemResource.java`

### 4. API для работы с QR кодами

**Endpoints:**
- `POST /api/receipt-raw` - отправка QR для обработки
- `GET /api/receipt-raw/{id}` - статус обработки
- `GET /api/receipt-raw/{id}/receipt` - получение обработанного чека
- `GET /api/receipt-raw/pending` - список необработанных (служебный)
- `GET /api/receipt-raw/user/{userId}` - QR коды пользователя

**Компонент:** `ReceiptRawResource.java`

---

## Интеграции

### Tax.gov.me API (Черногория)

**Endpoint:** `https://mapr.tax.gov.me/ic/api/verifyInvoice`

**Метод:** POST (Form data)

**Параметры:**
- `iic` - Internal Invoice Code
- `dateTimeCreated` - дата/время создания чека
- `tin` - Tax Identification Number (налоговый номер)

**Ответ:**
```json
{
  "iic": "...",
  "totalPrice": 1234.56,
  "dateTimeCreated": "2025-01-06T14:30:00",
  "issuerTaxNumber": "12345678",
  "items": [
    {
      "name": "Товар",
      "code": "123",
      "unit": "kom",
      "quantity": 2.0,
      "priceAfterVat": 89.99
    }
  ]
}
```

**Обработка:** `QrProcessingService.fetchInvoiceFromTaxApi()`

---

## Конфигурация

**Файл:** `src/main/resources/application.properties`

**Основные настройки:**
```properties
# Database
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=receipt_user
quarkus.datasource.password=***
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/receipt_db

# MyBatis
quarkus.mybatis.scan-path=com.receipt
```

---

## База данных

### Схема

**Файл:** `src/main/sql/schema.sql`

**Таблицы:**
1. `user` - пользователи
2. `shop` - сети магазинов
3. `shop_point` - точки продаж (с tax_id)
4. `category` - категории товаров
5. `warranty` - гарантии
6. `receipt` - чеки (с user_id, shop_point_id, pos_time, tags)
7. `receipt_raw` - сырые QR коды (с status, created_at)
8. `receipt_item` - позиции чеков (с user_id, tags)

**Ключевые индексы:**
- `idx_receipt_raw_status` - для быстрой выборки pending QR
- `idx_shop_point_tax_id` - для поиска по tax_id при обработке
- Индексы для всех FK и дат

### Миграции

**Файлы:**
- `db-create.sql` - создание БД и пользователя
- `schema.sql` - схема таблиц и индексов
- `test-data.sql` - тестовые данные

---

## Особенности реализации

### 1. Поддержка PostgreSQL массивов

Поля `tags` (TEXT[]) используют `ArrayTypeHandler` для корректной работы с PostgreSQL массивами:

```java
@Insert("INSERT INTO receipt(..., tags) VALUES(..., #{tags, typeHandler=org.apache.ibatis.type.ArrayTypeHandler})")
```

### 2. Асинхронная обработка

`@Scheduled` аннотация Quarkus обеспечивает автоматический запуск обработки:

```java
@Scheduled(every = "30s")
void processPendingQrCodes() {
    // обработка pending QR кодов
}
```

### 3. Dependency Injection

Используется Jakarta CDI через `@Inject`:

```java
@Inject
ReceiptMapper receiptMapper;
```

### 4. REST API

JAX-RS аннотации для RESTful endpoints:

```java
@Path("/api/receipts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReceiptResource { ... }
```

---

## Запуск и разработка

### Разработка (dev mode)

```bash
cd be_quarkus
./mvnw quarkus:dev
```

Dev mode включает:
- Hot reload при изменении кода
- Dev UI: http://localhost:8080/q/dev

### Production build

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Native build (GraalVM)

```bash
./mvnw package -Pnative
./target/be_quarkus-1.0.0-SNAPSHOT-runner
```

---

## Тестирование

### Тесты

**Файл:** `src/test/java/com/receipt/ReceiptResourceTest.java`

Запуск:
```bash
./mvnw test
```

### Ручное тестирование API

**Примеры curl:**

```bash
# Отправка QR кода
curl -X POST http://localhost:8080/api/receipt-raw \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "url": "https://mapr.tax.gov.me/ic/verifyInvoice?iic=...&crtd=...&tin=..."}'

# Проверка статуса
curl http://localhost:8080/api/receipt-raw/1

# Получение чека
curl http://localhost:8080/api/receipt-raw/1/receipt

# Список чеков
curl http://localhost:8080/api/receipts
```

---

## Логирование

Quarkus использует JDK logging. Уровни логов настраиваются в `application.properties`:

```properties
quarkus.log.level=INFO
quarkus.log.category."com.receipt".level=DEBUG
```

**Логи в коде:**
```java
private static final Logger LOGGER = Logger.getLogger(QrProcessingService.class.getName());
LOGGER.info("Processing QR code: " + receiptRawId);
```

---

## Безопасность

**Текущее состояние:** Аутентификация не реализована.

**TODO для production:**
- Добавить JWT аутентификацию
- Настроить CORS
- Добавить rate limiting для API endpoints
- Валидация входных данных
- Защита от SQL injection (MyBatis prepared statements)

---

## Производительность

**Оптимизации:**
- Индексы на всех FK и часто используемых полях
- Connection pooling (настраивается в datasource)
- Асинхронная обработка QR кодов
- Pagination для больших списков (TODO)

**Мониторинг:**
- Quarkus Metrics: `/q/metrics`
- Health checks: `/q/health`

---

## Зависимости

**Основные (pom.xml):**
- `quarkus-resteasy-reactive-jackson` - REST + JSON
- `quarkus-jdbc-postgresql` - PostgreSQL driver
- `quarkus-mybatis` - MyBatis integration
- `quarkus-scheduler` - Scheduled tasks
- `quarkus-rest-client` - HTTP client для tax.gov.me

---

## Ссылки

- [Quarkus Documentation](https://quarkus.io/guides/)
- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [REST API Documentation](../api/receipt-raw.md)
- [Database Schema](../../be_quarkus/src/main/sql/schema.sql)
- [ER Diagram](../er_diagram.puml)
