package com.davidzhao.music.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.davidzhao.music.Adapter.MyAdapter;
import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.Utils.MediaTimer;
import com.davidzhao.music.Utils.MusicUtils;
import com.davidzhao.music.application.MusicApp;
import com.davidzhao.music.interfaces.IConstant;
import com.davidzhao.music.model.AlbumInfo;
import com.davidzhao.music.model.ArtistInfo;
import com.davidzhao.music.model.FolderInfo;
import com.davidzhao.music.model.MusicInfo;
import com.davidzhao.music.service.ServiceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-6.
 */

public class MyMusicUI extends CommonUI implements IConstant,
        View.OnTouchListener{
    private LayoutInflater mInflater;
    private Activity mActivity;

    private String TAG = MyMusicUI.class.getSimpleName();
    private MyAdapter mAdapter;
    private ListView mListView;
    private ServiceManager mServiceManager = null;
    private SlidingDrawerManager mSdm;
    private static MyMusicManager mUIm;
    private MediaTimer mMediaTimer;
    private MusicPlayBroadcast mPlayBroadcast;

    private int mFrom;
    private Object mObj;

    private RelativeLayout mBottomLayout, mMainLayout;
    private Bitmap defaultArtwork;

    private UIManager mUIManager;

    private List<MusicInfo> musicInfoList = new ArrayList<MusicInfo>();
    private MusicInfo music;


    private ImageButton play;
    private ImageButton pause;


    public MyMusicUI(Activity activity, UIManager manager) {
        this.mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        this.mUIManager = manager;
    }

    public static MyMusicManager getMyMusicManager() {
        return mUIm;
    }

    public View getView(int from) {
        return getView(from, null);
    }

    public View getView(int from, Object object) {
        View contentView = mInflater.inflate(R.layout.mymusic, null);
        mFrom = from;
        mObj = object;
        initBg(contentView);
        initView(contentView);

        return contentView;
    }

    private void initView(View view) {
        defaultArtwork = BitmapFactory.decodeResource(mActivity.getResources(),
                R.drawable.img_album_background);
        mServiceManager = MusicApp.mServiceManager;
        if (mServiceManager == null) {
            Log.d("david", "servicemanager is null");
        } else {
            Log.d("david", "servicemanager is ok");
        }

        mBottomLayout = (RelativeLayout) view.findViewById(R.id.bottomLayout);

        mListView = (ListView) view.findViewById(R.id.music_listview);
        play = (ImageButton)view.findViewById(R.id.btn_play2);
        pause = (ImageButton)view.findViewById(R.id.btn_pause2);

        mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mPlayBroadcast = new MusicPlayBroadcast();
        IntentFilter filter = new IntentFilter(BROADCAST_NAME);
        filter.addAction(BROADCAST_NAME);
        filter.addAction(BROADCAST_QUERY_COMPLETE_NAME);
        mActivity.registerReceiver(mPlayBroadcast, filter);

        mUIm = new MyMusicManager(mActivity, mServiceManager, view,
                mUIManager);
        mSdm = new SlidingDrawerManager(mActivity, mServiceManager, view);
        mMediaTimer = new MediaTimer(mSdm.mHandler, mUIm.mHandler);
        mSdm.setMusicTimer(mMediaTimer);

        Log.e("david", "log for load");
        if (!mUIm.testLoad()) {
            Log.e("david", "needload");
            musicInfoList = MusicUtils.queryMusic(mActivity, START_FROM_LOCAL);
            if (musicInfoList.size() > 0) {
                music = musicInfoList.get(0);
                mSdm.refreshUI(0, music.duration, music);
                getMyMusicManager().refreshUI(0, music.duration, music);
            }
        }

        initListView();

        initListViewStatus();
    }

    private void initBg(View view) {
        mMainLayout = (RelativeLayout) view
                .findViewById(R.id.main_mymusic_layout);
        mMainLayout.setOnTouchListener(this);
        MusicPreferences mSp = new MusicPreferences(mActivity);
        String mDefaultBgPath = mSp.getMusicPath();
        Bitmap bitmap = mUIManager.getBitmapByPath(mDefaultBgPath);
        if (bitmap != null) {
            mMainLayout.setBackgroundDrawable(new BitmapDrawable(mActivity
                    .getResources(), bitmap));
        } else {
            mMainLayout.setBackgroundResource(R.drawable.bg);
        }
    }

    private void initListViewStatus() {
        try {
            mSdm.setListViewAdapter(mAdapter);
            int playState = mServiceManager.getPlayState();
            Log.e("david", "music playstate = " + playState);
            if (playState == MPS_NOFILE || playState == MPS_INVALID) {
                return;
            }

            if (playState == MPS_PLAYING) {
                mMediaTimer.startTimer();

                List<MusicInfo> musicList = mAdapter.getData();
                int playingSongPosition = MusicUtils.seekPosInListById(musicList,
                        mServiceManager.getCurMusicId());
                mAdapter.setPlayState(playState, playingSongPosition);
                MusicInfo music = mServiceManager.getCurMusic();
                mSdm.refreshUI(mServiceManager.position(), music.duration, music);

                mUIm.refreshUI(mServiceManager.position(), music.duration, music);
                mSdm.showPlay(false);
                mUIm.showPlay(false);
            } else if (playState == MPS_PAUSE) {
                List<MusicInfo> musicList = mAdapter.getData();
                int playingSongPosition = MusicUtils.seekPosInListById(musicList,
                        mServiceManager.getCurMusicId());
                mAdapter.setPlayState(playState, playingSongPosition);
                MusicInfo music = mServiceManager.getCurMusic();
                mSdm.refreshUI(mServiceManager.position(), music.duration, music);

                mUIm.refreshUI(mServiceManager.position(), music.duration, music);
                mSdm.showPlay(true);
                mUIm.showPlay(true);
            }

            //keep music sync with main

        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }

    public void initListView() {
        mAdapter = new MyAdapter(mActivity, mServiceManager, mSdm);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                mAdapter.refreshPlayingList();
                mServiceManager
                        .playById(mAdapter.getData().get(position).songId);
            }
        });
        StringBuffer select = new StringBuffer();
        switch (mFrom) {
            case START_FROM_ARTIST:
                ArtistInfo artistInfo = (ArtistInfo) mObj;
                 //select.append(" and " + Media.ARTIST + " = '"
                 //+ artistInfo.artist_name + "'");
                mAdapter.setData(MusicUtils.queryMusic(mActivity,
                        select.toString(), artistInfo.artist_name,
                        START_FROM_ARTIST));
                break;
            case START_FROM_ALBUM:
                AlbumInfo albumInfo = (AlbumInfo) mObj;
                // select.append(" and " + Media.ALBUM_ID + " = "
                // + albumInfo.album_id);
                mAdapter.setData(MusicUtils.queryMusic(mActivity,
                        select.toString(), albumInfo.album_id + "",
                        START_FROM_ALBUM));
                break;
            case START_FROM_FOLDER:
                FolderInfo folderInfo = (FolderInfo) mObj;
                // select.append(" and " + Media.DATA + " like '"
                // + folderInfo.folder_path + File.separator + "%'");
                mAdapter.setData(MusicUtils.queryMusic(mActivity,
                        select.toString(), folderInfo.folder_path,
                        START_FROM_FOLDER));
                break;
            case START_FROM_FAVORITE:
                mAdapter.setData(MusicUtils.queryFavorite(mActivity),
                        START_FROM_FAVORITE);
                break;
            default:
                mAdapter.setData(MusicUtils.queryMusic(mActivity, START_FROM_LOCAL));
                break;
        }
    }

    private class MusicPlayBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("david", "test MyMusicUI receive broadcat");
            if (intent.getAction().equals(BROADCAST_NAME)) {
                MusicInfo music = new MusicInfo();
                int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
                int curPlayIndex = intent.getIntExtra(PLAY_MUSIC_INDEX, -1);
                Bundle bundle = intent.getBundleExtra(MusicInfo.KEY_MUSIC);
                if (bundle != null) {
                    music = bundle.getParcelable(MusicInfo.KEY_MUSIC);
                }
                mAdapter.setPlayState(playState, curPlayIndex);
                switch (playState) {
                    case MPS_INVALID:// 考虑后面加上如果文件不可播放直接跳到下一首
                        mMediaTimer.stopTimer();
                        mSdm.refreshUI(0, music.duration, music);
                        mSdm.showPlay(true);

                        mUIm.refreshUI(0, music.duration, music);
                        mUIm.showPlay(true);
                        mServiceManager.next();
                        break;
                    case MPS_PAUSE:
                        Log.e("david" , " music bottom receive pause");
                        mMediaTimer.stopTimer();
                        mSdm.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mSdm.showPlay(true);

                        mUIm.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mUIm.showPlay(true);

                        mServiceManager.cancelNotification();
                        break;
                    case MPS_PLAYING:
                        Log.e("david" , " music bottom receive playing");
                        mMediaTimer.startTimer();
                        mSdm.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mSdm.showPlay(false);

                        mUIm.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mUIm.showPlay(false);

                        Bitmap bitmap = MusicUtils.getCachedArtwork(mActivity, music.songId,
                                music.albumId, defaultArtwork);
                        // Bitmap bitmap = MusicUtils.getArtwork(getActivity(),
                        // music._id, music.albumId);
                        // 更新顶部notification
                        mServiceManager.updateNotification(bitmap, music.musicName,
                                music.artist);

                        break;
                    case MPS_PREPARE:
                        mMediaTimer.stopTimer();
                        mSdm.refreshUI(0, music.duration, music);
                        mSdm.showPlay(true);

                        mUIm.refreshUI(0, music.duration, music);
                        mUIm.showPlay(true);

                        // 读取歌词文件
                        mSdm.loadLyric(music);
                        break;
                }
            }
        }
    }

    int oldY = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int bottomTop = mBottomLayout.getTop();
        System.out.println(bottomTop);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            oldY = (int) event.getY();
            Log.d("david", "bottomtop = " + bottomTop + "oldy =" + oldY);
            if (oldY > bottomTop) {
                mSdm.open();
            }
        }
        return true;
    }

    @Override
    protected void setBgByPath(String path) {
        Bitmap bitmap = mUIManager.getBitmapByPath(path);
        if (bitmap != null) {
            mMainLayout.setBackgroundDrawable(new BitmapDrawable(mActivity
                    .getResources(), bitmap));
        }
    }

    @Override
    public View getView() {
        return null;
    }
    public void destory() {
        if (mPlayBroadcast != null) {
            mActivity.unregisterReceiver(mPlayBroadcast);
        }
    }

}
