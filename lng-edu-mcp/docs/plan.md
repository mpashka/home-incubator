# План реализации MVP

@tag:vertical-slice @tag:mcp-tools @tag:request-id @tag:domain-model @tag:reading-block

Статусы: ⛔ не реализовано · 🚧 в процессе · ✅ реализовано.

Цель — рабочий backend первого вертикального сценария (см.
[`architecture.md`](architecture.md)) с REST и MCP поверх общего application
layer, PostgreSQL и Flyway. Схема — [`db-schema.md`](db-schema.md). Задание —
[`../prompts/backend-agent.md`](../prompts/backend-agent.md).

## Ограничения окружения

- JDK 21 и 25 доступны (`/opt/java/jdk-21`, `/opt/java/jdk-25`); Gradle
  запускаем на JDK 21.
- Docker в этом окружении **недоступен** → Testcontainers-тесты помечаются и
  пропускаются (skip), а не падают. Полная проверка миграций/интеграции — там,
  где есть Docker.

## Фазы

### Фаза A. Каркас, сборка, инфраструктура ✅

- ✅ Spring Boot приложение в `backend/` (Gradle Kotlin DSL, корневой wrapper).
- ✅ Зависимости: Spring Web, Validation, Data JPA, Flyway, PostgreSQL,
  Actuator, springdoc OpenAPI, Testcontainers, JUnit 5; компиляция `--release 21`.
- ✅ `infra/` — Docker Compose для PostgreSQL и `.env.example` без секретов.
- ✅ `application.yml` (datasource, Flyway on, JPA `ddl-auto=validate`, UTC).

### Фаза B. Схема и доменная модель ✅

- ✅ Flyway-миграции для всех таблиц из [`db-schema.md`](db-schema.md)
  (`backend/src/main/resources/db/migration/V1__init.sql`).
- ✅ JPA-сущности и репозитории: users, user_language_skills, books,
  book_texts, reading_progress, learning_sessions, vocabulary_items,
  word_events (package-by-feature под `dev.homeincubator.lngedu`).
- ✅ UTC timestamps, BCP 47 языки, уникальные ключи и FK.

### Фаза C. Application layer ✅

- ✅ Сервисы вертикального сценария: profiles (`ProfileService`), books
  (`BookService`), sessions (`SessionService`), reading (`ReadingService`),
  vocabulary (`VocabularyService`), stats (`StatsService`). Типизированные
  command/result-record DTO на каждый сервис; транспорт (REST/MCP) вне слоя.
- ✅ Динамическая сборка блока чтения вынесена в чистый `ReadingBlockAssembler`
  из `book_texts` по `user_language_skills` (min/max слов, предпочтение границы
  предложения/абзаца, hard-cut на max) (`@tag:reading-block`). `get_next_chunk`
  не двигает прогресс; `record_chunk_result` продвигает `position_char` до конца
  блока.
- ✅ Идемпотентность изменяющих операций по `request_id` (`@tag:request-id`):
  look-up-first по UNIQUE-колонкам (`learning_sessions.request_id`,
  `word_events.request_id`, `reading_events.request_id`) + перехват нарушения
  уникальности как backstop. `record_chunk_result` дополнительно продвигает
  прогресс монотонно (абсолютное смещение).
- ✅ Журнал событий чтения `reading_events` (миграция `V2__reading_events.sql`,
  сущность/репозиторий): `record_chunk_result` пишет строку и продвигает прогресс
  атомарно в одной транзакции; дневная статистика считает `charsRead`/`blocksRead`
  из `reading_events` в timezone пользователя (больше не 0).
- ✅ Unit-тесты (JUnit 5 + Mockito, без Docker/БД/Spring): `ReadingBlockAssembler`
  (10), `ReadingService` record/replay/monotonic (3), `StatsService` агрегация (1),
  идемпотентность start-session / unknown-word (3). Итог: 17 passed.

### Фаза D. REST-адаптер ✅

- ✅ REST endpoints под `/api`: profiles (GET), books (GET), sessions start/finish
  (POST), reading next/result (GET/POST), vocabulary add/list (POST/GET), stats
  daily (GET). Тонкие контроллеры поверх общего application layer, web-DTO с
  Jakarta Validation, без утечки JPA-сущностей.
- ✅ Единый Problem Details формат (`ApiExceptionHandler`, RFC 7807):
  NotFoundException→404, ValidationException/MethodArgumentNotValid/
  ConstraintViolation→400 с деталями полей, fallback→500; UTC ISO-8601
  `timestamp` в каждом ответе.
- ✅ OpenAPI (springdoc): бин `OpenApiConfig`, спека на `/v3/api-docs`, Swagger UI
  на `/swagger-ui.html`; JPA `jdbc.time_zone=UTC` и Jackson UTC дают корректные
  UTC-ответы.
- ✅ Seed/dev данные: `DevDataSeeder` (`@Profile("dev")` CommandLineRunner,
  идемпотентный) — два профиля со skills и по одной короткой книге для `sr` и
  `en` с текстом и `length_chars`. Тексты: public-domain (Austen, 1813) и
  синтетический сербский; без copyrighted content и секретов.
- ✅ Slice-тесты (`@WebMvcTest`, `@MockitoBean`, без БД/Docker): profiles list
  200 + JSON shape, validation 400 Problem Details, NotFound 404. Итог: 20 passed.

### Фаза E. MCP-адаптер ✅

- ✅ MCP endpoint и tools из [`architecture.md`](architecture.md), общий
  application layer с REST (Spring AI MCP server starter поверх Spring MVC, тот же
  порт; SSE `/sse` + сообщения `/mcp/message`). Пакет `dev.homeincubator.lngedu.mcp`:
  `LngEduMcpTools` (`@Tool` × 8) + `McpConfig` (`MethodToolCallbackProvider`).
  Тонкий адаптер поверх сервисов, без дублирования логики и без утечки JPA-сущностей.
- ✅ Все mutating tools (`start_learning_session`, `record_chunk_result`,
  `record_unknown_word`, `finish_learning_session`) принимают `request_id` и
  идемпотентны через существующую идемпотентность сервисов (@tag:request-id), без
  второго механизма. `finish` идемпотентен по природе (штампует `finished_at`
  один раз), `request_id` принимается для безопасного повтора клиентом.

### Фаза F. Тесты, проверка, документация ✅

- ✅ Testcontainers PostgreSQL (`postgres:16`): интеграционный тест
  `VerticalScenarioIT` (`@SpringBootTest`, профиль `dev` → Flyway-миграции на
  чистой БД + `DevDataSeeder`), сквозной сценарий start → next chunk → result →
  unknown word → finish → daily stats через общий service layer; datasource через
  `@DynamicPropertySource`. Класс гейтится на доступность Docker
  (`@EnabledIfDockerAvailable` + `DockerAvailableCondition` на `DockerClientFactory`,
  проверка ДО старта контейнера), поэтому без Docker **skip, а не fail**.
  Testcontainers-прогон здесь не выполнялся (нет Docker), но полный E2E проверен
  вживую на локальном PostgreSQL — см. журнал 2026-07-09.
- ✅ Идемпотентность в том же тесте: повтор `start_learning_session`,
  `record_chunk_result`, `record_unknown_word` и `finish_learning_session` с тем же
  `request_id` не меняет результат и не плодит строки (одна сессия, один
  `reading_event`, один `word_event`, прогресс не продвигается повторно).
- ✅ `./gradlew :backend:check` зелёный из корня: 23 теста, 1 skipped
  (`VerticalScenarioIT` без Docker), 0 failures, 0 errors.
- ✅ README: реальный backend — prereqs (JDK 21/25, Docker), запуск PostgreSQL через
  `infra/docker-compose.yml` и env-переменные, `bootRun --args=dev`, команды тестов,
  URL REST/OpenAPI/Swagger/Actuator/MCP, примеры curl и список MCP tools.

## Журнал

- 2026-07-08: план создан.
- 2026-07-08: реализованы фазы A и B — Spring Boot каркас, зависимости,
  `infra/` (Docker Compose + `.env.example`), `application.yml`, Flyway `V1__init.sql`
  и JPA-сущности/репозитории для всех таблиц. `:backend:compileJava` проходит.
- 2026-07-08: реализована фаза C — application/service layer (profiles, books,
  sessions, reading, vocabulary, stats) с чистым `ReadingBlockAssembler`,
  идемпотентностью по `request_id` и unit-тестами (13 passed, без Docker).
  `:backend:test` проходит.
- 2026-07-08: добавлен журнал `reading_events` (`V2__reading_events.sql`) —
  `record_chunk_result` персистит результат/comprehension и продвигает прогресс
  атомарно; `get_daily_stats` считает прочитанные символы/блоки из него. Итог
  unit-тестов: 17 passed. `:backend:test` проходит.
- 2026-07-08: реализована фаза D — REST-адаптер под `/api` (тонкие контроллеры
  profiles/books/sessions/reading/vocabulary/stats поверх сервисов), web-DTO с
  Jakarta Validation, единый Problem Details (`ApiExceptionHandler`, UTC
  timestamp), OpenAPI (`OpenApiConfig`, `/v3/api-docs` + `/swagger-ui.html`) и
  идемпотентный dev-сидер (`DevDataSeeder`, `@Profile("dev")`) с двумя профилями
  и короткими книгами `sr`/`en` (Austen 1813 + синтетический текст). Добавлены
  slice-тесты (`@WebMvcTest`): profiles 200, validation 400, NotFound 404. Итог
  тестов: 20 passed. Полный boot требует PostgreSQL (нет Docker) — не проверялся.
- 2026-07-08: реализована фаза E — MCP-адаптер на официальном Spring AI MCP server
  starter (`org.springframework.ai:spring-ai-starter-mcp-server-webmvc`, BOM
  `spring-ai-bom:1.0.1`, Maven Central, без milestone-репозитория). 8 `@Tool`-методов
  (`LngEduMcpTools`) поверх общих сервисов + `McpConfig`
  (`MethodToolCallbackProvider`); MCP-сервер `lng-edu` в том же приложении/порту
  (SSE `/sse`, сообщения `/mcp/message`) через `application.yml`. Все mutating tools
  принимают `request_id` и идемпотентны через существующую идемпотентность сервисов.
  Добавлен pure-Mockito unit-тест (`LngEduMcpToolsTest`, без Spring/MCP/БД): делегация
  `list_learners` и проброс `request_id` в `start_learning_session`. Итог тестов:
  22 passed (20 существующих + 2 новых). Полный MCP-рантайм требует boot приложения
  с PostgreSQL (нет Docker) — не проверялся; проверены компиляция и unit/slice-тесты.
- 2026-07-08: реализована фаза F — интеграционный тест `VerticalScenarioIT`
  (Testcontainers `postgres:16`, `@SpringBootTest` + профиль `dev`): сквозной
  сценарий через общий service layer с проверкой идемпотентности повторов по
  `request_id`. Гейт на Docker через `@EnabledIfDockerAvailable` +
  `DockerAvailableCondition` (`DockerClientFactory`, проверка до старта контейнера,
  не `@Testcontainers`), поэтому без Docker тест — **skip, а не fail**.
  `:backend:check` зелёный: 23 теста, 1 skipped, 0 failures, 0 errors. Обновлён
  README (запуск backend/PostgreSQL, env-переменные, URL, примеры curl, MCP tools).
  Сам сквозной прогон здесь не выполнялся (нет Docker) — проверены компиляция,
  разводка и чистый skip; полный E2E — там, где есть Docker.
- 2026-07-09: живая проверка на локальном PostgreSQL (роль/БД `lngedu`).
  `bootRun --spring.profiles.active=dev` поднял приложение: Flyway применил V1+V2
  на чистой БД, `DevDataSeeder` загрузил 2 профиля и книги `en`/`sr`. Пройден
  сквозной сценарий через REST для Ana/EN: start → next chunk (0..390, граница
  слова) → record result (understood) → progress=390, следующий блок 390..779 →
  record unknown word → finish → daily stats (sessions=1, newWords=1, charsRead=390,
  blocksRead=1). Идемпотентность подтверждена в БД: повтор с тем же `request_id`
  даёт по одной строке (learning_sessions=1, reading_events=1, word_events=1,
  progress не продвигается повторно). MCP SSE `/sse` → 200 text/event-stream,
  8 tools зарегистрированы; OpenAPI `/v3/api-docs` → 200; `/actuator/health` → 200.
