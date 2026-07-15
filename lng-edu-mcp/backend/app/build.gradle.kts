plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.homeincubator.lngedu"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:1.20.6")
        // Spring AI 1.0.x GA aligns with Spring Boot 3.4.x; provides the MCP server starter.
        mavenBom("org.springframework.ai:spring-ai-bom:1.0.1")
    }
}

dependencies {
    // Shared kernel (entities, repositories, services, migrations) + auth scaffold.
    implementation(project(":backend:core"))
    implementation(project(":backend:auth"))

    // Web / REST transport
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Operations
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // OpenAPI / Swagger UI for REST
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    // MCP transport: official Spring AI MCP server over HTTP/SSE in the existing Spring MVC app.
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Resource-server security tests: mint/mock JWTs (jwt() post-processor) for @WebMvcTest slices.
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

// Stay Java 21 compatible while building with a newer JDK (no toolchain block:
// the local JDK 21 install is broken, so we compile with JDK 25 targeting 21).
tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

tasks.test {
    useJUnitPlatform()
    // ByteBuddy (behind Mockito) must mock concrete @Service classes in the @WebMvcTest slices;
    // the local JDK 21 is broken so tests run on JDK 25, whose class-file version ByteBuddy does
    // not yet officially support. This flag lets it proceed on the newer JDK.
    systemProperty("net.bytebuddy.experimental", "true")
}
