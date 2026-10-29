package com.freetube.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout

class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var root: FrameLayout
    private var customView: View? = null
    private var customCallback: WebChromeClient.CustomViewCallback? = null
    private val base = "https://app.freetube.local/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = FrameLayout(this)
        web = WebView(this)
        root.addView(web, ViewGroup.LayoutParams(-1, -1))
        setContentView(root)

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean {
                val u = r.url
                if (u.host == "app.freetube.local" || u.host?.endsWith("youtube.com") == true &&
                    u.path?.startsWith("/embed") == true) return false
                startActivity(Intent(Intent.ACTION_VIEW, u)); return true
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(v: View, cb: CustomViewCallback) {
                customView = v; customCallback = cb
                root.addView(v, ViewGroup.LayoutParams(-1, -1))
                web.visibility = View.GONE
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
            override fun onHideCustomView() {
                customView?.let { root.removeView(it) }
                customView = null; customCallback?.onCustomViewHidden()
                web.visibility = View.VISIBLE
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
        val html = assets.open("index.html").bufferedReader().use { it.readText() }
        web.loadDataWithBaseURL(base, html, "text/html", "UTF-8", null)
    }

    override fun onPause() { super.onPause(); web.onPause() }
    override fun onResume() { super.onResume(); web.onResume() }
    override fun onBackPressed() {
        if (customView != null) web.webChromeClient?.onHideCustomView()
        else if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
