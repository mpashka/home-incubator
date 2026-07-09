plugins {
    base
}

val webPackageJson = layout.projectDirectory.file("web/package.json").asFile
val webPackageLock = layout.projectDirectory.file("web/package-lock.json").asFile

if (webPackageLock.isFile) {
    tasks.register<Exec>("webInstall") {
        group = "build"
        description = "Installs locked web dependencies."
        workingDir(layout.projectDirectory.dir("web"))
        commandLine("npm", "ci")
    }
} else {
    tasks.register("webInstall") {
        group = "build"
        description = "Placeholder until web/package-lock.json exists."
    }
}

if (webPackageJson.isFile) {
    tasks.register<Exec>("webBuild") {
        group = "build"
        description = "Builds the Vue application."
        dependsOn("webInstall")
        workingDir(layout.projectDirectory.dir("web"))
        commandLine("npm", "run", "build")
    }
} else {
    tasks.register("webBuild") {
        group = "build"
        description = "Placeholder until web/package.json exists."
        dependsOn("webInstall")
    }
}

tasks.named("build") {
    // :backend:app is the bootable module; its build aggregates the core + auth libraries it depends on.
    dependsOn(":backend:app:build", "webBuild")
}

tasks.named<Delete>("clean") {
    delete("web/dist", "web/coverage")
}
