package io.thernal.firebasekit.detektrules.packageboundary

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtFile

/**
 * Keeps every source of an `api` or `impl` module inside one of the three layer packages — or, in a
 * module named after a layer (`core.presentation.api`), directly under the module, never in a layer
 * package again.
 */
class LayerPackageRequired(config: Config) : Rule(
    config = config,
    description = "An api or impl module holds only data, domain, and presentation packages.",
) {
    // The finding belongs to the file rather than to any declaration inside it, so the rule hooks
    // the per-file entry point instead of a tree visit.
    override fun visit(root: KtFile) {
        super.visit(root)

        val modulePackage = root.packageFqName.asString().toModulePackage() ?: return
        val namedLayer = modulePackage.namedLayer
        if (namedLayer != null) {
            reportRepeatedLayer(root, modulePackage, namedLayer)
            return
        }
        if (modulePackage.layer != null) {
            return
        }

        val topLevelPackage = modulePackage.topLevelPackage
        val message = if (topLevelPackage == null) {
            "`${modulePackage.moduleRoot}` holds sources directly. Every file of an api or impl " +
                "module belongs to its `data`, `domain`, or `presentation` package."
        } else {
            "`$topLevelPackage` is not a layer package. An api or impl module holds only `data`, " +
                "`domain`, and `presentation` below its root."
        }
        report(Finding(Entity.from(root.packageDirective ?: root), message))
    }

    // A module named after its layer holds that layer directly; a layer package inside it either
    // repeats the layer or contradicts it.
    private fun reportRepeatedLayer(
        root: KtFile,
        modulePackage: ModulePackage,
        namedLayer: Layer,
    ) {
        val nested = Layer.of(modulePackage.topLevelPackage) ?: return
        val message = "`${modulePackage.moduleRoot}` is the `${namedLayer.packageName}` layer already; " +
            "`${nested.packageName}` below it repeats a layer. Put the code directly under the module root."
        report(Finding(Entity.from(root.packageDirective ?: root), message))
    }
}
