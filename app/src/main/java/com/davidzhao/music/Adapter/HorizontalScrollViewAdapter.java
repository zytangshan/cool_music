package com.davidzhao.music.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidzhao.music.R;
import com.davidzhao.music.model.ImageInfo;
import com.lidroid.xutils.BitmapUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-20.
 */

public class HorizontalScrollViewAdapter extends BaseAdapter {
    private ArrayList<String> name;
    private ArrayList<ImageInfo> imageInfos;
    private LayoutInflater layoutInflater;
    private float width, height;
    private BitmapUtils bitmapUtils;

    public HorizontalScrollViewAdapter(Context context, ArrayList<ImageInfo> imageInfos) {
        this.imageInfos = imageInfos;
        layoutInflater = LayoutInflater.from(context);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        bitmapUtils = new BitmapUtils(context, "first");
    }

    @Override
    public int getCount() {
        return imageInfos.size();
    }

    @Override
    public Object getItem(int arg0) {
        return imageInfos.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(
                    R.layout.image_pagescroll, null);
            holder.iv = (ImageView) convertView
                    .findViewById(R.id.image);
            holder.nameTv = (TextView) convertView
                    .findViewById(R.id.image_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //holder.iv.setImageBitmap(getbitMap(imageInfos.get(position).artist));

        //xutils 开源框架
        bitmapUtils.display(holder.iv, imageInfos.get(position).artist);
        bitmapUtils.configDefaultLoadingImage(R.drawable.splash);
        holder.nameTv.setText(imageInfos.get(position).imageName);

        return convertView;
    }

    private class ViewHolder {
        ImageView iv;
        TextView nameTv;
    }
    private Bitmap getbitMap(String imageFilePath) {

        Log.e("david", "getbitmap ");

        //加载图像的尺寸而不是图像本身
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap;
        int widthRatio = (int) Math.ceil(options.outWidth/(float)width);
        int heightRatio = (int) Math.ceil(options.outHeight/(float)height);

        Log.v("HEIGHTRATIO",""+heightRatio);
        Log.v("WIDTHRATIO",""+widthRatio);

        //如果两个比例都大于1，那么图像的一条边将大于屏幕
        if(heightRatio > 1 && widthRatio > 1){
            options.inSampleSize = Math.max(heightRatio,widthRatio);
        }

        //对它进行真正的解码
        options.inSampleSize = 4;
        options.inJustDecodeBounds = false; // 此处为false，不只是解码
        Log.e("david", "getbitmap 1");
        bitmap = BitmapFactory.decodeFile(imageFilePath,options);
        //修复图片方向
        /*Matrix m = repairBitmapDirection(imageFilePath);
        Log.e("david", "getbitmap 2");
        if(m != null){
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
        }
        Log.e("david", "getbitmap 3");*/

        return bitmap;
    }
    /**
     * 识别图片方向
     * @param filepath
     * @return
     */
    private Matrix repairBitmapDirection(String filepath) {
        //根据图片的filepath获取到一个ExifInterface的对象
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            e.printStackTrace();
            exif = null;
        }

        int degree = 0;
        if (exif != null) {
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            // 计算旋转角度
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
                    break;
            }

        }
        if (degree != 0) {
            // 旋转图片
            Matrix m = new Matrix();
            m.postRotate(degree);
            return m;
        }
        return null;
    }
}
