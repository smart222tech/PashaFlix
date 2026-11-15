package com.smart222tech.pashaflix.source.repository

import android.content.Context

/** Persists user-added repository URLs. */
class InstalledRepositoryStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "pashaflix_source_repositories", Context.MODE_PRIVATE
    )

    fun getAll(): List<String> =
        preferences.getStringSet(KEY_URLS, emptySet()).orEmpty().toList().sorted()

    fun add(url: String) {
        require(url.trim().startsWith("https://", ignoreCase = true)) {
            "Only HTTPS repositories are supported"
        }
        val updated = getAll().toMutableSet().apply { add(url.trim()) }
        preferences.edit().putStringSet(KEY_URLS, updated).apply()
    }

    fun remove(url: String) {
        val updated = getAll().toMutableSet().apply { remove(url) }
        preferences.edit().putStringSet(KEY_URLS, updated).apply()
    }

    companion object {
        private const val KEY_URLS = "repository_urls"
    }
}
