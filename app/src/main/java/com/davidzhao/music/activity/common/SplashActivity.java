package com.davidzhao.music.activity.common;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.davidzhao.music.Database.MusicInfoDB;
import com.davidzhao.music.R;
import com.davidzhao.music.Utils.MusicUtils;
import com.davidzhao.music.activity.common.MainActivity;
import com.davidzhao.music.application.MusicApp;
import com.davidzhao.music.interfaces.IConstant;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-3.
 */

public class SplashActivity extends Activity implements IConstant {
    private MusicInfoDB mMusicDB;
    private Handler mhandler;
    private long clickTime = 0;
    private ImageView splashView;
    private TextView splashText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /********activity transition******************/
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        Transition explode = TransitionInflater.from(SplashActivity.this)
                .inflateTransition(R.transition.fade);
        //exit
        getWindow().setExitTransition(explode);
        //first open
        getWindow().setEnterTransition(explode);
        //other open
        getWindow().setReenterTransition(explode);


        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        MusicApp.requestPermission(SplashActivity.this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }



        final Window window = getWindow();
        window.setWindowAnimations(R.style.dialog_anim_slide_left);

        mMusicDB = new MusicInfoDB(this);

        mhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);


                Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
                        SplashActivity.this).toBundle());
                finish();

            }
        };
        anim();
        getData();
    }

    @Override
    public Window getWindow() {
        return super.getWindow();
    }

    private void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //MusicApp.requestPermission(SplashActivity.this);
                if (mMusicDB.hasData()) {
                    // 如果有数据就等三秒跳转

                    mhandler.sendMessageDelayed(mhandler.obtainMessage(), 3000);

                } else {
                    MusicUtils.queryMusic(SplashActivity.this,
                            START_FROM_LOCAL);
                    MusicUtils.queryAlbums(SplashActivity.this);
                    MusicUtils.queryArtist(SplashActivity.this);
                    MusicUtils.queryFolder(SplashActivity.this);
                    mhandler.sendEmptyMessage(1);
                }

            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次后退键退出程序",
                    Toast.LENGTH_SHORT).show();
            clickTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }
    public void anim() {
        View view = getWindow().getDecorView();
        splashView = (ImageView) findViewById(R.id.mysplash);
        splashText = (TextView) findViewById(R.id.splash_text);

        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation scaleAnim = new ScaleAnimation((float)1.0, (float)0.5, (float)1.0, (float)0.5,
                Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
        rotateAnimation.setDuration(600);
        scaleAnim.setDuration(600);
        rotateAnimation.setStartOffset(600);
        animSet.addAnimation(scaleAnim);
        animSet.addAnimation(rotateAnimation);
        splashView.startAnimation(animSet);

        splashText.setVisibility(View.VISIBLE);
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnim1 = new ScaleAnimation(0, 1, 0, 1,
                Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
        RotateAnimation rotateAnimation1 = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
        rotateAnimation1.setDuration(600);
        scaleAnim1.setDuration(600);
        rotateAnimation1.setStartOffset(600);
        animationSet.addAnimation(scaleAnim1);
        animationSet.addAnimation(rotateAnimation1);
        splashText.startAnimation(animationSet);

    }
}
