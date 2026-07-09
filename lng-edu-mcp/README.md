# lng-edu-mcp

MCP-платформа для обучения сербскому и английскому через чтение, вопросы по тексту, учет незнакомых слов, интервальные повторения и статистику.

## Планируемые компоненты

- `backend/` — Spring Boot backend, MCP endpoint и REST API.
- `web/` — Vue web-клиент.
- `infra/` — PostgreSQL и локальная инфраструктура.
- `docs/` — архитектура и решения.
- `prompts/` — задания агентам и prompt учебного ассистента.

## Старт реализации

1. Выполнить `prompts/backend-agent.md`.
2. После стабилизации OpenAPI выполнить `prompts/frontend-agent.md`.
3. Подключить MCP endpoint к ChatGPT и использовать `prompts/chatgpt-tutor.md`.

## Backend

Реализован первый вертикальный сценарий: начать сессию → получить следующий блок
для чтения → зафиксировать понимание/незнакомое слово → завершить сессию → увидеть
дневную статистику. Один общий application/service layer, два адаптера над ним —
REST (для web) и MCP (для LLM-клиентов). PostgreSQL — источник истины, схема
управляется Flyway.

Backend — модульный монолит (одно разворачиваемое приложение, несколько Gradle-модулей
под `backend/`): `backend:core` (java-library — сущности, репозитории, сервисы, модель
аккаунтов, Flyway-миграции), `backend:auth` (java-library, зависит от `core` — каркас под
Spring Authorization Server, фаза H) и `backend:app` (Spring Boot bootJar — `LngEduApplication`,
REST-контроллеры, MCP, dev-сидер, `application.yml`; зависит от `core` + `auth`).

### Требования

- Gradle Wrapper 9.6.1 и Kotlin DSL — установленный в системе Gradle не нужен.
- JDK 21 (backend компилируется с `--release 21` и остаётся совместимым с Java 21).
  Локальный симлинк `/opt/java/jdk-21` сломан, поэтому Gradle запускаем на JDK 25
  (`JAVA_HOME=/opt/java/jdk-25`) — сборка `--release 21` при этом корректна.
- Docker — для запуска PostgreSQL и для интеграционных тестов (Testcontainers).
- Node.js и npm — после создания Vue-приложения (`web/` ещё не реализован).

### PostgreSQL и переменные окружения

Backend читает datasource из переменных окружения (см.
[`infra/.env.example`](infra/.env.example) и `backend/app/src/main/resources/application.yml`):

| Переменная    | Значение по умолчанию                     |
| ------------- | ----------------------------------------- |
| `DB_URL`      | `jdbc:postgresql://localhost:5432/lngedu` |
| `DB_USERNAME` | `lngedu`                                   |
| `DB_PASSWORD` | `lngedu`                                   |

Локальную БД поднимаем через Docker Compose:

```bash
cp infra/.env.example infra/.env      # при необходимости поправить
docker compose -f infra/docker-compose.yml up -d
```

Значения по умолчанию совпадают с Compose, так что для локального запуска экспортировать
переменные не обязательно; для другого окружения — задайте их явно.

### Запуск backend

```bash
export JAVA_HOME=/opt/java/jdk-25
# профиль dev запускает Flyway-миграции и загружает seed-данные (2 профиля + книги sr/en)
./gradlew :backend:app:bootRun --args='--spring.profiles.active=dev'
```

Приложение слушает `http://localhost:8080`. Flyway создаёт схему на старте; Hibernate
работает в режиме `ddl-auto=validate`.

### Тесты

```bash
export JAVA_HOME=/opt/java/jdk-25
./gradlew :backend:core:test  # доменные unit-тесты (JUnit 5 + Mockito, без Docker)
./gradlew :backend:app:test   # slice- и MCP-тесты (без Docker; VerticalScenarioIT — skip)
./gradlew check               # полная проверка всех модулей (core + auth + app)
```

Интеграционный тест (`VerticalScenarioIT`) гоняет сквозной сценарий на настоящем
PostgreSQL через Testcontainers (`postgres:16`). Он требует Docker и **пропускается**
(reported as skipped), когда Docker недоступен, поэтому `check` остаётся зелёным
и без Docker.

### Сборка и очистка

```bash
export JAVA_HOME=/opt/java/jdk-25
./gradlew :backend:app:build   # bootJar deployable-приложения (+ core/auth библиотеки)
./gradlew clean
```

### Endpoints

- REST API: база `http://localhost:8080/api`.
- OpenAPI-спека: `http://localhost:8080/v3/api-docs`.
- Swagger UI: `http://localhost:8080/swagger-ui.html`.
- Actuator health: `http://localhost:8080/actuator/health`.
- MCP-сервер `lng-edu` в том же приложении/порту: SSE `http://localhost:8080/sse`,
  endpoint сообщений `http://localhost:8080/mcp/message`.

### Примеры REST-вызовов

```bash
# список учебных профилей (возьмите id учащегося и id книги отсюда / из GET /api/books)
curl http://localhost:8080/api/profiles

# книги нужного языка вместе с прогрессом чтения выбранного учащегося
curl "http://localhost:8080/api/books?language=en&userId=<USER_ID>"

# старт учебной сессии — идемпотентен по requestId (повтор с тем же requestId
# возвращает ту же сессию и не создаёт дубль)
curl -X POST http://localhost:8080/api/sessions \
  -H 'Content-Type: application/json' \
  -d '{"userId":"<USER_ID>","bookId":"<BOOK_ID>","requestId":"start-1"}'

# следующий блок для чтения (не двигает прогресс)
curl "http://localhost:8080/api/reading/next?userId=<USER_ID>&bookId=<BOOK_ID>"

# зафиксировать результат блока (comprehension: understood|partial|unclear),
# endOffset берём из ответа /api/reading/next
curl -X POST http://localhost:8080/api/reading/result \
  -H 'Content-Type: application/json' \
  -d '{"userId":"<USER_ID>","bookId":"<BOOK_ID>","sessionId":"<SESSION_ID>","endOffset":320,"comprehension":"understood","requestId":"chunk-1"}'
```

Ошибки возвращаются в формате Problem Details (RFC 7807).

### MCP tools

MCP-адаптер (`LngEduMcpTools`) экспонирует тот же сценарий как небольшие типизированные
tools: `list_learners`, `list_books`, `start_learning_session`, `get_next_chunk`,
`record_chunk_result`, `record_unknown_word`, `finish_learning_session`,
`get_daily_stats`. Изменяющие tools принимают `request_id` и идемпотентны при повторе.
LLM-клиент подключается к MCP endpoint и получает список tools через MCP-протокол
(SSE `/sse`).

## Web

`web/` — Vue-клиент; ещё не реализован (см. [`prompts/frontend-agent.md`](prompts/frontend-agent.md)).
