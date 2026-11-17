package com.smart222tech.pashaflix.browser

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.ByteArrayInputStream
import kotlin.math.max
import kotlin.math.min

@UnstableApi
class BrowserActivity : ComponentActivity() {
    private lateinit var root: FrameLayout
    private lateinit var web: WebView
    private lateinit var cursor: CursorView
    private var playerView: PlayerView? = null
    private var player: ExoPlayer? = null
    private var pointerMode = false
    private var cx = 0f
    private var cy = 0f
    private var lastStream = ""
    private var lastHeaders: Map<String, String> = emptyMap()
    private var playing = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startUrl = intent.getStringExtra("url") ?: run { finish(); return }
        val tv = packageManager.hasSystemFeature("android.software.leanback")
        pointerMode = tv
        root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK); isFocusableInTouchMode = true }
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    return request.url.scheme !in listOf("http", "https")
                }
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                    val uri = request.url
                    val host = uri.host.orEmpty().lowercase()
                    if (isAdHost(host) || isAdPath(uri.toString())) {
                        return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                    }
                    val path = uri.path.orEmpty().lowercase()
                    if (path.endsWith(".m3u8") || path.endsWith(".mpd")) {
                        val headers = request.requestHeaders.toMap()
                        runOnUiThread {
                            lastStream = uri.toString()
                            lastHeaders = headers
                            if (!playing) showNativePlayer()
                        }
                    }
                    return null
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean = false
            }
        }
        root.addView(web, FrameLayout.LayoutParams(-1, -1))
        cursor = CursorView()
        root.addView(cursor, FrameLayout.LayoutParams(34, 34))
        setContentView(root)
        root.post { cx = root.width / 2f; cy = root.height / 2f; moveCursor() }
        web.loadUrl(startUrl)
    }

    private fun isAdHost(host: String): Boolean {
        val blocked = listOf("doubleclick.net", "googlesyndication.com", "adservice.google.", "popads.net", "popcash.net", "exoclick.com", "onclick.com", "trafficjunky.net")
        return blocked.any { host == it || host.endsWith(".$it") }
    }

    private fun isAdPath(url: String): Boolean {
        val s = url.lowercase()
        return listOf("/ads/", "/advert/", "/popup/", "/popunder/").any { it in s }
    }

    private fun showNativePlayer() {
        if (lastStream.isBlank() || playing) return
        playing = true
        try {
            val dataSource = DefaultHttpDataSource.Factory().setUserAgent(web.settings.userAgentString)
                .setDefaultRequestProperties(lastHeaders)
            player = ExoPlayer.Builder(this).setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSource)
            ).build().also { exo ->
                playerView = PlayerView(this).also { view ->
                    view.player = exo
                    view.useController = true
                    root.addView(view, FrameLayout.LayoutParams(-1, -1))
                }
                exo.setMediaItem(MediaItem.fromUri(lastStream))
                exo.prepare()
                exo.playWhenReady = true
            }
            Toast.makeText(this, "Видеопоток найден — открываю плеер", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            playing = false
            Toast.makeText(this, "Не удалось открыть поток: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun closePlayer() {
        playerView?.let { root.removeView(it) }
        playerView = null
        player?.release()
        player = null
        playing = false
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (playing) {
            if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) { closePlayer(); return true }
            return super.dispatchKeyEvent(event)
        }
        if (event.action != KeyEvent.ACTION_DOWN) return super.dispatchKeyEvent(event)
        when (event.keyCode) {
            KeyEvent.KEYCODE_MENU -> { pointerMode = !pointerMode; cursor.visibility = if (pointerMode) View.VISIBLE else View.GONE; Toast.makeText(this, if (pointerMode) "Режим курсора" else "Режим фокуса", Toast.LENGTH_SHORT).show(); return true }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (!pointerMode) return super.dispatchKeyEvent(event)
                val step = if (event.isShiftPressed) 48f else 24f
                when (event.keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> cx -= step
                    KeyEvent.KEYCODE_DPAD_RIGHT -> cx += step
                    KeyEvent.KEYCODE_DPAD_UP -> cy -= step
                    KeyEvent.KEYCODE_DPAD_DOWN -> cy += step
                }
                cx = min(max(8f, cx), (root.width - 8).toFloat())
                cy = min(max(8f, cy), (root.height - 8).toFloat())
                moveCursor()
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                if (pointerMode) { clickAtCursor(); return true }
            }
            KeyEvent.KEYCODE_BACK -> {
                if (web.canGoBack()) { web.goBack(); return true }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun moveCursor() {
        cursor.x = cx - cursor.width / 2f
        cursor.y = cy - cursor.height / 2f
        cursor.invalidate()
    }

    private fun clickAtCursor() {
        val now = android.os.SystemClock.uptimeMillis()
        val x = cx
        val y = cy
        web.dispatchTouchEvent(MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0))
        web.dispatchTouchEvent(MotionEvent.obtain(now, now + 60, MotionEvent.ACTION_UP, x, y, 0))
    }

    override fun onBackPressed() {
        if (playing) closePlayer() else if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        closePlayer()
        web.stopLoading()
        (web.parent as? ViewGroup)?.removeView(web)
        web.destroy()
        super.onDestroy()
    }

    private inner class CursorView : View(this@BrowserActivity) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f }
        private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x8833B5E5.toInt(); style = Paint.Style.FILL }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val r = width / 2f
            canvas.drawCircle(r, r, r - 3f, paint)
            canvas.drawCircle(r, r, 4f, fill)
        }
    }
}
