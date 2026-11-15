package com.smart222tech.pashaflix.library

import android.content.Context
import com.smart222tech.pashaflix.catalog.CatalogKind
import com.smart222tech.pashaflix.catalog.CatalogTitle
import org.json.JSONArray
import org.json.JSONObject

class LibraryStore(context: Context) {
    private val prefs = context.getSharedPreferences("pashaflix_library", Context.MODE_PRIVATE)

    fun favorites(): List<CatalogTitle> = read("favorites")
    fun history(): List<CatalogTitle> = read("history")

    fun isFavorite(item: CatalogTitle): Boolean = favorites().any { it.pageUrl == item.pageUrl }

    fun toggleFavorite(item: CatalogTitle): Boolean {
        val items = favorites().toMutableList()
        val exists = items.any { it.pageUrl == item.pageUrl }
        if (exists) items.removeAll { it.pageUrl == item.pageUrl } else items.add(0, item)
        write("favorites", items)
        return !exists
    }

    fun addToHistory(item: CatalogTitle) {
        val items = history().filterNot { it.pageUrl == item.pageUrl }.toMutableList()
        items.add(0, item)
        write("history", items.take(100))
    }

    private fun read(key: String): List<CatalogTitle> {
        val array = runCatching { JSONArray(prefs.getString(key, "[]")) }.getOrNull() ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val value = array.optJSONObject(index) ?: continue
                val kind = runCatching { CatalogKind.valueOf(value.optString("kind")) }.getOrNull() ?: continue
                val title = value.optString("title")
                val url = value.optString("url")
                if (title.isNotBlank() && url.startsWith("https://")) {
                    add(CatalogTitle(title, url, value.optString("poster").takeIf(String::isNotBlank), kind, value.optString("source")))
                }
            }
        }
    }

    private fun write(key: String, items: List<CatalogTitle>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().apply {
                put("title", item.title)
                put("url", item.pageUrl)
                put("poster", item.posterUrl ?: "")
                put("kind", item.kind.name)
                put("source", item.sourceName)
            })
        }
        prefs.edit().putString(key, array.toString()).apply()
    }
}
