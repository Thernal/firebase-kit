package io.thernal.firebasekit.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Compose Multiplatform on top of [KmpLibraryConventionPlugin], with the runtime and UI artifacts every
 * Compose module needs — as `implementation`: nothing is re-exported, so a consumer that uses Compose
 * types applies Compose itself. A module adds anything beyond these (animation, material) on its own.
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

        if (providers.gradleProperty(STABILITY_REPORT_PROPERTY).orNull == "true") {
            extensions.configure<ComposeCompilerGradlePluginExtension> {
                metricsDestination.set(layout.buildDirectory.dir("compose-metrics"))
                reportsDestination.set(layout.buildDirectory.dir("compose-reports"))
            }
        }
    }
}

/** Set by whoever wants a Compose stability report; off for every ordinary build. */
private const val STABILITY_REPORT_PROPERTY = "composeStabilityReport"
