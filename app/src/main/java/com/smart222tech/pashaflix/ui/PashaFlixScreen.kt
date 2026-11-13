package com.smart222tech.pashaflix.ui

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.smart222tech.pashaflix.catalog.CatalogKind
import com.smart222tech.pashaflix.catalog.CatalogSource
import com.smart222tech.pashaflix.catalog.CatalogTitle
import com.smart222tech.pashaflix.catalog.KinoBesplatnoCatalogSource
import com.smart222tech.pashaflix.catalog.ZonaCatalogSource

private enum class Screen { HOME, CATALOG, DETAILS, PLAYBACK }

private val background = Color(0xFF0D1117)
private val surface = Color(0xFF18212B)
private val accent = Color(0xFF28A9E8)
private val muted = Color(0xFF9AA9B8)

@Composable
fun PashaFlixScreen(modifier: Modifier = Modifier) {
    val sources = remember {
        listOf< CatalogSource>(ZonaCatalogSource(), KinoBesplatnoCatalogSource())
    }
    var screen by remember { mutableStateOf(Screen.HOME) }
    var source by remember { mutableStateOf<CatalogSource>(sources.first()) }
    var kind by remember { mutableStateOf(CatalogKind.MOVIE) }
    var titles by remember { mutableStateOf<List<CatalogTitle>>(emptyList()) }
    var selected by remember { mutableStateOf<CatalogTitle?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = screen != Screen.HOME) {
        screen = when (screen) {
            Screen.PLAYBACK -> Screen.DETAILS
            Screen.DETAILS -> Screen.CATALOG
            Screen.CATALOG -> Screen.HOME
            Screen.HOME -> Screen.HOME
        }
    }

    LaunchedEffect(screen, source, kind) {
        if (screen == Screen.CATALOG) {
            loading = true
            error = null
            try {
                titles = source.load(kind)
                if (titles.isEmpty()) error = "Каталог пока не вернул список. Попробуй обновить позже."
            } catch (e: Exception) {
                titles = emptyList()
                error = "Не удалось загрузить каталог: ${e.localizedMessage ?: "ошибка соединения"}"
            } finally {
                loading = false
            }
        }
    }

    when (screen) {
        Screen.HOME -> HomeScreen(
            modifier = modifier,
            onSource = {
                source = it
                kind = CatalogKind.MOVIE
                screen = Screen.CATALOG
            }
        )
        Screen.CATALOG -> CatalogScreen(
            modifier = modifier,
            sourceName = source.displayName,
            kind = kind,
            titles = titles,
            loading = loading,
            error = error,
            onKind = { kind = it },
            onBack = { screen = Screen.HOME },
            onTitle = {
                selected = it
                screen = Screen.DETAILS
            }
        )
        Screen.DETAILS -> selected?.let { item ->
            DetailsScreen(
                modifier = modifier,
                item = item,
                onBack = { screen = Screen.CATALOG },
                onWatch = { screen = Screen.PLAYBACK }
            )
        }
        Screen.PLAYBACK -> selected?.let { item ->
            PlaybackScreen(modifier = modifier, url = item.pageUrl)
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    onSource: (CatalogSource) -> Unit
) {
    val activeSources = remember { listOf(ZonaCatalogSource(), KinoBesplatnoCatalogSource()) }
    Column(
        modifier = modifier.fillMaxSize().background(background).padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Text("PASHAFLIX", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Твоя видеотека", color = muted, fontSize = 18.sp)
        Text("ВЫБЕРИ КИНОТЕКУ", color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 210.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(activeSources) { item ->
                SourceCard(name = item.displayName, active = true) { onSource(item) }
            }
            items(listOf("Kinogo", "HDRezka", "Filmix", "Baskino", "LordFilm", "Seasonvar", "Kinokrad")) { name ->
                SourceCard(name = name, active = false, onClick = {})
            }
        }
        Text(
            "Другие источники появятся после проверки каталогов и доступности.",
            color = muted,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SourceCard(name: String, active: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        enabled = active,
        modifier = Modifier.fillMaxWidth().height(112.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surface, disabledContainerColor = surface.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Text(if (active) "Фильмы и сериалы  ›" else "Скоро", color = if (active) accent else muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CatalogScreen(
    modifier: Modifier,
    sourceName: String,
    kind: CatalogKind,
    titles: List<CatalogTitle>,
    loading: Boolean,
    error: String?,
    onKind: (CatalogKind) -> Unit,
    onBack: () -> Unit,
    onTitle: (CatalogTitle) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().background(background).padding(horizontal = 24.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onBack) { Text("‹  Назад") }
            Column {
                Text(sourceName, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("Каталог", color = muted, fontSize = 13.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { onKind(CatalogKind.MOVIE) }, enabled = kind != CatalogKind.MOVIE) {
                Text("Фильмы")
            }
            Button(onClick = { onKind(CatalogKind.SERIES) }, enabled = kind != CatalogKind.SERIES) {
                Text("Сериалы")
            }
        }
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accent)
            }
            error != null -> Text(error, color = Color(0xFFFFB4A9))
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(titles) { item -> TitleCard(item, onClick = { onTitle(item) }) }
            }
        }
    }
}

@Composable
private fun TitleCard(item: CatalogTitle, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            if (item.posterUrl != null) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxWidth().height(210.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier.fillMaxWidth().height(210.dp).background(Color(0xFF253342)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("PASHAFLIX", color = muted, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                item.title,
                modifier = Modifier.padding(10.dp),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun DetailsScreen(
    modifier: Modifier,
    item: CatalogTitle,
    onBack: () -> Unit,
    onWatch: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().background(background).padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        OutlinedButton(onClick = onBack) { Text("‹  Назад к каталогу") }
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            if (item.posterUrl != null) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier.width(190.dp).height(280.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(item.title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("${item.sourceName}  •  ${if (item.kind == CatalogKind.MOVIE) "Фильм" else "Сериал"}", color = muted)
                Button(onClick = onWatch) { Text("▶  Смотреть") }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PlaybackScreen(modifier: Modifier, url: String) {
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
            webView.destroy()
        }
    }
    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize().background(Color.Black),
        update = { }
    )
}
