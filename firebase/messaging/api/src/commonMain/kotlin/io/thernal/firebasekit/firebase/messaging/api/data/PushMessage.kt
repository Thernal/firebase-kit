package io.thernal.firebasekit.firebase.messaging.api.data

/**
 * A push message, platform types left behind. [data] is the message's custom key-value payload;
 * [deepLink] is its `deepLink` data key, else the notification's link — where a tap should lead.
 */
data class PushMessage(
    val title: String?,
    val body: String?,
    val data: Map<String, String>,
    val deepLink: String?,
)

/** Builds a [PushMessage]; the `deepLink` data key wins over the notification's own link. */
fun pushMessage(
    title: String?,
    body: String?,
    data: Map<String, String>,
    link: String? = null,
): PushMessage {
    return PushMessage(
        title = title?.takeIf(String::isNotEmpty),
        body = body?.takeIf(String::isNotEmpty),
        data = data,
        deepLink = data[DEEP_LINK_KEY] ?: link,
    )
}

/** The data key a message's destination travels in. */
const val DEEP_LINK_KEY = "deepLink"
