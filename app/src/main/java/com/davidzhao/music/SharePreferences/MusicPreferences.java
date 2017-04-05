package com.davidzhao.music.SharePreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.davidzhao.music.interfaces.IConstant;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-3.
 */

public class MusicPreferences implements IConstant{
    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;

    public MusicPreferences(Context context) {
        mSp = context.getSharedPreferences(SP_NAME,
                Context.MODE_APPEND);
        mEditor = mSp.edit();
    }

    /**
     * 保存背景图片的地址
     */
    public void saveMusicPath(String path) {
        mEditor.putString(SP_MUSIC_BG_PATH, path);
        mEditor.commit();
    }
    public void saveVideoPath(String path) {
        mEditor.putString(SP_VIDEO_BG_PATH, path);
        mEditor.commit();
    }
    public void saveImagePath(String path) {
        mEditor.putString(SP_IMAGE_BG_PATH, path);
        mEditor.commit();
    }

    /**
     * 获取背景图片的地址
     * @return
     */
    public String getMusicPath() {
        return mSp.getString(SP_MUSIC_BG_PATH, null);
    }
    public String getVideoPath() {
        return mSp.getString(SP_VIDEO_BG_PATH, null);
    }
    public String getImagePath() {
        return mSp.getString(SP_IMAGE_BG_PATH, null);
    }

    public void saveShake(boolean shake) {
        mEditor.putBoolean(SP_SHAKE_CHANGE_SONG, shake);
        mEditor.commit();
    }

    public void setCurrentType(String type){
        mEditor.putString(SP_CURRENT_TYPE, type);
        mEditor.commit();
    }
     public String getCurrntType() {
         return mSp.getString(SP_CURRENT_TYPE, "music");
     }

    public boolean getShake() {
        return mSp.getBoolean(SP_SHAKE_CHANGE_SONG, false);
    }

    public void saveAutoLyric(boolean auto) {
        mEditor.putBoolean(SP_AUTO_DOWNLOAD_LYRIC, auto);
        mEditor.commit();
    }

    public boolean getAutoLyric() {
        return mSp.getBoolean(SP_AUTO_DOWNLOAD_LYRIC, false);
    }

    public void saveFilterSize(boolean size) {
        mEditor.putBoolean(SP_FILTER_SIZE, size);
        mEditor.commit();
    }

    public boolean getFilterSize() {
        return mSp.getBoolean(SP_FILTER_SIZE, false);
    }

    public void saveFilterTime(boolean time) {
        mEditor.putBoolean(SP_FILTER_TIME, time);
        mEditor.commit();
    }

    public boolean getFilterTime() {
        return mSp.getBoolean(SP_FILTER_TIME, false);
    }
}
