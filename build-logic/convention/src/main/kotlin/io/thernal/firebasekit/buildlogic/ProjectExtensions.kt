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

/** Gradle property: the package prefix every module's namespace starts with. */
internal const val NAMESPACE_PROPERTY = "app.namespace"

/** Gradle property: the directory modules live under, relative to the repository root ("" = root). */
internal const val MODULES_ROOT_PROPERTY = "app.modules.root"

/** `app.modules.root` as path segments: `fixture` → `[fixture]`, unset → `[]`. */
internal fun parseModulesRoot(value: String?): List<String> {
    return value.orEmpty().splitToSequence('/', ':').map(String::trim).filter(String::isNotEmpty).toList()
}

/** [segments][List] of a project path with the modules root in front of them removed. */
internal fun List<String>.belowModulesRoot(modulesRoot: List<String>): List<String> {
    if (take(modulesRoot.size) == modulesRoot) {
        return drop(modulesRoot.size)
    }
    return this
}

/**
 * The Android namespace of a project path such as `:features:profile:impl` — the same derivation as
 * build-kit's, so a kit's modules and an app's agree. The modules root is a directory, not part of any
 * module's identity, so it is left out. Segments are split on both `:` and `-`, so a
 * `venue-management` directory contributes two package segments, matching the source layout.
 */
internal fun namespaceFor(
    prefix: String,
    projectPath: String,
    modulesRoot: List<String> = emptyList(),
): String {
    val suffix = projectPath.removePrefix(":").split(':')
        .belowModulesRoot(modulesRoot)
        .asSequence()
        .flatMap { segment -> segment.split('-') }
        .filter(String::isNotBlank)
        .joinToString(".") { segment -> segment.filter { char -> char.isLetterOrDigit() || char == '_' } }
    return listOf(prefix, suffix).filter(String::isNotBlank).joinToString(".")
}

internal fun Project.defaultNamespace(): String {
    val prefix = providers.gradleProperty(NAMESPACE_PROPERTY).orNull
        ?.takeIf(String::isNotBlank)
        ?: error("Set $NAMESPACE_PROPERTY in gradle.properties, for example $NAMESPACE_PROPERTY=io.thernal.firebasekit")
    return namespaceFor(
        prefix = prefix,
        projectPath = path,
        modulesRoot = parseModulesRoot(providers.gradleProperty(MODULES_ROOT_PROPERTY).orNull),
    )
}
