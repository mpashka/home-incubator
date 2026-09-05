---
tags: "@tag:meta"
---

# Документация

Электронный словарь сербского языка. Инструкции для агента — в [AGENTS.md](../AGENTS.md).

Документация ведётся по соглашению [llm-wiki-tags](https://github.com/mpashka/llm-wiki-tags):
вики с `index.md` в каждой значимой директории плюс тэги `@tag:<slug>`, связывающие код
и документацию поперёк дерева. Установленные правила — `.claude/rules/llm-wiki-tags/`.

## Страницы

- [plan.md](plan.md) — план работ по проекту, отмечается по ходу выполнения
- [tags.md](tags.md) — реестр тэгов

## Поддиректории

- [specification/](specification/index.md) — как словарь выглядит снаружи: что он умеет,
  REST API
- [implementation/](implementation/index.md) — как он устроен внутри: окружение, схема базы,
  правила разбора, источники сведений, работа с моделями
- [testing/](testing/index.md) — как он проверяется: модульные тесты и прогоны правил
  на всей исходной базе
- [requests/](requests/index.md) — исходные постановки задач от пользователя

## Код

- [../core/index.md](../core/index.md) — модель словаря, разбор разметки, правила частей речи
  и словоформ
- [../importer/index.md](../importer/index.md) — конвертация исходной базы в целевую и отчёты
  о качестве правил
- [../backend/index.md](../backend/index.md) — Spring Boot: REST API, поиск
- [../frontend/index.md](../frontend/index.md) — веб-фронтенд на Vue
