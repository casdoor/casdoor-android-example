package com.example.casdoor_android_example;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ZoomButtonsController;

import java.lang.reflect.Field;

public class WebviewUtils {
    private static final String APP_CACAHE_DIRNAME = "/webcache";

    public static void hideWebViewZoomControls(WebView webView) {
        webView.getSettings().setDisplayZoomControls(false);

    }

    public static void setZoomControlGone(View view) {
        try {
            Class classType = WebView.class;
            Field field = classType.getDeclaredField("mZoomButtonsController");
            field.setAccessible(true);
            ZoomButtonsController mZoomButtonsController = new ZoomButtonsController(view);
            mZoomButtonsController.getZoomControls().setVisibility(View.GONE);

            try {
                field.set(view, mZoomButtonsController);
            } catch (IllegalArgumentException var5) {
                var5.printStackTrace();
            } catch (IllegalAccessException var6) {
                var6.printStackTrace();
            }
        } catch (SecurityException var7) {
            var7.printStackTrace();
        } catch (NoSuchFieldException var8) {
            var8.printStackTrace();
        }

    }

    public static void loadWebView(WebView webView, Context context) {
        if (Build.VERSION.SDK_INT >= 19) {
            WebView.setWebContentsDebuggingEnabled(false);
        }

        hideWebViewZoomControls(webView);
        webView.setHorizontalScrollBarEnabled(false);
        WebSettings webSettings = webView.getSettings();
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDatabaseEnabled(true);
        String cacheDirPath = context.getFilesDir().getAbsolutePath() + "/webcache";
        webSettings.setDatabasePath(cacheDirPath);
        String ua = webSettings.getUserAgentString();// 获取头信息
        webSettings.setUserAgentString(ua + "AndroidApp");// 设置头信息
        webSettings.setAppCachePath(cacheDirPath);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(0);
        }

    }

}
