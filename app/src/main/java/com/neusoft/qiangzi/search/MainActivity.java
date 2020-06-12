package com.neusoft.qiangzi.search;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BaiduOcr baiduOcr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        baiduOcr = new BaiduOcr(this);
        baiduOcr.initAccessTokenWithAkSk();
        //设置获取相机图片监听器
//        baiduOcr.setOnGotImageListener(new BaiduOcr.OnShotImageListener() {
//            @Override
//            public void imageResult(String fpath) {
//                Intent i = new Intent(MainActivity.this,ChineseResultActivity.class);
//                startActivity(i);
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        baiduOcr.setContext(this);
    }

    public void onCharRecgBtnClick(View v){
        baiduOcr.startHighAccCharActivityForResult();
//        baiduOcr.startCommonCharWithPosActivityForResult();
//        baiduOcr.startHighAccCharWithPosActivityForResult();
//        baiduOcr.startHandWrittingActivityForResult();
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

}
