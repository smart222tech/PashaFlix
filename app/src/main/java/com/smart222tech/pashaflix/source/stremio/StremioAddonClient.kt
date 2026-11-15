package com.smart222tech.pashaflix.source.stremio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class StremioAddonManifest(
    val id: String,
    val name: String,
    val version: String,
    val types: Set<String>,
    val idPrefixes: List<String>,
    val resources: Set<String>
)

data class StremioStream(
    val name: String,
    val title: String,
    val url: String,
    val mimeType: String?,
    val requestHeaders: Map<String, String>,
    val subtitles: List<StremioSubtitle>
)

data class StremioSubtitle(
    val id: String?,
    val language: String,
    val url: String,
    val format: String?
)

/** Minimal Stremio add-on protocol client. Direct HTTPS streams only. */
class StremioAddonClient(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 12_000
) {
    suspend fun getManifest(manifestUrl: String): StremioAddonManifest = withContext(Dispatchers.IO) {
        val json = JSONObject(getHttps(normalizeManifestUrl(manifestUrl)))
        StremioAddonManifest(
            id = json.optString("id"),
            name = json.optString("name", "Stremio Add-on"),
            version = json.optString("version"),
            types = json.optJSONArray("types").toStringSet(),
            idPrefixes = json.optJSONArray("idPrefixes").toStringList(),
            resources = json.optJSONArray("resources").toStringSet()
        )
    }

    suspend fun getMovieStreams(addonBaseUrl: String, videoId: String): List<StremioStream> =
        getStreams(addonBaseUrl, "movie", videoId)

    suspend fun getEpisodeStreams(
        addonBaseUrl: String,
        seriesId: String,
        season: Int,
        episode: Int
    ): List<StremioStream> = getStreams(addonBaseUrl, "series", "$seriesId:$season:$episode")

    private suspend fun getStreams(baseUrl: String, type: String, id: String): List<StremioStream> =
        withContext(Dispatchers.IO) {
            require(type == "movie" || type == "series") { "Unsupported Stremio type" }
            val base = normalizeBaseUrl(baseUrl)
            val encodedId = URLEncoder.encode(id, Charsets.UTF_8.name()).replace("+", "%20").replace("%3A", ":")
            val json = JSONObject(getHttps("$base/stream/$type/$encodedId.json"))
            val streams = json.optJSONArray("streams") ?: return@withContext emptyList()
            buildList {
                for (index in 0 until streams.length()) {
                    val item = streams.optJSONObject(index) ?: continue
                    val mediaUrl = item.optString("url").trim()
                    if (!mediaUrl.startsWith("https://", ignoreCase = true)) continue
                    val hints = item.optJSONObject("behaviorHints")
                    val proxyHeaders = hints?.optJSONObject("proxyHeaders")?.optJSONObject("request")
                    val headers = linkedMapOf<String, String>()
                    proxyHeaders?.keys()?.forEach { key ->
                        val value = proxyHeaders.optString(key)
                        if (key.length <= 100 && value.length <= 2_000) headers[key] = value
                    }
                    val subtitleArray = item.optJSONArray("subtitles")
                    val subtitles = buildList {
                        if (subtitleArray != null) {
                            for (subIndex in 0 until subtitleArray.length()) {
                                val sub = subtitleArray.optJSONObject(subIndex) ?: continue
                                val subUrl = sub.optString("url")
                                if (!subUrl.startsWith("https://", ignoreCase = true)) continue
                                add(StremioSubtitle(
                                    id = sub.optString("id").takeIf { it.isNotBlank() },
                                    language = sub.optString("lang", "und"),
                                    url = subUrl,
                                    format = sub.optString("type").takeIf { it.isNotBlank() }
                                ))
                            }
                        }
                    }
                    add(StremioStream(
                        name = item.optString("name"),
                        title = item.optString("title"),
                        url = mediaUrl,
                        mimeType = null,
                        requestHeaders = headers,
                        subtitles = subtitles
                    ))
                }
            }
        }

    private fun normalizeManifestUrl(raw: String): String {
        val trimmed = raw.trim()
        require(trimmed.startsWith("https://", true)) { "Stremio add-ons must use HTTPS" }
        return if (trimmed.endsWith("/manifest.json")) trimmed else trimmed.trimEnd('/') + "/manifest.json"
    }

    private fun normalizeBaseUrl(raw: String): String {
        val trimmed = raw.trim().removeSuffix("/manifest.json").trimEnd('/')
        require(trimmed.startsWith("https://", true)) { "Stremio add-ons must use HTTPS" }
        return trimmed
    }

    private fun getHttps(rawUrl: String): String {
        val connection = (URL(rawUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            instanceFollowRedirects = true
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "PashaFlix/1.0 StremioClient")
        }
        try {
            val status = connection.responseCode
            require(connection.url.protocol.equals("https", ignoreCase = true)) { "Add-on redirected away from HTTPS" }
            require(status in 200..299) { "Add-on returned HTTP $status" }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            require(body.length <= 2_000_000) { "Add-on response is too large" }
            return body
        } finally {
            connection.disconnect()
        }
    }

    private fun org.json.JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).map { optString(it) }.filter { it.isNotBlank() }
    }

    private fun org.json.JSONArray?.toStringSet(): Set<String> = toStringList().toSet()
}
