package io.thernal.firebasekit.firebase.config.impl.data

import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigSource
import kotlin.time.Duration

/**
 * Firebase Remote Config as a [RemoteConfigSource]. [minimumFetchInterval] is Firebase's own throttle
 * (the console's fetch quota); [fetchTimeout] bounds one fetch.
 */
expect fun firebaseRemoteConfigSource(
    minimumFetchInterval: Duration,
    fetchTimeout: Duration,
): RemoteConfigSource
