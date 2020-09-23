package com.neusoft.qiangzi.search.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.neusoft.qiangzi.search.R;
import com.neusoft.qiangzi.search.data.NewWordRepository;
import com.neusoft.qiangzi.search.pinyin.PinyinUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class WebSearchActivity extends AppCompatActivity {

    private static final String TAG = "WebSearchActivity";
    WebView webView;
    NewWordRepository newWordRepository;
    String wordUrlString = "";
    String urlString; // = "https://www.bilibili.com/video/av753885718";
    private boolean needsZuciMenu;
    private NewWordRepository repository;

    @SuppressLint("SetJavaScriptEnabled")
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
        String type = intent.getStringExtra("type");
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setLoadsImagesAutomatically(true);// 设置可以自动加载图片
        webSettings.setAllowFileAccess(true);// 可以读取文件缓存(manifest生效)
        webSettings.setAppCacheEnabled(true);// 应用可以有缓存
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setDomStorageEnabled(true);
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//        webSettings.setPluginState(WebSettings.PluginState.ON);
//        webSettings.setAllowFileAccessFromFileURLs(true);

        repository = new NewWordRepository(this);

        try {
            wordUrlString = URLEncoder.encode(word, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            urlString = getString(R.string.BAIDU_HANYU_HOME_URL);
            wordUrlString = "";
        }
        if (type!=null && type.equals("baike")) {
            urlString = getString(R.string.BAIDU_BAIKE_ITEM_URL) + wordUrlString;
            /* 保存到数据库 */
            if(word != null && !word.isEmpty()){
                repository.saveKeyWord(word, NewWordRepository.KEYWORD_TYPE.BAIKE);
            }
        }else {
            urlString = getString(R.string.BAIDU_HANYU_ZICI_URL) + wordUrlString;
            needsZuciMenu = true;
            /* 保存到数据库 */
            if(word != null && word.length()==1 && PinyinUtils.isChinese(word)){
                repository.saveNewWord(word);
            }else {
                repository.saveKeyWord(word, NewWordRepository.KEYWORD_TYPE.ZUCI);
            }
        }

        webView.loadUrl(urlString);
        newWordRepository = new NewWordRepository(this);
    }

    /**
     * webview客户端
     */
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                Log.d(TAG, "url=" + url);
                //在这里你可以拦截url
//                if(url.equals("https://hanyu.baidu.com/")
//                        || url.contains("app.gif")
//                        || url.contains("view")
//                        || url.contains("personal")
//                        || url.contains("contact")){
//
//                }
                //重定向地址
                if (url.equals(getString(R.string.BAIDU_HANYU_HOME_URL))) {
                    Log.d(TAG, "reload page to home...");
                    view.loadUrl(urlString);
                    view.clearHistory();
                    return true;
                }
                //允许访问地址
                else if (url.contains(getString(R.string.BAIDU_HANYU_ZICI_URL))
                        || url.contains(getString(R.string.BAIDU_HANYU_SEARCH_URL))) {
                    //获取组词
                    if ((url.contains(getString(R.string.BAIDU_HANYU_ZICI_URL))
                            && url.contains("cf=zuci")) ||
                            (url.contains(getString(R.string.BAIDU_HANYU_SEARCH_URL))
                                    && url.contains("ptype=zici"))) {
                        String str = url.substring(url.indexOf("wd=") + 3);
                        String zuci = str.substring(0, str.indexOf("&"));
                        zuci = URLDecoderString(zuci);
                        String word = getIntent().getStringExtra("word");
                        if (!zuci.endsWith("组词")) {
                            Log.d(TAG, "zuci=" + zuci);
                            newWordRepository.appendZuci(word, zuci);
                        }
                    }
                    view.loadUrl(url);
                    return false;
                } else if (url.contains(getString(R.string.BAIDU_BAIKE_ITEM_URL))) {
                    Log.d(TAG, "shouldOverrideUrlLoading: baike");
                    view.loadUrl(url);
                    return false;
                } else {
//                    view.loadUrl(url);
                    return true;
                }
            } catch (Exception e) {
                Log.i("webview", "该链接无效");
                return true;
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            urlString = url;
            Log.d(TAG, "onPageFinished: url="+urlString);
        }
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
        MenuItem menuItem = menu.findItem(R.id.menu_goto_zuci);
        menuItem.setVisible(needsZuciMenu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// back button
                if(webView.canGoBack()){
                    webView.goBack();
                }
                else this.finish();
                break;
            case R.id.menu_goto_zuci:
                webView.findAllAsync("组词");
                break;
            case R.id.menu_refresh_web:
                webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                webView.reload();
                break;
            case R.id.menu_open_in_browser:
                Uri uri = Uri.parse(urlString);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(webView.canGoBack()) {
                webView.goBack();
            }
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
