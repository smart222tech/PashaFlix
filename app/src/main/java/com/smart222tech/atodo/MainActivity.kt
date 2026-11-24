package com.smart222tech.lampa

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private var webView: WebView? = null
    private var fullView: View? = null
    private var fullCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        setContentView(root)
        hideSystemUi()
        openLampa()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openLampa() {
        val web = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            isFocusable = true
            isFocusableInTouchMode = true
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(false)
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 9; AFTKA) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (view == null || fullView != null) {
                        callback?.onCustomViewHidden()
                        return
                    }
                    fullView = view
                    fullCallback = callback
                    root.removeView(this@apply)
                    root.addView(view, FrameLayout.LayoutParams(-1, -1))
                    hideSystemUi()
                }

                override fun onHideCustomView() {
                    hideFullView()
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false
            }
        }
        webView = web
        root.addView(web, FrameLayout.LayoutParams(-1, -1))
        web.loadUrl("http://lampa.maxvol.pro")
        web.requestFocus(View.FOCUS_DOWN)
    }

    private fun hideFullView() {
        fullView?.let { root.removeView(it) }
        fullView = null
        fullCallback?.onCustomViewHidden()
        fullCallback = null
        webView?.let {
            if (it.parent == null) root.addView(it, FrameLayout.LayoutParams(-1, -1))
            it.requestFocus()
        }
        hideSystemUi()
    }

    private fun hideSystemUi() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUi()
    }

    @Deprecated("Deprecated by Android")
    override fun onBackPressed() {
        handleRemoteBack()
    }

    private fun handleRemoteBack() {
        if (fullView != null) {
            hideFullView()
            return
        }
        val web = webView ?: return
        // Lampa is a single-page web app: some player/detail screens use
        // JavaScript history rather than WebView's native back-forward list.
        // Try native history first, then dispatch browser history.back().
        if (web.canGoBack()) {
            web.goBack()
        } else {
            web.evaluateJavascript(
                "(function(){ if (history.length > 1) { history.back(); return 'back'; } return 'home'; })();",
                null
            )
        }
        web.postDelayed({ web.requestFocus() }, 150)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_UP) handleRemoteBack()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val web = webView
        val navKeys = keyCode in setOf(
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER
        )
        if (web != null && navKeys && event != null) {
            return (fullView ?: web).dispatchKeyEvent(event)
        }
        val mediaKeys = keyCode in setOf(
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        )
        if (web != null && mediaKeys && event != null) {
            return (fullView ?: web).dispatchKeyEvent(event)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        webView?.stopLoading()
        webView?.destroy()
        webView = null
        super.onDestroy()
    }
}
