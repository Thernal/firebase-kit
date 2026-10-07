package io.thernal.firebasekit.detektrules

import dev.detekt.api.Config
import dev.detekt.test.lint
import io.thernal.firebasekit.detektrules.packageboundary.LayerPackageRequired
import org.junit.Assert.assertEquals
import org.junit.Test

class LayerPackageRequiredTest {
    private val rule = LayerPackageRequired(Config.empty)

    @Test
    fun `reports a file in the root package of an impl module`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.features.profile.impl

            class BackStackNavigator
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `reports a file in the root package of an api module`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.features.profile.api

            interface Navigator
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `reports a package that is not a layer`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.features.profile.api.deeplink

            class DeepLink
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `allows each layer package and its topical sub packages`() {
        val sources = listOf(
            "io.thernal.firebasekit.features.profile.api.domain",
            "io.thernal.firebasekit.features.profile.api.presentation.navigator",
            "io.thernal.firebasekit.features.profile.impl.data",
            "io.thernal.firebasekit.features.profile.impl.domain.deeplink",
            "io.thernal.firebasekit.features.profile.impl.presentation.scene",
        )

        sources.forEach { packageName ->
            assertEquals(0, rule.lint("package $packageName").size)
        }
    }

    @Test
    fun `ignores wiring build-logic and non-module packages`() {
        val sources = listOf(
            "io.thernal.firebasekit.features.profile.wiring",
            "io.thernal.firebasekit.buildlogic",
            "io.thernal.firebasekit.detektrules.style",
        )

        sources.forEach { packageName ->
            assertEquals(0, rule.lint("package $packageName").size)
        }
    }

    @Test
    fun `ignores a segment that only starts with a module name`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.implementation.detail

            class Detail
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `accepts a module named after its layer holding code directly`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.core.presentation.api.plugin.state

            interface StateHandler
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `reports a layer package repeated inside a module named after it`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.core.presentation.api.presentation.plugin

            interface PluginContext
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `reports another layer package inside a module named after a layer`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.core.presentation.impl.domain

            class Mapper
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }
}
