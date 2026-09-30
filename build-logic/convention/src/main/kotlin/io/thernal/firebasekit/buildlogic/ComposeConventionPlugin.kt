package io.thernal.firebasekit.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Compose Multiplatform on top of [KmpLibraryConventionPlugin], with the runtime and UI artifacts
 * every Compose module here needs — as `implementation`: nothing is re-exported, so a consumer that
 * uses Compose types applies Compose itself.
 */
class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.thernal.firebasekit.kmp.library")
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val catalog = libs

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.named("commonMain") {
                dependencies {
                    implementation(catalog.library("compose-runtime"))
                    implementation(catalog.library("compose-foundation"))
                    implementation(catalog.library("compose-ui"))
                }
            }
        }
    }
}
