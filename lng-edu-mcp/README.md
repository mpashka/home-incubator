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

## Сборка

Проект использует Gradle Wrapper 9.6.1 и Kotlin DSL. Установленный в системе Gradle не требуется.

Требования:

- JDK 21 или новее; локально можно использовать `/opt/java/jdk-25`;
- backend компилируется с `--release 21` и остается совместимым с Java 21;
- Node.js и npm после создания Vue-приложения.

Команды:

```bash
export JAVA_HOME=/opt/java/jdk-25
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew build
./gradlew :backend:build
./gradlew webBuild
./gradlew clean
```

Пока исходный код приложений не создан, backend собирается как пустой Java-модуль, а задачи web пропускаются.
