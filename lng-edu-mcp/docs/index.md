# docs

Корень LLM-wiki проекта. Документация: архитектура MVP, рабочие допущения, ADR и реестр тегов llm-wiki-plus. `index.md` живёт только здесь, в `docs/`; структура каталогов кода описана в [`../AGENTS.md`](../AGENTS.md) (раздел «Структура»).

## Файлы

- [`architecture.md`](architecture.md) — границы системы, первый вертикальный сценарий, начальная модель данных и список MCP tools.
- [`db-schema.md`](db-schema.md) — проектируемая схема PostgreSQL: таблицы, ключи, связи и соответствие вертикальному сценарию.
- [`plan.md`](plan.md) — план реализации MVP с отметками о статусе выполнения.
- [`decisions.md`](decisions.md) — рабочие допущения и зафиксированные решения по scope MVP.
- [`tags.md`](tags.md) — реестр тегов llm-wiki-plus, формат тегов и команды поиска.

## Каталоги

- [`adr/`](adr/index.md) — записи об архитектурных решениях (Architecture Decision Records).

## Вне docs/

`index.md` в этих каталогах не создаём; здесь — только указатели.

- `../prompts/` — задания агентам и prompt учебного ассистента: `backend-agent.md`, `frontend-agent.md`, `chatgpt-tutor.md`.
- `../backend/`, `../web/`, `../infra/` — модули проекта; их назначение описано в [`../AGENTS.md`](../AGENTS.md) (раздел «Структура»).
