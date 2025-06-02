package com.repoint.dashboard.ui

import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun BotScreen(url: String) {
    val context = LocalContext.current

    AndroidView(factory = {
        WebView(context).apply {
            WebView.setWebContentsDebuggingEnabled(true)

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            webChromeClient = WebChromeClient()

            // Spoof user agent to match Trust Wallet
            settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 12; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    Log.d("WebView", "UserAgent: ${settings.userAgentString}")
                    Log.d("WebView", "Trying to load: $url")

                    Log.d("WebView", "✅ Finished loading: $url")
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    Log.d("WebView", "failed UserAgent: ${settings.userAgentString}")
                    Log.d("WebView", "failed Trying to load: $url")

                    Log.e("WebView", "❌ Error: ${error.description}")
                }
            }
            Log.d("WebView", "Loading URL: $url")
            loadUrl(url)
        }
    }, modifier = Modifier.fillMaxSize())
}
