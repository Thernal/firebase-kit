package io.thernal.firebasekit.firebase.testing

import io.thernal.firebasekit.firebase.auth.api.data.SignInResult
import io.thernal.firebasekit.firebase.config.api.domain.ConfigKey
import io.thernal.firebasekit.firebase.messaging.api.data.pushMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FakesTest {
    @Test
    fun `the crash reporter records`() {
        val reporter = FakeCrashReporter()

        reporter.setUserId("u1")
        reporter.recordException(IllegalStateException("x"))

        assertEquals("u1", reporter.userId)
        assertEquals("x", reporter.exceptions.single().message)
    }

    @Test
    fun `test remote config serves the given values and defaults for the rest`() {
        val config = testRemoteConfig("limit" to "5")

        assertEquals(5, config.get(ConfigKey(key = "limit", default = 1)))
        assertEquals("d", config.get(ConfigKey(key = "other", default = "d")))
    }

    @Test
    fun `push fakes deliver what the test sends`() {
        runTest {
            val messaging = FakePushMessaging()
            val stream = FakePushMessageStream()
            val message = pushMessage(title = "t", body = null, data = emptyMap())

            messaging.newToken("t2")
            messaging.subscribe("news")
            stream.open(message)

            assertEquals("t2", messaging.tokens.first())
            assertEquals(setOf("news"), messaging.topics)
            assertEquals(message, stream.opened.first())
        }
    }

    @Test
    fun `sign-in answers as told and records the provider`() {
        runTest {
            val signIn = FakeSocialSignIn(answer = { SignInResult.Cancelled })

            assertEquals(SignInResult.Cancelled, signIn.signInWithApple())
            assertEquals(listOf(FakeSocialSignIn.Provider.Apple), signIn.attempts)
        }
    }
}
