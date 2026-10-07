package io.thernal.firebasekit.firebase.messaging.api.data

import kotlinx.coroutines.flow.Flow

/** Messages as they arrive, and the ones the user opens. */
interface PushMessageStream {
    /** Received while the app is running — the foreground; the system shows the rest. */
    val messages: Flow<PushMessage>

    /**
     * Notifications the user tapped. Kept until collected, so the tap that launched the app is not
     * lost before the first screen collects; collect it once, at the root.
     */
    val opened: Flow<PushMessage>
}
