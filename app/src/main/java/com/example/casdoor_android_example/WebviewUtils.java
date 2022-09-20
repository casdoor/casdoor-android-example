package com.example.casdoor_android_example;

import android.annotation.SuppressLint;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebviewUtils {

    @SuppressLint("SetJavaScriptEnabled")
    public static void loadWebView(WebView webView) {
        webView.getSettings().setDisplayZoomControls(false);
        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

    }

}
