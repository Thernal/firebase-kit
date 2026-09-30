package io.thernal.firebasekit.detektrules

import dev.detekt.api.Config
import dev.detekt.test.lint
import io.thernal.firebasekit.detektrules.style.ExpressionBodyNotAllowed
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpressionBodyNotAllowedTest {
    private val rule = ExpressionBodyNotAllowed(Config.empty)

    @Test
    fun `reports a function with an expression body`() {
        val findings = rule.lint(
            """
            fun square(value: Int) = value * value
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `reports an expression body with a declared return type`() {
        val findings = rule.lint(
            """
            fun square(value: Int): Int = value * value
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `allows a block body`() {
        val findings = rule.lint(
            """
            fun square(value: Int): Int {
                return value * value
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `allows a function without a body`() {
        val findings = rule.lint(
            """
            interface Calculator {
                fun square(value: Int): Int
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `allows a component destructuring operator with an expression body`() {
        val findings = rule.lint(
            """
            class Point(val x: Int, val y: Int) {
                operator fun component1() = x
                operator fun component2() = y
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `allows a property accessor with an expression body`() {
        val findings = rule.lint(
            """
            class Box(val value: Int) {
                val doubled: Int
                    get() = value * 2
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }
}
