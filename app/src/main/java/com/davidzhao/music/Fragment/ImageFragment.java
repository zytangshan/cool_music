package com.davidzhao.music.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.Utils.ImageUtils;
import com.davidzhao.music.interfaces.IConstant;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-16.
 */

public class ImageFragment extends Fragment implements IConstant {
    private ImageGridFragment imageGridFragment;
    private ImageFolderFragment imageFolderFragment;
    private BackBroadcastReceiver backBroadcastReceiver;
    private RelativeLayout mMainLayout;
    private ChangeBgReceiver changeBgReceiver;
    private static GridViewAdapter myAdapter;
    private MyHandler myHandler;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.picture_main,container,false);
        myAdapter = new ImageFragment.GridViewAdapter();
        GridView videoGridView = (GridView) view.findViewById(R.id.picture_gridview);
        videoGridView.setAdapter(myAdapter);
        myHandler = new MyHandler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                imageGridFragment = new ImageGridFragment();
                imageFolderFragment = new ImageFolderFragment();
                myAdapter.setNum(ImageUtils.getImage(getContext()).size(),
                        ImageUtils.queryFolder(getContext()).size(), 0);
                myHandler.sendEmptyMessage(0);
            }
        }).start();

        backBroadcastReceiver = new BackBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("Image Back Press");
        intentFilter.addAction("Image Back Press");
        getActivity().registerReceiver(backBroadcastReceiver, intentFilter);
        mMainLayout = (RelativeLayout) view.findViewById(R.id.main_layout);
        IntentFilter filter = new IntentFilter(BROADCAST_CHANGEBG);
        changeBgReceiver = new ChangeBgReceiver();
        getActivity().registerReceiver(changeBgReceiver, filter);
        MusicPreferences mSp = new MusicPreferences(getActivity());
        String mDefaultBgPath = mSp.getImagePath();
        Bitmap bitmap = getBitmapByPath(mDefaultBgPath);
        if(bitmap != null) {
            mMainLayout.setBackground(new BitmapDrawable(getActivity().getResources(), bitmap));
        }

        //如果第一次进来 SharedPreference中没有数据
        if(TextUtils.isEmpty(mDefaultBgPath)) {
            mSp.saveVideoPath("004.jpg");
        }

        return view;
    }

    public static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            myAdapter.notifyDataSetChanged();
        }
    }

    private class GridViewAdapter extends BaseAdapter {

        private int[] drawable = new int[] { R.drawable.icon_local_music,
                R.drawable.icon_favorites, R.drawable.icon_folder_plus};
        private String[] name = new String[] { "我的图片", "我的最爱", "文件夹" };
        private int videoNum = 0, folderNum = 0, favoriteNum = 0;

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setNum(int music_num, int folder_num, int favorite_num) {
            videoNum = music_num;
            folderNum = folder_num;
            favoriteNum = favorite_num;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {

            ImageFragment.GridViewAdapter.ViewHolder holder;
            if (convertView == null) {
                holder = new ImageFragment.GridViewAdapter.ViewHolder();
                convertView = getActivity().getLayoutInflater().inflate(
                        R.layout.main_gridview_item, null);
                holder.iv = (ImageView) convertView
                        .findViewById(R.id.gridview_item_iv);
                holder.nameTv = (TextView) convertView
                        .findViewById(R.id.gridview_item_name);
                holder.numTv = (TextView) convertView
                        .findViewById(R.id.gridview_item_num);
                convertView.setTag(holder);
            } else {
                holder = (ImageFragment.GridViewAdapter.ViewHolder) convertView.getTag();
            }

            switch (position) {
                case 0:// 我的音乐
                    holder.numTv.setText(videoNum + "");
                    break;
                case 1:// 我的最爱
                    holder.numTv.setText(favoriteNum + "");
                    break;
                case 2:// 文件夹
                    holder.numTv.setText(folderNum + "");
            }

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int from = -1;
                    switch (position) {
                        case 0:// 我的音乐
                            from = START_FROM_LOCAL;
                            getImage();
                            break;
                        case 1:// 我的最爱
                            from = START_FROM_FAVORITE;
                            break;
                        case 2:// 文件夹
                            from = START_FROM_FOLDER;
                            getFolder();
                            break;
                    }
                    //mUIManager.setContentType(from);
                }
            });

            holder.iv.setImageResource(drawable[position]);
            holder.nameTv.setText(name[position]);

            return convertView;
        }

        private class ViewHolder {
            ImageView iv;
            TextView nameTv, numTv;
        }
    }

    private class BackBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String content = intent.getStringExtra("Back").toString();
            if (content.equals("Image")) {
                Log.e("david", "receive back");
                getChildFragmentManager().beginTransaction().remove(imageGridFragment).commit();
                getChildFragmentManager().beginTransaction().remove(imageFolderFragment).commit();
            } else if (content.equals("Folder")) {
                getChildFragmentManager().beginTransaction().remove(imageFolderFragment).commit();
            }
        }
    }

    public void getImage() {
        getChildFragmentManager().beginTransaction().replace(R.id.main_layout,
                imageGridFragment).commit();
    }
    public void getFolder() {
        getChildFragmentManager().beginTransaction().replace(R.id.main_layout,
                imageFolderFragment).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(backBroadcastReceiver);
    }
    private class ChangeBgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicPreferences sp = new MusicPreferences(context);
            String type = sp.getCurrntType();
            if (type.equals("image")) {
                String path = intent.getStringExtra("path");
                Bitmap bitmap = getBitmapByPath(path);
                if (bitmap != null) {
                    mMainLayout.setBackground(new BitmapDrawable(getActivity().getResources(), bitmap));
                }
                /*if (commonUI != null) {
                    commonUI.setBgByPath(path);
                }*/
            }
        }
    }
    public Bitmap getBitmapByPath(String path) {
        AssetManager am = getActivity().getAssets();
        Bitmap bitmap = null;
        try {
            InputStream is = am.open("bkgs/" + path);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
