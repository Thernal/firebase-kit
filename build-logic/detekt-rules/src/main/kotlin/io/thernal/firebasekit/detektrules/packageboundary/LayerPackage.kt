package io.thernal.firebasekit.detektrules.packageboundary

internal enum class Layer(val packageName: String) {
    DATA("data"),
    DOMAIN("domain"),
    PRESENTATION("presentation"),
    ;

    // `domain` is the innermost layer: the other two build on it, and it may not reach back out.
    val forbiddenTargets: Set<Layer>
        get() = when (this) {
            DATA -> setOf(PRESENTATION)
            PRESENTATION -> setOf(DATA)
            DOMAIN -> setOf(DATA, PRESENTATION)
        }

    companion object {
        fun of(packageName: String?): Layer? = entries.firstOrNull { layer ->
            layer.packageName == packageName
        }
    }
}

internal data class ModulePackage(val moduleRoot: String, val remainder: String?) {
    val topLevelPackage: String? get() = remainder?.substringBefore('.')
    val layer: Layer? get() = Layer.of(topLevelPackage)
}

internal data class ModuleLayer(val moduleRoot: String, val layer: Layer)

// A module root package ends at its first `api` or `impl` segment. Capability directories vary in
// depth, and a segment named `api` further down belongs to a layer's contents rather than to
// another module. `wiring` modules match nothing here and are therefore exempt from both rules:
// a binding container is neither data, domain, nor presentation.
// The prefix is a plain string, not part of the pattern, so a kit install's package rename reaches it.
private const val ROOT_PACKAGE = "io.thernal.firebasekit"
private val moduleRootPattern = Regex("""^(${Regex.escape(ROOT_PACKAGE)}\.(?:[^.]+\.)*?(?:api|impl))(?:\.(.+))?$""")

internal fun String.toModulePackage(): ModulePackage? {
    val match = moduleRootPattern.find(this) ?: return null
    return ModulePackage(
        moduleRoot = match.groupValues[1],
        remainder = match.groupValues[2].ifEmpty { null },
    )
}

internal fun String.toModuleLayer(): ModuleLayer? {
    val modulePackage = toModulePackage() ?: return null
    val layer = modulePackage.layer ?: return null
    return ModuleLayer(moduleRoot = modulePackage.moduleRoot, layer = layer)
}
