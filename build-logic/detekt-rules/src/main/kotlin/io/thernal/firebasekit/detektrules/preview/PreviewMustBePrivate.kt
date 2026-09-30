package io.thernal.firebasekit.detektrules.preview

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction

class PreviewMustBePrivate(config: Config) : Rule(
    config = config,
    description = "Preview functions are IDE-only and must not expand a module's public API.",
) {
    private val previewAnnotations: List<String> by config(listOf("Preview"))

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        if (function.hasModifier(KtTokens.PRIVATE_KEYWORD)) {
            return
        }

        val annotation = function.annotationEntries
            .mapNotNull { it.typeReference?.text?.substringAfterLast('.') }
            .firstOrNull { it in previewAnnotations } ?: return

        report(Finding(Entity.atName(function), "@$annotation function `${function.name}` must be private."))
    }
}
