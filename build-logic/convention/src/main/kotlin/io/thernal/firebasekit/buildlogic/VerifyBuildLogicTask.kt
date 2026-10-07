package io.thernal.firebasekit.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails when `build-logic/` no longer matches its `SYNCED` manifest. The build logic is
 * conventions-kit's: change it there and run `scripts/sync-kits.py`, never here.
 */
abstract class VerifyBuildLogicTask : DefaultTask() {
    @get:Internal
    abstract val buildLogic: DirectoryProperty

    /** The manifest and every synced file, so the task reruns whenever any of them changes. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val syncedFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val stamp: RegularFileProperty

    @TaskAction
    fun verify() {
        val root = buildLogic.get().asFile
        val manifest = root.resolve(BuildLogicManifest.FILE_NAME)
        if (!manifest.isFile) {
            throw GradleException(
                "build-logic/${BuildLogicManifest.FILE_NAME} is missing. This build logic is synced from " +
                    "conventions-kit: run its scripts/sync-kits.py.",
            )
        }
        val drift = BuildLogicManifest.drift(root, BuildLogicManifest.parse(manifest.readText()))
        if (drift.isNotEmpty()) {
            throw GradleException(
                "build-logic/ differs from what conventions-kit synced:\n" +
                    drift.joinToString("\n") { line -> "  $line" } +
                    "\nMake the change in conventions-kit and run its scripts/sync-kits.py; build logic is not " +
                    "edited in a kit.",
            )
        }
        stamp.get().asFile.writeText("in sync\n")
    }
}
