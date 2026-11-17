package com.smart222tech.pashaflix.browser

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SavedSite(val name: String, val url: String)

class SiteStore(context: Context) {
    private val prefs = context.getSharedPreferences("pashaflix_sites", Context.MODE_PRIVATE)
    fun all(): List<SavedSite> {
        val a = runCatching { JSONArray(prefs.getString("items", "[]")) }.getOrNull() ?: JSONArray()
        return buildList {
            for (i in 0 until a.length()) {
                val o = a.optJSONObject(i) ?: continue
                val name = o.optString("name").trim()
                val url = o.optString("url").trim()
                if (name.isNotBlank() && (url.startsWith("https://") || url.startsWith("http://"))) add(SavedSite(name, url))
            }
        }
    }
    fun add(name: String, rawUrl: String): Boolean {
        val url = if (rawUrl.contains("://")) rawUrl.trim() else "https://${rawUrl.trim()}"
        val parsed = runCatching { android.net.Uri.parse(url) }.getOrNull() ?: return false
        if (parsed.scheme !in listOf("http", "https") || parsed.host.isNullOrBlank()) return false
        val items = all().filterNot { it.url.equals(url, true) }.toMutableList()
        items.add(0, SavedSite(name.trim(), url))
        val a = JSONArray()
        items.take(40).forEach { a.put(JSONObject().put("name", it.name).put("url", it.url)) }
        prefs.edit().putString("items", a.toString()).apply()
        return true
    }
    fun remove(site: SavedSite) {
        val a = JSONArray()
        all().filterNot { it.url == site.url }.forEach { a.put(JSONObject().put("name", it.name).put("url", it.url)) }
        prefs.edit().putString("items", a.toString()).apply()
    }
}
