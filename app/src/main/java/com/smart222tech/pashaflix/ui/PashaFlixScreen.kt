package com.smart222tech.pashaflix.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.smart222tech.pashaflix.player.MediaCandidate
import com.smart222tech.pashaflix.player.MediaResourceInspector

private const val DEFAULT_MOVIE_URL = "https://w140.zona.plus/movies/odisseya-2026"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PashaFlixScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val url = remember { mutableStateOf(DEFAULT_MOVIE_URL) }
    val pageTitle = remember { mutableStateOf("PashaFlix") }
    val pageStatus = remember { mutableStateOf("Готово к открытию источника") }
    val candidates = remember { mutableStateListOf<MediaCandidate>() }
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = true
            settings.loadsImagesAutomatically = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    if (!title.isNullOrBlank()) pageTitle.value = title
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, pageUrl: String?, favicon: Bitmap?) {
                    pageStatus.value = "Загружается страница источника…"
                    candidates.clear()
                }
                override fun onPageFinished(view: WebView?, pageUrl: String?) {
                    pageStatus.value = "Страница открыта. Наблюдение за медиаресурсами активно."
                }
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): android.webkit.WebResourceResponse? {
                    val candidate = request?.url?.toString()?.let(MediaResourceInspector::inspect)
                    if (candidate != null) {
                        post {
                            if (candidates.none {
                                it.kind == candidate.kind && it.host == candidate.host && it.pathHint == candidate.pathHint
                            }) {
                                candidates.add(0, candidate)
                                while (candidates.size > 12) candidates.removeAt(candidates.lastIndex)
                            }
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }
            }
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFF101419)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PASHAFLIX", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = url.value,
                onValueChange = { url.value = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Адрес фильма") }
            )
            Button(onClick = {
                val target = url.value.trim()
                if (target.startsWith("https://")) webView.loadUrl(target)
                else pageStatus.value = "Используй полный HTTPS-адрес."
            }) { Text("Открыть") }
        }
        Text(pageStatus.value, color = Color(0xFFB8C5D2), style = MaterialTheme.typography.bodySmall)
        Card(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Сетевой монитор", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Определяются типы медиазапросов. Полные URL и токены не сохраняются.",
                    color = Color(0xFFB8C5D2),
                    style = MaterialTheme.typography.bodySmall
                )
                if (candidates.isEmpty()) {
                    Text("Медиаресурсы пока не обнаружены.", color = Color.LightGray)
                } else {
                    candidates.forEach { item ->
                        Text(
                            "${item.kind}  •  ${item.host}  •  ${item.pathHint}",
                            color = Color(0xFF7DD3FC),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (webView.url == null) webView.loadUrl(DEFAULT_MOVIE_URL)
    }
}
