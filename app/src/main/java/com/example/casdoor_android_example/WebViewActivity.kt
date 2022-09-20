package com.example.casdoor_android_example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        val webview = findViewById<WebView>(R.id.webview)
        WebviewUtils.loadWebView(webview)

        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                val uri = Uri.parse(url)
                val code: String? = uri.getQueryParameter("code")
                if (!TextUtils.isEmpty(code)) {
                    setResult(RESULT_OK, Intent().putExtra("code", code))
                    finish()
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        intent.getStringExtra("url")?.let { webview.loadUrl(it) }
    }
}