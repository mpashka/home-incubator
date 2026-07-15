pluginManagement {
    val quarkusPluginVersion = "3.15.1"
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("io.quarkus") version quarkusPluginVersion
    }
}

rootProject.name = "shopping-backend"
