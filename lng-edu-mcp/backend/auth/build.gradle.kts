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
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.5")
    }
}

dependencies {
    // Depends on the shared kernel (AccountService resolves Google identities to app_accounts).
    api(project(":backend:core"))

    // Spring Authorization Server (OAuth 2.1 Auth Code + PKCE, OIDC discovery, DCR endpoint).
    api("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    // OAuth2 Login client to federate the end-user login into Google.
    api("org.springframework.boot:spring-boot-starter-oauth2-client")
    // spring-boot-starter-security comes in transitively via the two starters above.

    // Testing: MockMvc-based metadata/redirect checks (no DB, no real Google).
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Stay Java 21 compatible while building with a newer JDK.
tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

tasks.test {
    useJUnitPlatform()
    systemProperty("net.bytebuddy.experimental", "true")
}
