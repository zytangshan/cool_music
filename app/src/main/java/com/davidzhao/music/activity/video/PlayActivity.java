package com.davidzhao.music.activity.video;

import android.app.Activity;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.davidzhao.music.R;
import com.davidzhao.music.Utils.MediaTimer;
import com.davidzhao.music.Utils.VideoUtils;
import com.davidzhao.music.model.VideoInfo;

import org.xutils.common.util.DensityUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-20.
 */

public class PlayActivity extends Activity {
    private SeekBar seekBar;
    private ImageButton btn_play, btn_replay,
            btn_pause, btn_stop, btn_next, btn_pre;
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private int currentPosition;
    private Boolean playing = false;
    private LinearLayout progresslayout;
    private LinearLayout controlLayout;
    private Boolean fullScreen;
    private String video;
    private int curPosition;
    private String playState;
    private ArrayList<VideoInfo> videoInfos;
    private MediaTimer videoTimer;
    public Handler videoHandler;
    private TextView videoCurTime, videoTotalTime;
    private GestureDetector gestureDetector;
    private int mVolume = -1;
    private int mMaxVolume;
    private float mBrightness = -1f;
    private AudioManager mAudioManager;
    private ImageView volume, bright, volume_slient;
    private Boolean firstScroll;
    private int GESTURE_FLAG = 0;
    private final int GESTURE_MODIFY_PROGRESS = 9;
    private final int GESTURE_MODIFY_VOLUME = 1;
    private final int GESTURE_MODIFY_BRIGHT = 2;
    private float STEP_VOLUME = 10;
    private TextView volume_text, bright_text;
    private RelativeLayout layout_volume, layout_bright, layout_silent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.play_video);

        fullScreen = false;
        curPosition = 0;
        videoInfos = new ArrayList<>();
        videoInfos.addAll(VideoUtils.getVideo(this));

        seekBar = (SeekBar) findViewById(R.id.playback_seekbar);
        surfaceView = (SurfaceView) findViewById(R.id.video_surface);

        btn_play = (ImageButton) findViewById(R.id.video_play);
        btn_pause = (ImageButton) findViewById(R.id.video_pause);
        btn_pre = (ImageButton) findViewById(R.id.video_playPre);
        btn_next = (ImageButton) findViewById(R.id.video_next);
        btn_stop = (ImageButton) findViewById(R.id.video_stop);
        btn_replay = (ImageButton) findViewById(R.id.video_replay);
        progresslayout = (LinearLayout) findViewById(R.id.progressLayout);
        controlLayout = (LinearLayout) findViewById(R.id.controlLayout);
        volume = (ImageView) findViewById(R.id.volume);
        bright = (ImageView) findViewById(R.id.bright);
        volume_slient = (ImageView) findViewById(R.id.volume_slient);
        volume_text = (TextView) findViewById(R.id.volume_text);
        bright_text = (TextView) findViewById(R.id.bright_text);
        layout_volume = (RelativeLayout) findViewById(R.id.layout_volume);
        layout_bright = (RelativeLayout) findViewById(R.id.layout_bright);
        layout_silent = (RelativeLayout) findViewById(R.id.layout_silent);

        btn_play.setOnClickListener(click);
        btn_pause.setOnClickListener(click);
        btn_replay.setOnClickListener(click);
        btn_stop.setOnClickListener(click);
        btn_next.setOnClickListener(click);
        btn_pre.setOnClickListener(click);
        gestureDetector = new GestureDetector(this,
                new VideoGestureListener());
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (gestureDetector == null) {
                    Log.e("david", "gestureDetector");
                }
                gestureDetector.onTouchEvent(motionEvent);
                switch(motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        Log.e("david", "touch action down");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.e("david", "touch action move");

                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e("david", "touch action up");
                        layout_volume.setVisibility(View.GONE);
                        layout_bright.setVisibility(View.GONE);
                        layout_silent.setVisibility(View.GONE);
                        break;
                }
                return false;
            }
        });
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("david", "click");
                if (!fullScreen) {
                    progresslayout.setVisibility(View.GONE);
                    controlLayout.setVisibility(View.GONE);
                    fullScreen = true;
                } else {
                    progresslayout.setVisibility(View.VISIBLE);
                    controlLayout.setVisibility(View.VISIBLE);
                    fullScreen = false;
                }
            }
        });

        // add SurfaceHolder callback
        surfaceView.getHolder().addCallback(callback);
        // after 4.0 no need
        // sv.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        seekBar.setOnSeekBarChangeListener(change);
        IntentFilter intentFilter = new IntentFilter();
        video = getIntent().getCharSequenceExtra("path").toString();
        curPosition = getIntent().getIntExtra("position", 0);
        Log.e("david", "open file " + video + ":" + curPosition);

        videoCurTime = (TextView) findViewById(R.id.video_currentTime);
        videoTotalTime = (TextView) findViewById(R.id.video_totalTime);

        videoHandler = new  Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //update video play time
                refreshProgress(mediaPlayer.getCurrentPosition(),
                        videoInfos.get(curPosition).duration);
            }
        };
        videoTimer = new MediaTimer(videoHandler);
        //play(0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            progresslayout.setVisibility(View.VISIBLE);
            controlLayout.setVisibility(View.VISIBLE);

        } else if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE) {
            progresslayout.setVisibility(View.GONE);
            controlLayout.setVisibility(View.GONE);
        }
    }

    private void showButtonPlay(Boolean status) {
        if (status) {
            btn_pause.setVisibility(View.VISIBLE);
            btn_play.setVisibility(View.GONE);
        } else {
            btn_pause.setVisibility(View.GONE);
            btn_play.setVisibility(View.VISIBLE);
        }
    }
    private View.OnClickListener click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.video_play:
                    play(currentPosition);
                    break;
                case R.id.video_pause:
                    pause();
                    break;
                case R.id.video_next:
                    next();
                    break;
                case R.id.video_playPre:
                    prev();
                    break;
                case R.id.video_replay:
                    replay();
                    break;
                default:
                    break;

            }
        }
    };
    private SurfaceHolder.Callback  callback = new SurfaceHolder.Callback(){
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.e("david", "surface create");
            if (currentPosition > 0) {
                play(currentPosition);
                currentPosition = 0;
            } else {
                play(currentPosition);
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            Log.e("david", "surface change");

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.e("david", "surface destroy");
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
            }

        }
    };
    private SeekBar.OnSeekBarChangeListener change = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

            if (mediaPlayer != null) {
                refreshProgress(mediaPlayer.getCurrentPosition(),
                        videoInfos.get(curPosition).duration);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int position = seekBar.getProgress();
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(position);
            }

        }
    };
    private void play(final int position) {
        Log.e("david", "play" + video);
        File file = new File(video);
        if (!file.exists()) {
            Toast.makeText(this, "视频文件路径错误", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            videoTimer.startTimer();
            videoTotalTime.setText(updateTotalTime(videoInfos.get(curPosition).duration));
            if (mediaPlayer != null && playState.equals("pause")) {
                mediaPlayer.start();
                showButtonPlay(true);
                return;
            } else if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.setDisplay(surfaceView.getHolder());
            Log.e("david", "开始装载");
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.e("david", "装载完成");
                    mediaPlayer.start();
                    mediaPlayer.seekTo(position);
                    seekBar.setMax(mediaPlayer.getDuration());
                    showButtonPlay(true);
                    //new thread update play
                    new Thread() {

                        @Override
                        public void run() {
                            try {
                                playing = true;
                                while (playing) {
                                    int current = mediaPlayer
                                            .getCurrentPosition();
                                    seekBar.setProgress(current);
                                    sleep(1000);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                    //btn_play.setEnabled(false);
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // play over
                    seekBar.setProgress(mediaPlayer.getDuration());
                    videoCurTime.setText(updateTotalTime(mediaPlayer.getDuration()));
                    stop();
                    showButtonPlay(false);
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // error replay
                    play(0);
                    playing = false;
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void pause() {
        Log.e("david", "pause");
        videoTimer.stopTimer();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playState = "pause";
            showButtonPlay(false);
        }
    }
    private void next() {
        curPosition ++;
        curPosition= reviseIndex(curPosition);
        video = videoInfos.get(curPosition).data;
        playState = "next";
        stop();
        Log.e("david", "next" + video + ":" + curPosition);
        play(currentPosition);
    }
    private void prev() {
        Log.e("david", "curPosition:" + curPosition);
        curPosition --;
        Log.e("david", "curPosition:" + curPosition);
        curPosition = reviseIndex(curPosition);
        video = videoInfos.get(curPosition).data;
        playState = "prev";
        stop();
        Log.e("david", "prev" + video + "cur:" + curPosition + "size:" + videoInfos.size());
        play(currentPosition);
    }
    private void stop() {
        Log.e("david", "stop");
        videoTimer.stopTimer();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            playState = "stop";
            mediaPlayer = null;
            playing = false;
            showButtonPlay(false);
        }
    }
    private void replay() {
        videoCurTime.setText("00:00");
        Log.e("david", "replay");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(0);
            Toast.makeText(this, "replay", Toast.LENGTH_SHORT).show();
            playState = "replay";
            showButtonPlay(true);
            return;
        }
        playing = false;
        play(0);
    }
    private int reviseIndex(int index) {
        if(index < 0) {
            index = videoInfos.size() - 1;
        }
        if(index >= videoInfos.size()) {
            index = 0;
        }
        return index;
    }
    private void refreshProgress(int curTime, int totalTime) {
        curTime /= 1000;
        totalTime /= 1000;
        int curminute = curTime / 60;
        int cursecond = curTime % 60;

        String curTimeString = String.format("%02d:%02d", curminute, cursecond);
        videoCurTime.setText(curTimeString);

        int rate = 0;
        if (totalTime != 0) {
            rate = (int) ((float) curTime / totalTime * 100);
        }
        //seekBar.setProgress(rate);
    }
    private String updateTotalTime(int totalTime) {
        totalTime /= 1000;
        int totalminute = totalTime / 60;
        int totalsecond = totalTime % 60;
        return String.format("%02d:%02d", totalminute, totalsecond);

    }

    public class VideoGestureListener extends GestureDetector.SimpleOnGestureListener {
        protected MotionEvent mLastOnDownEvent = null;

        @Override
        public boolean onDown(MotionEvent e) {
            //mLastOnDownEvent = e;
            firstScroll = true;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //return super.onScroll(e1, e2, distanceX, distanceY);
            /*Log.e("david", "scroll");
            //workground e1 is null
            if (e1 == null) {
                e1 = mLastOnDownEvent;
            }
            if (e1 == null || e2 == null) {
                Log.e("david", "e1 or e2 is null");
                //return false;
            }
            float mOldX, mOldY;
            mOldX = e1.getX();
            mOldY = e1.getY();
            int y = (int) e2.getRawY();
            int x = (int) e2.getRawY();

            Display disp = getWindowManager().getDefaultDisplay();

            DisplayMetrics metrics = new DisplayMetrics();
            disp.getMetrics(metrics);
            int windowWidth = metrics.widthPixels;
            int windowHeight = metrics.heightPixels;


            /*if (mOldX > windowWidth * 4.0 / 5)
                onVolumeSlide((mOldY - y) / windowHeight);
            else if (mOldX < windowWidth / 5.0)
                onBrightnessSlide((mOldY - y) / windowHeight);*/
            /*if (Math.abs(x) > windowWidth * 3.0 / 10) {
                Log.e("david", "volume" + y + ":" + windowWidth);
                onVolumeSlide(y / windowWidth);
            } else if (Math.abs(y) > windowHeight * 3.0 / 10) {
                Log.e("david", "bright" + x + ":" + windowHeight);
                onBrightnessSlide(y / windowHeight);
            }

            //return super.onScroll(e1, e2, distanceX, distanceY);
            return true;
        }*/
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getY();
            Display disp = getWindowManager().getDefaultDisplay();

            DisplayMetrics metrics = new DisplayMetrics();
            disp.getMetrics(metrics);
            int windowWidth = metrics.widthPixels;
            int windowHeight = metrics.heightPixels;
            if (firstScroll) {
                if (Math.abs(distanceX) >= Math.abs(distanceY)) {
                /*gesture_progress_layout.setVisibility(View.VISIBLE);
                gesture_volume_layout.setVisibility(View.GONE);
                gesture_bright_layout.setVisibility(View.GONE);*/
                    GESTURE_FLAG = GESTURE_MODIFY_PROGRESS;
                } else {
                    if (mOldX > windowWidth * 3.0 / 5) {
                        if (mVolume == 0) {
                            layout_silent.setVisibility(View.VISIBLE);
                            volume_slient.setVisibility(View.VISIBLE);
                            layout_volume.setVisibility(View.GONE);
                            volume.setVisibility(View.GONE);
                        } else {
                            layout_volume.setVisibility(View.VISIBLE);
                            layout_silent.setVisibility(View.GONE);
                            volume.setVisibility(View.VISIBLE);
                            volume_slient.setVisibility(View.GONE);
                        }
                        GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
                    } else if (mOldX < windowWidth * 2.0 / 5) {
                        layout_bright.setVisibility(View.VISIBLE);
                        bright.setVisibility(View.VISIBLE);
                        GESTURE_FLAG = GESTURE_MODIFY_BRIGHT;
                    }
                }
            }

            // touch go on until leave screen
            if (GESTURE_FLAG == GESTURE_MODIFY_PROGRESS) {
                // distanceX=lastScrollPositionX-currentScrollPositionX，因此为正时是快进
                /*if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动
                    if (distanceX >= DensityUtil.dip2px(this, STEP_PROGRESS)) {// 快退，用步长控制改变速度，可微调
                        gesture_iv_progress.setImageResource(R.drawable.souhu_player_backward);
                        if (playingTime > 3) {// 避免为负
                            playingTime -= 3;// scroll方法执行一次快退3秒
                        } else {
                            playingTime = 0;
                        }
                    } else if (distanceX <= -DensityUtil.dip2px(this, STEP_PROGRESS)) {// 快进
                        gesture_iv_progress.setImageResource(R.drawable.souhu_player_forward);
                        if (playingTime < mediaPlayer.getDuration() - 16) {// 避免超过总时长
                            playingTime += 3;// scroll执行一次快进3秒
                        } else {
                            playingTime = mediaPlayer.getDuration() - 10;
                        }
                    }
                    if (playingTime < 0) {
                        playingTime = 0;
                    }
                    tv_pro_play.seekTo(playingTime);
                    geture_tv_progress_time.setText(DateTools.getTimeStr(playingTime) + "/" + DateTools.getTimeStr(videoTotalTime));
                }*/
            }

            else if (GESTURE_FLAG == GESTURE_MODIFY_VOLUME) {
                int mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                    if (distanceY >= DensityUtil.dip2px(STEP_VOLUME)) {// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                        if (mVolume < mMaxVolume) {// 为避免调节过快，distanceY应大于一个设定值
                            mVolume++;
                        }
                    } else if (distanceY <= -DensityUtil.dip2px(STEP_VOLUME)) {// 音量调小
                        if (mVolume > 0) {
                            mVolume--;
                            if (mVolume == 0) {// 静音，设定静音独有的图片
                                layout_silent.setVisibility(View.VISIBLE);
                                volume_slient.setVisibility(View.VISIBLE);
                                layout_volume.setVisibility(View.GONE);
                                volume.setVisibility(View.GONE);
                            }
                        }
                    }
                    int percentage = (mVolume * 100) / mMaxVolume;
                    volume_text.setText(percentage + "%");
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolume, 0);
                }
            }

            else if (GESTURE_FLAG == GESTURE_MODIFY_BRIGHT) {
                if (mBrightness < 0) {
                    mBrightness = getWindow().getAttributes().screenBrightness;
                    if (mBrightness <= 0.00f)
                        mBrightness = 0.00f;
                }
                WindowManager.LayoutParams lpa = getWindow().getAttributes();
                lpa.screenBrightness = mBrightness + (mOldY - y) / windowHeight;
                if (lpa.screenBrightness > 1.0f)
                    lpa.screenBrightness = 1.0f;
                else if (lpa.screenBrightness < 0.01f)
                    lpa.screenBrightness = 0.01f;
                getWindow().setAttributes(lpa);
                mBrightness = getWindow().getAttributes().screenBrightness;
                bright_text.setText((int) (lpa.screenBrightness * 100) + "%");
            }

            firstScroll = false;// 第一次scroll执行完成，修改标志
            return false;
        }
    }

}
