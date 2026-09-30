package io.thernal.firebasekit.detektrules

import dev.detekt.api.Config
import dev.detekt.test.lint
import io.thernal.firebasekit.detektrules.style.MultilineConstructorRequired
import org.junit.Assert.assertEquals
import org.junit.Test

class MultilineConstructorRequiredTest {
    private val rule = MultilineConstructorRequired(Config.empty)

    @Test
    fun `reports two parameters on the same line`() {
        val findings = rule.lint(
            """
            class Point(val x: Int, val y: Int)
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `reports three parameters where two share a line`() {
        val findings = rule.lint(
            """
            class Point(
                val x: Int, val y: Int,
                val z: Int,
            )
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `allows two parameters each on their own line`() {
        val findings = rule.lint(
            """
            class Point(
                val x: Int,
                val y: Int,
            )
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `allows a single parameter on the class line`() {
        val findings = rule.lint(
            """
            class Box(val value: Int)
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `allows a single parameter on its own line`() {
        val findings = rule.lint(
            """
            enum class SlideDirection(
                val axis: Int,
            ) {
                LEFT(0),
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `allows a class without a primary constructor`() {
        val findings = rule.lint(
            """
            class NoConstructor
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }
}
