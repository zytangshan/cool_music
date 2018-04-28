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
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.davidzhao.music.Adapter.VideoAdapter;
import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.Utils.VideoUtils;
import com.davidzhao.music.activity.video.PlayActivity;
import com.davidzhao.music.interfaces.IConstant;
import com.davidzhao.music.model.VideoInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-16.
 */

public class VideoListFragment extends Fragment implements IConstant {
    private ImageButton back;
    private static ListView videoList;
    private RelativeLayout mMainLayout;
    private ChangeBgReceiver changeBgReceiver;
    private static Context mContext;
    private MyHandler myHandler;
    private static VideoAdapter videoAdapter;
    private static ArrayList<VideoInfo> videoInfos;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.myvideo, container, false);
        back = (ImageButton)view.findViewById(R.id.video_backBtn);
        back.setOnClickListener(new ViewOnClickListener());
        videoList = (ListView)view.findViewById(R.id.video_listview);
        myHandler = new MyHandler();
        mContext =getContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                videoInfos = VideoUtils.getVideo(mContext);
                myHandler.sendEmptyMessage(0);
            }
        }).start();
        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String path;
                ArrayList<VideoInfo> videolist = new ArrayList<VideoInfo>();
                videolist.addAll(VideoUtils.getVideo(getContext()));
                path = videolist.get(i).data;
                Log.e("david", "open video" + path);
                Intent intent = new Intent(getActivity(), PlayActivity.class);
                intent.putExtra("path", path);
                intent.putExtra("position", i);
                startActivity(intent);
            }
        });
        mMainLayout = (RelativeLayout) view.findViewById(R.id.main_mymusic_layout);
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
            videoAdapter = new VideoAdapter(mContext, videoInfos);
            videoList.setAdapter(videoAdapter);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private class ViewOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.video_backBtn:
                    Log.e("david", "video back button");
                    //getActivity().getSupportFragmentManager().beginTransaction().remove(new VideoListFragment()).commit();
                    Intent intent = new Intent("Back Press");
                    intent.putExtra("Back", "Video");
                    getActivity().sendBroadcast(intent);
                    break;
                default:
                    break;
            }

        }
    }
    private class ChangeBgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            MusicPreferences sp = new MusicPreferences(context);
            String type = sp.getCurrntType();
            Log.e("david", "video list receive" + type);
            if (type.equals("video")) {
                Log.e("david", "video list receive video");
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
