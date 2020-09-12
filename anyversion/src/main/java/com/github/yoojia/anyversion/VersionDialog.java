package com.github.yoojia.anyversion;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
class VersionDialog {

    private static final String TAG = "VersionDialog";
    private final AlertDialog dialog;

    public VersionDialog(final Context context, final Version version, final Downloads downloads) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_System_Alert)
                .setTitle(version.name)
                .setMessage(Html.fromHtml(version.note))
                .setCancelable(false)
                .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.update_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloads.submit(context, version);
                        dialog.cancel();
                    }
                })
                ;
        this.dialog = builder.create();

    }

    public void show(){
        int type;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
//            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        }else {
//            type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//        }
//        this.dialog.getWindow().setType(type);
        try{
            dialog.show();
        }catch (Exception e){
            Log.e(TAG, "show: ", e);
//            throw new IllegalArgumentException("Required " +
//                    "'<uses-permission android:name=\"android.permission.SYSTEM_ALERT_WINDOW\" />' !");
        }
    }
}
