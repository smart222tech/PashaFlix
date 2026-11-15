package com.smart222tech.pashaflix.source.repository

import com.smart222tech.pashaflix.source.model.SourceEngine
import com.smart222tech.pashaflix.source.model.SourceExtension
import com.smart222tech.pashaflix.source.model.SourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Loads declarative JSON repositories. Remote JSON is data, never executable code. */
class ExtensionRepositoryClient(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 10_000,
    private val maxDocuments: Int = 12
) {
    suspend fun load(repositoryUrl: String): SourceRepository = withContext(Dispatchers.IO) {
        val visited = linkedSetOf<String>()
        val allExtensions = linkedMapOf<String, SourceExtension>()
        var root: SourceRepository? = null

        suspend fun visit(rawUrl: String, isRoot: Boolean) {
            if (visited.size >= maxDocuments) return
            val normalized = normalizeRepositoryUrl(rawUrl)
            if (!visited.add(normalized)) return
            val rawDocument = readHttps(normalized)
            val parsed = parseDocument(rawDocument, "External repository")
            val isStremioManifest = runCatching {
                val manifest = JSONObject(rawDocument)
                manifest.has("resources") && manifest.has("types")
            }.getOrDefault(false)
            val resolved = if (isStremioManifest) parsed.copy(
                extensions = parsed.extensions.map { extension ->
                    extension.copy(engine = extension.engine.copy(endpoint = normalized.removeSuffix("/manifest.json")))
                }
            ) else parsed
            if (isRoot) root = resolved
            resolved.extensions.forEach { allExtensions[it.id] = it }
            parsed.extensionLists.forEach { child ->
                if (child.isNotBlank()) visit(child, false)
            }
        }

        visit(repositoryUrl, true)
        val header = root ?: SourceRepository("External repository")
        header.copy(
            extensionLists = visited.toList().drop(1),
            extensions = allExtensions.values.sortedBy { it.engine.priority }
        )
    }

    fun parseDocument(raw: String, fallbackName: String = "PashaFlix repository"): SourceRepository {
        val objectJson = runCatching { JSONObject(raw) }.getOrNull()
        val arrayJson = if (objectJson == null) runCatching { JSONArray(raw) }.getOrNull() else null
        val entries = objectJson?.optJSONArray("extensions") ?: arrayJson ?: JSONArray()
        val extensions = linkedMapOf<String, SourceExtension>()
        for (index in 0 until entries.length()) {
            val entry = entries.optJSONObject(index) ?: continue
            parseExtension(entry)?.let { extensions[it.id] = it }
        }
        if (objectJson != null && objectJson.has("resources") && objectJson.has("types")) {
            parseStremioManifest(objectJson)?.let { extensions[it.id] = it }
        }
        val lists = objectJson?.optJSONArray("extensionLists")
        val childUrls = if (lists == null) emptyList() else
            (0 until lists.length()).map { lists.optString(it) }.filter { it.isNotBlank() }
        return SourceRepository(
            name = objectJson?.optString("name")?.takeIf { it.isNotBlank() } ?: fallbackName,
            description = objectJson?.optString("description") ?: "",
            website = objectJson?.optString("website")?.takeIf { it.isNotBlank() },
            extensionLists = childUrls,
            extensions = extensions.values.sortedBy { it.engine.priority }
        )
    }

    private fun parseStremioManifest(json: JSONObject): SourceExtension? {
        val id = json.optString("id").trim()
        val name = json.optString("name").trim()
        val resources = json.optJSONArray("resources") ?: return null
        val supportsStream = (0 until resources.length()).any { index ->
            val value = resources.opt(index)
            value == "stream" || (value is JSONObject && value.optString("name") == "stream")
        }
        if (id.isBlank() || name.isBlank() || !supportsStream) return null
        return SourceExtension(
            id = id.replace(Regex("[^a-zA-Z0-9._-]"), "-").take(80),
            name = name,
            description = "Stremio add-on",
            version = json.optString("version", "1.0.0"),
            apiVersion = API_VERSION,
            language = "Multi",
            status = 3,
            engine = SourceEngine(type = "stremio", endpoint = null, priority = 100)
        )
    }

    private fun parseExtension(json: JSONObject): SourceExtension? {
        val id = json.optString("id").trim()
        val name = json.optString("name").trim()
        val engineJson = json.optJSONObject("engine") ?: return null
        val engineType = engineJson.optString("type").trim()
        if (!id.matches(Regex("[a-zA-Z0-9._-]{2,80}")) || name.isBlank() || engineType.isBlank()) return null
        if (engineType !in SUPPORTED_ENGINE_TYPES) return null
        val endpoint = engineJson.optString("endpoint").takeIf { it.isNotBlank() }
        if (endpoint != null && endpoint.startsWith("http://", ignoreCase = true)) return null

        return SourceExtension(
            id = id,
            name = name,
            description = json.optString("description"),
            version = json.optString("version", "1.0.0"),
            versionCode = json.optInt("versionCode", 1),
            apiVersion = json.optInt("apiVersion", 1),
            authors = json.optStringArray("authors"),
            language = json.optString("language", "Multi"),
            tags = json.optStringArray("tags"),
            status = json.optInt("status", 3),
            installedByDefault = json.optBoolean("installedByDefault", false),
            engine = SourceEngine(engineType, endpoint, engineJson.optInt("priority", 100))
        )
    }

    private fun JSONObject.optStringArray(key: String): List<String> {
        val array = optJSONArray(key) ?: return emptyList()
        return (0 until array.length()).map { array.optString(it) }.filter { it.isNotBlank() }
    }

    private fun normalizeRepositoryUrl(raw: String): String {
        val trimmed = raw.trim()
        require(trimmed.startsWith("https://", ignoreCase = true)) { "Repository must use HTTPS" }
        require(URL(trimmed).host.isNotBlank()) { "Invalid repository URL" }
        return trimmed
    }

    private fun readHttps(rawUrl: String): String {
        val connection = (URL(rawUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            instanceFollowRedirects = true
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "PashaFlix/1.0")
        }
        try {
            val status = connection.responseCode
            require(connection.url.protocol.equals("https", ignoreCase = true)) { "Repository redirected away from HTTPS" }
            require(status in 200..299) { "Repository returned HTTP $status" }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            require(body.length <= MAX_DOCUMENT_CHARS) { "Repository document is too large" }
            return body
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val API_VERSION = 1
        private const val MAX_DOCUMENT_CHARS = 1_500_000
        private val SUPPORTED_ENGINE_TYPES = setOf("catalog-only", "stremio")
    }
}
