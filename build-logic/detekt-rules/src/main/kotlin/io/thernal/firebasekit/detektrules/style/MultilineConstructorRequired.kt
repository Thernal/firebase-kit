package io.thernal.firebasekit.detektrules.style

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter

class MultilineConstructorRequired(config: Config) : Rule(
    config = config,
    description = "A primary constructor with 2 or more parameters must put each parameter on its own " +
        "line. A single parameter may stay on its own line even when the whole signature would fit on one.",
) {
    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        val constructor = klass.primaryConstructor ?: return
        val parameters = constructor.valueParameters
        if (parameters.size < 2) {
            return
        }

        val lines = parameters.map(::startLine)
        if (lines.size != lines.toSet().size) {
            report(
                Finding(
                    Entity.from(constructor),
                    "Primary constructor parameters must each be on their own line.",
                ),
            )
        }
    }

    private fun startLine(parameter: KtParameter): Int {
        val text = parameter.containingKtFile.text
        val offset = parameter.textRange.startOffset
        return text.substring(0, offset).count { it == '\n' }
    }
}
