package io.thernal.firebasekit.firebase.config.impl.domain

import io.thernal.firebasekit.firebase.config.api.domain.ConfigKey
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigKey
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TestTimeSource

private enum class Flags(
    override val key: String,
    override val default: Boolean,
) : RemoteConfigKey<Boolean> {
    PROMO("show_promo", default = false),
}

private val MIN_BUILD = ConfigKey(key = "min_build", default = 0)
private val GREETING = ConfigKey(key = "greeting", default = "hi")

private class FakeSource(
    var activated: Map<String, String> = emptyMap(),
    var next: Map<String, String> = emptyMap(),
    var isFailing: Boolean = false,
) : RemoteConfigSource {
    var fetches = 0

    override fun activated(): Map<String, String> {
        return activated
    }

    override suspend fun fetch(): Map<String, String> {
        fetches++
        if (isFailing) {
            error("offline")
        }
        return next
    }
}

class RemoteConfigImplTest {
    @Test
    fun `a key reads its default before anything is fetched`() {
        val config = RemoteConfigImpl(FakeSource())

        assertFalse(config.get(Flags.PROMO))
        assertEquals("hi", config.get(GREETING))
    }

    @Test
    fun `values activated on an earlier launch are read at once`() {
        val config = RemoteConfigImpl(FakeSource(activated = mapOf("show_promo" to "true", "min_build" to "42")))

        assertTrue(config.get(Flags.PROMO))
        assertEquals(42, config.get(MIN_BUILD))
    }

    @Test
    fun `a fetch replaces the values and observers see the change`() {
        runTest {
            val source = FakeSource(next = mapOf("greeting" to "hello"))
            val config = RemoteConfigImpl(source)
            val seen = mutableListOf<String>()
            val job = launch { config.observe(GREETING).take(2).toList(seen) }
            runCurrent()

            assertTrue(config.fetchAndActivate())
            job.join()

            assertEquals(listOf("hi", "hello"), seen)
        }
    }

    @Test
    fun `a failed fetch keeps what was activated`() {
        runTest {
            val source = FakeSource(activated = mapOf("show_promo" to "true"), isFailing = true)
            val config = RemoteConfigImpl(source)

            assertFalse(config.fetchAndActivate())

            assertTrue(config.get(Flags.PROMO))
        }
    }

    @Test
    fun `fetches closer than the minimum interval are skipped`() {
        runTest {
            val time = TestTimeSource()
            val source = FakeSource()
            val config = RemoteConfigImpl(source = source, minimumFetchInterval = 10.minutes, timeSource = time)

            assertTrue(config.fetchAndActivate())
            assertFalse(config.fetchAndActivate())
            time += 11.minutes
            assertTrue(config.fetchAndActivate())

            assertEquals(2, source.fetches)
        }
    }

    @Test
    fun `a failed fetch does not start the interval`() {
        runTest {
            val source = FakeSource(isFailing = true)
            val config = RemoteConfigImpl(
                source = source,
                minimumFetchInterval = 10.minutes,
                timeSource = TestTimeSource(),
            )

            config.fetchAndActivate()
            source.isFailing = false

            assertTrue(config.fetchAndActivate())
        }
    }

    @Test
    fun `observing an unchanged key emits once`() {
        runTest {
            val source = FakeSource(next = mapOf("greeting" to "hi"))
            val config = RemoteConfigImpl(source)

            config.fetchAndActivate()

            assertEquals("hi", config.observe(GREETING).first())
        }
    }

    @Test
    fun `values decode to the default's type and fall back when they do not parse`() {
        assertEquals(true, decodeConfigValue(raw = " TRUE ", default = false))
        assertEquals(false, decodeConfigValue(raw = "yes", default = false))
        assertEquals(7L, decodeConfigValue(raw = "7", default = 0L))
        assertEquals(0, decodeConfigValue(raw = "7.5", default = 0))
        assertEquals(1.5, decodeConfigValue(raw = "1.5", default = 0.0))
        assertEquals("x", decodeConfigValue(raw = null, default = "x"))
    }
}
