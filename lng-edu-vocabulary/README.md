# lng-edu-vocabulary — электронный словарь сербского языка

Поиск слова по любой его словоформе, ударение у заглавного слова и форм, значения
раздельно, примеры употребления, связь с корнями. Данные — сербско-русский словарь на
45 633 статьи, перенесённый в Postgres; сверху Spring Boot и веб-оболочка на Vue.

- Что словарь умеет — [docs/specification/dictionary.md](docs/specification/dictionary.md)
- Как устроен — [docs/implementation/index.md](docs/implementation/index.md)
- План работ — [docs/plan.md](docs/plan.md)
- Инструкции для ИИ-агента — [AGENTS.md](AGENTS.md)

## Что нужно на машине

Java 25 (`/opt/java/jdk-25`), Node.js 22, PostgreSQL 18 на порту 5432. Gradle отдельно
ставить не надо — в репозитории лежит обёртка. Подробности локального окружения —
[docs/implementation/development.md](docs/implementation/development.md).

## Секреты

**Пароля в репозитории нет.** Настройки подключения к базе (а позже — ключи к языковым
моделям) лежат в файле `secrets.properties` в корне репозитория. Этот файл закрыт
`.gitignore`; в git едет только шаблон
[`secrets.properties.template`](secrets.properties.template) с описанием ключей.

Первый шаг после клонирования:

```bash
cp secrets.properties.template secrets.properties
```

Дальше вписать значения:

| Ключ | Что вписать |
|---|---|
| `vocabulary.db.url` | адрес базы; для локальной разработки `jdbc:postgresql://localhost:5432/vocabulary` |
| `vocabulary.db.username` | роль в Postgres, под которой работает словарь — `vocabulary` |
| `vocabulary.db.password` | **пароль этой роли**; тот, что задан при её создании |

Роль и база заводятся один раз:

```bash
sudo -u postgres createuser --pwprompt vocabulary
sudo -u postgres createdb --owner vocabulary vocabulary
```

Пароль, который спросит `createuser`, и попадает в `vocabulary.db.password`. Схему
накатывать не нужно: миграции Flyway применяются при запуске бэкенда.

Файл читают оба потребителя, и оба ищут его **в корне репозитория**: бэкенд — через
`spring.config.import` в `application.yaml`, конвертер данных — сам при запуске. Поэтому
всё запускаемое стартует из корня (`workingDir` задан в корневом `build.gradle.kts`).

Заводя новый секрет, добавляйте ключ **и в шаблон, с описанием** — иначе о нём узнает
только тот, у кого уже есть заполненный файл. Значения в шаблон не пишутся никогда.

## Запуск

Наполнить базу словарём (один раз, ~45 тыс. статей):

```bash
./gradlew :importer:migrate
```

Дальше две команды в разных терминалах:

```bash
./gradlew :backend:bootRun          # http://localhost:8180
cd frontend && npm install && npm run dev   # http://localhost:8181
```

Словарь открывается на `http://localhost:8181`. Что делать, если не стартует, —
[docs/implementation/development.md](docs/implementation/development.md), раздел
«Если не запускается».

## Проверка

```bash
./gradlew build                     # сборка и 77 тестов ядра
./gradlew :importer:run             # прогон правил части речи на всей исходной базе
cd frontend && npm test             # преобразование кириллицы в латиницу
```

Что чем проверяется — [docs/testing/index.md](docs/testing/index.md).
