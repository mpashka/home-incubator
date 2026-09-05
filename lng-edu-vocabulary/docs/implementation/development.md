---
tags: "@tag:meta"
---

# Локальное окружение

Родительский индекс: [index.md](index.md)

## Сборка

Java 25 — `/opt/java/jdk-25`, она же по умолчанию в `PATH`. Сборка через обёртку Gradle
(Gradle 9.6.1), отдельно ставить Gradle не нужно:

```bash
./gradlew build          # собрать всё
./gradlew :core:test     # тесты одного модуля
./gradlew :backend:bootRun
```

## IntelliJ IDEA

Общие конфигурации запуска хранятся в `.run/`:

- `Vocabulary Backend` — Spring Boot на `http://localhost:8180`;
- `Vocabulary Frontend` — Vite на `http://localhost:8181`, с перенаправлением `/api`
  на бэкенд.

Перед первым запуском фронтенда установите его зависимости: `cd frontend && npm install`.

Модули:

| Модуль | Назначение |
|---|---|
| `core` | Модель словаря, разбор исходной разметки, правила частей речи и словоформ |
| `importer` | Конвертация исходной sqlite-базы в целевую Postgres |
| `backend` | Spring Boot: поиск по словоформам, REST API, пополнение через LLM |

У каждого модуля свой `index.md`: [core](../../core/index.md), [importer](../../importer/index.md),
[backend](../../backend/index.md), [frontend](../../frontend/index.md).

## Запуск словаря

Двумя командами, в разных терминалах; база должна быть поднята и заполнена.

```bash
./gradlew :backend:bootRun          # http://localhost:8180
cd frontend && npm run dev          # http://localhost:8181, /api уходит на бэкенд
```

Проверка, что бэкенд отвечает:

```bash
curl 'http://localhost:8180/api/words?q=%D0%B2%D0%BE%D0%B4&limit=3'
```

### Если не запускается

- **Flyway: `Migration checksum mismatch`.** Файл миграции изменили после того, как его
  применили к базе — даже правка комментария меняет контрольную сумму. Либо вернуть файл
  как был, либо привести историю в соответствие (`flyway repair`; вручную —
  `update flyway_schema_history set checksum = <локальная сумма> where version = '<номер>'`,
  число печатается в самой ошибке как «Resolved locally»).
- **Vite: `ENOSPC: System limit for number of file watchers reached`.** Кончились
  inotify-наблюдатели, их съели другие программы (обычно IDE). Либо поднять предел
  (`sysctl fs.inotify.max_user_watches`, нужен root), либо запустить со слежением
  опросом: `CHOKIDAR_USEPOLLING=1 npm run dev`.

## База данных

Для разработки используется локальный PostgreSQL 18 на порту **5432**. Бэкенд подключается
к отдельной базе `vocabulary` под ролью `vocabulary`; схема применяется Flyway при запуске.
Диапазон **8180–8189** отведён проекту: бэкенд занимает 8180, фронтенд — 8181.

```bash
psql -h localhost -p 5432 -U vocabulary -d vocabulary   # проверка
```

Резервная копия локальной базы хранится вне Git в `.data/backups/`. Восстановить её в
пустую базу можно командой:

```bash
pg_restore -h localhost -p 5432 -U vocabulary -d vocabulary .data/backups/vocabulary-YYYY-MM-DD.dump
```

## Исходная база словаря

`/home/ya-pashka/Documents/Srpski/Recnik/srb_rus_apk/srbbase.db` — **только чтение**.
Утилиты `sqlite3` в системе нет, смотреть через `python3`:

```python
import sqlite3
db = sqlite3.connect(
    'file:/home/ya-pashka/Documents/Srpski/Recnik/srb_rus_apk/srbbase.db?mode=ro',
    uri=True)
```

Формат базы описан в [source-db.md](source-db.md).
