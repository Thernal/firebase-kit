package io.thernal.firebasekit.detektrules.style

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction

class ExpressionBodyNotAllowed(config: Config) : Rule(
    config = config,
    description = "Function bodies must use a block body { ... } instead of an expression body (= ...).",
) {
    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        if (function.hasBlockBody() || function.bodyExpression == null) {
            return
        }
        if (function.isComponentDestructuringOperator()) {
            return
        }

        report(Finding(Entity.atName(function), "Use a block body { ... } instead of an expression body."))
    }

    private fun KtNamedFunction.isComponentDestructuringOperator(): Boolean =
        hasModifier(KtTokens.OPERATOR_KEYWORD) && name?.matches(Regex("component\\d+")) == true
}
