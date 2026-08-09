# bssid-show (Linux PC)

Linux-клиент для этого ноутбука: текущая точка Wi-Fi и сбор замеров для настройки роуминга.
Правила агента — [`AGENTS.md`](AGENTS.md), продуктовый контракт —
[`docs/specifications/product.md`](docs/specifications/product.md).

## Содержимое

- [`.gitignore`](.gitignore) — исключает локальные CSV-результаты обхода.
- [`AGENTS.md`](AGENTS.md) — обязательный контекст и границы для ИИ-агента.
- [`aps.txt`](aps.txt) — карта BSSID→SSH-точка, hostapd-интерфейс и понятное имя.
- [`bssid-show`](bssid-show) — CLI: `bssid`, `roam` и интерактивный обход с CSV-замером,
  помеченным hostname устройства.
- [`docs/`](docs/index.md) — спецификация, будущие решения реализации, эксплуатация и тесты.
- [`points.txt`](points.txt) — маршрут контрольных точек для обхода.

Запуск и проверка описаны в [`docs/production/`](docs/production/index.md) и
[`docs/testing/`](docs/testing/index.md).

Вверх: [`../index.md`](../index.md).
