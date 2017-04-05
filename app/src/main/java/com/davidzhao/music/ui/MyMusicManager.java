package com.davidzhao.music.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davidzhao.music.R;
import com.davidzhao.music.Utils.MusicUtils;
import com.davidzhao.music.activity.common.MainActivity;
import com.davidzhao.music.activity.music.MusicSearchListActivity;
import com.davidzhao.music.interfaces.IConstant;
import com.davidzhao.music.model.MusicInfo;
import com.davidzhao.music.service.ServiceManager;
import com.davidzhao.music.view.AlwaysMarqueeTextView;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-6.
 */

public class MyMusicManager implements IConstant, View.OnClickListener {
    private Activity mActivity;
    private View mView;
    private ServiceManager mServiceManager;
    private AlwaysMarqueeTextView mMusicNameTv, mArtistTv;
    private TextView mPositionTv, mDurationTv;
    private ImageButton mPlayBtn, mPauseBtn, mNextBtn, mMenuBtn;
    private ProgressBar mPlaybackProgress;
    public Handler mHandler;
    private Bitmap mDefaultAlbumIcon;
    private ImageView mHeadIcon;

    private ImageButton mSearchBtn, mBackBtn;
    private UIManager mUIManager;

    public MyMusicManager(Activity a, ServiceManager sm, View view, UIManager manager) {
        this.mActivity = a;
        this.mView = view;
        this.mUIManager = manager;
        this.mServiceManager = sm;
        initView();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                refreshSeekProgress(mServiceManager.position(),
                       mServiceManager.duration());
            }
        };
    }

    private void initView() {
        mSearchBtn = (ImageButton) findViewById(R.id.searchBtn);
        mBackBtn = (ImageButton) findViewById(R.id.backBtn);
        mMusicNameTv = (AlwaysMarqueeTextView) findViewById(R.id.musicname_tv2);
        mArtistTv = (AlwaysMarqueeTextView) findViewById(R.id.artist_tv2);
        mPositionTv = (TextView) findViewById(R.id.position_tv2);
        mDurationTv = (TextView) findViewById(R.id.duration_tv2);

        mPlayBtn = (ImageButton) findViewById(R.id.btn_play2);
        mPauseBtn = (ImageButton) findViewById(R.id.btn_pause2);
        mNextBtn = (ImageButton) findViewById(R.id.btn_playNext2);
        mMenuBtn = (ImageButton) findViewById(R.id.btn_menu2);

        mSearchBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mMenuBtn.setOnClickListener(this);

        mPlaybackProgress = (ProgressBar) findViewById(R.id.playback_seekbar2);

        mDefaultAlbumIcon = BitmapFactory.decodeResource(
                mActivity.getResources(), R.drawable.img_album_background);
		/*
		 * mDefaultAlbumIcon = new BitmapDrawable(mActivity.getResources(), b);
		 * // no filter or dither, it's a lot faster and we can't tell the //
		 * difference mDefaultAlbumIcon.setFilterBitmap(false);
		 * mDefaultAlbumIcon.setDither(false);
		 */

        mHeadIcon = (ImageView) findViewById(R.id.headicon_iv);


    }

    //add for first enter display music if exist
    public Boolean testLoad() {
        String load = mMusicNameTv.getText().toString();
        if (load.isEmpty()) {
            Log.e("david", "testload false");
            return false;
        } else {
            Log.e("david", "testload true");
            return true;
        }
    }

    public void refreshSeekProgress(int curTime, int totalTime) {

        curTime /= 1000;
        totalTime /= 1000;
        int curminute = curTime / 60;
        int cursecond = curTime % 60;

        String curTimeString = String.format("%02d:%02d", curminute, cursecond);
        mPositionTv.setText(curTimeString);

        int rate = 0;
        if (totalTime != 0) {
            rate = (int) ((float) curTime / totalTime * 100);
        }
        mPlaybackProgress.setProgress(rate);
    }

    public void refreshUI(int curTime, int totalTime, MusicInfo music) {

        int tempCurTime = curTime;
        int tempTotalTime = totalTime;

        totalTime /= 1000;
        int totalminute = totalTime / 60;
        int totalsecond = totalTime % 60;
        String totalTimeString = String.format("%02d:%02d", totalminute,
                totalsecond);

        mDurationTv.setText(totalTimeString);

        mMusicNameTv.setText(music.musicName);
        mArtistTv.setText(music.artist);

        Bitmap bitmap = MusicUtils.getCachedArtwork(mActivity, music.songId, music.albumId,
                mDefaultAlbumIcon);

        mHeadIcon.setBackgroundDrawable(new BitmapDrawable(mActivity
                .getResources(), bitmap));
        refreshSeekProgress(tempCurTime, tempTotalTime);
    }

    public void showPlay(boolean flag) {
        if (flag) {
            mPlayBtn.setVisibility(View.VISIBLE);
            mPauseBtn.setVisibility(View.GONE);
        } else {
            mPlayBtn.setVisibility(View.GONE);
            mPauseBtn.setVisibility(View.VISIBLE);
        }
    }

    public View findViewById(int id) {
        return mView.findViewById(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play2:
                Log.e("david", "music play press");
                mServiceManager.rePlay();
                break;
            case R.id.btn_pause2:
                Log.e("david", "music pause press");
                mServiceManager.pause();
                break;
            case R.id.btn_playNext2:
                mServiceManager.next();
                break;
            case R.id.searchBtn:
                mActivity.startActivity(new Intent(mActivity,
                        MusicSearchListActivity.class));
            //    break;
            case R.id.backBtn:
   //			MainContentActivity.mSlidingMenu.showMenu();
		    	mActivity.onBackPressed();
                mUIManager.setCurrentItem();
                break;
            case R.id.btn_menu2:
                ((MainActivity)mActivity).mSlidingMenu.showMenu(true);
                break;
        }
    }
}
