@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "firebase-kit"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")

// Every directory under the given root that carries a build file is a module. Adding one is
// creating the directory — there is no list here to keep in sync with the tree.
fun includeModulesUnder(path: String) {
    val root = settingsDir.resolve(path)
    if (!root.exists()) return

    root.walkTopDown()
        .filter { it.isDirectory && it.resolve("build.gradle.kts").isFile }
        .forEach { moduleDir ->
            val modulePath = moduleDir
                .relativeTo(settingsDir)
                .invariantSeparatorsPath
                .replace('/', ':')
            include(":$modulePath")
        }
}

includeModulesUnder("firebase")
includeModulesUnder("sample")
