package com.smart222tech.atodo

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private var geckoView: GeckoView? = null
    private var geckoSession: GeckoSession? = null

    companion object {
        private const val ATODO_URL = "https://atodo.fun"
        private var runtime: GeckoRuntime? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        setContentView(root)
        hideSystemUi()
        openAtodo()
    }

    private fun openAtodo() {
        closeBrowser()
        root.removeAllViews()
        val viewport = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            clipChildren = true
            clipToPadding = true
        }
        val browser = GeckoView(this).apply {
            setBackgroundColor(Color.BLACK)
            isFocusable = true
            isFocusableInTouchMode = true
        }
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        browser.pivotX = 0f
        browser.pivotY = 0f
        browser.scaleX = 0.5f
        browser.scaleY = 0.5f
        viewport.addView(browser, FrameLayout.LayoutParams(screenWidth * 2, screenHeight * 2, Gravity.TOP or Gravity.START))
        root.addView(viewport, FrameLayout.LayoutParams(-1, -1))

        val activeRuntime = runtime ?: GeckoRuntime.create(this).also { runtime = it }
        val settings = GeckoSessionSettings.Builder()
            .userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_DESKTOP)
            .viewportMode(GeckoSessionSettings.VIEWPORT_MODE_DESKTOP)
            .build()
        val session = GeckoSession(settings)
        session.open(activeRuntime)
        session.setContentDelegate(object : GeckoSession.ContentDelegate {})
        browser.setSession(session)
        geckoView = browser
        geckoSession = session
        browser.requestFocus()
        session.loadUri(ATODO_URL)
        hideSystemUi()
    }

    /**
     * Fire TV sends the remote's Back button as KEYCODE_BACK.
     * Handle it before GeckoView can consume it and keep the current browser
     * session alive while navigating through the site's history.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_UP) {
                navigateBackInAtodo()
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun navigateBackInAtodo() {
        val session = geckoSession
        if (session != null && session.isOpen) {
            // goBack() is safe when there is no previous history entry: it
            // leaves the current page open instead of returning to app start.
            session.goBack()
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (geckoSession != null) {
            navigateBackInAtodo()
        } else {
            super.onBackPressed()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUi()
    }

    private fun hideSystemUi() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }

    private fun closeBrowser() {
        geckoView?.releaseSession()
        geckoSession?.close()
        geckoView = null
        geckoSession = null
    }

    override fun onDestroy() {
        closeBrowser()
        super.onDestroy()
    }
}
