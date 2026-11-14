package com.smart222tech.pashaflix.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.smart222tech.pashaflix.catalog.*
import com.smart222tech.pashaflix.library.LibraryStore

private enum class Screen { HOME, CATALOG, DETAILS, PLAYBACK, FAVORITES, HISTORY }
private val bg = Color(0xFF0D1117)
private val panel = Color(0xFF18212B)
private val blue = Color(0xFF28A9E8)
private val muted = Color(0xFF9AA9B8)

@Composable
fun PashaFlixScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sources = remember { listOf<DetailedCatalogSource>(ZonaCatalogSource(), KinoBesplatnoCatalogSource()) }
    val library = remember { LibraryStore(context.applicationContext) }
    var screen by remember { mutableStateOf(Screen.HOME) }
    var source by remember { mutableStateOf(sources.first()) }
    var kind by remember { mutableStateOf(CatalogKind.MOVIE) }
    var titles by remember { mutableStateOf(emptyList<CatalogTitle>()) }
    var selected by remember { mutableStateOf<CatalogTitle?>(null) }
    var details by remember { mutableStateOf<CatalogDetails?>(null) }
    var playbackUrl by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var favorite by remember { mutableStateOf(false) }
    var libraryVersion by remember { mutableIntStateOf(0) }

    BackHandler(screen != Screen.HOME) {
        screen = when (screen) {
            Screen.PLAYBACK -> Screen.DETAILS
            Screen.DETAILS -> Screen.CATALOG
            else -> Screen.HOME
        }
    }
    LaunchedEffect(screen, source, kind) {
        if (screen == Screen.CATALOG) {
            loading = true
            error = null
            try {
                titles = source.load(kind)
                if (titles.isEmpty()) error = "Каталог не вернул карточки. Возможно, сайт изменил разметку или ограничил доступ."
            } catch (e: Exception) {
                titles = emptyList()
                error = "Не удалось загрузить каталог: " + (e.localizedMessage ?: "ошибка соединения")
            } finally { loading = false }
        }
    }
    LaunchedEffect(screen, selected) {
        val item = selected
        if (screen == Screen.DETAILS && item != null) {
            details = null
            favorite = library.isFavorite(item)
            details = runCatching { sourceFor(item, sources).details(item) }.getOrNull()
        }
    }
    when (screen) {
        Screen.HOME -> Home(modifier, sources,
            onSource = { source = it; kind = CatalogKind.MOVIE; screen = Screen.CATALOG },
            onFavorites = { libraryVersion++; screen = Screen.FAVORITES },
            onHistory = { libraryVersion++; screen = Screen.HISTORY })
        Screen.CATALOG -> Catalog(modifier, source.displayName, kind, titles, loading, error,
            onKind = { kind = it }, onBack = { screen = Screen.HOME },
            onSelect = { selected = it; screen = Screen.DETAILS })
        Screen.DETAILS -> selected?.let { item ->
            Details(modifier, item, details, favorite,
                onFavorite = { favorite = library.toggleFavorite(item); libraryVersion++ },
                onBack = { screen = Screen.CATALOG },
                onWatch = { url -> playbackUrl = url; library.addToHistory(item); libraryVersion++; screen = Screen.PLAYBACK })
        }
        Screen.PLAYBACK -> Playback(modifier, playbackUrl)
        Screen.FAVORITES -> LibraryPage(modifier, "Избранное", library.favorites(), libraryVersion,
            { screen = Screen.HOME }, { selected = it; source = sourceFor(it, sources); screen = Screen.DETAILS })
        Screen.HISTORY -> LibraryPage(modifier, "История просмотров", library.history(), libraryVersion,
            { screen = Screen.HOME }, { selected = it; source = sourceFor(it, sources); screen = Screen.DETAILS })
    }
}

private fun sourceFor(item: CatalogTitle, sources: List<DetailedCatalogSource>) =
    sources.firstOrNull { it.displayName == item.sourceName } ?: sources.first()

@Composable
private fun Home(
    modifier: Modifier, sources: List<DetailedCatalogSource>,
    onSource: (DetailedCatalogSource) -> Unit, onFavorites: () -> Unit, onHistory: () -> Unit
) {
    Column(modifier.fillMaxSize().background(bg).padding(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("PASHAFLIX", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Твоя видеотека", color = muted, fontSize = 18.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onFavorites) { Text("♡  Избранное") }
            OutlinedButton(onClick = onHistory) { Text("◷  История") }
        }
        Text("ВЫБЕРИ КИНОТЕКУ", color = blue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(GridCells.Adaptive(210.dp), Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(sources) { src -> SourceCard(src.displayName, true) { onSource(src) } }
            items(listOf("Kinogo", "HDRezka", "Filmix", "Baskino", "LordFilm", "Seasonvar", "Kinokrad")) {
                name -> SourceCard(name, false) {}
            }
        }
        Text("Новые источники подключаются после проверки каталога и способа просмотра.", color = muted, fontSize = 12.sp)
    }
}

@Composable
private fun SourceCard(name: String, active: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, enabled = active, modifier = Modifier.fillMaxWidth().height(112.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = panel, disabledContainerColor = panel.copy(alpha = .5f))) {
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(name, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Text(if (active) "Фильмы и сериалы  ›" else "Скоро", color = if (active) blue else muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun Catalog(
    modifier: Modifier, sourceName: String, kind: CatalogKind, titles: List<CatalogTitle>,
    loading: Boolean, error: String?, onKind: (CatalogKind) -> Unit,
    onBack: () -> Unit, onSelect: (CatalogTitle) -> Unit
) {
    var query by remember(sourceName, kind) { mutableStateOf("") }
    val filtered = remember(titles, query) {
        if (query.isBlank()) titles else titles.filter { it.title.contains(query.trim(), ignoreCase = true) }
    }
    Column(modifier.fillMaxSize().background(bg).padding(horizontal = 24.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onBack) { Text("‹  Назад") }
            Column {
                Text(sourceName, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("Каталог", color = muted, fontSize = 13.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { onKind(CatalogKind.MOVIE) }, enabled = kind != CatalogKind.MOVIE) { Text("Фильмы") }
            Button(onClick = { onKind(CatalogKind.SERIES) }, enabled = kind != CatalogKind.SERIES) { Text("Сериалы") }
        }
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Поиск в каталоге") }, placeholder = { Text("Название фильма или сериала") })
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = blue) }
            error != null -> Text(error, color = Color(0xFFFFB4A9))
            filtered.isEmpty() -> Text("Ничего не найдено по запросу «" + query + "». ", color = muted)
            else -> LazyVerticalGrid(GridCells.Adaptive(150.dp), contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(filtered) { item -> TitleCard(item) { onSelect(item) } }
            }
        }
    }
}

@Composable
private fun TitleCard(item: CatalogTitle, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(12.dp)) {
        Column {
            if (!item.posterUrl.isNullOrBlank()) {
                AsyncImage(item.posterUrl, item.title, Modifier.fillMaxWidth().height(210.dp), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxWidth().height(210.dp).background(Color(0xFF253342)), contentAlignment = Alignment.Center) {
                    Text("PASHAFLIX", color = muted, fontWeight = FontWeight.Bold)
                }
            }
            Text(item.title, Modifier.padding(10.dp), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 2)
        }
    }
}

@Composable
private fun Details(
    modifier: Modifier, item: CatalogTitle, details: CatalogDetails?, favorite: Boolean,
    onFavorite: () -> Unit, onBack: () -> Unit, onWatch: (String) -> Unit
) {
    Column(modifier.fillMaxSize().background(bg).padding(26.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedButton(onClick = onBack) { Text("‹  Назад к каталогу") }
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.Top) {
            if (!item.posterUrl.isNullOrBlank()) {
                AsyncImage(item.posterUrl, item.title, Modifier.width(190.dp).height(280.dp), contentScale = ContentScale.Crop)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(details?.title ?: item.title, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text(listOfNotNull(item.sourceName, details?.year, if (item.kind == CatalogKind.MOVIE) "Фильм" else "Сериал").joinToString("  •  "), color = muted)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { onWatch(item.pageUrl) }) { Text("▶  Смотреть") }
                    OutlinedButton(onClick = onFavorite) { Text(if (favorite) "♥ В избранном" else "♡ В избранное") }
                }
                if (details == null) Text("Загружаю описание…", color = muted)
                details?.facts?.forEach { Text(it, color = muted, fontSize = 13.sp) }
            }
        }
        details?.description?.let {
            Text("О фильме", color = blue, fontWeight = FontWeight.Bold)
            Text(it, color = Color(0xFFD6DEE7), fontSize = 14.sp)
        }
        if (item.kind == CatalogKind.SERIES) {
            Text("Сезоны и серии", color = blue, fontWeight = FontWeight.Bold)
            val episodes = details?.relatedEpisodes.orEmpty()
            if (episodes.isEmpty()) Text("Список серий не удалось получить автоматически. Открой страницу источника для просмотра доступных серий.", color = muted, fontSize = 13.sp)
            else Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                episodes.forEachIndexed { index, episode ->
                    OutlinedButton(onClick = { onWatch(episode.pageUrl) }, modifier = Modifier.fillMaxWidth()) {
                        Text((index + 1).toString() + ".  " + episode.title, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryPage(
    modifier: Modifier, title: String, entries: List<CatalogTitle>, version: Int,
    onBack: () -> Unit, onSelect: (CatalogTitle) -> Unit
) {
    val shown = remember(entries, version) { entries }
    Column(modifier.fillMaxSize().background(bg).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onBack) { Text("‹  Назад") }
            Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        if (shown.isEmpty()) Text("Здесь пока пусто.", color = muted)
        else LazyVerticalGrid(GridCells.Adaptive(150.dp), contentPadding = PaddingValues(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(shown) { item -> TitleCard(item) { onSelect(item) } }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun Playback(modifier: Modifier, url: String) {
    val context = LocalContext.current
    val webView = remember(url) {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = true
            settings.loadsImagesAutomatically = true
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            loadUrl(url)
        }
    }
    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.destroy()
        }
    }
    AndroidView(factory = { webView }, modifier = modifier.fillMaxSize().background(Color.Black), update = {})
}
