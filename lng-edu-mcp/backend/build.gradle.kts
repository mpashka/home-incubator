plugins {
    java
}

group = "dev.homeincubator.lngedu"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

tasks.test {
    useJUnitPlatform()
}
