package com.repoint.dashboard.activity

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class WebBotActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this).apply {
            WebView.setWebContentsDebuggingEnabled(true)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()

            val url = intent?.getStringExtra("bot_url") ?: "https://repoint.app"
            loadUrl(url)
        }
        setContentView(webView)
    }
}
