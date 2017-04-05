package com.davidzhao.music.application;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.davidzhao.music.activity.common.MainActivity;
import com.davidzhao.music.service.ServiceManager;

import org.xutils.x;

import java.io.File;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-6.
 */

public class MusicApp extends Application {
    public static boolean mIsSleepClockSetting = false;
    public static ServiceManager mServiceManager = null;
    private static String rootPath = "/mymusic";
    public static String lrcPath = "/lrc";
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);

        mServiceManager = new ServiceManager(this);
        mContext = getApplicationContext();
        initPath();
    }

    private void initPath() {
        String ROOT = "";
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ROOT = Environment.getExternalStorageDirectory().getPath();
        }
        rootPath = ROOT + rootPath;
        lrcPath = rootPath + lrcPath;
        File lrcFile = new File(lrcPath);
        Log.e("david", "lyric path" + lrcFile);
        if(!lrcFile.exists()) {
            lrcFile.mkdirs();
            Log.e("david", "lyric path mkdir" + lrcFile);
        }
    }

    public static void requestPermission(Activity activity) {
        int permissionCheck1 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck3 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.INTERNET);
        int permissionCheck4 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.CHANGE_CONFIGURATION);
        int permissionCheck5 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.MANAGE_DOCUMENTS);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED
                || permissionCheck2 != PackageManager.PERMISSION_GRANTED
                || permissionCheck3 != PackageManager.PERMISSION_GRANTED
                || permissionCheck4 != PackageManager.PERMISSION_GRANTED
                || permissionCheck5 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.CHANGE_CONFIGURATION,
                            Manifest.permission.MANAGE_DOCUMENTS }, 124);
        }
    }
}
