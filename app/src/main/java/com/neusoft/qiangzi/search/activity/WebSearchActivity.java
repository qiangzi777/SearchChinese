package com.neusoft.qiangzi.search.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.neusoft.qiangzi.search.R;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class WebSearchActivity extends AppCompatActivity {

    private static final String TAG = "WebSearchActivity";
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_search);

        setTitle(getString(R.string.webSearchActivityTitle));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        String word = intent.getStringExtra("word");
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setDomStorageEnabled(true);

        webView.loadUrl(getString(R.string.BAIDU_HANYU_URL) + word);
    }

    /**
     * webview客户端
     */
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                Log.d(TAG, "url="+url);
                //在这里你可以拦截url
//                if(url.equals("https://hanyu.baidu.com/")
//                        || url.contains("app.gif")
//                        || url.contains("view")
//                        || url.contains("personal")
//                        || url.contains("contact")){
//
//                }
                //重定向地址
                if(url.equals("https://hanyu.baidu.com/")){
                    Log.d(TAG, "reload page to home...");
                    Intent intent = getIntent();
                    String word = intent.getStringExtra("word");
                    view.loadUrl(getString(R.string.BAIDU_HANYU_URL) + word);
                    view.clearHistory();
                    return true;
                }
                //允许访问地址
                else if(url.contains("https://hanyu.baidu.com/zici/s?wd=")
                        ||url.contains("https://hanyu.baidu.com/s?wd=")) {
                    //获取组词
                    if ((url.contains("https://hanyu.baidu.com/zici/s?wd=")
                            && url.contains("cf=zuci")) ||
                            (url.contains("https://hanyu.baidu.com/s?wd=")
                                    && url.contains("ptype=zici"))) {
                        String str = url.substring(url.indexOf("wd=") + 3);
                        String zuci = str.substring(0, str.indexOf("&"));
                        zuci = URLDecoderString(zuci);
                        if(!zuci.endsWith("组词")) {
                            Log.d(TAG, "zuci=" + zuci);
                        }
                    }
                    view.loadUrl(url);
                    return false;
                }else {
                    return true;
                }
            } catch (Exception e) {
                Log.i("webview", "该链接无效");
                return true;
            }
        }

//        @Override
//        public void onPageFinished(WebView view, String url) {
//            view.getSettings().setJavaScriptEnabled(true);
//            super.onPageFinished(view, url);
//        }
//
//        @Override
//        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            view.getSettings().setJavaScriptEnabled(true);
//            super.onPageStarted(view, url, favicon);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// back button
                if(webView.canGoBack()) webView.goBack();
                else this.finish();
                break;
            case R.id.menu_refresh_web:
                webView.reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(webView.canGoBack()) webView.goBack();
            else this.finish();
        }
        return true;
    }

    public static String URLDecoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

}
