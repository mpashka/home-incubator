# fe_mobile_android

Нативное Android приложение для сканирования QR кодов чеков.

## Особенности

- **Google ML Kit** для сканирования QR кодов - обеспечивает высокое качество распознавания больших QR кодов (до 900 байт)
- **CameraX** для работы с камерой - современный API, оптимизированный для Android 15
- **Material Design 3** - современный UI
- Поддержка **Android 15** (API level 35)
- Оптимизировано для **Xiaomi HyperOS 2**

## Требования

- Android 8.0+ (API 26+)
- Камера
- Интернет-соединение

## Структура проекта

```
fe_mobile_android/
├── app/
│   ├── src/main/
│   │   ├── java/com/receipt/scanner/
│   │   │   ├── api/           # API клиент (Retrofit)
│   │   │   ├── data/          # Репозитории и модели данных
│   │   │   ├── ui/            # Activities и Adapters
│   │   │   ├── util/          # Утилиты (QR анализатор)
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

### MainActivity
Главный экран с камерой для сканирования QR кодов:
- Превью камеры в полноэкранном режиме
- Overlay с рамкой для сканирования
- Статусные сообщения
- Кнопки для настроек и истории

### SettingsActivity
Настройки приложения:
- Backend URL для отправки QR кодов
- User ID

### HistoryActivity
История отсканированных QR кодов:
- Список всех сканирований
- Статус обработки (pending, completed, failed)
- ID чека после обработки

### QrCodeAnalyzer
Анализатор изображений с камеры:
- Использует ML Kit Barcode Scanning
- Фильтрует только чековые QR коды (tax.gov, verifyInvoice)
- Оптимизирован для больших QR кодов

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
- **Retrofit** 2.11.0 - HTTP клиент
- **OkHttp** 4.12.0 - HTTP interceptors
- **Coroutines** 1.9.0 - асинхронные операции
- **DataStore** 1.1.1 - хранение настроек
- **Material Components** 1.12.0 - UI компоненты
