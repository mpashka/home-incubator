---
tags: "@tag:import @tag:source-db"
---

# Перенос данных и отчёты

Родительский индекс: [../docs/index.md](../docs/index.md) ·
Прогоны и их числа: [../docs/testing/rules-reports.md](../docs/testing/rules-reports.md)

Конвертация исходной sqlite-базы в Postgres и отчёты о качестве правил. Исходная база
открывается **только на чтение** — это чужой файл из Android-приложения.

Код: `src/main/java/org/mpashka/vocabulary/importer/`.

## Перенос

- `SourceReader.java` — последовательное чтение всех статей исходной базы
- `MigrateToPostgres.java` — перенос словаря в Postgres, `./gradlew :importer:migrate`
  (схема — [db-schema.md](../docs/implementation/db-schema.md))
- `Homonyms.java` — разделение омонимов, упакованных исходной базой в одну строку;
  ударение каждого восстанавливается из разметки
- `WordForms.java` — сборка словоформ для указателя поиска: порождаются только буквы,
  без ударения

## Отчёты о качестве правил

Печатают сводку в консоль, ничего не меняя. Что и с чем сверяется и какая точность
достигнута — [rules-reports.md](../docs/testing/rules-reports.md).

- `PartOfSpeechReport.java` — часть речи вслепую, `./gradlew :importer:run`
- `NounFormsReport.java` — родительный падеж, `./gradlew :importer:runNounForms`
- `VerbFormsReport.java` — настоящее время, `./gradlew :importer:runVerbForms`
- `AdjectiveFormsReport.java` — формы родов, `./gradlew :importer:runAdjectiveForms`
