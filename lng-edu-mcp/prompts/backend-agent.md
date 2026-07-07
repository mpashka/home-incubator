# Задание агенту: backend и MCP

Работай в репозитории `lng-edu-mcp`. Сначала прочитай `AGENTS.md`, `README.md`, `docs/architecture.md` и `docs/decisions.md`.

Создай production-minded MVP backend на Java 21, актуальном стабильном Spring Boot, Gradle Kotlin DSL и PostgreSQL. Не ограничивайся каркасом: реализуй и проверь первый вертикальный учебный сценарий.

## Требуемый результат

1. Развивай существующий Gradle multi-project build и создай Spring Boot приложение в `backend/`. Используй корневой Gradle Wrapper и Kotlin DSL, не добавляй Maven Wrapper.
2. Подключи Spring Web, Validation, Data JPA, Flyway, PostgreSQL, Actuator, OpenAPI и актуальный официальный Java MCP SDK/стартер.
3. Добавь Docker Compose в `infra/` для локального PostgreSQL и `.env.example` без секретов.
4. Реализуй Flyway-миграции и доменную модель:
   - users;
   - books;
   - book_chunks;
   - reading_progress;
   - learning_sessions;
   - vocabulary_items;
   - word_events.
5. Реализуй единый application layer и два адаптера:
   - REST API для web-клиента;
   - MCP endpoint с tools из `docs/architecture.md`.
6. Все mutating MCP tools должны принимать `request_id` и не создавать дубль при повторном вызове.
7. Добавь seed/dev данные: два локальных профиля, короткая public-domain или явно тестовая книга для `sr` и `en`.
8. Реализуй REST Problem Details, валидацию, корректную обработку транзакций и UTC timestamps.
9. Добавь unit tests и интеграционные тесты с Testcontainers PostgreSQL.
10. Обнови README точными командами запуска, тестирования, URL REST/OpenAPI/MCP и примерами tool-вызовов.

## REST API минимум

- profiles: список;
- books: список и выбор;
- sessions: начать/закончить;
- reading: следующий фрагмент и запись результата;
- vocabulary: запись незнакомого слова и список;
- stats: дневная статистика.

Не добавляй JWT, Kubernetes, брокер сообщений, LLM-интеграцию и сложный SRS на первом этапе.

## Критерии приемки

- `./gradlew :backend:check` проходит из корня проекта;
- чистая БД полностью создается миграциями;
- интеграционный тест проходит сквозной сценарий start → next chunk → result/unknown word → finish → stats;
- повтор mutating tool с тем же `request_id` не меняет результат второй раз;
- MCP и REST используют одинаковую бизнес-логику;
- секретов и copyrighted book content в git нет.

В конце сообщи: что реализовано, какие допущения сделаны, команды проверки и оставшиеся ограничения.
