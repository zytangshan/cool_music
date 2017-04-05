package com.davidzhao.music.activity.common;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.davidzhao.music.Fragment.MainFragment;
import com.davidzhao.music.Fragment.MenuFragment;
import com.davidzhao.music.Fragment.ImageFragment;
import com.davidzhao.music.Fragment.VideoFragment;
import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.slidemenu.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    private final String TAG = "MusicMain";
    private long clickTime = 0;
    private List<OnBackListener> mBackListeners = new ArrayList<OnBackListener>();
    public SlidingMenu mSlidingMenu;
    public MainFragment mMainFragment;

    //test
    public VideoFragment videoFragment;
    public ImageFragment imageFragment;

    private Button musicButton;
    private Button videoButton;
    private Button pictureButton;
    private MusicPreferences sp;
    private Context mContext;

    public interface OnBackListener {
        public abstract void onBack();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }*/
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
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

        mContext = getApplicationContext();
        musicButton = (Button) findViewById(R.id.music_btn);
        videoButton = (Button) findViewById(R.id.video_btn);
        pictureButton = (Button) findViewById(R.id.picture_btn);
        ViewOnClickListener viewOnClickListener = new ViewOnClickListener();
        musicButton.setOnClickListener(viewOnClickListener);
        videoButton.setOnClickListener(viewOnClickListener);
        pictureButton.setOnClickListener(viewOnClickListener);

        /*videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "video press", Toast.LENGTH_SHORT).show();
                setTabSeletion(2);
            }
        });*/

        mMainFragment = new MainFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame_layout, mMainFragment).commit();
        sp = new MusicPreferences(this);

        // configure the SlidingMenu
        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        mSlidingMenu.setMode(SlidingMenu.RIGHT);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        mSlidingMenu.setMenu(R.layout.frame_menu);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.sliding, new MenuFragment()).commit();


        int permissionCheck1 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck3 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.INTERNET);
        int permissionCheck4 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.CHANGE_CONFIGURATION);
        int permissionCheck5 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.MANAGE_DOCUMENTS);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED
                || permissionCheck2 != PackageManager.PERMISSION_GRANTED
                || permissionCheck3 != PackageManager.PERMISSION_GRANTED
                || permissionCheck4 != PackageManager.PERMISSION_GRANTED
                || permissionCheck5 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.CHANGE_CONFIGURATION,
                    Manifest.permission.MANAGE_DOCUMENTS }, 124);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            Toast.makeText(getApplicationContext(), "one more,exit",
                    Toast.LENGTH_SHORT).show();
            clickTime = System.currentTimeMillis();
        } else {
            Log.e(TAG, "exit application");
            finish();
            System.exit(0);
        }
    }
    public void registerBackListener(OnBackListener listener) {
        if (!mBackListeners.contains(listener)) {
            mBackListeners.add(listener);
        }
    }

    public void unRegisterBackListener(OnBackListener listener) {
        mBackListeners.remove(listener);
    }

    private void clearSelection() {
        // TODO Auto-generated method stub
        Log.e("david", "clear");
        musicButton.setTextColor(Color.parseColor("#a6bff2"));
        videoButton.setTextColor(Color.parseColor("#a6bff2"));
        pictureButton.setTextColor(Color.parseColor("#a6bff2"));

        //musicButton.setBackgroundColor(Color.parseColor("#3f000000"));
        //videoButton.setBackgroundColor(Color.parseColor("#3f000000"));
        //pictureButton.setBackgroundColor(Color.parseColor("#3f000000"));
    }


    private void setTabSeletion(int index) {
        // TODO Auto-generated method stub
        clearSelection();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        switch(index){
            case 0:
                musicButton.setTextColor(Color.parseColor("#ceefff"));
                //musicButton.setBackgroundColor(Color.parseColor("#5f000000"));
                sp.setCurrentType("music");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.sliding, new MenuFragment()).commit();
                if(null == mMainFragment){
                    mMainFragment = new MainFragment();
                    transaction.add(R.id.main_frame_layout, mMainFragment);
                }else {
                    transaction.show(mMainFragment);
                }
                break;

//		case 1:
//			myloveBtn.setTextColor(Color.parseColor("#ceefff"));
//			myloveBtn.setBackgroundColor(Color.parseColor("#5f000000"));
//			if(null == myloveFragment){
//				myloveFragment = new MyLoveFragment();
//				transaction.add(R.id.musiclist, myloveFragment);
//			}else {
//				transaction.show(myloveFragment);
//			}
//			break;

            case 2:
                videoButton.setTextColor(Color.parseColor("#ceefff"));
                //videoButton.setBackgroundColor(Color.parseColor("#5f000000"));
                sp.setCurrentType("video");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.sliding, new MenuFragment()).commit();
                if(null == videoFragment){
                    videoFragment = new VideoFragment();
                    transaction.add(R.id.main_frame_layout, videoFragment);
                }else {
                    transaction.show(videoFragment);
                }
                break;

            case 3:
                pictureButton.setTextColor(Color.parseColor("#ceefff"));
                //pictureButton.setBackgroundColor(Color.parseColor("#5f000000"));
                sp.setCurrentType("image");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.sliding, new MenuFragment()).commit();
                if(null == imageFragment){
                    imageFragment = new ImageFragment();
                    transaction.add(R.id.main_frame_layout, imageFragment);
                }else {
                    transaction.show(imageFragment);
                }
                break;

            default:
                break;
        }
        transaction.commit();
    }

    private class ViewOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            switch(arg0.getId()) {
                case R.id.music_btn:
                    Log.e("david", "click==" + arg0);
                    setTabSeletion(0);
                    initFragment1();
                    break;
//			case R.id.mylove_btn:
//				setTabSeletion(1);
//				break;
                case R.id.video_btn:
                    Log.e("david", "click==" + arg0);
                    setTabSeletion(2);
                    initFragment2();

                    break;
                case R.id.picture_btn:
                    Log.e("david", "click==" + arg0);
                    setTabSeletion(3);
                    initFragment3();

                    break;
                default:
                    break;
            }
        }


    }
    private void hideFragments(FragmentTransaction transaction) {
        // TODO Auto-generated method stub
        if(null != mMainFragment)
            transaction.hide(mMainFragment);

        if(null != videoFragment)
            transaction.hide(videoFragment);

        if(null != imageFragment)
            transaction.hide(imageFragment);

    }

    private void initFragment1(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(mMainFragment == null){
            mMainFragment = new MainFragment();
            transaction.add(R.id.main_frame_layout, mMainFragment);
        }

        hideFragment(transaction);
        transaction.show(mMainFragment);

        //(replace)
//        if(f1 == null){
//            f1 = new MainFragment();
//        }
//        transaction.replace(R.id.main_frame_layout, f1);

        transaction.commit();
    }

    private void initFragment2(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(videoFragment == null){
            videoFragment = new VideoFragment();
            transaction.add(R.id.main_frame_layout, videoFragment);
        }
        hideFragment(transaction);
        transaction.show(videoFragment);

//        if(f2 == null) {
//            f2 = new VideoFragment(");
//        }
//        transaction.replace(R.id.main_frame_layout, f2);

        transaction.commit();
    }

    private void initFragment3(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(imageFragment == null){
            imageFragment = new ImageFragment();
            transaction.add(R.id.main_frame_layout, imageFragment);
        }
        hideFragment(transaction);
        transaction.show(imageFragment);

//        if(f3 == null) {
//            f3 = new ImageFragment();
//        }
//        transaction.replace(R.id.main_frame_layout, f3);

        transaction.commit();
    }

    //hide all fragment
    private void hideFragment(FragmentTransaction transaction){
        if(mMainFragment != null){
            transaction.hide(mMainFragment);
        }
        if(videoFragment != null){
            transaction.hide(videoFragment);
        }
        if(imageFragment != null){
            transaction.hide(imageFragment);
        }

    }

}
