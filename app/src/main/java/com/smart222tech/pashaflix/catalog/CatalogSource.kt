package com.smart222tech.pashaflix.catalog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

enum class CatalogKind { MOVIE, SERIES }

data class CatalogTitle(
    val title: String,
    val pageUrl: String,
    val posterUrl: String?,
    val kind: CatalogKind,
    val sourceName: String
)

data class CatalogEpisode(val title: String, val pageUrl: String, val seasonLabel: String? = null)

data class CatalogDetails(
    val title: String,
    val description: String?,
    val year: String?,
    val facts: List<String>,
    val relatedEpisodes: List<CatalogEpisode>
)

interface CatalogSource {
    val displayName: String
    suspend fun load(kind: CatalogKind): List<CatalogTitle>
}

interface DetailedCatalogSource : CatalogSource {
    suspend fun details(title: CatalogTitle): CatalogDetails
}

private fun parseListing(base: String, path: String, kind: CatalogKind, sourceName: String): List<CatalogTitle> {
    val document = Jsoup.connect(base + path)
        .userAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36")
        .timeout(20_000)
        .get()
    val segment = if (kind == CatalogKind.MOVIE) "/movies/" else "/tvseries/"
    val results = linkedMapOf<String, CatalogTitle>()
    document.select("a[href]").forEach { anchor ->
        val href = anchor.absUrl("href").substringBefore('#')
        if (!href.startsWith(base) || !href.contains(segment)) return@forEach
        val slug = href.substringAfter(segment).substringBefore('?').trim('/')
        if (slug.isBlank() || slug.contains('/')) return@forEach
        val image = anchor.selectFirst("img")
        val title = sequenceOf(anchor.attr("title"), image?.attr("alt").orEmpty(), anchor.text())
            .map(String::trim).firstOrNull { it.length >= 2 } ?: slug
        val poster = image?.let {
            sequenceOf(it.absUrl("src"), it.absUrl("data-src"), it.absUrl("data-original"))
                .firstOrNull(String::isNotBlank)
        }
        results.putIfAbsent(href, CatalogTitle(title, href, poster?.takeIf(String::isNotBlank), kind, sourceName))
    }
    return results.values.take(100)
}

private fun parseDetails(item: CatalogTitle, allowedHost: String): CatalogDetails {
    val document = Jsoup.connect(item.pageUrl)
        .userAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36")
        .timeout(20_000)
        .get()
    val title = sequenceOf(
        document.selectFirst("meta[property=og:title]")?.attr("content").orEmpty(),
        document.selectFirst("h1")?.text().orEmpty(),
        item.title
    ).map(String::trim).firstOrNull(String::isNotBlank) ?: item.title
    val description = sequenceOf(
        document.selectFirst("meta[property=og:description]")?.attr("content").orEmpty(),
        document.selectFirst("meta[name=description]")?.attr("content").orEmpty(),
        document.selectFirst(".description, .film-description, .movie-description, .serial-description")?.text().orEmpty()
    ).map(String::trim).firstOrNull { it.length > 25 }
    val bodyText = document.body()?.text().orEmpty()
    val year = Regex("\\b(19|20)\\d{2}\\b").find(bodyText)?.value
    val facts = linkedSetOf<String>()
    listOf("Жанр", "Страна", "Режиссёр", "Режиссер", "В ролях", "Продолжительность", "Рейтинг").forEach { label ->
        val element = document.select("*").firstOrNull { it.ownText().trim().startsWith(label, ignoreCase = true) }
        val value = element?.text()?.trim()
        if (!value.isNullOrBlank() && value.length < 180) facts += value
    }
    val episodes = linkedMapOf<String, CatalogEpisode>()
    if (item.kind == CatalogKind.SERIES) {
        document.select("a[href]").forEach { anchor ->
            val href = anchor.absUrl("href").substringBefore('#')
            val path = runCatching { java.net.URI(href).path.orEmpty() }.getOrDefault("")
            val text = anchor.text().trim()
            val isSeasonOrEpisode = Regex("(season|episode|seriya|serial|epizod|сезон|серия)", RegexOption.IGNORE_CASE)
                .containsMatchIn(path + " " + text)
            if (isSeasonOrEpisode && href.startsWith("https://$allowedHost/") && href != item.pageUrl && text.length >= 2) {
                episodes.putIfAbsent(href, CatalogEpisode(text, href))
            }
        }
    }
    return CatalogDetails(title, description, year, facts.toList(), episodes.values.take(100))
}

class ZonaCatalogSource : DetailedCatalogSource {
    override val displayName: String = "ZONA"
    private val base = "https://w140.zona.plus"

    override suspend fun load(kind: CatalogKind): List<CatalogTitle> = withContext(Dispatchers.IO) {
        parseListing(base, if (kind == CatalogKind.MOVIE) "/movies" else "/tvseries", kind, displayName)
    }

    override suspend fun details(title: CatalogTitle): CatalogDetails = withContext(Dispatchers.IO) {
        parseDetails(title, "w140.zona.plus")
    }
}

class KinoBesplatnoCatalogSource : DetailedCatalogSource {
    override val displayName: String = "KinoBesplatno"
    private val base = "https://kinobesplatno.online"

    override suspend fun load(kind: CatalogKind): List<CatalogTitle> = withContext(Dispatchers.IO) {
        parseListing(base, if (kind == CatalogKind.MOVIE) "/filmy/" else "/serialy/", kind, displayName)
    }

    override suspend fun details(title: CatalogTitle): CatalogDetails = withContext(Dispatchers.IO) {
        parseDetails(title, "kinobesplatno.online")
    }
}
