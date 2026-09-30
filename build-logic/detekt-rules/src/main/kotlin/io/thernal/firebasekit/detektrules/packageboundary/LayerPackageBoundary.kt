package io.thernal.firebasekit.detektrules.packageboundary

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtImportDirective

/** Keeps the layers of one `api` or `impl` module pointing inwards, towards `domain`. */
class LayerPackageBoundary(config: Config) : Rule(
    config = config,
    description = "Inside an api or impl module, domain stays free of data and presentation, and " +
        "those two never import each other.",
) {
    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)

        val source =
            importDirective.containingKtFile.packageFqName.asString().toModuleLayer() ?: return
        val importedPath = importDirective.importPath?.pathStr ?: return
        val target = importedPath.toModuleLayer() ?: return
        if (source.moduleRoot != target.moduleRoot || target.layer !in source.layer.forbiddenTargets) {
            return
        }

        report(
            Finding(
                Entity.from(importDirective),
                "`${source.layer.packageName}` must not import `${target.layer.packageName}` " +
                    "inside the same module.",
            ),
        )
    }
}
