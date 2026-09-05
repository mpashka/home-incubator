plugins {
    java
}

// Значения из каталога версий берём здесь: внутри блока subprojects
// типизированный доступ `libs` недоступен.
val javaVersion = libs.versions.java.get().toInt()
val junitJupiter = libs.junit.jupiter
val assertjCore = libs.assertj.core
val junitLauncher = libs.junit.platform.launcher

allprojects {
    group = "org.mpashka.vocabulary"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(javaVersion)
        }
    }

    dependencies {
        "testImplementation"(junitJupiter)
        "testImplementation"(assertjCore)
        "testRuntimeOnly"(junitLauncher)
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
