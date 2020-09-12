package com.neusoft.qiangzi.search.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.neusoft.qiangzi.search.R;

public class AboutActivity extends AppCompatActivity {

    TextView tvVersionName,tvVersionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setTitle(getString(R.string.aboutAppActivityTitle));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        tvVersionName = findViewById(R.id.tvVersionName);
        tvVersionCode = findViewById(R.id.tvVersionCode);

        setAppInfo();
    }

    private void setAppInfo() {
        try {
            String pkName = getPackageName();
            String versionName = getPackageManager().getPackageInfo(pkName, 0).versionName;
            int versionCode = getPackageManager().getPackageInfo(pkName, 0).versionCode;
            tvVersionName.setText("版本号：" + versionName);
            tvVersionCode.setText("发布序号：" + versionCode);
        } catch (Exception e) {
        }
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