package com.github.yoojia.anyversion;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.File;

import androidx.core.content.FileProvider;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
class Installations {
    private static final String TAG = "Installations";

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (!Downloads.KEEPS.contains(reference)) return;
            // 下载完成，自动安装
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(reference);
            DownloadManager download = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor cursor = download.query(query);
            try{
                if (cursor.moveToFirst()) {
                    int fileUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                    String fileUri = cursor.getString(fileUriIdx);
                    String fileName = null;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        if (fileUri != null) {
                            fileName = Uri.parse(fileUri).getPath();
                        }
                    } else {
                        //Android 7.0以上的方式：请求获取写入权限，这一步报错
                        //过时的方式：DownloadManager.COLUMN_LOCAL_FILENAME
                        int fileNameIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                        fileName = cursor.getString(fileNameIdx);
                    }
                    Log.d(TAG, "onReceive: filename="+fileName);
                    if (fileName.endsWith(".apk")){
                        Log.d(TAG, "onReceive: start install...");
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri contentUri = FileProvider.getUriForFile(context, context.getString(R.string.fileProvider_authorities), new File(fileName));
                            install.setDataAndType(contentUri, "application/vnd.android.package-archive");
                        } else {
                            install.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
                        }
                        context.startActivity(install);
                    }
                }
            }finally {
                cursor.close();
            }

        }
    };

    public void register(Context context){
        Preconditions.requiredMainUIThread();
        context.getApplicationContext().registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void unregister(Context context){
        Preconditions.requiredMainUIThread();
        context.getApplicationContext().unregisterReceiver(downloadReceiver);
    }
}
