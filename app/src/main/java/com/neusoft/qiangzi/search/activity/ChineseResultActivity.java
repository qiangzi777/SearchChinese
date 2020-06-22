package com.neusoft.qiangzi.search.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.ResponseResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.neusoft.qiangzi.search.R;
import com.neusoft.qiangzi.search.baidu.BaiduOcr;
import com.neusoft.qiangzi.search.pinyin.PinyinUtils;
import com.neusoft.qiangzi.search.pinyin.SpellTextView;
import com.neusoft.qiangzi.search.view.WarpLinearLayout;

import java.io.File;

public class ChineseResultActivity extends AppCompatActivity {

    private static final String TAG = "ChineseResultActivity";
    private static final int MAX_DISPLAY_CHARS = 30;
    private BaiduOcr baiduOcr;

    private ImageView imageView;
    private WarpLinearLayout resultLayout;
//    private PinyinTextView pinyinTextView;
//    private SpellTextView spellTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chinese_result);

        setTitle(getString(R.string.chineseResultActivityTitle));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        imageView = findViewById(R.id.imageView);
//        pinyinTextView = findViewById(R.id.pinyinTextView);
//        spellTextView = findViewById(R.id.spellTextView);
        resultLayout = findViewById(R.id.resultLayout);

        baiduOcr = BaiduOcr.getInstance(this);
        if(!baiduOcr.isHasGotToken()){
            baiduOcr.init();
        }
        //设置获取相机图片监听器
        baiduOcr.setOnImageListener(new BaiduOcr.OnShotImageListener() {
            @Override
            public void imageResult(String fpath) {
                //显示拍照图片
                imageView.setImageURI(null);
                imageView.setImageURI(Uri.fromFile(new File(fpath)));
            }
        });
        baiduOcr.setOnRecgResultListener(new BaiduOcr.OnRecgResultListener() {
            @Override
            public void onResult(ResponseResult result) {

                GeneralResult r = (GeneralResult)result;//通用、高精度文字识别结果
                Log.d(TAG, "onResult: "+r.getJsonRes());

                resultLayout.removeAllViews();

                if(r.getWordList().size()==0){
                    Log.d(TAG, "onResult: recgnized nothing!");
                    return;
                }

                int charCount = 0;
                for (WordSimple word : r.getWordList()) {
                    String strWord = word.getWords();
                    Log.d(TAG, "onResult: word=" + strWord);
                    for (int i =0;i<strWord.length();i++) {
                        if(!PinyinUtils.isChinese(strWord.charAt(i)))continue;//判断是否为汉字
                        charCount++;
                        if(charCount>MAX_DISPLAY_CHARS){
                            Log.d(TAG, "onResult: too many chars, skip!");
                            return;
                        }
//                        Log.d(TAG, "onResult: char="+strWord.charAt(i));
                        View tv = PinyinUtils.getCharView(ChineseResultActivity.this,strWord.charAt(i));
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                SpellTextView tv = (SpellTextView)view;
                                String w = tv.getChineseString();
                                Intent i = new Intent(ChineseResultActivity.this,WebSearchActivity.class);
                                i.putExtra("word",w);
                                startActivity(i);
                            }
                        });
                        resultLayout.addView(tv);
                    }
                }
//                Word word = (Word) r.getWordList().get(0);
//                pinyinTextView.setPinyin(PinyinUtils.getPinyinString(word.getWords()));
//                pinyinTextView.setHanzi(PinyinUtils.getFormatHanzi(word.getWords()));
//                spellTextView.setStringResource(word.getWords());
            }
        });
        baiduOcr.setImageResult(getIntent());
    }

    public void onRetryShotPicBtnClick(View v){
        baiduOcr.startHighAccCharActivityForResult();
//        baiduOcr.startCommonCharWithPosActivityForResult();
//        baiduOcr.startCommonCharActivityForResult();
//        baiduOcr.startHighAccCharWithPosActivityForResult();
//        baiduOcr.startHandWrittingActivityForResult();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
        super.onActivityResult(requestCode, resultCode, i);
        i.setClass(this,ChineseResultActivity.class);
        i.putExtra("requestCode",requestCode);
        i.putExtra("resultCode",resultCode);
        baiduOcr.setImageResult(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// back button
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
