package io.thernal.firebasekit.buildlogic

import java.io.File
import java.security.MessageDigest

/**
 * `build-logic/SYNCED`: what `conventions-kit/scripts/sync-kits.py` wrote into this build logic — a
 * header naming the conventions-kit commit, then `<sha-256>  <path>` per file, paths relative to
 * `build-logic/`. The kit's build compares the files against it, so a hand edit fails here rather
 * than drifting until someone remembers to run the script.
 */
internal object BuildLogicManifest {
    const val FILE_NAME = "SYNCED"

    /** Directories under `build-logic/` that are never synced: build output and tool state. */
    val IGNORED_DIRECTORIES = setOf("build", ".gradle", ".kotlin", ".idea")

    fun parse(text: String): Map<String, String> {
        return text.lineSequence()
            .map(String::trim)
            .filter { line -> line.isNotEmpty() && !line.startsWith("#") }
            .associate { line ->
                val hash = line.substringBefore(' ')
                val path = line.substringAfter(' ').trim()
                path to hash
            }
    }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    /** Every file under [root] that the sync owns, relative to it. */
    fun files(root: File): List<String> {
        return root.walkTopDown()
            .onEnter { directory -> directory == root || directory.name !in IGNORED_DIRECTORIES }
            .filter(File::isFile)
            .map { file -> file.relativeTo(root).invariantSeparatorsPath }
            .filter { path -> path != FILE_NAME && !path.endsWith(".DS_Store") }
            .sorted()
            .toList()
    }

    /** The differences between [root] and its manifest, one readable line each; empty when in sync. */
    fun drift(root: File, manifest: Map<String, String>): List<String> {
        val present = files(root).toSet()
        val changed = manifest.filter { (path, hash) -> path in present && sha256(root.resolve(path)) != hash }
        return buildList {
            changed.keys.forEach { path -> add("edited: $path") }
            (manifest.keys - present).forEach { path -> add("deleted: $path") }
            (present - manifest.keys).forEach { path -> add("added: $path") }
        }.sorted()
    }
}
