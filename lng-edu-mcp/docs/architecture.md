# Архитектура MVP

@tag:vertical-slice @tag:mcp-tools @tag:request-id @tag:domain-model @tag:reading-block @tag:difficulty-model @tag:auth

## Границы системы

Backend хранит все долгоживущее состояние обучения и предоставляет два транспорта:

- MCP для ChatGPT/LLM-клиентов;
- REST для Vue web-клиента.

Оба транспорта вызывают общий application layer. ChatGPT управляет диалогом и формулирует объяснения, но не является источником истины для прогресса.

## Первый вертикальный сценарий

1. Выбрать пользователя, язык и книгу.
2. Начать учебную сессию.
3. Получить следующий фрагмент книги.
4. Записать результат: понятно, частично понятно или непонятно.
5. Записать незнакомое слово и контекст при необходимости.
6. Завершить сессию.
7. Получить дневную статистику и текущий прогресс.

## Начальная модель

- `users`
- `user_language_skills`
- `books`
- `book_texts`
- `reading_progress`
- `learning_sessions`
- `vocabulary_items`
- `word_events`

Детальная схема таблиц, ключей и связей — в [`db-schema.md`](db-schema.md).

Следующим этапом добавляются вопросы, ответы, упражнения и расписание интервальных повторений.

## Единица чтения (@tag:reading-block)

Текст книги хранится крупным блоком (`book_texts`), позиция чтения — символьное
смещение. Блок для одного шага занятия собирается динамически в application layer
с ограничениями min/max размера и предпочтением закончить на границе
предложения/абзаца. Размер блока — свойство связки «пользователь + язык»
(`user_language_skills`), а не книги, и растёт с уровнем владения. В дальнейшем он
определяется моделью сложности слов (`@tag:difficulty-model`). Обоснование и
варианты — в [`adr/0001-reading-blocks-and-text-storage.md`](adr/0001-reading-blocks-and-text-storage.md).

## Аутентификация и владение профилями (@tag:auth)

Дизайн — [`adr/0002-auth-oauth-and-account-linking.md`](adr/0002-auth-oauth-and-account-linking.md).
MCP/REST — **OAuth2 Resource Server**: защищённая поверхность (`/api/**`, `/sse`,
`/mcp/**`) требует валидный JWT нашего Authorization Server (проверяются issuer и
audience = идентификатор MCP-ресурса). Аккаунт берётся из claim `account_id` токена,
и адаптеры (контроллеры/MCP tools) работают только с профилями этого аккаунта:
`list_learners`/`GET /api/profiles` фильтруют по владельцу, любая операция с чужим
учащимся отклоняется (`403`). Так закрывается правило 5 AGENTS — `userId` из запроса
больше не доверенный вход. Каталог книг общий для всех аккаунтов.

Для bootstrap MCP-клиента сервер отдаёт **Protected Resource Metadata** (RFC 9728) на
`/.well-known/oauth-protected-resource` и на `401` добавляет заголовок
`WWW-Authenticate: Bearer resource_metadata="…"`. Открытыми остаются PRM, метаданные AS,
`/actuator/health` и OpenAPI/Swagger.

## MCP tools MVP

- `list_learners`
- `list_books`
- `start_learning_session`
- `get_next_chunk`
- `record_chunk_result`
- `record_unknown_word`
- `finish_learning_session`
- `get_daily_stats`

Каждая изменяющая операция принимает `request_id` для безопасного повтора.
