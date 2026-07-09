# Схема БД

@tag:domain-model @tag:request-id @tag:reading-block @tag:difficulty-model @tag:auth @tag:account-linking

Проектируемая схема PostgreSQL для MVP. Соответствует начальной модели и
вертикальному сценарию из [`architecture.md`](architecture.md). Схема реализована
Flyway-миграцией `backend/core/src/main/resources/db/migration/V1__init.sql`, которая и
является источником истины (см. правило 6 в [`../AGENTS.md`](../AGENTS.md)).

> **Пересмотр (2026-07-07).** Отказались от хранения книги как множества мелких
> строк `book_chunks`. Текст хранится крупным блоком, позиция чтения — смещение,
> а блок чтения собирается динамически в application layer. Размер блока —
> свойство связки «пользователь + язык», а не книги. Обоснование и варианты — в
> [`adr/0001-reading-blocks-and-text-storage.md`](adr/0001-reading-blocks-and-text-storage.md).

## Соглашения

- Первичные ключи — `uuid` (решение зафиксировать отдельным ADR при
  необходимости).
- Все временные метки — `timestamptz` в UTC.
- Языки — BCP 47 (`sr`, `en`), тип `text` с CHECK-ограничением на поддерживаемый
  набор.
- Изменяющие операции идемпотентны по `request_id` (`@tag:request-id`):
  уникальный ключ на `request_id` отклоняет повторную вставку.
- Ссылочная целостность — внешние ключи с `ON DELETE` по смыслу; каскад только
  для строго подчинённых сущностей (текст книги).

## Единица чтения (@tag:reading-block)

Блок чтения — это динамически собираемый отрезок текста для одного шага занятия.
Он **не хранится** как строка БД. `get_next_chunk` берёт текст книги, начинает с
сохранённого смещения и набирает окно между `block_min_words` и `block_max_words`,
предпочитая закончить на границе предложения/абзаца. В дальнейшем размер блока
определяется моделью сложности слов (`@tag:difficulty-model`, см. ADR 0001).

## Таблицы

### users

Локальные учебные профили (без production-аутентификации на первом этапе).

| Колонка | Тип | Ограничения |
| --- | --- | --- |
| `id` | uuid | PK |
| `display_name` | text | NOT NULL |
| `timezone` | text | NOT NULL, IANA tz (для расчёта пользовательских дат) |
| `created_at` | timestamptz | NOT NULL, default now() |
| `owner_account_id` | uuid | NULL, FK → app_account(id) ON DELETE SET NULL (аккаунт-владелец профиля, `@tag:account-linking`; nullable для обратной совместимости) |

Колонка `owner_account_id` добавлена миграцией `V3__accounts.sql` (ADR 0002); индекс
`idx_users_owner_account`.

### user_language_skills (@tag:reading-block)

Уровень владения пользователя языком и параметры адаптивного блока чтения. Одна
строка на пару user–language. Именно здесь живёт «размер блока», который со
временем растёт.

| Колонка | Тип | Ограничения |
| --- | --- | --- |
| `id` | uuid | PK |
| `user_id` | uuid | FK → users(id) |
| `language` | text | NOT NULL, CHECK IN (`sr`,`en`) |
| `level` | text | NOT NULL, уровень владения (напр. CEFR-подобный или числовой) |
| `block_min_words` | int | NOT NULL, минимальный размер блока |
| `block_max_words` | int | NOT NULL, максимальный размер блока |
| `target_unknown_ratio` | numeric | NULL, целевая доля незнакомых слов (для `@tag:difficulty-model`) |
| `updated_at` | timestamptz | NOT NULL |

Уникальность: `(user_id, language)`.

### books

Метаданные книги на конкретном языке (строка остаётся лёгкой; текст — в
`book_texts`).

| Колонка | Тип | Ограничения |
| --- | --- | --- |
| `id` | uuid | PK |
| `title` | text | NOT NULL |
| `language` | text | NOT NULL, CHECK IN (`sr`,`en`) |
| `author` | text | NULL |
| `source` | text | NULL, происхождение/лицензия |
| `created_at` | timestamptz | NOT NULL, default now() |

### book_texts

Полный текст книги крупным блоком (memo). Отдельная таблица 1:1 к `books`, чтобы
не грузить текст при выборке метаданных.

| Колонка | Тип | Ограничения |
| --- | --- | --- |
| `book_id` | uuid | PK, FK → books(id) ON DELETE CASCADE |
| `content` | text | NOT NULL, полный текст |
| `length_chars` | int | NOT NULL, длина в символах (для границ смещения) |

Чтение окна: `substr(content, offset + 1, max_window_chars)` — в память попадает
только нужный срез, затем он подрезается до границы предложения/абзаца.

> Альтернатива (ADR 0001, вариант B/C): хранить текст внешним файлом или крупными
> блоками-строками `book_blocks (book_id, position, content)`. Отложено.

### reading_progress

Позиция чтения пользователя в книге (одна строка на пару user–book). Позиция —
**символьное смещение**, а не индекс чанка.

| Колонка | Тип | Ограничения |
| --- | --- | --- |
| `id` | uuid | PK |
| `user_id` | uuid | FK → users(id) |
| `book_id` | uuid | FK → books(id) |
| `position_char` | int | NOT NULL, default 0, смещение в символах |
| `updated_at` | timestamptz | NOT NULL |

Уникальность: `(user_id, book_id)`.

### learning_sessions

Учебная сессия: интервал занятия по книге.

| Колонка | Тип | Ограничения |
| --- | --- | --- |
| `id` | uuid | PK |
| `user_id` | uuid | FK → users(id) |
| `book_id` | uuid | FK → books(id) |
| `started_at` | timestamptz | NOT NULL |
| `finished_at` | timestamptz | NULL (NULL — активная сессия) |
| `request_id` | text | NOT NULL, UNIQUE (идемпотентность start) |

### vocabulary_items

Словарная запись пользователя: слово в изучаемом языке и его текущий статус.

| Колонка | Тип | Ограничения |
| --- | --- | --- |
| `id` | uuid | PK |
| `user_id` | uuid | FK → users(id) |
| `language` | text | NOT NULL, CHECK IN (`sr`,`en`) |
| `lemma` | text | NOT NULL, нормализованная форма слова |
| `status` | text | NOT NULL, напр. `new`/`learning`/`known` |
| `last_context` | text | NULL, последний встреченный контекст |
| `last_seen_at` | timestamptz | NOT NULL |
| `created_at` | timestamptz | NOT NULL, default now() |

Уникальность: `(user_id, language, lemma)`.

### word_events

Журнал событий по слову (встреча, отметка «непонятно», результат повторения).

| Колонка | Тип | Ограничения |
| --- | --- | --- |
| `id` | uuid | PK |
| `vocabulary_item_id` | uuid | FK → vocabulary_items(id) ON DELETE CASCADE |
| `session_id` | uuid | FK → learning_sessions(id) NULL |
| `event_type` | text | NOT NULL, напр. `unknown`/`review`/`seen` |
| `context` | text | NULL, контекст употребления |
| `occurred_at` | timestamptz | NOT NULL |
| `request_id` | text | NULL, UNIQUE (идемпотентность записи события) |

### reading_events (@tag:reading-block @tag:request-id)

Журнал шагов чтения: одна строка на выданный блок, результат которого зафиксирован
через `record_chunk_result`. Делает оценку понимания долговечной и позволяет дневной
статистике считать прочитанные символы/блоки.

| Колонка | Тип | Ограничения |
| --- | --- | --- |
| `id` | uuid | PK |
| `user_id` | uuid | FK → users(id) ON DELETE CASCADE |
| `book_id` | uuid | FK → books(id) ON DELETE CASCADE |
| `session_id` | uuid | NULL, FK → learning_sessions(id) ON DELETE SET NULL |
| `start_offset` | int | NOT NULL, смещение начала блока |
| `end_offset` | int | NOT NULL, смещение конца блока |
| `chars_read` | int | NOT NULL, `end_offset − start_offset` (не отрицательно) |
| `comprehension` | text | NOT NULL, CHECK IN (`understood`,`partial`,`unclear`) (понятно/частично/непонятно) |
| `request_id` | text | NULL, UNIQUE (идемпотентность записи результата) |
| `occurred_at` | timestamptz | NOT NULL |

Индекс: `(user_id, occurred_at)` для дневной агрегации по UTC-окну. Миграция —
`V2__reading_events.sql`.

## Аккаунты и идентичности (@tag:auth @tag:account-linking)

Модель аутентификации ADR 0002: аккаунты приложения отделены от учебных профилей
(`users`). Один `app_account` владеет одним или несколькими профилями, а несколько
внешних OAuth-идентичностей могут резолвиться в один аккаунт (ребёнок на нескольких
устройствах). Реализовано миграцией `V3__accounts.sql`. Подробности и решение —
[`adr/0002-auth-oauth-and-account-linking.md`](adr/0002-auth-oauth-and-account-linking.md).
На этом этапе — только модель данных и логика резолва, без OAuth/enforcement (фазы H/I).

### app_account

Аккаунт приложения: идентичность, в которую резолвится `sub` токена.

| Колонка | Тип | Ограничения |
| --- | --- | --- |
| `id` | uuid | PK, default gen_random_uuid() |
| `display_name` | text | NOT NULL |
| `role` | text | NOT NULL, CHECK IN (`owner`,`child`) |
| `created_at` | timestamptz | NOT NULL, default now() |

### external_identity

Внешняя OAuth-идентичность (IdP `sub`), связанная с `app_account`. Много
идентичностей → один аккаунт. Резолв идемпотентен по `(provider, subject)`.

| Колонка | Тип | Ограничения |
| --- | --- | --- |
| `id` | uuid | PK, default gen_random_uuid() |
| `account_id` | uuid | NOT NULL, FK → app_account(id) ON DELETE CASCADE |
| `provider` | text | NOT NULL, напр. `google` |
| `subject` | text | NOT NULL, IdP `sub` |
| `email` | text | NULL |
| `linked_at` | timestamptz | NOT NULL, default now() |

Уникальность: `(provider, subject)`. Индекс: `idx_external_identity_account (account_id)`.

## Будущее: модель сложности (@tag:difficulty-model)

Не входит в первый vertical slice, документируется как направление (ADR 0001):

- лексикон `word_difficulty (language, lemma, difficulty)` — уровень сложности
  слова (частотность/ранг);
- оценка знания слов пользователем строится из `vocabulary_items` + допущений;
- при сборке блока backend подбирает окно так, чтобы доля незнакомых слов попала
  в `target_unknown_ratio`.

## Связи

```
users 1───* user_language_skills            (размер блока по языку)
users 1───* reading_progress   *───1 books
users 1───* learning_sessions  *───1 books
books 1───1 book_texts
users 1───* vocabulary_items
vocabulary_items 1───* word_events *───0..1 learning_sessions
users 1───* reading_events   *───1 books   *───0..1 learning_sessions
app_account 1───* external_identity          (несколько идентичностей → один аккаунт)
app_account 1───* users(learner)             (owner_account_id, аккаунт владеет профилями)
```

## Отношение к вертикальному сценарию

- `start_learning_session` → INSERT в `learning_sessions` (идемпотентно по
  `request_id`).
- `get_next_chunk` → чтение `reading_progress.position_char` + окна из
  `book_texts` с учётом `user_language_skills`; блок собирается динамически.
- `record_chunk_result` → в одной транзакции: INSERT в `reading_events`
  (идемпотентно по `request_id`) + продвижение `reading_progress.position_char`
  до конца выданного блока (монотонно, повтор не двигает дважды).
- `record_unknown_word` → upsert `vocabulary_items` + INSERT `word_events`
  (идемпотентно по `request_id`).
- `finish_learning_session` → UPDATE `learning_sessions.finished_at`.
- `get_daily_stats` → агрегация в timezone пользователя: минуты и число сессий из
  `learning_sessions`, прочитанные символы/блоки из `reading_events`
  (`sum(chars_read)` и число строк за день), новые слова из `vocabulary_items`.
