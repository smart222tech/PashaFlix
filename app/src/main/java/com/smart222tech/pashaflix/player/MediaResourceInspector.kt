package com.smart222tech.pashaflix.player

import android.net.Uri

enum class MediaKind { HLS, DASH, MP4, TRANSPORT_STREAM, MEDIA_SEGMENT }

data class MediaCandidate(
    val kind: MediaKind,
    val host: String,
    val pathHint: String
)

/**
 * Diagnostic classifier for media-shaped WebView requests.
 * It never persists complete third-party media URLs or request credentials.
 */
object MediaResourceInspector {
    private val extensions = mapOf(
        "m3u8" to MediaKind.HLS,
        "mpd" to MediaKind.DASH,
        "mp4" to MediaKind.MP4,
        "m4v" to MediaKind.MP4,
        "ts" to MediaKind.TRANSPORT_STREAM,
        "m4s" to MediaKind.MEDIA_SEGMENT
    )

    fun inspect(requestUrl: String): MediaCandidate? {
        val uri = runCatching { Uri.parse(requestUrl) }.getOrNull() ?: return null
        if (uri.scheme !in setOf("http", "https")) return null
        val extension = uri.lastPathSegment
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?: return null
        val kind = extensions[extension] ?: return null
        return MediaCandidate(kind, uri.host.orEmpty(), uri.lastPathSegment.orEmpty().take(48))
    }
}
