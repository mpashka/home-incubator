# Task: Создание Android модуля fe_mobile_android

## Требования
- Версия android: 15
- OS: Xiaomi HyperOS 2
- Сканирование QR кодов чеков (~900 байт)
- Отправка на backend

## Статус: ВЫПОЛНЕНО

## Реализация

Создан модуль `fe_mobile_android/` со следующей функциональностью:

### Технологии
- **Kotlin** - основной язык
- **ML Kit Barcode Scanning** - распознавание QR кодов (оптимизирован для больших QR)
- **CameraX** - работа с камерой
- **Retrofit + OkHttp** - HTTP клиент
- **DataStore** - хранение настроек
- **Material Design 3** - UI

### Функционал
1. Сканирование QR кодов чеков
2. Автоматическая отправка на backend
3. Настройка URL backend'а и User ID
4. История сканирований
5. Вибрация при успешном сканировании
6. Фильтрация только чековых QR кодов (tax.gov, verifyInvoice)

### Структура
```
fe_mobile_android/
├── app/src/main/java/com/receipt/scanner/
│   ├── api/          # API клиент
│   ├── data/         # Репозитории
│   ├── ui/           # Activities
│   └── util/         # QR анализатор
├── gradle/libs.versions.toml
└── build.gradle.kts
```

### Сборка
```bash
cd fe_mobile_android
./gradlew assembleDebug
```

Документация: `@/docs/fe_mobile_android/overview.md`
