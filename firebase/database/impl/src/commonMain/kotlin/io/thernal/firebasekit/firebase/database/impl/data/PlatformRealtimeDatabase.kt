package io.thernal.firebasekit.firebase.database.impl.data

import io.thernal.firebasekit.firebase.database.api.data.RealtimeDatabase

/** The database for the platform this is compiled for; `wiring` binds it. */
expect fun platformRealtimeDatabase(): RealtimeDatabase
