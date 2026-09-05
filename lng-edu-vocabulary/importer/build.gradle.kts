// Конвертация исходной sqlite-базы словаря в целевую Postgres.
plugins {
    application
}

dependencies {
    implementation(project(":core"))
    implementation(rootProject.libs.sqlite.jdbc)
    implementation(rootProject.libs.postgresql)
}

application {
    // Отчёт о качестве правил определения части речи: ./gradlew :importer:run
    mainClass = "org.mpashka.vocabulary.importer.PartOfSpeechReport"
}

// Отчёт о качестве правил склонения существительных.
tasks.register<JavaExec>("runNounForms") {
    group = "application"
    description = "Сверяет порождённый родительный падеж с указанным в исходной базе"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "org.mpashka.vocabulary.importer.NounFormsReport"
}

// Отчёт о качестве правил спряжения глаголов.
tasks.register<JavaExec>("runVerbForms") {
    group = "application"
    description = "Сверяет порождённое настоящее время с указанным в исходной базе"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "org.mpashka.vocabulary.importer.VerbFormsReport"
}

// Отчёт по формам прилагательных.
tasks.register<JavaExec>("runAdjectiveForms") {
    group = "application"
    description = "Показывает, сколько форм родов есть готовыми и что добирают правила"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "org.mpashka.vocabulary.importer.AdjectiveFormsReport"
}

// Перенос словаря из sqlite в Postgres (этап 5).
tasks.register<JavaExec>("migrate") {
    group = "application"
    description = "Переносит словарь из исходной sqlite-базы в Postgres"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "org.mpashka.vocabulary.importer.MigrateToPostgres"
}
