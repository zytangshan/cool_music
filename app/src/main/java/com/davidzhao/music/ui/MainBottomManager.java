package com.davidzhao.music.ui;

import android.app.Activity;
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

import com.davidzhao.music.Fragment.MainFragment;
import com.davidzhao.music.R;
import com.davidzhao.music.Utils.MusicUtils;
import com.davidzhao.music.activity.common.MainActivity;
import com.davidzhao.music.application.MusicApp;
import com.davidzhao.music.model.MusicInfo;
import com.davidzhao.music.service.MusicControl;
import com.davidzhao.music.service.ServiceManager;
import com.davidzhao.music.view.AlwaysMarqueeTextView;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-7.
 */

public class MainBottomManager implements View.OnClickListener {
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
    private MusicControl musicControl;

    public MainBottomManager(Activity activity, View view) {
        this.mView = view;
        this.mActivity = activity;
        this.mServiceManager = MusicApp.mServiceManager;
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
        mMusicNameTv = (AlwaysMarqueeTextView) findViewById(R.id.musicname_tv2);
        mArtistTv = (AlwaysMarqueeTextView) findViewById(R.id.artist_tv2);
        mPositionTv = (TextView) findViewById(R.id.position_tv2);
        mDurationTv = (TextView) findViewById(R.id.duration_tv2);

        mPlayBtn = (ImageButton) findViewById(R.id.btn_play2);
        mPauseBtn = (ImageButton) findViewById(R.id.btn_pause2);
        mNextBtn = (ImageButton) findViewById(R.id.btn_playNext2);
        mMenuBtn = (ImageButton) findViewById(R.id.btn_menu2);

        mPlayBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mMenuBtn.setOnClickListener(this);

        mPlaybackProgress = (ProgressBar) findViewById(R.id.playback_seekbar2);

        mDefaultAlbumIcon = BitmapFactory.decodeResource(
                mActivity.getResources(), R.drawable.img_album_background);

        mHeadIcon = (ImageView) findViewById(R.id.headicon_iv);
        musicControl = new MusicControl(mActivity);
    }

    private View findViewById(int id) {
        return mView.findViewById(id);
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

        Log.e("david", "play" + music.musicName + music.artist);

        mMusicNameTv.setText(music.musicName);
        mArtistTv.setText(music.artist);

        Bitmap bitmap = MusicUtils.getCachedArtwork(mActivity, music.songId, music.albumId,
                mDefaultAlbumIcon);

        mHeadIcon.setBackground(new BitmapDrawable(mActivity
                .getResources(), bitmap));
        /*mHeadIcon.setBackground(new BitmapDrawable(mActivity
                .getResources(), MusicUtils.getAlbumArt(mActivity,music.albumId)));*/
        refreshSeekProgress(tempCurTime, tempTotalTime);
    }

    public void showPlay(boolean flag) {
        Log.e("david", "play change");
        if (flag) {
            mPlayBtn.setVisibility(View.VISIBLE);
            mPauseBtn.setVisibility(View.GONE);
        } else {
            mPlayBtn.setVisibility(View.GONE);
            mPauseBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play2:
                Log.e("david", "main bottom play press");
                if (MainFragment.firstOpenMusic == 1) {
                    ((MainActivity) mActivity).mMainFragment.playLoadMusic();
                    MainFragment.firstOpenMusic ++;
                }else {
                    mServiceManager.rePlay();
                }
                break;
            case R.id.btn_pause2:
                Log.e("david", "main bottom pause press");
                mServiceManager.pause();
                break;
            case R.id.btn_playNext2:
                mServiceManager.next();
                break;
            case R.id.btn_menu2:
                ((MainActivity) mActivity).mSlidingMenu.showMenu(true);
                break;
        }
    }


}