package com.davidzhao.music.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.davidzhao.music.Adapter.LyricAdapter;
import com.davidzhao.music.Adapter.MyAdapter;
import com.davidzhao.music.Database.FavoriteInfoDB;
import com.davidzhao.music.Database.MusicInfoDB;
import com.davidzhao.music.Lyric.LyricDownloadManager;
import com.davidzhao.music.Lyric.LyricLoadHelper;
import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.Utils.MediaTimer;
import com.davidzhao.music.activity.music.PlayQueueActivity;
import com.davidzhao.music.application.MusicApp;
import com.davidzhao.music.interfaces.IConstant;
import com.davidzhao.music.model.LyricSentence;
import com.davidzhao.music.model.MusicInfo;
import com.davidzhao.music.service.ServiceManager;
import com.davidzhao.music.view.MySlidingDrawer;

import java.io.File;
import java.util.List;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-8.
 */

public class SlidingDrawerManager implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, IConstant, android.widget.SlidingDrawer.OnDrawerOpenListener,
        android.widget.SlidingDrawer.OnDrawerCloseListener {
    private MySlidingDrawer mSliding;
    private TextView mMusicNameTv, mArtistTv, mCurTimeTv, mTotalTimeTv;
    private ImageButton mPrevBtn, mNextBtn, mPlayBtn, mPauseBtn, mVolumeBtn,
            mFavoriteBtn;
    private ListView mLrcListView;
    private LinearLayout mVolumeLayout;
    private Activity mActivity;
    private View mView;
    private ServiceManager mServiceManager;
    private SeekBar mPlaybackSeekBar, mVolumeSeekBar;
    public Handler mHandler;
    private boolean mPlayAuto = true;

    private AudioManager mAudioManager;
    private int mMaxVolume;
    private int mCurVolume;

    private Animation view_in, view_out;
    // private LrcUtil mLrcUtil;
    // private LrcView mLrcView;
    private ListView mListView;
    private GridView mGridView;

    private ImageButton mShowMoreBtn;

    private ImageView mMoveIv;
    private boolean mIsFavorite = false;
    private FavoriteInfoDB mFavoriteDao;
    private MusicInfoDB mMusicDao;
    private MusicInfo mCurrentMusicInfo;
    private boolean mListNeedRefresh = false;
    private MyAdapter mAdapter;;
    private MediaTimer mMediaTimer;
    private int mProgress;
    private LyricDownloadManager mLyricDownloadManager;
    private LyricLoadHelper mLyricLoadHelper;
    private LyricAdapter mLyricAdapter;
    private TextView mLrcEmptyView;
    private int mScreenWidth;

    private MusicPreferences mSp;
    /** 歌词是否正在下载 */
    private boolean mIsLyricDownloading;

    public SlidingDrawerManager(Activity a, ServiceManager sm, View view) {
        this.mServiceManager = sm;
        this.mActivity = a;
        this.mView = view;
        mFavoriteDao = new FavoriteInfoDB(a);
        mMusicDao = new MusicInfoDB(a);
        mSp = new MusicPreferences(a);
        mLyricDownloadManager = new LyricDownloadManager(a);
        mLyricLoadHelper = new LyricLoadHelper();
        mLyricLoadHelper.setLyricListener(mLyricListener);

        DisplayMetrics metric = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels;

        // 歌词秀设置---------------------------------------------------------------
        mLyricAdapter = new LyricAdapter(a);

        // mLrcUtil = new LrcUtil();

        mAudioManager = (AudioManager) a
                .getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        view_in = AnimationUtils.loadAnimation(a, R.anim.fade_in);
        view_out = AnimationUtils.loadAnimation(a, R.anim.fade_out);

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
        mListView = (ListView) findViewById(R.id.music_listview);
        mGridView = (GridView) findViewById(R.id.gridview);
        mSliding = (MySlidingDrawer) findViewById(R.id.slidingDrawer);
        mMusicNameTv = (TextView) findViewById(R.id.musicname_tv);
        mArtistTv = (TextView) findViewById(R.id.artist_tv);
        mPrevBtn = (ImageButton) findViewById(R.id.btn_playPre);
        mNextBtn = (ImageButton) findViewById(R.id.btn_playNext);
        mPlayBtn = (ImageButton) findViewById(R.id.btn_play);
        mPauseBtn = (ImageButton) findViewById(R.id.btn_pause);
        mVolumeBtn = (ImageButton) findViewById(R.id.btn_volume);
        mShowMoreBtn = (ImageButton) findViewById(R.id.btn_more);
        mFavoriteBtn = (ImageButton) findViewById(R.id.btn_favorite);
        mMoveIv = (ImageView) findViewById(R.id.move_iv);
        mLrcListView = (ListView) findViewById(R.id.lyricshow);
        mLrcEmptyView = (TextView) findViewById(R.id.lyric_empty);

        mLrcListView.setAdapter(mLyricAdapter);
        mLrcListView.setEmptyView(mLrcEmptyView);
        mLrcListView.startAnimation(AnimationUtils.loadAnimation(mActivity,
                android.R.anim.fade_in));

        mSliding.setOnDrawerCloseListener(this);
        mSliding.setOnDrawerOpenListener(this);

        mPrevBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mVolumeBtn.setOnClickListener(this);
        mShowMoreBtn.setOnClickListener(this);
        mFavoriteBtn.setOnClickListener(this);
        mLrcEmptyView.setOnClickListener(this);

        mPlaybackSeekBar = (SeekBar) findViewById(R.id.playback_seekbar);
        mVolumeSeekBar = (SeekBar) findViewById(R.id.volume_seekbar);
        mVolumeSeekBar.setMax(mMaxVolume);
        mVolumeSeekBar.setProgress(mCurVolume);

        mPlaybackSeekBar.setOnSeekBarChangeListener(this);
        mVolumeSeekBar.setOnSeekBarChangeListener(this);

        mCurTimeTv = (TextView) findViewById(R.id.currentTime_tv);
        mTotalTimeTv = (TextView) findViewById(R.id.totalTime_tv);

        mVolumeLayout = (LinearLayout) findViewById(R.id.volumeLayout);

        // mLrcView = (LrcView) findViewById(R.id.lrctextview);
    }

    public void refreshSeekProgress(int curTime, int totalTime) {

        int tempCurTime = curTime;

        curTime /= 1000;
        totalTime /= 1000;
        int curminute = curTime / 60;
        int cursecond = curTime % 60;

        String curTimeString = String.format("%02d:%02d", curminute, cursecond);
        mCurTimeTv.setText(curTimeString);

        int rate = 0;
        if (totalTime != 0) {
            rate = (int) ((float) curTime / totalTime * 100);
        }
        mPlaybackSeekBar.setProgress(rate);

        mLyricLoadHelper.notifyTime(tempCurTime);
        // mLrcView.updateIndex(rate < 1 ? 100 : rate * 1000);
        // mLrcView.updateIndex(tempCurTime);
    }

    public void refreshUI(int curTime, int totalTime, MusicInfo music) {

        mCurrentMusicInfo = music;
        if (music.favorite == 1) {
            mIsFavorite = true;
            mFavoriteBtn.setImageResource(R.drawable.icon_favourite_checked);
        } else {
            mIsFavorite = false;
            mFavoriteBtn.setImageResource(R.drawable.icon_favourite_normal);
        }

        int tempCurTime = curTime;
        int tempTotalTime = totalTime;

        totalTime /= 1000;
        int totalminute = totalTime / 60;
        int totalsecond = totalTime % 60;
        String totalTimeString = String.format("%02d:%02d", totalminute,
                totalsecond);

        mTotalTimeTv.setText(totalTimeString);

        mMusicNameTv.setText(music.musicName);
        mArtistTv.setText(music.artist);

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

    private View findViewById(int id) {
        return mView.findViewById(id);
    }

    public void refreshFavorite(int favorite) {
        if (favorite == 1) {
            mIsFavorite = true;
            mFavoriteBtn.setImageResource(R.drawable.icon_favourite_checked);
        } else {
            mIsFavorite = false;
            mFavoriteBtn.setImageResource(R.drawable.icon_favourite_normal);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_playPre:
                if (mCurrentMusicInfo == null) {
                    return;
                }
                mServiceManager.prev();
                break;
            case R.id.btn_play:
                if (mCurrentMusicInfo == null) {
                    return;
                }
                mServiceManager.rePlay();
                break;
            case R.id.btn_playNext:
                if (mCurrentMusicInfo == null) {
                    return;
                }
                mServiceManager.next();
                break;
            case R.id.btn_pause:
                mServiceManager.pause();
                break;
            case R.id.btn_volume:
                if (mVolumeLayout.isShown()) {
                    mVolumeLayout.setVisibility(View.INVISIBLE);
                    mVolumeLayout.startAnimation(view_out);
                } else {
                    mVolumeLayout.setVisibility(View.VISIBLE);
                    mVolumeLayout.startAnimation(view_in);
                }
                break;
            case R.id.btn_more:
                mActivity.startActivity(new Intent(mActivity,
                        PlayQueueActivity.class));
                break;
            case R.id.btn_favorite:
                if (mCurrentMusicInfo == null) {
                    return;
                }
                mListNeedRefresh = true;
                if (!mIsFavorite) {
                    startAnimation(mMoveIv);
                    mFavoriteDao.saveMusicInfo(mCurrentMusicInfo);
                    mMusicDao.setFavoriteStateById(mCurrentMusicInfo._id, 1);
                    mFavoriteBtn.setImageResource(R.drawable.icon_favorite_on);
                } else {
                    mFavoriteDao.deleteById(mCurrentMusicInfo._id);
                    mMusicDao.setFavoriteStateById(mCurrentMusicInfo._id, 0);
                    mFavoriteBtn.setImageResource(R.drawable.icon_favorite);
                }
                mIsFavorite = !mIsFavorite;
                break;
            case R.id.lyric_empty:
                // 点击下载歌词
                if (mCurrentMusicInfo == null) {
                    return;
                }
                showLrcDialog();
                break;
        }
    }

    private void showLrcDialog() {
        View view = View.inflate(mActivity, R.layout.lrc_dialog, null);
        view.setMinimumWidth(mScreenWidth - 40);
        final Dialog dialog = new Dialog(mActivity, R.style.lrc_dialog);

        final Button okBtn = (Button) view.findViewById(R.id.ok_btn);
        final Button cancleBtn = (Button) view.findViewById(R.id.cancel_btn);
        final EditText artistEt = (EditText) view.findViewById(R.id.artist_tv);
        final EditText musicEt = (EditText) view.findViewById(R.id.music_tv);

        artistEt.setText(mCurrentMusicInfo.artist);
        musicEt.setText(mCurrentMusicInfo.musicName);
        View.OnClickListener btnListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v == okBtn) {
                    String artist = artistEt.getText().toString().trim();
                    String music = musicEt.getText().toString().trim();
                    if (TextUtils.isEmpty(artist) || TextUtils.isEmpty(music)) {
                        Toast.makeText(mActivity, "歌手和歌曲不能为空",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // 开始搜索
//						loadLyric(music, artist);
                        loadLyricByHand(music, artist);
                        dialog.dismiss();
                    }
                } else if (v == cancleBtn) {
                    dialog.dismiss();
                }
            }
        };
        okBtn.setOnClickListener(btnListener);
        cancleBtn.setOnClickListener(btnListener);
        dialog.setContentView(view);
        dialog.show();
    }

    public void setMusicTimer(MediaTimer mediaTimer) {
        this.mMediaTimer = mediaTimer;
    }

    public void open() {
        mSliding.setVisibility(View.VISIBLE);
        mSliding.animateOpen();
        TextView mMusicNameTv = (TextView) findViewById(R.id.musicname_tv);
        TextView mArtistTv = (TextView) findViewById(R.id.artist_tv);
        String music = mMusicNameTv.getText().toString().trim();
        String artist = mArtistTv.getText().toString().trim();
        Log.e("david", music + artist);
        loadLyric(music, artist);
    }

    public void close() {
        mSliding.animateClose();
    }

    public boolean isOpened() {
        return mSliding.isOpened();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (seekBar == mPlaybackSeekBar) {
            if (!mPlayAuto) {
                mProgress = progress;
                // mServiceManager.seekTo(progress);
                // refreshSeekProgress(mServiceManager.position(),
                // mServiceManager.duration());
            }
        } else if (seekBar == mVolumeSeekBar) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress,
                    0);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (seekBar == mPlaybackSeekBar) {
            mPlayAuto = false;
            mMediaTimer.stopTimer();
            mServiceManager.pause();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == mPlaybackSeekBar) {
            mPlayAuto = true;
            mServiceManager.seekTo(mProgress);
            refreshSeekProgress(mServiceManager.position(),
                    mServiceManager.duration());
            mServiceManager.rePlay();
            mMediaTimer.startTimer();
        }
    }

    private void loadLyric(String music, String artist) {
        MusicInfo info = new MusicInfo();
        info.musicName = music;
        info.artist = artist;
        loadLyric(info);
    }

    /**
     * 读取本地歌词文件
     */
    public void loadLyric(MusicInfo playingSong) {
        if (playingSong == null) {
            return;
        }
        // 取得歌曲同目录下的歌词文件绝对路径
        String lyricFilePath = MusicApp.lrcPath + "/" + playingSong.musicName
                + ".lrc";
        File lyricfile = new File(lyricFilePath);

        if (lyricfile.exists()) {
            // 本地有歌词，直接读取
            // Log.i(TAG, "loadLyric()--->本地有歌词，直接读取");
            Log.e("david", "lrc exit");
            mLyricLoadHelper.loadLyric(lyricFilePath);
        } else {
            if (mSp.getAutoLyric()) {
                mIsLyricDownloading = true;
                // 尝试网络获取歌词
                // Log.i(TAG, "loadLyric()--->本地无歌词，尝试从网络获取");
                new LyricDownloadAsyncTask().execute(playingSong.musicName,
                        playingSong.artist);
            } else {
                // 设置歌词为空
                mLyricLoadHelper.loadLyric(null);
            }
        }
    }

    private void loadLyricByHand(String musicName, String artist) {
        // 取得歌曲同目录下的歌词文件绝对路径
        String lyricFilePath = MusicApp.lrcPath + "/" + musicName + ".lrc";
        File lyricfile = new File(lyricFilePath);

        if (lyricfile.exists()) {
            // 本地有歌词，直接读取
            // Log.i(TAG, "loadLyric()--->本地有歌词，直接读取");
            mLyricLoadHelper.loadLyric(lyricFilePath);
        } else {
            mIsLyricDownloading = true;
            // 尝试网络获取歌词
            // Log.i(TAG, "loadLyric()--->本地无歌词，尝试从网络获取");
            new LyricDownloadAsyncTask().execute(musicName, artist);

        }
    }

	/*
	 * private boolean getMusicLrc(MusicInfo music, String path) { try { if
	 * (music != null) { File lrcFile = new File(path); FileInputStream fis =
	 * new FileInputStream(lrcFile); BufferedReader br = new BufferedReader(new
	 * InputStreamReader( fis, "GBK")); StringBuilder sb = new StringBuilder();
	 * String data; while ((data = br.readLine()) != null) { sb.append(data); }
	 * String result = sb.toString(); List<SentenceModel> list =
	 * mLrcUtil.parseLrc(result, music); LyricModel model = new
	 * LyricModel(list); mLrcView.setLyric(model);
	 * mLrcView.setVisibility(View.VISIBLE); return true; } else { LinearLayout
	 * layout = new LinearLayout(mActivity); LinearLayout.LayoutParams
	 * linerParams = new LinearLayout.LayoutParams(
	 * ViewGroup.LayoutParams.MATCH_PARENT,
	 * ViewGroup.LayoutParams.MATCH_PARENT);
	 * mLrcView.setVisibility(View.VISIBLE); return false; } } catch (Exception
	 * e) { e.printStackTrace(); } return false; }
	 */

    class LyricDownloadAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // 从网络获取歌词，然后保存到本地
            String lyricFilePath = mLyricDownloadManager.searchLyricFromWeb(
                    params[0], params[1], mCurrentMusicInfo.musicName);
            // 返回本地歌词路径
            mIsLyricDownloading = false;
            return lyricFilePath;
        }

        @Override
        protected void onPostExecute(String result) {
            // Log.i(TAG, "网络获取歌词完毕，歌词保存路径:" + result);
            // 读取保存到本地的歌曲
            mLyricLoadHelper.loadLyric(result);
        };
    };

    private LyricLoadHelper.LyricListener mLyricListener = new LyricLoadHelper.LyricListener() {

        @Override
        public void onLyricLoaded(List<LyricSentence> lyricSentences, int index) {
            // Log.i(TAG, "onLyricLoaded");
            if (lyricSentences != null) {
                // Log.i(TAG, "onLyricLoaded--->歌词句子数目=" + lyricSentences.size()
                // + ",当前句子索引=" + index);
                mLyricAdapter.setLyric(lyricSentences);
                mLyricAdapter.setCurrentSentenceIndex(index);
                mLyricAdapter.notifyDataSetChanged();
                // 本方法执行时，lyricshow的控件还没有加载完成，所以延迟下再执行相关命令
                // mHandler.sendMessageDelayed(
                // Message.obtain(null, MSG_SET_LYRIC_INDEX, index, 0),
                // 100);
            }
        }

        @Override
        public void onLyricSentenceChanged(int indexOfCurSentence) {
            // Log.i(TAG, "onLyricSentenceChanged--->当前句子索引=" +
            // indexOfCurSentence);
            mLyricAdapter.setCurrentSentenceIndex(indexOfCurSentence);
            mLyricAdapter.notifyDataSetChanged();
            mLrcListView.smoothScrollToPositionFromTop(indexOfCurSentence,
                    mLrcListView.getHeight() / 2, 500);
        }
    };

    private void startAnimation(View view) {
        view.setVisibility(View.VISIBLE);
        int fromX = view.getLeft();
        int fromY = view.getTop();

        AnimationSet animSet = new AnimationSet(true);
        TranslateAnimation transAnim = new TranslateAnimation(
                Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -fromX,
                Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -fromY);

        AlphaAnimation alphaAnim1 = new AlphaAnimation(0f, 1f);
        ScaleAnimation scaleAnim1 = new ScaleAnimation(0, 1, 0, 1,
                Animation.RELATIVE_TO_PARENT, Animation.RELATIVE_TO_PARENT);

        AlphaAnimation alphaAnim2 = new AlphaAnimation(1f, 0f);
        ScaleAnimation scaleAnim2 = new ScaleAnimation(1, 0, 1, 0,
                Animation.RELATIVE_TO_PARENT, Animation.RELATIVE_TO_PARENT);
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360);

        transAnim.setDuration(600);
        rotateAnimation.setDuration(300);

        scaleAnim1.setDuration(600);
        alphaAnim1.setDuration(600);

        scaleAnim2.setDuration(800);
        alphaAnim2.setDuration(800);
        scaleAnim2.setStartOffset(900);
        alphaAnim2.setStartOffset(900);
        transAnim.setStartOffset(900);
        rotateAnimation.setStartOffset(600);

        animSet.addAnimation(scaleAnim1);
        animSet.addAnimation(alphaAnim1);

        animSet.addAnimation(scaleAnim2);
        animSet.addAnimation(alphaAnim2);
        animSet.addAnimation(transAnim);
        animSet.addAnimation(rotateAnimation);
        view.startAnimation(animSet);
        view.setVisibility(View.GONE);
    }

    public void setListViewAdapter(MyAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void onDrawerClosed() {
        if (mListView != null) {
            mListView.setVisibility(View.VISIBLE);
        }
        if (mGridView != null) {
            mGridView.setVisibility(View.VISIBLE);
        }
        mSliding.setVisibility(View.GONE);
        if (mListNeedRefresh) {
            try {
                if (mIsFavorite) {
                    mAdapter.refreshFavoriteById(mCurrentMusicInfo.songId, 1);
                } else {
                    mAdapter.refreshFavoriteById(mCurrentMusicInfo.songId, 0);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDrawerOpened() {
        if (mListView != null) {
            mListView.setVisibility(View.INVISIBLE);
        }
        if (mGridView != null) {
            mGridView.setVisibility(View.INVISIBLE);
        }
        if (!mIsLyricDownloading) {
            // 读取歌词文件
            loadLyric(mCurrentMusicInfo);
        }
    }
}
