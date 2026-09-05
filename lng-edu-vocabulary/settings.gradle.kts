rootProject.name = "lng-edu-vocabulary"

// core     — модель словаря, разбор исходной разметки, правила частей речи и словоформ
// importer — конвертация исходной sqlite-базы в целевую Postgres
// backend  — Spring Boot: поиск по словоформам, REST API, пополнение через LLM
include("core", "importer", "backend")
