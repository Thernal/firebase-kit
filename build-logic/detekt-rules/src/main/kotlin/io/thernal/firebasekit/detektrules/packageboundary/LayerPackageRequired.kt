package io.thernal.firebasekit.detektrules.packageboundary

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtFile

/** Keeps every source of an `api` or `impl` module inside one of the three layer packages. */
class LayerPackageRequired(config: Config) : Rule(
    config = config,
    description = "An api or impl module holds only data, domain, and presentation packages.",
) {
    // The finding belongs to the file rather than to any declaration inside it, so the rule hooks
    // the per-file entry point instead of a tree visit.
    override fun visit(root: KtFile) {
        super.visit(root)

        val modulePackage = root.packageFqName.asString().toModulePackage() ?: return
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
}
