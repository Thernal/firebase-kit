package io.thernal.firebasekit.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Enables the injection framework's code generation without adding architecture or project
 * dependencies. Metro is the current implementation; modules only ever name the capability.
 */
class InjectionConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("dev.zacsweers.metro")

        val catalog = libs

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.named("commonMain") {
                dependencies {
                    implementation(catalog.library("metro-runtime"))
                }
            }
        }
    }
}
