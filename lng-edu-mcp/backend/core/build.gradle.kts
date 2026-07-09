plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.homeincubator.lngedu"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        // Use the Spring Boot BOM for dependency versions WITHOUT applying the boot application plugin.
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.5")
    }
}

dependencies {
    // Persistence + domain
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-validation")

    // Database migrations
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // PostgreSQL driver (runtime only)
    runtimeOnly("org.postgresql:postgresql")

    // Testing (pure unit tests for the domain/service layer)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Without the Spring Boot application plugin, the JUnit Platform launcher must be added
    // explicitly so Gradle can run useJUnitPlatform() tests.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Stay Java 21 compatible while building with a newer JDK (no toolchain block:
// the local JDK 21 install is broken, so we compile with JDK 25 targeting 21).
tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

tasks.test {
    useJUnitPlatform()
    // ByteBuddy (behind Mockito) mocks concrete classes; the local JDK 21 is broken so tests run
    // on JDK 25, whose class-file version ByteBuddy does not yet officially support.
    systemProperty("net.bytebuddy.experimental", "true")
}
