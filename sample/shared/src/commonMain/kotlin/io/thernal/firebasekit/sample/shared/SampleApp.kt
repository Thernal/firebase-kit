package io.thernal.firebasekit.sample.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.zacsweers.metro.createGraph
import io.thernal.firebasekit.firebase.auth.api.data.SignInResult
import io.thernal.firebasekit.firebase.config.api.domain.ConfigKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val MAINTENANCE = ConfigKey(key = "is_in_maintenance", default = false)

@Composable
fun SampleApp() {
    val graph = remember { createGraph<SampleGraph>() }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SampleScreen(graph)
        }
    }
}

@Composable
private fun SampleScreen(graph: SampleGraph) {
    val scope = rememberCoroutineScope()
    val events = remember { mutableStateListOf<String>() }
    var token by remember { mutableStateOf("—") }
    val greeting by graph.remoteConfig.observe(
        SampleFlags.GREETING,
    ).collectAsState(initial = SampleFlags.GREETING.default)
    LaunchedEffect(graph) {
        launch {
            graph.pushMessages.messages.collect { message ->
                events.add(
                    index = 0,
                    element = "received: ${message.title.orEmpty()} ${message.data}",
                )
            }
        }
        launch {
            graph.pushMessages.opened.collect { message ->
                events.add(
                    index = 0,
                    element = "opened: ${message.deepLink ?: message.data}",
                )
            }
        }
        launch { graph.pushMessaging.tokens.collect { token = it } }
    }
    Column(
        modifier = Modifier.safeDrawingPadding().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "firebase-kit", style = MaterialTheme.typography.headlineSmall)
        Section(title = "Remote config") {
            Text(text = "greeting: $greeting")
            Text(text = "maintenance: ${graph.remoteConfig.get(MAINTENANCE)}")
            Button(
                onClick = {
                    scope.launch {
                        events.add(
                            index = 0,
                            element = "fetch: ${graph.remoteConfig.fetchAndActivate()}",
                        )
                    }
                },
            ) {
                Text(text = "Fetch and activate")
            }
        }
        Section(title = "Push") {
            Text(text = "token: $token")
            OutlinedButton(
                onClick = {
                    scope.launch {
                        events.add(
                            index = 0,
                            element = attempt(label = "subscribe") { graph.pushMessaging.subscribe("sample") },
                        )
                    }
                },
            ) {
                Text(text = "Subscribe to \"sample\"")
            }
        }
        Section(title = "Sign-in") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            events.add(
                                index = 0,
                                element = graph.signIn.signInWithGoogle().describe("Google"),
                            )
                        }
                    },
                ) {
                    Text(text = "Google")
                }
                Button(
                    onClick = {
                        scope.launch {
                            events.add(
                                index = 0,
                                element = graph.signIn.signInWithApple().describe("Apple"),
                            )
                        }
                    },
                ) {
                    Text(text = "Apple")
                }
                OutlinedButton(onClick = { scope.launch { graph.signIn.signOut() } }) { Text(text = "Sign out") }
            }
        }
        Section(title = "Crash reporting") {
            OutlinedButton(
                onClick = {
                    graph.crashReporter.log("sample: reporting a test error")
                    graph.crashReporter.recordException(IllegalStateException("A test non-fatal from the sample"))
                    events.add(index = 0, element = "recorded a non-fatal")
                },
            ) {
                Text(text = "Record a non-fatal")
            }
        }
        HorizontalDivider()
        events.forEach { Text(text = it, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

private suspend fun attempt(
    label: String,
    action: suspend () -> Unit,
): String {
    return try {
        action()
        "$label: done"
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (
        // The sample shows why it failed — usually no GoogleService file in this build.
        @Suppress("TooGenericExceptionCaught") failure: Exception,
    ) {
        "$label failed: ${failure.message.orEmpty()}"
    }
}

private fun SignInResult.describe(provider: String): String {
    return when (this) {
        is SignInResult.Success -> "$provider: signed in (${idToken.take(TOKEN_PREVIEW)}…)"
        SignInResult.Cancelled -> "$provider: cancelled"
        is SignInResult.Failed -> "$provider: $message"
    }
}

private const val TOKEN_PREVIEW = 12
