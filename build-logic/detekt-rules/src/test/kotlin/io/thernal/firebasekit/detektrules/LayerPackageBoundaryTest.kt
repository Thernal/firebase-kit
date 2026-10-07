package io.thernal.firebasekit.detektrules

import dev.detekt.api.Config
import dev.detekt.test.lint
import io.thernal.firebasekit.detektrules.packageboundary.LayerPackageBoundary
import org.junit.Assert.assertEquals
import org.junit.Test

class LayerPackageBoundaryTest {
    private val rule = LayerPackageBoundary(Config.empty)

    @Test
    fun `reports data imports from presentation and allows domain`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.feature.impl.data

            import io.thernal.firebasekit.feature.impl.domain.parser.FeedParser
            import io.thernal.firebasekit.feature.impl.presentation.feed.FeedView
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `reports presentation imports from data and allows domain`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.feature.impl.presentation.feed

            import io.thernal.firebasekit.feature.impl.data.RemoteFeedSource
            import io.thernal.firebasekit.feature.impl.domain.feed.FeedLoader
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `reports domain imports from data and presentation`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.feature.impl.domain.feed

            import io.thernal.firebasekit.feature.api.presentation.model.FeedItem
            import io.thernal.firebasekit.feature.impl.data.RemoteFeedSource
            import io.thernal.firebasekit.feature.impl.presentation.feed.FeedView
            """.trimIndent(),
        )

        assertEquals(2, findings.size)
    }

    @Test
    fun `allows presentation to name a domain type inside an api module`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.feature.api.presentation.feed

            import io.thernal.firebasekit.feature.api.domain.FeedSource
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `ignores the api module of the same capability`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.feature.impl.domain.feed

            import io.thernal.firebasekit.feature.api.data.FeedService
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `ignores another module and non layered packages`() {
        val findings = rule.lint(
            """
            package io.thernal.firebasekit.feature.impl.data

            import io.thernal.firebasekit.session.impl.presentation.SessionState
            import io.thernal.firebasekit.feature.wiring.FeedProvidersModule
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }
}
