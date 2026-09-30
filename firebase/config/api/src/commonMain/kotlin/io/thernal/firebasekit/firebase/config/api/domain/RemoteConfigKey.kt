package io.thernal.firebasekit.firebase.config.api.domain

/**
 * A remote flag a feature owns, with the value it has until the server says otherwise. Implement it
 * on the feature's own enum — the property is `key`, not `name`, because `Enum.name` is final:
 *
 * ```
 * enum class HomeFlags(override val key: String, override val default: Boolean) : RemoteConfigKey<Boolean> {
 *     SHOW_PROMO_BANNER("show_promo_banner", default = false),
 * }
 * ```
 *
 * `T` is `String`, `Boolean`, `Int`, `Long` or `Double`; a value the server sends that does not parse
 * as `T` reads as [default].
 */
interface RemoteConfigKey<T : Any> {
    val key: String
    val default: T
}

/** A one-off or mixed-type key, for a caller with no enum to put it on. */
data class ConfigKey<T : Any>(
    override val key: String,
    override val default: T,
) : RemoteConfigKey<T>
