package com.neusoft.qiangzi.search.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.github.yoojia.anyversion.AnyVersion;
import com.github.yoojia.anyversion.NotifyStyle;
import com.neusoft.qiangzi.search.R;
import com.neusoft.qiangzi.search.baidu.BaiduOcr;
import com.neusoft.qiangzi.search.data.NewWordRepository;
import com.neusoft.qiangzi.search.pinyin.PinyinUtils;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BaiduOcr baiduOcr;
    private NewWordRepository repository;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getString(R.string.mainActivityTitle));
        baiduOcr = BaiduOcr.getInstance(this);
        baiduOcr.init();

        repository = new NewWordRepository(this);

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

        // 弹出悬浮窗
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
                if(s != null && s.length()==1 && PinyinUtils.isChinese(s)){
                    repository.saveNewWord(s);
                }

                /* 打开百度汉语 */
                Intent i = new Intent(MainActivity.this, WebSearchActivity.class);
                i.putExtra("word", s);
                startActivity(i);
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
    protected void onDestroy() {
        super.onDestroy();
        baiduOcr.release();
    }
}
