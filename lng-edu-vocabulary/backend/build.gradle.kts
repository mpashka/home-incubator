// Spring Boot: поиск по словоформам, REST API для фронтенда, пополнение через LLM.
plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dep.mgmt)
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // Схема ведётся миграциями: db/migration. Замысел — docs/implementation/db-schema.md.
    // Нужен именно стартер: в Spring Boot 4 автонастройки разнесены по модулям,
    // и одного flyway-core не хватает — миграции молча не применяются.
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly(rootProject.libs.postgresql)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
