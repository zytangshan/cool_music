package com.davidzhao.music.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.davidzhao.music.R;
import com.davidzhao.music.activity.common.MainActivity;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.interfaces.IConstant;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-6.
 */

public class UIManager implements IConstant, MainActivity.OnBackListener {

    private Activity mActivity;
    private View mView;
    private LayoutInflater mInflater;
    /** mViewPager为第一层 mViewPagerSub为第二层（例如从文件夹或歌手进入列表，点击列表会进入第二层） */
    private ViewPager mViewPager, mViewPagerSub;
    private List<View> mListViews, mListViewsSub;

    private OnRefreshListener mRefreshListener;
    private MainActivity mMainActivity;

    private RelativeLayout mMainLayout;
    private ChangeBgReceiver mReceiver;
    private CommonUI commonUI;

    public interface OnRefreshListener {
        public void onRefresh();
    }

    @Override
    public void onBack() {
        if (mViewPagerSub.isShown()) {
            mViewPagerSub.setCurrentItem(0, true);
        } else if (mViewPager.isShown()) {
            mViewPager.setCurrentItem(0, true);
        }
    }
    public UIManager(Activity activity, View view) {
        this.mActivity = activity;
        this.mView = view;
        mMainActivity = (MainActivity) activity;
        this.mInflater = LayoutInflater.from(activity);
        initBroadCast();
        initBg();
        init();
    }
    private void init() {

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPagerSub = (ViewPager) findViewById(R.id.viewPagerSub);

        mListViews = new ArrayList<View>();
        mListViewsSub = new ArrayList<View>();
        mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        mViewPagerSub.addOnPageChangeListener(new MyOnPageChangeListenerSub());
    }

    private void initBg() {
        MusicPreferences mSp = new MusicPreferences(mActivity);
        String mDefaultBgPath = mSp.getVideoPath();
        mMainLayout = (RelativeLayout) findViewById(R.id.main_layout);


        /*mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // 生成一个状态栏大小的矩形
        int resourceId = mActivity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = mActivity.getResources().getDimensionPixelSize(resourceId);
        // 绘制一个和状态栏一样高的矩形
        View statusView = new View(mActivity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,statusBarHeight);
        statusView.setLayoutParams(params);
        statusView.setBackgroundColor(Color.TRANSPARENT);
        // 添加 statusView 到布局中
        ViewGroup rootView = (ViewGroup) ((ViewGroup) findViewById(R.id.main_layout));
        rootView.addView(statusView, 0);// addView(ViewGroup view, index);
        rootView.setFitsSystemWindows(true);
        rootView.setClipToPadding(true);*/


        Bitmap bitmap = getBitmapByPath(mDefaultBgPath);
        if(bitmap != null) {
            mMainLayout.setBackground(new BitmapDrawable(mActivity.getResources(), bitmap));
        }

        //如果第一次进来 SharedPreference中没有数据
        if(TextUtils.isEmpty(mDefaultBgPath)) {
            mSp.saveVideoPath("004.jpg");
        }
    }

    private void initBroadCast() {
        mReceiver = new ChangeBgReceiver();
        IntentFilter filter = new IntentFilter(BROADCAST_CHANGEBG);
        mActivity.registerReceiver(mReceiver, filter);
    }
    private class ChangeBgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicPreferences sp = new MusicPreferences(mActivity);
            String type = sp.getCurrntType();
            if (type.equals("music")) {
                String path = intent.getStringExtra("path");
                Bitmap bitmap = getBitmapByPath(path);
                if (bitmap != null) {
                    mMainLayout.setBackground(new BitmapDrawable(mActivity.getResources(), bitmap));
                }
                if (commonUI != null) {
                    commonUI.setBgByPath(path);
                }
            }
        }
    }
    public Bitmap getBitmapByPath(String path) {
        AssetManager am = mActivity.getAssets();
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
    private View findViewById(int id) {
        return mView.findViewById(id);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mRefreshListener = listener;
    }
    public void setContentType(int type) {
        // 此处可以根据传递过来的view和type分开来处理
        setContentType(type, null);
    }

    public void setCurrentItem() {
        if (mViewPagerSub.getChildCount() > 0) {
            mViewPagerSub.setCurrentItem(0, true);
        } else {
            mViewPager.setCurrentItem(0, true);
        }
    }
    public void setContentType(int type, Object obj) {
        // 注册监听返回按钮
        mMainActivity.registerBackListener(this);
        switch (type) {
            case START_FROM_LOCAL:
                commonUI = new MyMusicUI(mActivity, this);
                View transView1 = mInflater.inflate(
                        R.layout.viewpager_trans_layout, null);
                View contentView1 = commonUI.getView(START_FROM_LOCAL);
                mViewPager.setVisibility(View.VISIBLE);
                mListViews.clear();
                mViewPager.removeAllViews();

                mListViews.add(transView1);
                mListViews.add(contentView1);
                mViewPager.setAdapter(new MyPagerAdapter(mListViews));
                mViewPager.setCurrentItem(1, true);
                break;
            case START_FROM_FAVORITE:
                commonUI = new MyMusicUI(mActivity, this);
                View transView2 = mInflater.inflate(
                        R.layout.viewpager_trans_layout, null);
                View contentView2 = commonUI.getView(START_FROM_FAVORITE);
                mViewPager.setVisibility(View.VISIBLE);
                mListViews.clear();
                mViewPager.removeAllViews();

                mListViews.add(transView2);
                mListViews.add(contentView2);
                mViewPager.setAdapter(new MyPagerAdapter(mListViews));
                mViewPager.setCurrentItem(1, true);
                break;
            case START_FROM_FOLDER:
                commonUI = new FolderUI(
                        mActivity, this);
                View transView3 = mInflater.inflate(
                        R.layout.viewpager_trans_layout, null);
                View folderView = commonUI.getView();
                mViewPager.setVisibility(View.VISIBLE);
                mListViews.clear();
                mViewPager.removeAllViews();

                mListViews.add(transView3);
                mListViews.add(folderView);
                mViewPager.setAdapter(new MyPagerAdapter(mListViews));
                mViewPager.setCurrentItem(1, true);
                break;
            case START_FROM_ARTIST:
                commonUI = new ArtistUI(
                        mActivity, this);
                View transView4 = mInflater.inflate(
                        R.layout.viewpager_trans_layout, null);
                View artistView = commonUI.getView();
                mViewPager.setVisibility(View.VISIBLE);
                mListViews.clear();
                mViewPager.removeAllViews();

                mListViews.add(transView4);
                mListViews.add(artistView);
                mViewPager.setAdapter(new MyPagerAdapter(mListViews));
                mViewPager.setCurrentItem(1, true);
                break;
            case START_FROM_ALBUM:
                commonUI = new AlbumUI(
                        mActivity, this);
                View transView5 = mInflater.inflate(
                        R.layout.viewpager_trans_layout, null);
                View albumView = commonUI.getView();
                mViewPager.setVisibility(View.VISIBLE);
                mListViews.clear();
                mViewPager.removeAllViews();

                mListViews.add(transView5);
                mListViews.add(albumView);
                mViewPager.setAdapter(new MyPagerAdapter(mListViews));
                mViewPager.setCurrentItem(1, true);
                break;
            case FOLDER_TO_MYMUSIC:
                commonUI = new MyMusicUI(mActivity, this);
                View transViewSub1 = mInflater.inflate(
                        R.layout.viewpager_trans_layout, null);
                View contentViewSub1 = commonUI.getView(START_FROM_FOLDER, obj);
                mViewPagerSub.setVisibility(View.VISIBLE);
                mListViewsSub.clear();
                mViewPagerSub.removeAllViews();

                mListViewsSub.add(transViewSub1);
                mListViewsSub.add(contentViewSub1);
                mViewPagerSub.setAdapter(new MyPagerAdapter(mListViewsSub));
                mViewPagerSub.setCurrentItem(1, true);
                break;
            case ARTIST_TO_MYMUSIC:
                commonUI = new MyMusicUI(mActivity, this);
                View transViewSub2 = mInflater.inflate(
                        R.layout.viewpager_trans_layout, null);
                View contentViewSub2 = commonUI.getView(START_FROM_ARTIST, obj);
                mViewPagerSub.setVisibility(View.VISIBLE);
                mListViewsSub.clear();
                mViewPagerSub.removeAllViews();

                mListViewsSub.add(transViewSub2);
                mListViewsSub.add(contentViewSub2);
                mViewPagerSub.setAdapter(new MyPagerAdapter(mListViewsSub));
                mViewPagerSub.setCurrentItem(1, true);
                break;
            case ALBUM_TO_MYMUSIC:
                commonUI = new MyMusicUI(mActivity, this);
                View transViewSub3 = mInflater.inflate(
                        R.layout.viewpager_trans_layout, null);
                View contentViewSub3 = commonUI.getView(START_FROM_ALBUM, obj);
                mViewPagerSub.setVisibility(View.VISIBLE);
                mListViewsSub.clear();
                mViewPagerSub.removeAllViews();

                mListViewsSub.add(transViewSub3);
                mListViewsSub.add(contentViewSub3);
                mViewPagerSub.setAdapter(new MyPagerAdapter(mListViewsSub));
                mViewPagerSub.setCurrentItem(1, true);
                break;
        }
    }
    private class MyPagerAdapter extends PagerAdapter {

        private List<View> listViews;

        public MyPagerAdapter(List<View> views) {
            this.listViews = views;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(listViews.get(position));// 删除页卡
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {// 这个方法用来实例化页卡
            container.addView(listViews.get(position));// 添加页卡
            return listViews.get(position);
        }

        @Override
        public int getCount() {
            return listViews.size();// 返回页卡的数量
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;// 官方提示这样写
        }
    }
    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int onPageScrolled = -1;

        // 当滑动状态改变时调用
        @Override
        public void onPageScrollStateChanged(int arg0) {
            System.out.println("onPageScrollStateChanged--->" + arg0);
            if (onPageScrolled == 0 && arg0 == 0) {
                mMainActivity.unRegisterBackListener(UIManager.this);
                mViewPager.removeAllViews();
                mViewPager.setVisibility(View.INVISIBLE);
                if (mRefreshListener != null) {
                    mRefreshListener.onRefresh();
                }
            }
        }

        // 当当前页面被滑动时调用
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            onPageScrolled = arg0;
            // System.out.println("onPageScrolled--->" + "arg0=" + arg0 +
            // " arg1="
            // + arg1 + " arg2=" + arg2);
        }

        // 当新的页面被选中时调用
        @Override
        public void onPageSelected(int arg0) {
            // System.out.println("onPageSelected--->" + arg0);
        }
    }

    private class MyOnPageChangeListenerSub implements ViewPager.OnPageChangeListener {

        int onPageScrolled = -1;

        // 当滑动状态改变时调用
        @Override
        public void onPageScrollStateChanged(int arg0) {
            if (onPageScrolled == 0 && arg0 == 0) {
                mViewPagerSub.removeAllViews();
                mViewPagerSub.setVisibility(View.INVISIBLE);
            }
        }

        // 当当前页面被滑动时调用
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            onPageScrolled = arg0;
        }

        // 当新的页面被选中时调用
        @Override
        public void onPageSelected(int arg0) {
        }
    }
    public void unRegister() {
        mActivity.unregisterReceiver(mReceiver);
    }

}
