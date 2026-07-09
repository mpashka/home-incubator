# ADR 0002. Аутентификация MCP: OAuth 2.1 и связывание аккаунтов

@tag:auth @tag:account-linking

- Статус: Accepted (реализуется поэтапно)
- Дата: 2026-07-09
- Связано: [`../plan.md`](../plan.md), [`../architecture.md`](../architecture.md),
  [`../db-schema.md`](../db-schema.md), правило 5 в [`../../AGENTS.md`](../../AGENTS.md)

## Контекст

MVP сознательно без аутентификации (правило 5 AGENTS): явные локальные профили,
`userId`/`learnerId` передаются как параметр без проверки владельца. Чтобы
запустить MVP «по-настоящему» — выставить MCP наружу и подключить к ChatGPT —
нужны: (1) OAuth-авторизация на MCP, (2) ограничение использования, (3)
возможность связать несколько OAuth-идентичностей в один аккаунт приложения
(у ребёнка на каждом устройстве свой Google-аккаунт → один app-аккаунт).

## Проверенные внешние требования

MCP (спека авторизации 2025-11-25, ужесточена в RC 2026-07-28):

- MCP-сервер — **OAuth 2.1 Resource Server**; для доступа из интернета OAuth
  обязателен (Authorization Code + PKCE/S256).
- Сервер **обязан** отдавать Protected Resource Metadata (RFC 9728) на
  `/.well-known/oauth-protected-resource` (поля `authorization_servers`,
  `scopes_supported`) и на неавторизованный запрос возвращать
  `401 WWW-Authenticate: ... resource_metadata="…"`.
- Authorization Server — метаданные OAuth 2.0 (RFC 8414) или OIDC Discovery
  (`authorization_endpoint`, `token_endpoint`, `registration_endpoint`).
- Регистрация клиента: приоритет CIMD → **DCR (RFC 7591)** → вручную. Требуются
  RFC 8707 (resource indicators / audience) и RFC 9207 (`iss`).

ChatGPT (custom connector / «apps», Developer Mode):

- Нужен **HTTPS**-endpoint и **Developer Mode** (платные планы Pro/Team/
  Enterprise/Edu).
- ChatGPT **регистрируется динамически (DCR/CIMD)** и сам подставляет redirect
  URI. → Наш AS обязан поддерживать DCR и PKCE.

## Решение

Свой **Spring Authorization Server**, федерирующий вход через Google; MCP/REST —
**OAuth2 Resource Server**. Внешний managed IdP отклонён ради контроля над
логикой связывания аккаунтов.

### Роли

- **Authorization Server** (Spring Authorization Server): OAuth 2.1 Auth Code +
  PKCE, метаданные OIDC/AS discovery, **DCR-endpoint включён** (чтобы ChatGPT
  зарегистрировался). Вход пользователя — федерация в Google (Spring Security
  OAuth2 Login). Выдаёт наш JWT: `sub` = app-аккаунт, `aud` = ресурс MCP,
  scopes.
- **Resource Server** (MCP + REST): валидирует JWT (issuer нашего AS, audience
  ресурса), отдаёт PRM, на неавторизованное — `401 WWW-Authenticate`. Аккаунт
  берётся **из токена**, инструменты работают только с профилями этого аккаунта
  (закрывает правило 5 и текущую дыру доверия к `userId`).

### Модель аккаунтов и идентичностей

Новые таблицы, отдельно от учебных профилей (`users` = learner):

```
app_account(id, display_name, role, created_at)
external_identity(id, account_id FK -> app_account,
    provider, subject, email, linked_at)          -- UNIQUE(provider, subject)
users(learner).owner_account_id FK -> app_account  -- профиль принадлежит аккаунту
```

- Много `external_identity` → один `app_account` (сценарий ребёнка: несколько
  Google-`subject` → один аккаунт).
- Аккаунт владеет одним или несколькими учебными профилями (родитель ведёт
  профили детей).

### Связывание идентичностей (@tag:account-linking)

- Первый вход: создаётся `app_account` + `external_identity`.
- Привязка второй идентичности: либо «войти в существующий аккаунт → добавить
  идентичность», либо (для чужого устройства ребёнка) **код-приглашение /
  родительская привязка** из админ-действия. Google-`subject` идемпотентно
  резолвится в аккаунт при каждом входе.

### Ограничение использования

Allowlist разрешённых аккаунтов/идентичностей; scopes/роли; rate-limit на
аккаунт. Неразрешённой идентичности — отказ на этапе выдачи токена или на
resource server.

## Последствия

- Крупная отдельная работа (фазы G–L в [`../plan.md`](../plan.md)): модель
  аккаунтов, AS + Google, resource server + PRM, связывание, ограничение,
  выставление наружу + ChatGPT.
- Требуются секреты Google OAuth (client id/secret) и, для ChatGPT, платный план
  с Developer Mode и HTTPS-домен (Cloudflare Tunnel + DNS).
- Инструменты/REST перестают принимать `userId` от клиента как доверенный —
  аккаунт только из токена.

## Отклонённые варианты

- Внешний managed IdP (Auth0/Cognito/Clerk) — меньше кода, но зависимость,
  стоимость и меньше контроля над связыванием.
- Google напрямую как AS — не даёт нашего управления связыванием и требует
  прослойки под требования MCP (DCR, PRM, audience).
- Bearer-токен без OAuth — не принимается коннектором ChatGPT.
