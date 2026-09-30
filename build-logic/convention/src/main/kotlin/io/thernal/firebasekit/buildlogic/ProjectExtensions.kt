package io.thernal.firebasekit.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> {
    return findLibrary(alias).orElseThrow {
        IllegalStateException("Missing library alias '$alias' in libs.versions.toml")
    }
}

internal fun VersionCatalog.version(alias: String): String {
    return findVersion(alias)
        .orElseThrow { IllegalStateException("Missing version '$alias' in libs.versions.toml") }
        .requiredVersion
}

private const val DEFAULT_NAMESPACE_PREFIX = "io.thernal.firebasekit"

/**
 * Derives the Android namespace for a Gradle project path such as `:firebase:crash:impl`. Segments are
 * split on both `:` and `-` so a `firebase-kit`-style directory name contributes two package segments,
 * matching the source layout.
 */
internal fun Project.defaultNamespace(): String {
    val prefix = findProperty("firebasekitNamespacePrefix")
        ?.toString()
        ?.takeIf(String::isNotBlank)
        ?: DEFAULT_NAMESPACE_PREFIX
    val suffix = path
        .removePrefix(":")
        .split(':', '-')
        .filter(String::isNotBlank)
        .joinToString(".") { segment -> segment.filter { it.isLetterOrDigit() || it == '_' } }
    return listOf(prefix, suffix).filter(String::isNotBlank).joinToString(".")
}
