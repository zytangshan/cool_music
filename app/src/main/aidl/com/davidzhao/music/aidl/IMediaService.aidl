// IMedisService.aidl
package com.davidzhao.music.aidl;
import com.davidzhao.music.model.MusicInfo;
import android.graphics.Bitmap;

// Declare any non-default types here with import statements

interface IMediaService {
    boolean play(int pos);
    boolean playById(int id);
    boolean rePlay();
    boolean pause();
    boolean prev();
    boolean next();
    int duration();
    int position();
    boolean seekTo(int progress);
    void refreshMusicList(in List<MusicInfo> musicList);
    void getMusicList(out List<MusicInfo> musicList);

    int getPlayState();
    int getPlayMode();
    void setPlayMode(int mode);
    void sendPlayStateBrocast();
    void exit();
    int getCurMusicId();
    void updateNotification(in Bitmap bitmap, String title, String name);
    void cancelNotification();
    MusicInfo getCurMusic();
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    /*void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);*/


}
