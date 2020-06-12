package com.neusoft.qiangzi.search;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.ResponseResult;
import com.baidu.ocr.sdk.model.WordSimple;

import java.io.File;

public class ChineseResultActivity extends AppCompatActivity {

    private static final String TAG = "ChineseResultActivity";
    private BaiduOcr baiduOcr;

    private ImageView imageView;
    private LinearLayout resultLayout;
//    private PinyinTextView pinyinTextView;
//    private SpellTextView spellTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chinese_result);

        imageView = findViewById(R.id.imageView);
//        pinyinTextView = findViewById(R.id.pinyinTextView);
//        spellTextView = findViewById(R.id.spellTextView);
        resultLayout = findViewById(R.id.resultLayout);

        baiduOcr = BaiduOcr.getInstance();
        baiduOcr.setContext(this);
        //设置获取相机图片监听器
        baiduOcr.setOnImageListener(new BaiduOcr.OnShotImageListener() {
            @Override
            public void imageResult(String fpath) {
                //显示拍照图片
                imageView.setImageURI(Uri.fromFile(new File(fpath)));
            }
        });
        baiduOcr.setOnRecgResultListener(new BaiduOcr.OnRecgResultListener() {
            @Override
            public void onResult(ResponseResult result) {

                GeneralResult r = (GeneralResult)result;//通用、高精度文字识别结果
                Log.d(TAG, "onResult: "+r.getJsonRes());

                if(r.getWordList().size()==0){
                    Log.d(TAG, "onResult: recgnized nothing!");
                    resultLayout.removeAllViews();
//                    pinyinTextView.setText("");
//                    spellTextView.setStringResource("");
                    return;
                }

                int charCount = 0;
                for (WordSimple word : r.getWordList()) {
                    String strWord = word.getWords();
                    for (int i =0;i<strWord.length();i++) {
                        charCount++;
                        if(charCount>10){
                            Log.d(TAG, "onResult: too many chars, skip!");
                            return;
                        }
                        Log.d(TAG, "onResult: char="+strWord.charAt(i));
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
}
