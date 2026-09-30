package io.thernal.firebasekit.detektrules.collections

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtArrayAccessExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression

class UnsafeCollectionIndexAccess(config: Config) : RequiresAnalysisApi, Rule(
    config = config,
    description = "List and array index access can throw; use getOrNull when bounds are not proven.",
) {
    override fun visitArrayAccessExpression(expression: KtArrayAccessExpression) {
        super.visitArrayAccessExpression(expression)

        if (expression.indexExpressions.size != 1) {
            return
        }
        val receiver = expression.arrayExpression ?: return

        if (isListOrArray(receiver)) {
            report(Finding(Entity.from(expression), "Use getOrNull(index)."))
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        if (expression.calleeExpression?.text != "get" || expression.valueArguments.size != 1) {
            return
        }
        val receiver = (expression.parent as? KtDotQualifiedExpression)?.receiverExpression ?: return

        if (isListOrArray(receiver)) {
            report(Finding(Entity.from(expression), "Use getOrNull(index)."))
        }
    }

    private fun isListOrArray(receiver: KtExpression): Boolean = analyze(receiver) {
        val type = receiver.expressionType ?: return@analyze false
        type.isArrayOrPrimitiveArray || type.isSubtypeOf(StandardClassIds.List)
    }
}
