package io.thernal.firebasekit.firebase.messaging.wiring

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessageStream
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessaging
import io.thernal.firebasekit.firebase.messaging.impl.data.platformPushMessaging
import io.thernal.firebasekit.firebase.messaging.impl.data.pushMessageStream

/** Binds [PushMessaging] and [PushMessageStream]. */
@BindingContainer
@ContributesTo(AppScope::class)
interface MessagingWiring {
    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun providePushMessaging(): PushMessaging {
            return platformPushMessaging()
        }

        @Provides
        fun providePushMessageStream(): PushMessageStream {
            return pushMessageStream()
        }
    }
}
