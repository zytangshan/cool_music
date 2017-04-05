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

import com.davidzhao.music.model.FolderInfo;
import com.davidzhao.music.model.ImageInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-17.
 */

public class ImageUtils {
    private static String[] proj_folder = new String[] { MediaStore.Files.FileColumns.DATA };
    public static ArrayList<ImageInfo> getImage(Context context) {
        ArrayList<ImageInfo> imageInfos = new ArrayList<ImageInfo>();
        ContentResolver contentResolver = context.getContentResolver();
        String[] pro = {MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        Log.e("david" , "imageutils getimage");
        if (cursor.moveToFirst()) {
            Log.e("david" , "imageutils getimage not null");
            do {
                ImageInfo imageInfo = new ImageInfo();
                imageInfo.imageName = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                        contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND,options);
                /*imageInfo.duration = cursor.getInt(
                        cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                Toast.makeText(context, "david image title",
                        Toast.LENGTH_SHORT).show();*/
                imageInfo.bitmap = bitmap;
                byte[] date = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                String imageDate = new String(date, 0, date.length - 1);
                imageInfo.artist = imageDate;
                Log.e("david", "image title" + imageInfo.imageName);
                imageInfos.add(imageInfo);
            }while (cursor.moveToNext());

        } else {
            Log.e("david" , "imageutils getimage null");
        }
        cursor.close();
        return imageInfos;
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

    public static Bitmap getImageThumbnail(Context context) {
        ContentResolver testcr = context.getContentResolver();
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, };
        //String whereClause = MediaStore.Images.Media.DATA + " = '" + Imagepath + "'";
        //Cursor cursor = testcr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, whereClause,null, null);
        Cursor cursor = testcr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        int _id = 0;
        String imagePath = "";
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }else if (cursor.moveToFirst()) {
            int _idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int _dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                _id = cursor.getInt(_idColumn);
                imagePath = cursor.getString(_dataColumn);
            } while (cursor.moveToNext());
        }
        cursor.close();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(testcr, _id, MediaStore.Images.Thumbnails.MINI_KIND,options);
        return bitmap;
    }
    public static ArrayList<FolderInfo> queryFolder(Context context) {
        Uri uri = MediaStore.Files.getContentUri("external");
        ContentResolver cr = context.getContentResolver();
        StringBuilder mSelection = new StringBuilder(MediaStore.Files.FileColumns.MEDIA_TYPE
                + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE );

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
}
