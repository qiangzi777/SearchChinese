package com.neusoft.qiangzi.search.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.RecogResult;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.StatusRecogListener;
import com.baidu.speech.asr.SpeechConstant;
import com.github.yoojia.anyversion.AnyVersion;
import com.github.yoojia.anyversion.NotifyStyle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.neusoft.qiangzi.search.R;
import com.neusoft.qiangzi.search.baidu.BaiduOcr;
import com.neusoft.qiangzi.search.data.KeyWord;
import com.neusoft.qiangzi.search.data.NewWordRepository;
import com.neusoft.qiangzi.search.pinyin.PinyinUtils;
import com.neusoft.qiangzi.search.pinyin.SpellTextView;
import com.neusoft.qiangzi.search.view.WarpLinearLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BaiduOcr baiduOcr;
    private NewWordRepository repository;
    private FloatingActionButton fabVoice;
    private WarpLinearLayout resultLayout;
    private TextView tvVoiceHint;
    private ToggleButton tgbSearchBaike;
    protected MyRecognizer mRecognizer;//语音识别对象

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getString(R.string.mainActivityTitle));
        baiduOcr = BaiduOcr.getInstance(this);
        baiduOcr.init();

        repository = new NewWordRepository(this);
        repository.getAllKeyWords().observe(this, new Observer<List<KeyWord>>() {
            @Override
            public void onChanged(List<KeyWord> keyWords) {
                resultLayout.removeAllViews();
                for (int i = Math.min(keyWords.size()-1, 6); i>=0; i--) {
                    appendKeywordView(keyWords.get(i).keyWord);
                }
                if (resultLayout.getChildCount() == 0) {
                    tvVoiceHint.setVisibility(View.VISIBLE);
                    resultLayout.addView(tvVoiceHint);
                }
            }
        });

        //设置获取相机图片监听器
//        baiduOcr.setOnGotImageListener(new BaiduOcr.OnShotImageListener() {
//            @Override
//            public void imageResult(String fpath) {
//                Intent i = new Intent(MainActivity.this,ChineseResultActivity.class);
//                startActivity(i);
//            }
//        });
//        if (checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},1);
//        }

        mRecognizer = new MyRecognizer(this, recogListener);//初始化asr

        resultLayout = findViewById(R.id.resultLayout);
        tvVoiceHint = findViewById(R.id.tvVoiceHint);
        fabVoice = findViewById(R.id.fabVoice);
        tgbSearchBaike = findViewById(R.id.tgbSearchBaike);
        fabVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recogStart();
            }
        });
        tgbSearchBaike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    repository.setKeyWordType(NewWordRepository.KEYWORD_TYPE.BAIKE);
                }else {
                    repository.setKeyWordType(NewWordRepository.KEYWORD_TYPE.ZUCI);
                }
            }
        });

        initPermission();//动态权限

        // 检查版本，弹出悬浮窗
        AnyVersion version = AnyVersion.getInstance(this);
        version.check(NotifyStyle.Dialog);
    }

    @Override
    protected void onStart() {
        super.onStart();
        baiduOcr = BaiduOcr.getInstance(this);
    }

    public void onCharRecgBtnClick(View v){
        baiduOcr.startCommonCharActivityForResult();
//        baiduOcr.startHighAccCharActivityForResult();
//        baiduOcr.startCommonCharWithPosActivityForResult();
//        baiduOcr.startHighAccCharWithPosActivityForResult();
//        baiduOcr.startHandWrittingActivityForResult();
    }

    public void onNewWordsBtnClick(View v){
        Intent intent = new Intent(MainActivity.this, NewWordListActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
        super.onActivityResult(requestCode, resultCode, i);
//        baiduOcr.setImageResult(requestCode,resultCode,data);
//        Intent i = new Intent(MainActivity.this,ChineseResultActivity.class);

        if(i==null){
            Log.d(TAG, "onActivityResult: intent is null!");
            return;
        }
        i.setClass(this,ChineseResultActivity.class);
        i.putExtra("requestCode",requestCode);
        i.putExtra("resultCode",resultCode);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_page_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_main_page_search);//在菜单中找到对应控件的item
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                /* 保存生字到数据库 */
//                if(s != null && s.length()==1 && PinyinUtils.isChinese(s)){
//                    repository.saveNewWord(s);
//                }

                startWebActivity(s);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ItemAboutApp:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.itemRemoveAllKeywords:
                if (tgbSearchBaike.isChecked()) {
                    repository.deleteKeyWordsByType(NewWordRepository.KEYWORD_TYPE.BAIKE);
                } else {
                    repository.deleteKeyWordsByType(NewWordRepository.KEYWORD_TYPE.ZUCI);
                }
                break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void recogStart() {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = new LinkedHashMap<String, Object>();
        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        Log.i(TAG, "设置的start输入参数：" + params);
        // DEMO集成步骤2.2 开始识别
        mRecognizer.start(params);
    }
    IRecogListener recogListener = new StatusRecogListener() {
        @Override
        public void onAsrFinalResult(String[] results, RecogResult recogResult) {
            for (String word : results
            ) {
                if (word.endsWith("，") || word.endsWith(",") || word.endsWith("。") || word.endsWith(".")) {
                    word = word.substring(0, word.length() - 1);
                }
                appendKeywordView(word);
            }
        }
    };

    private void appendKeywordView(String word) {
        View tv = PinyinUtils.getPinyinView(MainActivity.this,word);
        tv.requestFocus();
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpellTextView tv = (SpellTextView)view;
                String w = tv.getChineseString();
                /* 保存生字到数据库 */
//                        repository.saveNewWord(w);
                startWebActivity(w);
            }
        });
        if (tvVoiceHint.getVisibility() == View.VISIBLE) {
            tvVoiceHint.setVisibility(View.INVISIBLE);
            resultLayout.removeView(tvVoiceHint);
        }
        if(resultLayout.getChildCount()>6)resultLayout.removeViewAt(0);
        resultLayout.addView(tv);
    }

    private void startWebActivity(String w) {
        /* 打开百度汉语 */
        Intent i = new Intent(MainActivity.this, WebSearchActivity.class);
        i.putExtra("word",w);
        if (tgbSearchBaike.isChecked()) {
            i.putExtra("type","baike");
        }else{
            i.putExtra("type","hanyu");
        }
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        baiduOcr.release();
        mRecognizer.release();
    }

    private void initPermission() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

}
