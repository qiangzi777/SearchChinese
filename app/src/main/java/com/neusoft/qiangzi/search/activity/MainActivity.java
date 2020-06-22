package com.neusoft.qiangzi.search.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.neusoft.qiangzi.search.R;
import com.neusoft.qiangzi.search.baidu.BaiduOcr;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BaiduOcr baiduOcr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getString(R.string.mainActivityTitle));
        baiduOcr = BaiduOcr.getInstance(this);
        baiduOcr.init();
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
        Toast.makeText(this,"强子正努力开发中，敬请期待！",Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        super.onDestroy();
        baiduOcr.release();
    }
}
