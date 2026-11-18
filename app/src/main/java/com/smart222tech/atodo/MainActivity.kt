package com.smart222tech.kinoflix

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var root: FrameLayout
    private var webView: WebView? = null
    private var fullView: View? = null
    private var fullCallback: WebChromeClient.CustomViewCallback? = null
    private var tts: TextToSpeech? = null
    private data class Library(val title:String,val url:String,val domain:String,val letter:String,val color:Int)
    private val libraries = listOf(
        Library("Atodo","https://msx.atodo.fun","msx.atodo.fun","A",Color.rgb(80,190,255)),
        Library("Зона","http://m.zona.ms","m.zona.ms","Z",Color.rgb(255,83,103)),
        Library("Лампа","http://lampa.maxvol.pro","lampa.maxvol.pro","L",Color.rgb(255,190,55))
    )
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        root=FrameLayout(this).apply{setBackgroundColor(Color.rgb(7,9,17))}
        setContentView(root); hideSystemUi(); showHome(); showSplash()
    }

    private fun showHome(){
        webView?.let{it.stopLoading();it.destroy()};webView=null;root.removeAllViews()
        val scroll=ScrollView(this).apply{isFillViewport=true;overScrollMode=View.OVER_SCROLL_NEVER}
        val page=LinearLayout(this).apply{orientation=1;gravity=Gravity.CENTER_HORIZONTAL;setPadding(dp(26),dp(20),dp(26),dp(20))}
        scroll.addView(page,ScrollView.LayoutParams(-1,-2))
        page.addView(TextView(this).apply{
            text="KinoFlix";textSize=38f;typeface=android.graphics.Typeface.create("sans-serif-black",1)
            gravity=Gravity.CENTER;setTextColor(Color.WHITE);setShadowLayer(dp(18).toFloat(),0f,0f,Color.rgb(255,100,45))
        },LinearLayout.LayoutParams(-1,dp(58)))
        page.addView(TextView(this).apply{
            text="ТРИ ВИДЕОТЕКИ — ОДНО ПРИЛОЖЕНИЕ";textSize=12f;letterSpacing=.12f
            gravity=Gravity.CENTER;setTextColor(Color.rgb(179,188,208))
        },LinearLayout.LayoutParams(-1,dp(28)))
        val portrait=resources.configuration.orientation==Configuration.ORIENTATION_PORTRAIT
        val cards=LinearLayout(this).apply{orientation=if(portrait)1 else 0;gravity=Gravity.CENTER}
        libraries.forEach{lib->
            val lp=LinearLayout.LayoutParams(if(portrait)-1 else 0,if(portrait)dp(158) else dp(220),if(portrait)0f else 1f)
            if(portrait)lp.bottomMargin=dp(12) else{lp.marginStart=dp(8);lp.marginEnd=dp(8)}
            cards.addView(makeCard(lib),lp)
        }
        page.addView(cards,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(20)})
        page.addView(TextView(this).apply{
            text="(RU · UA · EN)";textSize=13f;gravity=Gravity.CENTER;setTextColor(Color.rgb(198,205,220))
        },LinearLayout.LayoutParams(-1,dp(32)))
        page.addView(TextView(this).apply{
            text="Собственная разработка Pasha TV";textSize=13f;gravity=Gravity.CENTER;setTextColor(Color.rgb(124,139,166))
        },LinearLayout.LayoutParams(-1,dp(34)))
        root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
    }

    private fun makeCard(lib:Library):View{
        val card=LinearLayout(this).apply{
            orientation=1;gravity=Gravity.CENTER;isFocusable=true;isFocusableInTouchMode=true
            setPadding(dp(10),dp(12),dp(10),dp(10));background=cardBg(lib.color,false)
        }
        val circle=FrameLayout(this).apply{
            background=GradientDrawable().apply{shape=GradientDrawable.OVAL;setColor(Color.rgb(24,29,43));setStroke(dp(1),lib.color)}
        }
        val fallback=TextView(this).apply{text=lib.letter;textSize=34f;gravity=Gravity.CENTER;setTextColor(lib.color);typeface=android.graphics.Typeface.DEFAULT_BOLD}
        circle.addView(fallback,FrameLayout.LayoutParams(-1,-1))
        val logo=ImageView(this).apply{scaleType=ImageView.ScaleType.CENTER_INSIDE}
        circle.addView(logo,FrameLayout.LayoutParams(-1,-1))
        card.addView(circle,LinearLayout.LayoutParams(dp(72),dp(72)))
        val label=TextView(this).apply{text=lib.title;textSize=21f;gravity=Gravity.CENTER;typeface=android.graphics.Typeface.DEFAULT_BOLD;setTextColor(Color.WHITE)}
        card.addView(label,LinearLayout.LayoutParams(-1,dp(38)).apply{topMargin=dp(5)})
        card.addView(TextView(this).apply{text="(RU · UA · EN)";textSize=11f;gravity=Gravity.CENTER;setTextColor(Color.rgb(164,176,199))},LinearLayout.LayoutParams(-1,dp(22)))
        card.setOnFocusChangeListener{v,focus->
            v.animate().scaleX(if(focus)1.06f else 1f).scaleY(if(focus)1.06f else 1f)
                .translationZ(if(focus)dp(18).toFloat() else 0f).setDuration(170).start()
            v.elevation=if(focus)dp(18).toFloat() else dp(3).toFloat()
            v.background=cardBg(lib.color,focus);label.setTextColor(if(focus)lib.color else Color.WHITE)
            if(focus) label.setShadowLayer(dp(16).toFloat(),0f,0f,lib.color) else label.clearShadowLayer()
        }
        card.setOnClickListener{openLibrary(lib)}
        card.setOnKeyListener{_,key,event->
            if(event.action==KeyEvent.ACTION_UP&&(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER)){openLibrary(lib);true}else false
        }
        thread{
            try{
                val c=URL("https://www.google.com/s2/favicons?domain_url=https://"+lib.domain+"&sz=128").openConnection() as HttpURLConnection
                c.connectTimeout=5000;c.readTimeout=5000
                val bitmap=c.inputStream.use{BitmapFactory.decodeStream(it)};c.disconnect()
                if(bitmap!=null)runOnUiThread{logo.setImageBitmap(bitmap);fallback.visibility=View.GONE}
            }catch(_:Exception){}
        }
        return card
    }

    private fun cardBg(color:Int,focus:Boolean)=GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(if(focus)Color.rgb(38,43,64) else Color.rgb(23,28,43),Color.rgb(13,17,28))
    ).apply{cornerRadius=dp(18).toFloat();setStroke(if(focus)dp(3) else dp(1),if(focus)color else Color.rgb(49,58,78))}

    @SuppressLint("SetJavaScriptEnabled")
    private fun openLibrary(lib:Library){
        root.removeAllViews()
        val web=WebView(this).apply{
            setBackgroundColor(Color.BLACK);isFocusable=true;isFocusableInTouchMode=true
            settings.javaScriptEnabled=true;settings.domStorageEnabled=true;settings.databaseEnabled=true
            settings.mediaPlaybackRequiresUserGesture=false;settings.javaScriptCanOpenWindowsAutomatically=true
            settings.setSupportMultipleWindows(false);settings.loadsImagesAutomatically=true
            settings.useWideViewPort=true;settings.loadWithOverviewMode=false
            settings.builtInZoomControls=false;settings.displayZoomControls=false
            CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(this,true)
            webChromeClient=object:WebChromeClient(){
                override fun onShowCustomView(view:View?,callback:CustomViewCallback?){
                    if(view==null||fullView!=null){callback?.onCustomViewHidden();return}
                    fullView=view;fullCallback=callback;root.removeView(this@apply)
                    root.addView(view,FrameLayout.LayoutParams(-1,-1));hideSystemUi()
                }
                override fun onHideCustomView(){hideFullView()}
            }
            webViewClient=object:WebViewClient(){
                override fun shouldOverrideUrlLoading(view:WebView?,request:WebResourceRequest?)=false
            }
        }
        webView=web;root.addView(web,FrameLayout.LayoutParams(-1,-1));web.loadUrl(lib.url);web.requestFocus(View.FOCUS_DOWN)
    }

    private fun hideFullView(){
        fullView?.let{root.removeView(it)};fullView=null;fullCallback?.onCustomViewHidden();fullCallback=null
        webView?.let{if(it.parent==null)root.addView(it,FrameLayout.LayoutParams(-1,-1));it.requestFocus()}
        hideSystemUi()
    }

    private fun showSplash(){
        val splash=IntroView(this);root.addView(splash,FrameLayout.LayoutParams(-1,-1))
        tts=TextToSpeech(this,this)
        splash.postDelayed({root.removeView(splash);tts?.stop()},1950)
    }
    override fun onInit(status:Int){
        if(status==TextToSpeech.SUCCESS){
            tts?.language=Locale("ru","RU");tts?.setPitch(.72f);tts?.setSpeechRate(.78f)
            tts?.speak("КиноФликс",TextToSpeech.QUEUE_FLUSH,null,"kinoflix_intro")
        }
    }
    private class IntroView(context:Context):View(context){
        private val p=Paint(Paint.ANTI_ALIAS_FLAG);private var phase=0f
        init{android.animation.ValueAnimator.ofFloat(0f,1f).apply{duration=1850;addUpdateListener{phase=it.animatedValue as Float;invalidate()};start()}}
        override fun onDraw(c:Canvas){
            super.onDraw(c);val x=width/2f;val y=height/2f
            c.drawColor(Color.rgb(5,7,14))
            p.shader=RadialGradient(x,y,maxOf(width,height).toFloat()*.7f,intArrayOf(Color.argb(90,255,91,35),Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
            c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p);p.shader=null
            p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=Color.argb((180*(1-phase)).toInt(),255,168,75)
            c.drawCircle(x,y,30f+phase*minOf(width,height)*.42f,p);p.style=Paint.Style.FILL
            p.textAlign=Paint.Align.CENTER;p.typeface=android.graphics.Typeface.create("sans-serif-black",1)
            p.textSize=minOf(46f,width*.12f);p.setShadowLayer(28f,0f,0f,Color.rgb(255,92,35))
            p.color=Color.WHITE;p.alpha=(255*minOf(1f,phase*3)).toInt()
            c.drawText("KinoFlix",x,y+p.textSize*.35f,p);p.clearShadowLayer()
            p.textSize=10f;p.color=Color.rgb(220,177,140);c.drawText("P A S H A   T V",x,y+42f,p)
        }
    }

    private fun hideSystemUi(){
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility=(View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }
    override fun onWindowFocusChanged(hasFocus:Boolean){super.onWindowFocusChanged(hasFocus);if(hasFocus)hideSystemUi()}
    @Deprecated("Deprecated by Android")
    override fun onBackPressed(){
        if(fullView!=null)hideFullView() else{
            val web=webView
            if(web!=null&&web.canGoBack())web.goBack() else if(web!=null)showHome() else super.onBackPressed()
        }
    }
    override fun onKeyDown(keyCode:Int,event:KeyEvent?):Boolean{
        val web=webView
        val keys=keyCode in setOf(KeyEvent.KEYCODE_DPAD_UP,KeyEvent.KEYCODE_DPAD_DOWN,KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,KeyEvent.KEYCODE_DPAD_CENTER,KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_MEDIA_PLAY,KeyEvent.KEYCODE_MEDIA_PAUSE,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        if(web!=null&&keys&&event!=null)return(fullView?:web).dispatchKeyEvent(event)
        return super.onKeyDown(keyCode,event)
    }
    override fun onDestroy(){tts?.stop();tts?.shutdown();webView?.stopLoading();webView?.destroy();super.onDestroy()}
}