# fe_mobile_android

Нативное Android приложение для сканирования QR кодов и текста чеков.

## Особенности

- **Google ML Kit Barcode Scanning** для сканирования QR кодов - обеспечивает высокое качество распознавания больших QR кодов (до 900 байт)
- **Google ML Kit Text Recognition (Latin)** для offline OCR сканирования - быстрое распознавание латинских символов без интернета
- **Firebase ML Vision (Cloud)** для облачного OCR сканирования - полноценное распознавание кириллицы и латиницы через облако
- **CameraX** для работы с камерой - современный API, оптимизированный для Android 15
- **Java 17** - использование современных возможностей языка (records, var, text blocks) для модуля OCR
- **Material Design 3** - современный UI
- Поддержка **Android 15** (API level 35)
- Минимальная версия **Android 14** (API 34)
- Оптимизировано для **Xiaomi HyperOS 2**

## Требования

- Android 14+ (API 34+)
- Камера
- Интернет-соединение

## Структура проекта

```
fe_mobile_android/
├── app/
│   ├── src/main/
│   │   ├── java/com/receipt/scanner/
│   │   │   ├── api/           # API клиент (Retrofit)
│   │   │   │   ├── ApiClient.kt
│   │   │   │   ├── ReceiptApi.kt
│   │   │   │   └── ReceiptTextApi.java  # Java 17 API для OCR
│   │   │   ├── data/          # Репозитории и модели данных
│   │   │   ├── model/         # Java 17 Records для OCR данных
│   │   │   │   ├── ReceiptTextData.java
│   │   │   │   ├── ReceiptTextRequest.java
│   │   │   │   └── ReceiptTextResponse.java
│   │   │   ├── ui/            # Activities и Adapters
│   │   │   │   ├── HomeActivity.kt      # Главный экран с выбором режима
│   │   │   │   ├── MainActivity.kt      # QR сканирование
│   │   │   │   ├── TextScanActivity.kt  # OCR сканирование текста
│   │   │   │   ├── SettingsActivity.kt
│   │   │   │   └── HistoryActivity.kt
│   │   │   ├── util/          # Утилиты
│   │   │   │   ├── QrCodeAnalyzer.kt        # ML Kit Barcode
│   │   │   │   ├── TextReceiptAnalyzer.java # ML Kit Text Recognition (Java 17)
│   │   │   │   └── ReceiptTextParser.java   # Парсер PFR данных (Java 17)
│   │   │   └── ReceiptScannerApp.kt
│   │   ├── res/
│   │   │   ├── layout/        # XML layouts
│   │   │   ├── values/        # Strings, colors, themes
│   │   │   ├── drawable/      # Иконки и графика
│   │   │   └── xml/           # Конфигурации
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml     # Версии зависимостей
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew
```

## Основные компоненты

### HomeActivity
Главный экран с выбором режима сканирования:
- Кнопка "Scan QR Code" - переход к сканированию QR кодов
- Кнопка "Scan Receipt Text" - переход к OCR сканированию текста чека
- Кнопки настроек и истории

### MainActivity
Экран сканирования QR кодов:
- Превью камеры в полноэкранном режиме
- Overlay с рамкой для сканирования
- Статусные сообщения
- Автоматическое распознавание и отправка QR кодов

### TextScanActivity
Экран OCR сканирования текста чека:
- Превью камеры с увеличенной областью сканирования
- Распознавание кириллического текста (ML Kit Cyrillic)
- Автоматическое извлечение PFR данных:
  - ПФР време (время)
  - ПФР број рачуна (номер чека)
  - Бројач рачуна (счётчик)
- Отправка данных на backend

### SettingsActivity
Настройки приложения:
- Backend URL для отправки данных
- User ID

### HistoryActivity
История отсканированных QR кодов:
- Список всех сканирований
- Статус обработки (pending, completed, failed)
- ID чека после обработки

### QrCodeAnalyzer (Kotlin)
Анализатор QR кодов:
- Использует ML Kit Barcode Scanning
- Фильтрует только чековые QR коды (tax.gov, verifyInvoice)
- Оптимизирован для больших QR кодов

### TextReceiptAnalyzer (Java 17)
Анализатор текста чеков:
- Использует ML Kit Text Recognition с поддержкой кириллицы
- Использует Java 17 features (var, records, text blocks)
- Автоматическое извлечение PFR данных с помощью регулярных выражений

### ReceiptTextParser (Java 17)
Парсер PFR данных:
- Извлечение времени, номера чека и счётчика из OCR текста
- Валидация распознанных данных
- Debug-режим для отладки

## API

Приложение использует REST API backend'а:

### POST /api/receipt-raw
Отправка QR кода на обработку:
```json
{
  "userId": 1,
  "url": "https://mapr.tax.gov.me/ic/verifyInvoice?iic=...&crtd=...&tin=..."
}
```

Ответ:
```json
{
  "receiptRawId": 123,
  "status": "pending"
}
```

### POST /api/receipt-text
Отправка распознанного текста чека (PFR данные):
```json
{
  "userId": 1,
  "pfrTime": "07.09.2024. 19:25:47",
  "pfrReceiptNumber": "P8BPS55R-P8BPS55R-71822",
  "pfrCounter": "64763/71822ПП"
}
```

Ответ:
```json
{
  "receiptTextId": 123,
  "status": "received"
}
```

## Сборка

```bash
cd fe_mobile_android
./gradlew assembleDebug
```

APK будет в `app/build/outputs/apk/debug/app-debug.apk`

## Установка

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Использование

1. Запустите приложение
2. Нажмите на кнопку настроек (шестерёнка справа вверху)
3. Введите URL backend'а (например: `http://192.168.1.100:8080`)
4. Введите User ID
5. Сохраните настройки
6. Наведите камеру на QR код чека
7. Приложение автоматически распознает и отправит QR код

## Зависимости

- **CameraX** 1.4.1 - работа с камерой
- **ML Kit Barcode** 17.3.0 - распознавание QR кодов
- **ML Kit Text Recognition** 16.0.1 - offline OCR (Latin only)
- **Firebase ML Vision** - облачное OCR (Cyrillic + Latin)
- **Retrofit** 2.11.0 - HTTP клиент
- **OkHttp** 4.12.0 - HTTP interceptors
- **Coroutines** 1.9.0 - асинхронные операции
- **DataStore** 1.1.1 - хранение настроек
- **Material Components** 1.12.0 - UI компоненты

## OCR Режимы

Приложение поддерживает два режима распознавания текста:

### 1. ML Kit Offline (Latin)
- Работает без интернета
- Быстрое распознавание
- Только латинские символы (A-Z, 0-9)
- Подходит для номеров чеков: `P8BPS55R-P8BPS55R-71822`

### 2. Firebase ML Cloud (Cyrillic + Latin)
- Требует интернет-соединение
- Поддерживает кириллицу (сербский)
- Поддерживает латиницу
- Подходит для полного текста чека:
  - ПФР време
  - ПФР број рачуна
  - Бројач рачуна

## Java 17 Features

Модуль OCR сканирования использует современные возможности Java 17:

### Records
```java
public record ReceiptTextData(
    String pfrTime,
    String pfrReceiptNumber,
    String pfrCounter
) { }
```

### Var (Local Variable Type Inference)
```java
var matcher = pattern.matcher(text);
var parsedData = ReceiptTextParser.parse(recognizedText);
```

### Text Blocks
```java
public static final String RECEIPT_FORMAT_EXAMPLE = """
    ========================================
    ПФР време:          07.09.2024. 19:25:47
    ПФР број рачуна: P8BPS55R-P8BPS55R-71822
    Бројач рачуна:             64763/71822ПП
    ========================================
    """;
```
