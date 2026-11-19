package com.smart222tech.atodo

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private var geckoView: GeckoView? = null
    private var geckoSession: GeckoSession? = null
    private var canGoBack = false

    companion object {
        private const val ATODO_URL = "https://msx.atodo.fun"
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
        session.setNavigationDelegate(object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(session: GeckoSession, value: Boolean) {
                canGoBack = value
            }
        })
        session.setContentDelegate(object : GeckoSession.ContentDelegate {})
        browser.setSession(session)
        geckoView = browser
        geckoSession = session
        canGoBack = false
        browser.requestFocus()
        session.loadUri(ATODO_URL)
        hideSystemUi()
    }

    private fun showHome() {
        closeBrowser()
        root.removeAllViews()
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 24, 32, 24)
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(8, 14, 29), Color.rgb(3, 6, 14)))
        }
        page.addView(TextView(this).apply {
            text = "ATODO"
            textSize = 42f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            letterSpacing = 0.08f
        }, LinearLayout.LayoutParams(-1, 76))
        page.addView(TextView(this).apply {
            text = "ОНЛАЙН-ВИДЕОТЕКА"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(145, 180, 220))
            letterSpacing = 0.18f
        }, LinearLayout.LayoutParams(-1, 42))
        val launch = TextView(this).apply {
            text = "ОТКРЫТЬ ATODO"
            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-bold", Typeface.BOLD)
            isFocusable = true
            isFocusableInTouchMode = true
            background = GradientDrawable().apply {
                cornerRadius = 24f
                setColor(Color.rgb(28, 112, 190))
                setStroke(2, Color.rgb(92, 190, 255))
            }
            setPadding(28, 18, 28, 18)
            setOnClickListener { openAtodo() }
            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
                    openAtodo()
                    true
                } else false
            }
        }
        page.addView(launch, LinearLayout.LayoutParams(-1, 68).apply {
            topMargin = 28
            leftMargin = 12
            rightMargin = 12
        })
        root.addView(page, FrameLayout.LayoutParams(-1, -1))
        launch.requestFocus()
        hideSystemUi()
    }

    private fun closeBrowser() {
        geckoView?.releaseSession()
        geckoSession?.close()
        geckoView = null
        geckoSession = null
        canGoBack = false
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        val session = geckoSession
        if (session != null) {
            if (canGoBack) session.goBack() else showHome()
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

    override fun onDestroy() {
        closeBrowser()
        super.onDestroy()
    }
}
