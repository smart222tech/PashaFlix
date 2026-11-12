package com.smart222tech.pashaflix.player

import android.net.Uri

/**
 * Media3 hand-off gate. Hosts stay unapproved until external playback is
 * explicitly authorized by the content provider. Browser playback is fallback.
 */
class ExternalPlaybackPolicy(
    private val approvedHosts: Set<String> = emptySet()
) {
    fun mayUseExternalPlayer(mediaUrl: String): Boolean {
        val uri = runCatching { Uri.parse(mediaUrl) }.getOrNull() ?: return false
        if (uri.scheme != "https") return false
        val host = uri.host?.lowercase() ?: return false
        return approvedHosts.any { host == it || host.endsWith(".$it") }
    }
}
