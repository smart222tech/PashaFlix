package com.smart222tech.atodo

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var cursor: RemoteCursor
    private lateinit var container: FrameLayout

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUi()
        container = FrameLayout(this)
        webView = WebView(this).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            isFocusable = true; isFocusableInTouchMode = true
            settings.javaScriptEnabled = true; settings.domStorageEnabled = true
            settings.databaseEnabled = true; settings.mediaPlaybackRequiresUserGesture = false
            settings.javaScriptCanOpenWindowsAutomatically = true; settings.setSupportMultipleWindows(false)
            settings.loadsImagesAutomatically = true; settings.useWideViewPort = true
            settings.loadWithOverviewMode = false; settings.builtInZoomControls = false
            settings.displayZoomControls = false
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {}
        }
        container.addView(webView, FrameLayout.LayoutParams(-1, -1))
        cursor = RemoteCursor(this, webView)
        container.addView(cursor, FrameLayout.LayoutParams(-1, -1))
        setContentView(container)
        if (savedInstanceState == null) webView.loadUrl(ZONA_URL) else webView.restoreState(savedInstanceState)
        webView.requestFocus(View.FOCUS_DOWN)
    }

    private fun hideSystemUi() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) { super.onWindowFocusChanged(hasFocus); if (hasFocus) hideSystemUi() }
    override fun onSaveInstanceState(outState: Bundle) { webView.saveState(outState); super.onSaveInstanceState(outState) }

    @Deprecated("Deprecated by Android; retained for broad Android TV compatibility")
    override fun onBackPressed() { if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed() }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!::cursor.isInitialized || event == null) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { cursor.move(0, -1); return true }
            KeyEvent.KEYCODE_DPAD_DOWN -> { cursor.move(0, 1); return true }
            KeyEvent.KEYCODE_DPAD_LEFT -> { cursor.move(-1, 0); return true }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { cursor.move(1, 0); return true }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> { cursor.clickTarget(); return true }
        }
        return super.onKeyDown(keyCode, event)
    }
    override fun onDestroy() { if (::webView.isInitialized) { webView.stopLoading(); webView.destroy() }; super.onDestroy() }
    companion object { private const val ZONA_URL = "https://w140.zona.plus/" }

private class RemoteCursor(context: android.content.Context, private val target: WebView) : View(context) {
    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    private var cx = 0f
    private var cy = 0f
    private val step = (32f * resources.displayMetrics.density)
    init { isClickable = false; isFocusable = false }
    private fun centerIfNeeded() {
        if (cx == 0f || cy == 0f) { cx = width / 2f; cy = height / 2f }
    }
    fun move(dx: Int, dy: Int) {
        centerIfNeeded()
        cx = (cx + dx * step).coerceIn(18f, (width - 18).coerceAtLeast(18).toFloat())
        cy = (cy + dy * step).coerceIn(18f, (height - 18).coerceAtLeast(18).toFloat())
        invalidate()
    }
    fun clickTarget() {
        centerIfNeeded()
        val now = android.os.SystemClock.uptimeMillis()
        target.dispatchTouchEvent(android.view.MotionEvent.obtain(now, now, android.view.MotionEvent.ACTION_DOWN, cx, cy, 0))
        target.dispatchTouchEvent(android.view.MotionEvent.obtain(now, now + 70, android.view.MotionEvent.ACTION_UP, cx, cy, 0))
    }
    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas); centerIfNeeded()
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.argb(65, 255, 55, 55)
        canvas.drawCircle(cx, cy, 15f * resources.displayMetrics.density, paint)
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2.4f * resources.displayMetrics.density
        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(cx, cy, 11f * resources.displayMetrics.density, paint)
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.rgb(255, 65, 65)
        canvas.drawCircle(cx, cy, 3.5f * resources.displayMetrics.density, paint)
    }
}

}
