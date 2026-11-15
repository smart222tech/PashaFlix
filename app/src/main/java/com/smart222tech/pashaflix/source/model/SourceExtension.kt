package com.smart222tech.pashaflix.source.model

data class SourceRepository(
    val name: String,
    val description: String = "",
    val website: String? = null,
    val extensionLists: List<String> = emptyList(),
    val extensions: List<SourceExtension> = emptyList()
)

data class SourceExtension(
    val id: String,
    val name: String,
    val description: String = "",
    val version: String = "1.0.0",
    val versionCode: Int = 1,
    val apiVersion: Int = 1,
    val authors: List<String> = emptyList(),
    val language: String = "Multi",
    val tags: List<String> = emptyList(),
    val status: Int = 3,
    val installedByDefault: Boolean = false,
    val engine: SourceEngine
)

data class SourceEngine(
    val type: String,
    val endpoint: String? = null,
    val priority: Int = 100
)

enum class SourceHealth(val code: Int) {
    DOWN(0), ONLINE(1), SLOW(2), BETA(3);

    companion object {
        fun fromCode(code: Int): SourceHealth =
            entries.firstOrNull { it.code == code } ?: BETA
    }
}
