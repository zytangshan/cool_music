package com.davidzhao.music.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.davidzhao.music.Database.FolderInfoDB;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.model.FolderInfo;
import com.davidzhao.music.model.MusicInfo;
import com.davidzhao.music.model.VideoInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-17.
 */

public class VideoUtils {
    private static String[] proj_folder = new String[] { MediaStore.Files.FileColumns.DATA };
    public static ArrayList<VideoInfo> getVideo(Context context) {
        ArrayList<VideoInfo> videoInfos = new ArrayList<VideoInfo>();
        ContentResolver contentResolver = context.getContentResolver();
        String projection[] = new String[]{MediaStore.Video.Media.TITLE};
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        if (cursor.moveToFirst()) {
            do {
                VideoInfo videoInfo = new VideoInfo();
                videoInfo.videoName = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                videoInfo.duration = cursor.getInt(
                        cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                Log.e("david", "video title" + videoInfo.videoName);
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                        contentResolver, id, MediaStore.Video.Thumbnails.MINI_KIND, options);
                videoInfo.bitmap = bitmap;
                videoInfo.data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));

                videoInfos.add(videoInfo);
            }while (cursor.moveToNext());

        }
        cursor.close();
        return videoInfos;
    }
    public static ArrayList<FolderInfo> queryFolder(Context context) {
        Uri uri = MediaStore.Files.getContentUri("external");
        ContentResolver cr = context.getContentResolver();
        StringBuilder mSelection = new StringBuilder(MediaStore.Files.FileColumns.MEDIA_TYPE
                + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO );

        mSelection.append(") group by (" + MediaStore.Files.FileColumns.PARENT );
        ArrayList<FolderInfo> list = getFolderList(cr.query(uri, proj_folder, mSelection.toString(), null, null));
        return list;
    }
    private static ArrayList<FolderInfo> getFolderList(Cursor cursor) {
        ArrayList<FolderInfo> list = new ArrayList<FolderInfo>();
        while (cursor.moveToNext()) {
            FolderInfo info = new FolderInfo();
            String filePath = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Files.FileColumns.DATA));
            info.folder_path = filePath.substring(0,
                    filePath.lastIndexOf(File.separator));
            info.folder_name = info.folder_path.substring(info.folder_path
                    .lastIndexOf(File.separator) + 1);
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public static String makeTimeString(long milliSecs) {
        StringBuffer sb = new StringBuffer();
        long m = milliSecs / (60 * 1000);
        sb.append(m < 10 ? "0" + m : m);
        sb.append(":");
        long s = (milliSecs % (60 * 1000)) / 1000;
        sb.append(s < 10 ? "0" + s : s);
        return sb.toString();
    }
}
