package com.neusoft.qiangzi.search.activity;

import android.app.Application;
import android.util.Log;

import com.github.yoojia.anyversion.AnyVersion;
import com.github.yoojia.anyversion.Callback;
import com.github.yoojia.anyversion.NotifyStyle;
import com.github.yoojia.anyversion.Version;
import com.github.yoojia.anyversion.VersionParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: is called.");
        AnyVersion.init(this, new VersionParser() {
            @Override
            public Version onParse(String response) {
                Log.d(TAG, "onParse: response="+response);
                final JSONTokener tokener = new JSONTokener(response);
                try {
                    JSONObject json = (JSONObject) tokener.nextValue();
                    Log.d(TAG, "onParse: json="+json);
                    String name = json.getString("name");
                    String note = json.getString("note");
                    String url = json.getString("url");
                    int code = json.getInt("code");
                    Log.d(TAG, "onParse: name=" + name + ",note=" + note + ",url=" + url + ",code=" + code);
                    return new Version(name,note,url,code);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        AnyVersion version = AnyVersion.getInstance();
        version.setURL("http://www.facevall.com/source/file/download/search_dictionary_release.json");
        version.setCallback(new Callback() {
            @Override
            public void onVersion(Version version) {
                Log.d(TAG, "onVersion: callback is called....");
            }
        });
//        version.check(NotifyStyle.Dialog);
    }
}
