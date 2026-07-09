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

## Аутентификация и выставление наружу (ADR 0002)

Дизайн и проверенные требования — [`adr/0002-auth-oauth-and-account-linking.md`](adr/0002-auth-oauth-and-account-linking.md).
Свой Spring Authorization Server + вход через Google; MCP/REST — OAuth2 resource
server. Предпосылки: секреты Google OAuth; для ChatGPT — HTTPS-домен и платный
план с Developer Mode.

### Фаза G. Модель аккаунтов и идентичностей ✅

- ✅ Миграция `V3__accounts.sql`: `app_account`, `external_identity`
  (`UNIQUE(provider, subject)`), `users.owner_account_id` FK (`@tag:account-linking`).
- ✅ JPA-сущности/репозитории (пакет `account`: `AppAccount`, `ExternalIdentity` +
  репозитории, `UserRepository.findByOwnerAccountId`); dev-сидер создаёт owner-аккаунт
  + dev-идентичность и связывает существующие профили с аккаунтом (идемпотентно).
- ✅ Резолвер «идентичность → аккаунт → профили» (`AccountService`,
  идемпотентный upsert по `(provider, subject)`) в service layer, без enforcement;
  unit-тест (Mockito): create-on-first-seen, return-existing-on-repeat, список профилей.

### Фаза H0. Модуляризация (modular monolith) ✅

Одно приложение, разные Gradle-модули (без микросервисов). Нужна до AS, чтобы
`auth` зависел от домена, а не от boot-приложения.

- ✅ `backend:core` — сущности, репозитории, сервисы, модель аккаунтов + резолвер,
  Flyway-миграции, common (доменные исключения + `TimeConfig`). Library (java-library,
  без Spring Boot plugin; версии — через BOM `spring-boot-dependencies:3.4.5`).
- ✅ `backend:auth` — каркас модуля (пустой пакет `dev.homeincubator.lngedu.auth` +
  `package-info.java`), Spring Authorization Server + Google — фаза H. Зависит от `core`.
- ✅ `backend:app` — `@SpringBootApplication`, REST-контроллеры + MCP + web, dev-сидер,
  `application.yml`. Зависит от `core` + `auth`. Boot-jar.
- ✅ Поведение не меняется: boot + все тесты (26, 1 skipped) + живой сценарий на
  локальном PostgreSQL.

### Фаза H. Authorization Server + вход через Google ✅

- ✅ Spring Authorization Server (`backend:auth`, `@tag:auth`): OAuth 2.1 Auth Code +
  PKCE (S256), метаданные OIDC/AS discovery, включённый OIDC DCR-endpoint
  (`/connect/register`). Конфиг: `AuthorizationServerConfig` (`@Order(1)` filter
  chain через `OAuth2AuthorizationServerConfigurer`, seed-клиент, issuer-настройки),
  `DefaultSecurityConfig` (`@Order(2)` `oauth2Login` + permit всей текущей поверхности),
  `TokenConfig` (эфемерный RSA `JWKSource`, `JwtDecoder`, token customizer).
- ✅ Федерация входа в Google (Spring Security OAuth2 Login, `application.yml`,
  env `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET`); token customizer резолвит
  `google`+`sub`+email через `AccountService.resolveByExternalIdentity` (upsert
  `external_identity` → `app_account`).
- ✅ Выдача JWT: `sub`/`account_id`=app-аккаунт, `aud`=ресурс MCP
  (env `MCP_RESOURCE_URI`), issuer env `AUTH_ISSUER_URI`.
- ⚠️ Не проверено вживую (нет реального Google-клиента): интерактивный вход в
  Google и полная выдача токена. Проверены boot, discovery-метаданные, DCR-endpoint
  в метаданных, редирект `/oauth2/authorization/google` → accounts.google.com и
  JWKS — см. журнал 2026-07-09. Фаза I закрывает защиту `/api` и `/sse`
  (resource server), Phase L — реальные креды Google + ChatGPT.

### Фаза I. Resource Server на MCP/REST + PRM ⛔

- ⛔ OAuth2 resource server (валидация JWT, issuer/audience) на MCP и REST.
- ⛔ PRM на `/.well-known/oauth-protected-resource`; `401 WWW-Authenticate:
  resource_metadata` на MCP-endpoint.
- ⛔ Аккаунт из токена; инструменты/REST работают только с профилями аккаунта
  (закрывает правило 5, `userId` больше не доверенный вход).

### Фаза J. Связывание идентичностей ⛔

- ⛔ Привязка второй идентичности к существующему аккаунту (вход-в-аккаунт +
  добавить, либо код-приглашение / родительская привязка) (`@tag:account-linking`).
- ⛔ Родитель/админ привязывает детские профили к аккаунту.

### Фаза K. Ограничение использования ⛔

- ⛔ Allowlist аккаунтов/идентичностей, scopes/роли, rate-limit на аккаунт.

### Фаза L. Выставление наружу + ChatGPT ⛔

- ⛔ Cloudflare named tunnel + DNS-запись, HTTPS на MCP.
- ⛔ Регистрация custom connector в ChatGPT (Developer Mode), OAuth-flow, проверка
  видимости и вызова tools.

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
- 2026-07-09: реализована фаза G — модель аккаунтов и идентичностей (ADR 0002).
  Миграция `V3__accounts.sql` (forward-only): таблицы `app_account`
  (`role` CHECK IN owner/child), `external_identity` (`UNIQUE(provider, subject)`,
  индекс по `account_id`), колонка `users.owner_account_id` (nullable FK ON DELETE
  SET NULL, индекс). Пакет `account`: сущности `AppAccount`/`ExternalIdentity`,
  репозитории (`ExternalIdentityRepository.findByProviderAndSubject`,
  `AppAccountRepository`, `UserRepository.findByOwnerAccountId`) и `AccountService`
  (идемпотентный резолв идентичность→аккаунт с upsert по `(provider, subject)`,
  список профилей аккаунта; без security enforcement). `DevDataSeeder` создаёт
  owner-аккаунт + dev google-идентичность и связывает существующие профили
  (идемпотентно, guard на существующую идентичность). Unit-тест `AccountServiceTest`
  (Mockito, без БД): create-on-first-seen, return-existing-on-repeat, делегация списка.
  `:backend:compileJava :backend:compileTestJava :backend:test` — 26 тестов, 1 skipped
  (`VerticalScenarioIT` без Docker), 0 failures. Живая проверка на локальном
  PostgreSQL: Flyway применил V3 поверх V1/V2 на БД с существующими данными
  («Successfully applied 1 migration … now at version v3»), сидер залинковал
  2 профиля; psql: `app_account=1`, `external_identity=1`,
  `users where owner_account_id is not null=2`.
- 2026-07-09: реализована фаза H0 — модульный монолит. Единый модуль `backend`
  разбит на три Gradle-модуля под `backend/` (одно разворачиваемое приложение):
  `backend:core` (java-library, без Spring Boot plugin; версии — через BOM
  `spring-boot-dependencies:3.4.5`) — пакеты `user`/`book`/`reading`/`session`/
  `vocabulary`/`stats`/`account`, доменные исключения + `TimeConfig` из `common`,
  все Flyway-миграции (`db/migration/V1..V3`); `backend:auth` (java-library →
  зависит от `core`) — пустой каркас пакета `dev.homeincubator.lngedu.auth`
  (`package-info.java`), логику даёт фаза H; `backend:app` (Spring Boot bootJar →
  зависит от `core` + `auth`) — `LngEduApplication` (в базовом пакете, чтобы
  component/entity-scan покрывал core), REST-контроллеры + web-request-DTO +
  `web` (`ApiExceptionHandler`, `OpenApiConfig`), `mcp`, `DevDataSeeder`,
  `application.yml`. Пакеты и SQL не переименованы — только перемещены; `common`
  разделён между модулями (исключения + `TimeConfig` → core, `DevDataSeeder` → app),
  оба сохраняют имя пакета `…common` (split-package на одном classpath — допустимо
  для одного приложения). Контроллеры и их web-DTO вынесены в app (core без
  spring-web), transport-слой отделён от application/domain. `settings.gradle.kts`:
  `include("backend:core", "backend:auth", "backend:app")`; корневой `build`
  зависит от `:backend:app:build`. `core` явно добавляет
  `junit-platform-launcher` (без boot-plugin Gradle его не подтягивает).
  `./gradlew check` зелёный: 26 тестов, 1 skipped (`VerticalScenarioIT` без Docker),
  0 failures (core 20, app 6). Живая проверка на локальном PostgreSQL:
  `:backend:app:bootRun --spring.profiles.active=dev` (порт 8097, 8080 занят) —
  Flyway провалидировал 3 миграции (schema at v3), `DevDataSeeder` отработал
  идемпотентно, `/actuator/health` → 200 `{"status":"UP"}`, `/api/profiles` → 2
  профиля (Ana/Ben).
- 2026-07-09: реализована фаза H — Spring Authorization Server + федерация входа в
  Google (`backend:auth`, `@tag:auth`, ADR 0002). Зависимости модуля:
  `spring-boot-starter-oauth2-authorization-server` + `-oauth2-client` (security
  транзитивно). Классы в пакете `dev.homeincubator.lngedu.auth`:
  `AuthorizationServerConfig` (`@Order(1)` filter chain через
  `OAuth2AuthorizationServerConfigurer.authorizationServer()` с
  `.oidc(clientRegistrationEndpoint)` → включён OIDC DCR; entry-point редиректит
  неаутентифицированные AS-запросы в `/oauth2/authorization/google`; `RegisteredClientRepository`
  с одним публичным PKCE-клиентом `mcp-demo-client`; `AuthorizationServerSettings`
  с issuer из `AUTH_ISSUER_URI`), `DefaultSecurityConfig` (`@Order(2)` `oauth2Login`
  + `anyRequest().permitAll()` — вся текущая поверхность остаётся открытой в этой фазе),
  `TokenConfig` (эфемерный RSA `JWKSource` — токены не переживают рестарт, `JwtDecoder`,
  `OAuth2TokenCustomizer`: резолвит Google-`sub`/email через
  `AccountService.resolveByExternalIdentity` и ставит `sub`/`account_id`=app-аккаунт,
  `aud`=`MCP_RESOURCE_URI`). `application.yml`: блок
  `spring.security.oauth2.client.registration.google` с env
  `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` (dev-дефолты `dummy-*`, scope
  `openid,email,profile`). `.env.example` документирует `GOOGLE_CLIENT_ID`,
  `GOOGLE_CLIENT_SECRET`, `AUTH_ISSUER_URI`, `MCP_RESOURCE_URI`. Тесты: DB-free
  `AuthorizationServerMetadataTest` (`@SpringBootTest` RANDOM_PORT с исключённым
  DataSource/JPA + mock `AccountService`, MockMvc): discovery `200` с
  `issuer`/`authorization_endpoint`/`token_endpoint`/`jwks_uri`/`registration_endpoint`,
  редирект PKCE-authorize → `/oauth2/authorization/google`, `/oauth2/authorization/google`
  → `accounts.google.com`, JWKS публикует RSA-ключ. Существующие `@WebMvcTest`-срезы
  переведены на `@AutoConfigureMockMvc(addFilters = false)` (Security на classpath), их
  интент сохранён. `./gradlew check` (все модули) — **30 тестов, 1 skipped**
  (`VerticalScenarioIT` без Docker), 0 failures, 0 errors (core 20, auth 4, app 6).
  Живой boot на локальном PostgreSQL (порт 8097, dummy Google creds,
  `AUTH_ISSUER_URI`/`MCP_RESOURCE_URI`=`http://localhost:8097`): `Started` за 4 с;
  `curl /.well-known/openid-configuration` → 200 (`issuer`=localhost:8097,
  `authorization_endpoint=/oauth2/authorize`, `token_endpoint=/oauth2/token`,
  `jwks_uri=/oauth2/jwks`, `registration_endpoint=/connect/register`,
  `code_challenge_methods_supported=[S256]`); `/oauth2/authorization/google` → `302`
  на `https://accounts.google.com/o/oauth2/v2/auth?...client_id=dummy-client-id`;
  `/oauth2/jwks` → 200 (RSA-ключ); `/api/profiles` → 200, 2 профиля (Ana/Ben) —
  текущая поверхность осталась открытой. **Не проверено (нет реального Google-клиента):**
  интерактивный вход в Google и полная выдача токена — только boot, метаданные, DCR
  в метаданных и редирект.
