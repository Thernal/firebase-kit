package io.thernal.firebasekit.firebase.config.impl.domain

/** [raw] as the type of [default]; [default] when absent or unparsable. */
@Suppress("UNCHECKED_CAST")
fun <T : Any> decodeConfigValue(
    raw: String?,
    default: T,
): T {
    if (raw == null) {
        return default
    }
    val value: Any? = when (default) {
        is String -> raw
        is Boolean -> raw.trim().lowercase().toBooleanStrictOrNull()
        is Int -> raw.trim().toIntOrNull()
        is Long -> raw.trim().toLongOrNull()
        is Double -> raw.trim().toDoubleOrNull()
        else -> null
    }
    return (value ?: default) as T
}
