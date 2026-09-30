package io.thernal.firebasekit.detektrules

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider
import io.thernal.firebasekit.detektrules.collections.UnsafeCollectionIndexAccess
import io.thernal.firebasekit.detektrules.packageboundary.LayerPackageBoundary
import io.thernal.firebasekit.detektrules.packageboundary.LayerPackageRequired
import io.thernal.firebasekit.detektrules.preview.PreviewMustBePrivate
import io.thernal.firebasekit.detektrules.style.ExpressionBodyNotAllowed
import io.thernal.firebasekit.detektrules.style.MultilineConstructorRequired

class ProjectRuleSetProvider : RuleSetProvider {
    override val ruleSetId = RuleSetId("project")

    override fun instance() = RuleSet(
        ruleSetId,
        listOf(
            ::PreviewMustBePrivate,
            ::UnsafeCollectionIndexAccess,
            ::LayerPackageBoundary,
            ::LayerPackageRequired,
            ::ExpressionBodyNotAllowed,
            ::MultilineConstructorRequired,
        ),
    )
}
