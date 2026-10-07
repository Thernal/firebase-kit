package io.thernal.firebasekit.buildlogic

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/**
 * One strict, reproducible Detekt setup for every module. Applied by
 * [KmpLibraryConventionPlugin] rather than by modules, so it carries no plugin id of its own —
 * static analysis is not a capability a module opts into.
 *
 * Findings fail the build. The repository starts with none, and a warning nobody is forced to
 * clear is a rule that decays; a genuinely wrong finding is silenced in `config/detekt/detekt.yml`
 * or with `@Suppress`, where the decision is visible in review.
 */
internal class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("dev.detekt")

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig.set(true)
                allRules.set(false)
                parallel.set(true)
                config.setFrom(rootProject.files("config/detekt/detekt.yml"))
            }

            dependencies {
                add("detektPlugins", libs.library("detekt-ktlint-wrapper"))
                // Substituted for build-logic's `:detekt-rules` project, which is an included build.
                add("detektPlugins", "io.thernal.firebasekit.buildlogic:detekt-rules")
            }

            tasks.withType<Detekt>().configureEach {
                // `-PdetektAutoCorrect=true` lets ktlint fix formatting in place; findings still fail the build.
                autoCorrect.set(providers.gradleProperty("detektAutoCorrect").orNull == "true")
                exclude("**/build/**", "**/generated/**")
                reports {
                    html.required.set(true)
                    checkstyle.required.set(true)
                    sarif.required.set(false)
                    markdown.required.set(false)
                }
            }

            // Detekt registers two families of task for a multiplatform module: one per source set
            // (`detektCommonMainSourceSet`), and one per compilation (`detektMainAndroid`). Only the
            // compilation tasks run with type resolution, which `UnsafeCollectionIndexAccess` needs to
            // tell a List apart from anything else — but there are compilation tasks for Android only.
            // So `check` runs the Android compilations, which cover common and Android code, plus the
            // source-set tasks for what no Android compilation sees: the iOS source sets.
            val typeResolvedAnalysis = tasks.withType(Detekt::class.java).matching { task ->
                isCountedAnalysis(task.name)
            }
            val buildLogicVerification = registerBuildLogicVerification()
            tasks.named("check") {
                dependsOn(typeResolvedAnalysis)
                dependsOn(buildLogicVerification)
            }
        }
    }
}

/**
 * `verifyBuildLogic` in every module this plugin is applied to, part of its `check`: the build logic is
 * the repository's, and each module's check guards it. The task is up to date unless a synced file or
 * the manifest changed, so the repeats cost nothing.
 */
private fun Project.registerBuildLogicVerification(): TaskProvider<VerifyBuildLogicTask> {
    val buildLogic = rootProject.layout.projectDirectory.dir("build-logic")
    return tasks.register<VerifyBuildLogicTask>(VERIFY_BUILD_LOGIC_TASK) {
        group = "verification"
        description = "Fails when build-logic/ was edited here instead of synced from conventions-kit."
        this.buildLogic.set(buildLogic)
        syncedFiles.from(
            rootProject.fileTree(buildLogic) {
                BuildLogicManifest.IGNORED_DIRECTORIES.forEach { directory -> exclude("**/$directory/**") }
            },
        )
        stamp.set(layout.buildDirectory.file("verify-build-logic/stamp"))
    }
}

internal fun isCountedAnalysis(name: String): Boolean {
    if (name == "detekt") return false
    if (!name.endsWith("SourceSet")) return true
    val sourceSet = name.removePrefix("detekt").removeSuffix("SourceSet")
    return !sourceSet.startsWith("Common") && !sourceSet.startsWith("Android")
}

private const val VERIFY_BUILD_LOGIC_TASK = "verifyBuildLogic"
