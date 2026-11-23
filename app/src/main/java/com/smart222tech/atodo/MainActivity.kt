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
        Library("LIFT","https://liftw.ws/","liftw.ws","L",Color.rgb(70,170,255)),
        Library("LAMPA","http://lampa.maxvol.pro","lampa.maxvol.pro","L",Color.rgb(255,190,55)),
        Library("ZONA","https://w1.zona.im/","w1.zona.im","Z",Color.rgb(255,75,75))
    )
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        root=FrameLayout(this).apply{setBackgroundColor(Color.rgb(7,9,17))}
        setContentView(root); hideSystemUi(); showHome(); showSplash()
    }

    private fun showHome(){
        webView?.let{it.stopLoading();it.destroy()};webView=null
                root.removeAllViews()
        val scroll=ScrollView(this).apply{isFillViewport=true;overScrollMode=View.OVER_SCROLL_NEVER}
        val page=LinearLayout(this).apply{orientation=1;gravity=Gravity.CENTER_HORIZONTAL;setPadding(dp(24),dp(18),dp(24),dp(18))}
        scroll.addView(page,FrameLayout.LayoutParams(-1,-2))
        page.addView(TextView(this).apply{
            text="KINOFLIX";textSize=38f;typeface=android.graphics.Typeface.create("sans-serif-black",1)
            gravity=Gravity.CENTER;setTextColor(Color.WHITE);setShadowLayer(dp(18).toFloat(),0f,0f,Color.rgb(255,100,45))
        },LinearLayout.LayoutParams(-1,dp(58)))
        page.addView(TextView(this).apply{
            text="ТРИ ВИДЕОТЕКИ — ОДНО ПРИЛОЖЕНИЕ";textSize=12f;letterSpacing=.12f
            gravity=Gravity.CENTER;setTextColor(Color.rgb(179,188,208))
        },LinearLayout.LayoutParams(-1,dp(28)))
        val portrait=resources.configuration.orientation==Configuration.ORIENTATION_PORTRAIT
        val circles=LinearLayout(this).apply{orientation=if(portrait)1 else 0;gravity=Gravity.CENTER}
        libraries.forEachIndexed{index,lib->
            val size=if(portrait)dp(190) else dp(176)
            val lp=LinearLayout.LayoutParams(size,size)
            if(portrait)lp.bottomMargin=dp(22) else { if(index>0) lp.marginStart=dp(16); if(index<libraries.lastIndex) lp.marginEnd=dp(16) }
            circles.addView(makeCard(lib,size),lp)
        }
        page.addView(circles,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(34)})
        page.addView(TextView(this).apply{
            text="ТВОИ ВИДЕОТЕКИ В ОДНОМ ПРИЛОЖЕНИИ";textSize=13f;gravity=Gravity.CENTER
            setTextColor(Color.rgb(124,139,166))
        },LinearLayout.LayoutParams(-1,dp(44)).apply{topMargin=dp(20)})
        root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
    }

    private fun makeCard(lib:Library,size:Int):View{
        val card=FrameLayout(this).apply{isFocusable=true;isFocusableInTouchMode=true;background=circleBg(lib.color,false);elevation=dp(4).toFloat()}
        val content=LinearLayout(this).apply{orientation=1;gravity=Gravity.CENTER;setPadding(dp(12),dp(12),dp(12),dp(12))}
        val logoBox=FrameLayout(this)
        val fallback=TextView(this).apply{text=lib.letter;textSize=48f;gravity=Gravity.CENTER;setTextColor(lib.color);typeface=android.graphics.Typeface.DEFAULT_BOLD}
        val logo=ImageView(this).apply{scaleType=ImageView.ScaleType.FIT_CENTER}
        logoBox.addView(fallback,FrameLayout.LayoutParams(-1,-1))
        logoBox.addView(logo,FrameLayout.LayoutParams(-1,-1))
        if(lib.title=="ZONA"){
            logo.setImageResource(R.drawable.zona_logo)
            fallback.visibility=View.GONE
        }else if(lib.title=="LAMPA"){
            logo.setImageResource(R.drawable.ic_lampa)
            fallback.visibility=View.GONE
        }else{
            thread{
                try{
                    val c=URL("https://www.google.com/s2/favicons?domain_url=https://"+lib.domain+"&sz=128").openConnection() as HttpURLConnection
                    c.connectTimeout=5000;c.readTimeout=5000
                    val bitmap=c.inputStream.use{BitmapFactory.decodeStream(it)};c.disconnect()
                    if(bitmap!=null)runOnUiThread{logo.setImageBitmap(bitmap);fallback.visibility=View.GONE}
                }catch(_:Exception){}
            }
        }
        content.addView(logoBox,LinearLayout.LayoutParams(dp(88),dp(88)))
        val label=TextView(this).apply{text=lib.title;textSize=23f;gravity=Gravity.CENTER;typeface=android.graphics.Typeface.create("sans-serif-bold",1);setTextColor(Color.WHITE)}
        content.addView(label,LinearLayout.LayoutParams(-1,dp(42)).apply{topMargin=dp(8)})
        content.addView(TextView(this).apply{text="RU · UA · EN";textSize=11f;gravity=Gravity.CENTER;setTextColor(Color.rgb(174,187,211))},LinearLayout.LayoutParams(-1,dp(22)))
        card.addView(content,FrameLayout.LayoutParams(-1,-1))
        var pulse:android.animation.ValueAnimator?=null
        card.setOnFocusChangeListener{v,focus->
            pulse?.cancel();pulse=null
            if(focus){
                v.animate().scaleX(1.09f).scaleY(1.09f).translationZ(dp(22).toFloat()).setDuration(180).start()
                v.background=circleBg(lib.color,true);v.elevation=dp(22).toFloat()
                label.setTextColor(lib.color);label.setShadowLayer(dp(18).toFloat(),0f,0f,lib.color)
                pulse=android.animation.ValueAnimator.ofFloat(1f,1.035f).apply{
                    duration=850;repeatMode=android.animation.ValueAnimator.REVERSE;repeatCount=android.animation.ValueAnimator.INFINITE
                    addUpdateListener{val scale=it.animatedValue as Float;v.scaleX=1.09f*scale;v.scaleY=1.09f*scale};start()
                }
            }else{
                v.animate().scaleX(1f).scaleY(1f).translationZ(0f).setDuration(180).start()
                v.background=circleBg(lib.color,false);v.elevation=dp(4).toFloat()
                label.setTextColor(Color.WHITE);label.setShadowLayer(0f,0f,0f,Color.TRANSPARENT)
            }
        }
        card.setOnClickListener{openLibrary(lib)}
        card.setOnKeyListener{_,key,event->if(event.action==KeyEvent.ACTION_UP&&(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER)){openLibrary(lib);true}else false}
        return card
    }

    private fun circleBg(color:Int,focus:Boolean)=GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(if(focus)Color.rgb(40,49,73) else Color.rgb(20,26,42),Color.rgb(8,12,23))
    ).apply{shape=GradientDrawable.OVAL;setStroke(if(focus)dp(4) else dp(2),if(focus)color else Color.rgb(65,77,104))}

    @SuppressLint("SetJavaScriptEnabled")
    private fun openLibrary(lib:Library){
        root.removeAllViews()
        val web=WebView(this).apply{
            setBackgroundColor(Color.BLACK);isFocusable=true;isFocusableInTouchMode=true
            settings.javaScriptEnabled=true;settings.domStorageEnabled=true;settings.databaseEnabled=true
            settings.mediaPlaybackRequiresUserGesture=false;settings.javaScriptCanOpenWindowsAutomatically=true
            settings.setSupportMultipleWindows(false);settings.loadsImagesAutomatically=true
            settings.useWideViewPort=true
            settings.loadWithOverviewMode=true
            settings.userAgentString="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
            settings.builtInZoomControls=false;settings.displayZoomControls=false
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
                override fun onPageFinished(view:WebView?,url:String?){
                    super.onPageFinished(view,url)

                }
            }
        }
        webView=web
        root.addView(web,FrameLayout.LayoutParams(-1,-1))
        web.loadUrl(lib.url);web.requestFocus(View.FOCUS_DOWN)
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
        if(fullView!=null)hideFullView() else {
            val web=webView
            if(web!=null&&web.canGoBack())web.goBack() else if(web!=null)showHome() else super.onBackPressed()
        }
    }
    override fun onKeyDown(keyCode:Int,event:KeyEvent?):Boolean{
        val web=webView
        val navKeys=keyCode in setOf(KeyEvent.KEYCODE_DPAD_UP,KeyEvent.KEYCODE_DPAD_DOWN,KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,KeyEvent.KEYCODE_DPAD_CENTER,KeyEvent.KEYCODE_ENTER)
        if(web!=null && navKeys && event!=null) return (fullView?:web).dispatchKeyEvent(event)
        val keys=keyCode in setOf(KeyEvent.KEYCODE_MEDIA_PLAY,KeyEvent.KEYCODE_MEDIA_PAUSE,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        if(web!=null&&keys&&event!=null)return(fullView?:web).dispatchKeyEvent(event)
        return super.onKeyDown(keyCode,event)
    }
    override fun onDestroy(){tts?.stop();tts?.shutdown();webView?.stopLoading();webView?.destroy();super.onDestroy()}
}