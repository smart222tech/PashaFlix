package com.smart222tech.pashaflix.catalog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URL

enum class CatalogKind { MOVIE, SERIES }

data class CatalogTitle(
    val title: String,
    val pageUrl: String,
    val posterUrl: String?,
    val kind: CatalogKind,
    val sourceName: String
)

interface CatalogSource {
    val displayName: String
    suspend fun load(kind: CatalogKind): List<CatalogTitle>
}

class ZonaCatalogSource : CatalogSource {
    override val displayName: String = "ZONA"

    override suspend fun load(kind: CatalogKind): List<CatalogTitle> = withContext(Dispatchers.IO) {
        val base = "https://w140.zona.plus"
        val path = if (kind == CatalogKind.MOVIE) "/movies" else "/tvseries"
        val document = Jsoup.connect(base + path)
            .userAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36")
            .timeout(20_000)
            .get()

        val expectedSegment = if (kind == CatalogKind.MOVIE) "/movies/" else "/tvseries/"
        val results = linkedMapOf<String, CatalogTitle>()

        document.select("a[href]").forEach { anchor ->
            val href = anchor.absUrl("href")
            if (!href.startsWith(base) || !href.contains(expectedSegment)) return@forEach
            val slug = href.substringAfter(expectedSegment).substringBefore('?').trim('/')
            if (slug.isBlank() || slug.contains('/')) return@forEach

            val image = anchor.selectFirst("img")
            val title = sequenceOf(
                anchor.attr("title"),
                image?.attr("alt").orEmpty(),
                anchor.text()
            ).map { it.trim() }.firstOrNull { it.length >= 2 } ?: slug
            val poster = image?.let {
                sequenceOf(it.absUrl("src"), it.absUrl("data-src"), it.absUrl("data-original"))
                    .firstOrNull(String::isNotBlank)
            }

            results.putIfAbsent(href, CatalogTitle(
                title = title,
                pageUrl = href,
                posterUrl = poster?.takeIf(String::isNotBlank),
                kind = kind,
                sourceName = displayName
            ))
        }
        results.values.take(80)
    }
}

class KinoBesplatnoCatalogSource : CatalogSource {
    override val displayName: String = "KinoBesplatno"

    override suspend fun load(kind: CatalogKind): List<CatalogTitle> = withContext(Dispatchers.IO) {
        val base = "https://kinobesplatno.online"
        val path = if (kind == CatalogKind.MOVIE) "/filmy/" else "/serialy/"
        val document = Jsoup.connect(base + path)
            .userAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36")
            .timeout(20_000)
            .get()

        val expectedSegment = if (kind == CatalogKind.MOVIE) "/filmy/" else "/serialy/"
        val results = linkedMapOf<String, CatalogTitle>()
        document.select("a[href]").forEach { anchor ->
            val href = anchor.absUrl("href").substringBefore('#')
            if (!href.startsWith(base) || !href.contains(expectedSegment)) return@forEach
            val slug = href.substringAfter(expectedSegment).substringBefore('?').trim('/')
            if (slug.isBlank() || slug.contains('/')) return@forEach
            val image = anchor.selectFirst("img")
            val title = sequenceOf(anchor.attr("title"), image?.attr("alt").orEmpty(), anchor.text())
                .map(String::trim).firstOrNull { it.length >= 2 } ?: slug
            val poster = image?.let {
                sequenceOf(it.absUrl("src"), it.absUrl("data-src"), it.absUrl("data-original"))
                    .firstOrNull(String::isNotBlank)
            }
            results.putIfAbsent(href, CatalogTitle(
                title, href, poster?.takeIf(String::isNotBlank), kind, displayName
            ))
        }
        results.values.take(80)
    }
}
