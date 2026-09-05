---
tags: "@tag:meta"
---

# Реестр тэгов

Родительский индекс: [index.md](index.md) ·
Правила: `.claude/rules/llm-wiki-tags/tags.md`

Тэг — короткий slug в kebab-case, записанный токеном `@tag:<slug>`. **Один и тот же тэг
ставится и в коде, и в документации**, чтобы концепт, охватывающий несколько файлов и
папок, находился одним поиском.

## Тэги проекта

| Тэг | Концепт | Где живёт |
|---|---|---|
| `@tag:meta` | Служебные страницы репозитория: индексы, реестр тэгов, соглашения | `docs/*/index.md` |
| `@tag:plan` | План работ и его отметки о выполнении | [plan.md](plan.md) |
| `@tag:source-db` | Исходная sqlite-база словаря и её формат | [implementation/source-db.md](implementation/source-db.md), `importer/SourceReader` |
| `@tag:markup` | Разбор собственной разметки `$TAG#` из поля `xml` исходной базы | `core/MarkupParser`, `core/EntryParser` |
| `@tag:accent` | Ударение: сербские акценты, заударная долгота, разбор и показ | `core/Accent`, `core/Serbian`, `frontend/` |
| `@tag:part-of-speech` | Определение части речи | [implementation/part-of-speech.md](implementation/part-of-speech.md), `core/PartOfSpeechRules` |
| `@tag:word-forms` | Словоформы: правила образования и поиск по ним | [implementation/word-forms.md](implementation/word-forms.md), `core/*Declension`, `core/VerbConjugation` |
| `@tag:word-roots` | Связи слова с русскими, сербскими и историческими корнями | `backend/PostgresDictionary` (таблица пока пуста) |
| `@tag:import` | Конвертация исходной базы в целевую | `importer/`, [testing/rules-reports.md](testing/rules-reports.md) |
| `@tag:llm` | Пополнение словаря через LLM | [implementation/llm.md](implementation/llm.md) |
| `@tag:book-import` | Импорт книги и двуязычный перевод | пока только план, этап 11 |

Заводя новый тэг — добавь строку в эту таблицу **до** того, как поставишь токен.
Тэг — для сквозного концепта, а не для разовой детали.

## Формат тэга

Slug'и — строчные, в kebab-case (`[a-z0-9-]+`), при желании иерархические через `/`
(`@tag:word-forms/nouns`); поиск по родителю находит и потомков. Один и тот же токен
`@tag:<slug>` ставится и в коде, и в документации.

- **Документация** (`.md`): в **YAML front matter** в самом верху файла, поле `tags`
  со списком токенов через пробел; для директории — во front matter её `index.md`.
  Тэг, относящийся только к одному разделу, можно поставить строкой `@tag:<slug>`
  в теле рядом с этим разделом.

  ```markdown
  ---
  tags: "@tag:accent @tag:word-forms"
  ---
  ```

- **Код**: комментарий в синтаксисе языка с токеном, прямо над элементом. Тэг может
  маркировать **пакет**, **файл**, **класс** (или его аналог) либо **метод/функцию**.
  Java — `.claude/rules/llm-wiki-tags/code-tags-java.md`, JavaScript и Vue —
  `code-tags-javascript.md` рядом.
- **Несколько тэгов**: через пробел — `@tag:accent @tag:word-forms`.

## Раскладка документации

Страница относится ровно к одному из трёх деревьев: `specification/` (как словарь
выглядит снаружи), `implementation/` (как устроен внутри), `testing/` (как проверяется).
Рабочие файлы задач — `requests/<задача>/`. Правило целиком —
`.claude/rules/llm-wiki-tags/docs-layout.md`.

## Поиск

```bash
grep -rn "@tag:<slug>" .                      # все места тэга и его потомков
grep -oE "@tag:[a-z0-9/-]+" path/to/file      # тэги файла
grep -rhoE "@tag:[a-z0-9/-]+" . | sort -u     # все тэги репозитория
```
