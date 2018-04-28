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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.davidzhao.music.Adapter.ImageAdapter;
import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.Utils.VideoUtils;
import com.davidzhao.music.interfaces.IConstant;
import com.davidzhao.music.model.FolderInfo;
import com.davidzhao.music.model.ImageInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-22.
 */

public class VideoFolderFragment extends Fragment implements IConstant {
    private static FolderAdapter folderAdapter;
    private ImageButton back;
    private static ListView folderList;
    private VideoListFragment videoListFragment;
    private RelativeLayout mMainLayout;
    private ChangeBgReceiver changeBgReceiver;
    private Context mContext;
    private MyHandler myHandler;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.e("david", "create view 1");
        View view = inflater.inflate(R.layout.folderbrower, container, false);
        folderList = (ListView) view.findViewById(R.id.folder_listview);
        myHandler = new MyHandler();
        mContext = getContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                folderAdapter = new FolderAdapter(mContext, VideoUtils.queryFolder(mContext));
                myHandler.sendEmptyMessage(0);
            }
        }).start();
        back = (ImageButton)view.findViewById(R.id.backBtn);
        back.setOnClickListener(new ViewOnClickListener());
        videoListFragment = new VideoListFragment();
        folderList.setOnItemClickListener(new ItemOnClickListener());
        mMainLayout = (RelativeLayout) view.findViewById(R.id.main_folder_layout);
        IntentFilter filter = new IntentFilter(BROADCAST_CHANGEBG);
        changeBgReceiver = new ChangeBgReceiver();
        getActivity().registerReceiver(changeBgReceiver, filter);
        MusicPreferences mSp = new MusicPreferences(getActivity());
        String mDefaultBgPath = mSp.getVideoPath();
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
            folderList.setAdapter(folderAdapter);
        }
    }

    public class FolderAdapter extends BaseAdapter implements IConstant {
        private ArrayList<FolderInfo> folderInfos;
        LayoutInflater layoutInflater;

        public FolderAdapter(Context context, ArrayList<FolderInfo> mfolderInfos) {
            folderInfos = new ArrayList<FolderInfo>();
            folderInfos.addAll(mfolderInfos);
            Log.e("david", "folderInfos.size()" + folderInfos.size());
            layoutInflater = LayoutInflater.from(context);
            mContext = context;
        }

        @Override
        public int getCount() {
            return folderInfos.size();
        }

        @Override
        public Object getItem(int arg0) {
            return folderInfos.get(arg0);
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
                        R.layout.folderbrower_listitem, null);
                holder.nameTv = (TextView) convertView
                        .findViewById(R.id.folder_name_tv);
                holder.pathTv = (TextView) convertView
                        .findViewById(R.id.folder_path_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

        /*convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (position) {

                }
            }
        });*/

            Log.e("david", "video folder" + folderInfos.get(position).folder_name +
                    folderInfos.get(position).folder_path);
            holder.nameTv.setText(folderInfos.get(position).folder_name);
            holder.pathTv.setText(folderInfos.get(position).folder_path);

            return convertView;
        }


    }
    private class ViewHolder {
        TextView nameTv, pathTv;
    }
    private class ViewOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backBtn:
                    Log.e("david", "video back button");
                    //getActivity().getSupportFragmentManager().beginTransaction().remove(new VideoListFragment()).commit();
                    Intent intent = new Intent("Back Press");
                    intent.putExtra("Back", "Folder");
                    getActivity().sendBroadcast(intent);
                    break;
                default:
                    break;
            }

        }
    }
    private class ItemOnClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            getVideo();
        }
    }
    private void getVideo() {
        getChildFragmentManager().beginTransaction().replace(R.id.main_folder_layout,
                videoListFragment).commit();
    }
    private class ChangeBgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicPreferences sp = new MusicPreferences(context);
            String type = sp.getCurrntType();
            if (type.equals("video")) {
                String path = intent.getStringExtra("path");
                Bitmap bitmap = getBitmapByPath(path);
                if (bitmap != null) {
                    mMainLayout.setBackground(new BitmapDrawable(mContext.getResources(), bitmap));
                }
                /*if (commonUI != null) {
                    commonUI.setBgByPath(path);
                }*/
            }
        }
    }
    public Bitmap getBitmapByPath(String path) {
        AssetManager am = mContext.getAssets();
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
