# Receipt Raw API

API для работы с сырыми QR кодами чеков и асинхронной обработкой данных.

## Endpoints

### POST /api/receipt-raw
Создание записи для обработки QR кода чека.

**Request:**
```json
{
  "userId": 1,
  "url": "https://mapr.tax.gov.me/ic/verifyInvoice?iic=...&crtd=...&tin=..."
}
```

**Response:**
```json
{
  "receiptRawId": 123,
  "status": "pending"
}
```

**Статус коды:**
- 200 OK - успешно создано
- 400 Bad Request - неверные параметры
- 500 Internal Server Error - ошибка сервера

---

### GET /api/receipt-raw/{id}
Получение статуса обработки QR кода.

**Path Parameters:**
- `id` (integer) - ID записи receipt_raw

**Response:**
```json
{
  "receiptRawId": 123,
  "status": "completed",
  "receiptId": 456
}
```

**Статусы обработки:**
- `pending` - ожидает обработки
- `processing` - в процессе обработки
- `completed` - обработка завершена успешно
- `failed` - ошибка при обработке

**Статус коды:**
- 200 OK - успешно получено
- 404 Not Found - запись не найдена

---

### GET /api/receipt-raw/{id}/receipt
Получение обработанного чека с позициями.

**Path Parameters:**
- `id` (integer) - ID записи receipt_raw

**Response:**
```json
{
  "receipt": {
    "id": 456,
    "userId": 1,
    "shopPointId": 78,
    "posTime": "2025-01-06T14:30:00",
    "total": 1234.56,
    "imagePath": null,
    "createdAt": "2025-01-06T14:35:00",
    "tags": ["продукты", "idea"]
  },
  "items": [
    {
      "id": 789,
      "userId": 1,
      "receiptId": 456,
      "name": "Молоко",
      "categoryId": 3,
      "price": 89.99,
      "quantity": 2,
      "warrantyId": null,
      "tags": ["молочка"]
    },
    {
      "id": 790,
      "userId": 1,
      "receiptId": 456,
      "name": "Хлеб",
      "categoryId": 2,
      "price": 45.00,
      "quantity": 1,
      "warrantyId": null,
      "tags": []
    }
  ]
}
```

**Статус коды:**
- 200 OK - успешно получено
- 404 Not Found - запись не найдена или чек еще не обработан

---

### GET /api/receipt-raw/pending
Получение списка необработанных QR кодов (для внутреннего использования backend).

**Response:**
```json
[
  {
    "id": 123,
    "userId": 1,
    "url": "https://...",
    "receiptId": null,
    "createdAt": "2025-01-06T14:30:00",
    "status": "pending"
  },
  {
    "id": 124,
    "userId": 1,
    "url": "https://...",
    "receiptId": null,
    "createdAt": "2025-01-06T14:32:00",
    "status": "pending"
  }
]
```

**Статус коды:**
- 200 OK - успешно получено (может быть пустой массив)

---

### GET /api/receipt-raw/user/{userId}
Получение всех QR кодов пользователя.

**Path Parameters:**
- `userId` (integer) - ID пользователя

**Response:**
Массив объектов receipt_raw для данного пользователя.

**Статус коды:**
- 200 OK - успешно получено

---

## Процесс обработки

1. **Клиент** отправляет QR URL через `POST /api/receipt-raw`
2. **Backend** создает запись со статусом `pending`
3. **QrProcessingScheduler** (каждые 30 секунд):
   - Получает список pending QR кодов
   - Для каждого вызывает `QrProcessingService.processQrCode()`
4. **QrProcessingService**:
   - Обновляет статус на `processing`
   - Парсит URL (iic, crtd, tin)
   - Вызывает tax.gov.me API
   - Создает/находит ShopPoint по tax_id
   - Создает Receipt
   - Создает ReceiptItem для каждой позиции
   - Обновляет receipt_raw: `receiptId` и статус `completed`
   - При ошибке - статус `failed`
5. **Клиент** периодически проверяет статус через `GET /api/receipt-raw/{id}`
6. Когда статус `completed` - загружает данные через `GET /api/receipt-raw/{id}/receipt`

---

## Примеры использования

### Flutter приложение

```dart
// 1. Отправка QR кода
Future<int> sendQrToBackend(String qrUrl) async {
  final response = await http.post(
    Uri.parse('$backendUrl/api/receipt-raw'),
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode({'userId': currentUserId, 'url': qrUrl}),
  );
  final data = jsonDecode(response.body);
  return data['receiptRawId'];
}

// 2. Проверка статуса
Future<String> checkStatus(int receiptRawId) async {
  final response = await http.get(
    Uri.parse('$backendUrl/api/receipt-raw/$receiptRawId'),
  );
  final data = jsonDecode(response.body);
  return data['status'];
}

// 3. Загрузка данных
Future<void> loadReceiptData(int receiptRawId) async {
  final response = await http.get(
    Uri.parse('$backendUrl/api/receipt-raw/$receiptRawId/receipt'),
  );
  final data = jsonDecode(response.body);
  // Сохранить receipt и items в локальную БД
}
```

---

## Обработка ошибок

### Failed status
Если статус `failed`, данные чека не были получены. Возможные причины:
- tax.gov.me API недоступен
- Неверные параметры в QR коде
- Некорректный формат данных от tax.gov.me

Рекомендуется:
- Показать пользователю сообщение об ошибке
- Предложить повторно отсканировать QR код
- Логировать ошибку для анализа

### Timeout обработки
Если чек находится в статусе `pending` или `processing` длительное время (>5 минут):
- Проверить работу backend
- Проверить доступность tax.gov.me API
- Возможно, требуется ручная обработка
