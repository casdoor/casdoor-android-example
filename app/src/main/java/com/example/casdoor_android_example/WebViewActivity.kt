/*
 * Copyright 2022 The Casdoor Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.casdoor_android_example

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.casdoor_android_example.databinding.ActivityWebBinding

/**
 * Shows the Casdoor sign-in page and catches the redirect to the redirect URI,
 * which carries the authorization code.
 */
class WebViewActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val redirectUri = intent.getStringExtra(EXTRA_REDIRECT_URI).orEmpty()
        val state = intent.getStringExtra(EXTRA_STATE)

        // the Casdoor web UI needs JavaScript and DOM storage
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.domStorageEnabled = true
        // always ask for the account, instead of reusing the last Casdoor session of the WebView
        CookieManager.getInstance().removeAllCookies(null)

        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (!uri.toString().startsWith(redirectUri)) {
                    return false
                }

                val result = Intent()
                val error = uri.getQueryParameter("error")
                val code = uri.getQueryParameter("code")
                when {
                    error != null ->
                        result.putExtra(EXTRA_ERROR, uri.getQueryParameter("error_description") ?: error)
                    uri.getQueryParameter("state") != state || code.isNullOrEmpty() ->
                        result.putExtra(EXTRA_ERROR, "Invalid state or code, please sign in again")
                    else -> result.putExtra(EXTRA_CODE, code)
                }
                setResult(RESULT_OK, result)
                finish()
                return true
            }
        }
        intent.getStringExtra(EXTRA_URL)?.let { binding.webview.loadUrl(it) }
    }

    companion object {
        const val EXTRA_URL = "url"
        const val EXTRA_REDIRECT_URI = "redirectUri"
        const val EXTRA_STATE = "state"
        const val EXTRA_CODE = "code"
        const val EXTRA_ERROR = "error"
    }
}
