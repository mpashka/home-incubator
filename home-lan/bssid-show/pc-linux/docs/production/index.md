# index: `docs/production/`

Родитель: [`../index.md`](../index.md).

Сборки и установки нет: нужны системные `python3`, `iw` и `sudo`.

```bash
./bssid-show                         # список команд, затем текущий BSSID
./bssid-show bssid                   # текущие SSID, BSSID→имя и RSSI
./bssid-show roam                    # передёрнуть связь через текущую OpenWrt-точку
./bssid-show survey                  # сразу записать CSV в Ansible-проект
./bssid-show survey route.txt out.csv
./bssid-show help                    # только список команд
```

Статус работает без повышения прав. В режиме обхода `sudo` нужен только системной команде
активного сканирования `iw`; CSV остаётся файлом текущего пользователя.

Без явного `out.csv` результат создаётся в
`~/Projects/home/home-config-secrets/ansible/.configs/out/wifi-survey/`. Каталог создаётся
автоматически и игнорируется Git; копировать файл после обхода не нужно.

`roam` требует SSH-доступ по ключу к адресу текущей точки из `aps.txt`. Связь пропадёт на
несколько секунд; команда ждёт новый BSSID 20 секунд и явно сообщает, если перехода не было.
