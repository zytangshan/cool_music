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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.davidzhao.music.Database.AlbumInfoDB;
import com.davidzhao.music.Database.ArtistInfoDB;
import com.davidzhao.music.Database.FavoriteInfoDB;
import com.davidzhao.music.Database.FolderInfoDB;
import com.davidzhao.music.Database.MusicInfoDB;
import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.Utils.MediaTimer;
import com.davidzhao.music.Utils.MusicUtils;
import com.davidzhao.music.aidl.IMediaService;
import com.davidzhao.music.application.MusicApp;
import com.davidzhao.music.interfaces.IConstant;
import com.davidzhao.music.interfaces.IOnServiceConnectComplete;
import com.davidzhao.music.model.MusicInfo;
import com.davidzhao.music.service.MusicControl;
import com.davidzhao.music.service.ServiceManager;
import com.davidzhao.music.ui.MainBottomManager;
import com.davidzhao.music.ui.MyMusicUI;
import com.davidzhao.music.ui.SlidingDrawerManager;
import com.davidzhao.music.ui.UIManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-3.
 */

public class MainFragment extends Fragment implements IConstant,
        IOnServiceConnectComplete, UIManager.OnRefreshListener{
    private GridView mGridView;
    private GridViewAdapter mAdapter;
    private RelativeLayout mMainLayout;
    private RelativeLayout mBottomLayout;

    private MusicInfoDB mMusicDB;
    private FolderInfoDB mFolderDB;
    private ArtistInfoDB mArtistDB;
    private AlbumInfoDB mAlbumDB;
    private FavoriteInfoDB mFavoriteDB;

    MainBottomManager mainBottomManager;
    private MediaTimer mMediaTimer;
    private MusicPlayBroadcast mPlayBroadcast;
    public UIManager mUIManager;
    private ServiceManager mServiceManager;
    private SlidingDrawerManager mSdm;
    public static int firstOpenMusic;
    private List<MusicInfo> musicInfoList;
    private MusicInfo music;
    private MusicControl musicControl;
    private MyMusicUI myMusicUI;

    private Bitmap defaultArtwork;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMusicDB = new MusicInfoDB(getActivity());
        mFolderDB = new FolderInfoDB(getActivity());
        mArtistDB = new ArtistInfoDB(getActivity());
        mAlbumDB = new AlbumInfoDB(getActivity());
        mFavoriteDB = new FavoriteInfoDB(getActivity());
        mServiceManager = MusicApp.mServiceManager;
        musicControl = new MusicControl(getContext());

        defaultArtwork = BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.img_album_background);

        firstOpenMusic = 1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_main1, container, false);
        mGridView = (GridView) view.findViewById(R.id.gridview);
        mAdapter = new GridViewAdapter();

        mMainLayout = (RelativeLayout) view
                .findViewById(R.id.main_layout);

        mBottomLayout = (RelativeLayout) view.findViewById(R.id.bottomLayout);

        mGridView.setAdapter(mAdapter);
        mServiceManager.connectService();
        mServiceManager.setOnServiceConnectComplete(this);
        mUIManager = new UIManager(getActivity(), view);
        mUIManager.setOnRefreshListener(this);
        refreshNum();
        myMusicUI = new MyMusicUI(getActivity(),mUIManager);

        mSdm = new SlidingDrawerManager(getActivity(), mServiceManager, view);
        mainBottomManager = new MainBottomManager(getActivity(), view);
        mMediaTimer = new MediaTimer(mSdm.mHandler, mainBottomManager.mHandler);
        mSdm.setMusicTimer(mMediaTimer);

        mPlayBroadcast = new MusicPlayBroadcast();
        IntentFilter filter = new IntentFilter(BROADCAST_NAME);
        filter.addAction(BROADCAST_NAME);
        getActivity().registerReceiver(mPlayBroadcast, filter);
        view.setFocusable(true);
        mBottomLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int oldY = 0;
                Log.e("david", "touch main fragment ");
                int bottomTop = mBottomLayout.getTop();
                System.out.println(bottomTop);
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    oldY = (int) motionEvent.getRawY();
                    Log.d("david", "bottomtop = " + bottomTop + "oldy =" + oldY);
                    if (oldY > bottomTop) {
                        mSdm.open();
                    }
                }
                return true;
            }
        });
        MusicPreferences mSp = new MusicPreferences(getActivity());
        String mDefaultBgPath = mSp.getMusicPath();
        Bitmap bitmap = getBitmapByPath(mDefaultBgPath);
        if(bitmap != null) {
            mMainLayout.setBackground(new BitmapDrawable(getActivity().getResources(), bitmap));
        }

        //如果第一次进来 SharedPreference中没有数据
        if(TextUtils.isEmpty(mDefaultBgPath)) {
            mSp.saveVideoPath("004.jpg");
        }

        //mainFragment display music if exist when first enter
        Log.e("david", "load first song" + firstOpenMusic);
        if (firstOpenMusic == 1) {
            //ready to open first music
            musicInfoList = MusicUtils.queryMusic(getActivity(), START_FROM_LOCAL);
            if (musicInfoList.size() != 0) {
                music = musicInfoList.get(0);
                Log.e("david", "load first song" + music.musicName + ":" + music.duration);
                if ((music.musicName != null) || (music.duration != 0)) {
                    mSdm.refreshUI(0, music.duration, music);
                    mSdm.showPlay(true);

                    mainBottomManager.refreshUI(0, music.duration, music);
                    mainBottomManager.showPlay(true);
                    //myMusicUI.initListView();
                    //mServiceManager.playById(music.songId);
                    //musicControl.loadMusicPrepare(music.songId);
                }
            }

        }


        return view;
    }

    public void playLoadMusic() {
        if (firstOpenMusic == 1) {
            //ready to open first music
            musicInfoList = MusicUtils.queryMusic(getActivity(), START_FROM_LOCAL);
            music = musicInfoList.get(0);
            if ((music.musicName != null) || (music.duration != 0)) {
                //mSdm.refreshUI(0, music.duration, music);
                //mSdm.showPlay(true);

                //mainBottomManager.refreshUI(0, music.duration, music);
                //mainBottomManager.showPlay(true);
                //myMusicUI.initListView();
                //mServiceManager.playById(music.songId);
                Log.e("david", "first play" + music.musicName);
                musicControl.loadMusicPrepare(music.songId);
            }
        }
    }

    /*int oldY = 0;
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {


    }*/


    private class GridViewAdapter extends BaseAdapter {

        private int[] drawable = new int[] { R.drawable.icon_local_music,
                R.drawable.icon_favorites, R.drawable.icon_folder_plus,
                R.drawable.icon_artist_plus, R.drawable.icon_album_plus };
        private String[] name = new String[] { "我的音乐", "我的最爱", "文件夹", "歌手",
                "专辑" };
        private int musicNum = 0, artistNum = 0, albumNum = 0, folderNum = 0, favoriteNum = 0;

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setNum(int music_num, int artist_num, int album_num,
                           int folder_num, int favorite_num) {
            musicNum = music_num;
            artistNum = artist_num;
            albumNum = album_num;
            folderNum = folder_num;
            favoriteNum = favorite_num;;
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
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
                holder = (ViewHolder) convertView.getTag();
            }

            switch (position) {
                case 0:// 我的音乐
                    holder.numTv.setText(musicNum + "");
                    break;
                case 1:// 我的最爱
                    holder.numTv.setText(favoriteNum + "");
                    break;
                case 2:// 文件夹
                    holder.numTv.setText(folderNum + "");
                    break;
                case 3:// 歌手
                    holder.numTv.setText(artistNum + "");
                    break;
                case 4:// 专辑
                    holder.numTv.setText(albumNum + "");
                    break;
            }

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int from = -1;
                    switch (position) {
                        case 0:// 我的音乐
                            from = START_FROM_LOCAL;
                            break;
                        case 1:// 我的最爱
                            from = START_FROM_FAVORITE;
                            break;
                        case 2:// 文件夹
                            from = START_FROM_FOLDER;
                            break;
                        case 3:// 歌手
                            from = START_FROM_ARTIST;
                            break;
                        case 4:// 专辑
                            from = START_FROM_ALBUM;
                            break;
                    }
                    mUIManager.setContentType(from);
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

    public void refreshNum() {
        int musicCount = mMusicDB.getDataCount();
        int artistCount = mArtistDB.getDataCount();
        int albumCount = mAlbumDB.getDataCount();
        int folderCount = mFolderDB.getDataCount();
        int favoriteCount = mFavoriteDB.getDataCount();

        mAdapter.setNum(musicCount, artistCount, albumCount, folderCount, favoriteCount);
    }

    @Override
    public void onRefresh() {
        refreshNum();
    }

    @Override
    public void onServiceConnectComplete(IMediaService service) {
        refreshNum();
    }

    private class MusicPlayBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_NAME)) {
                MusicInfo music = new MusicInfo();
                int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
                int curPlayIndex = intent.getIntExtra(PLAY_MUSIC_INDEX, -1);
                Bundle bundle = intent.getBundleExtra(MusicInfo.KEY_MUSIC);
                if (bundle != null) {
                    music = bundle.getParcelable(MusicInfo.KEY_MUSIC);
                }
                switch (playState) {
                    case MPS_INVALID:// 考虑后面加上如果文件不可播放直接跳到下一首
                        mMediaTimer.stopTimer();
                        mSdm.refreshUI(0, music.duration, music);
                        mSdm.showPlay(true);

                        mainBottomManager.refreshUI(0, music.duration, music);
                        mainBottomManager.showPlay(true);
                        mServiceManager.next();
                        break;
                    case MPS_PAUSE:
                        mMediaTimer.stopTimer();
                        mSdm.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mSdm.showPlay(true);

                        mainBottomManager.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mainBottomManager.showPlay(true);

                        mServiceManager.cancelNotification();
                        break;
                    case MPS_PLAYING:
                        mMediaTimer.startTimer();
                        mSdm.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mSdm.showPlay(false);

                        mainBottomManager.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mainBottomManager.showPlay(false);
                        Bitmap bitmap = MusicUtils.getCachedArtwork(getActivity(), music.songId,
                                music.albumId, defaultArtwork);
                        mServiceManager.updateNotification(bitmap, music.musicName,
                                music.artist);
                        break;
                    case MPS_PREPARE:
                        mMediaTimer.stopTimer();
                        mSdm.refreshUI(0, music.duration, music);
                        mSdm.showPlay(true);

                        mainBottomManager.refreshUI(0, music.duration, music);
                        mainBottomManager.showPlay(true);

                        // 读取歌词文件
                        mSdm.loadLyric(music);
                        break;
                }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mPlayBroadcast);
        mUIManager.unRegister();
        myMusicUI.destory();
    }
}
