package io.thernal.firebasekit.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure

/**
 * The sample's Android application: Compose, the catalog's SDK levels, Detekt. Nothing an application
 * copies uses it — an application has its own application convention (build-kit's, for one).
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // No `org.jetbrains.kotlin.android`: since AGP 9 the Android plugin carries Kotlin support
        // itself, and applying the standalone plugin on top of it is an error.
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        QualityConventionPlugin().apply(target)

        val catalog = libs
        val jvm = catalog.version("jvm").toInt()

        extensions.configure<ApplicationExtension> {
            namespace = defaultNamespace()
            compileSdk = catalog.version("android-compile-sdk").toInt()

            defaultConfig {
                applicationId = defaultNamespace()
                minSdk = catalog.version("android-min-sdk").toInt()
                targetSdk = catalog.version("android-target-sdk").toInt()
                versionCode = 1
                versionName = "1.0"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.toVersion(jvm)
                targetCompatibility = JavaVersion.toVersion(jvm)
            }

            buildFeatures {
                compose = true
            }
        }

        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(jvm))
        }
    }
}
