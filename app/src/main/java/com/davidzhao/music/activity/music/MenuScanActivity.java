package com.davidzhao.music.activity.music;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.davidzhao.music.Fragment.LeftFragment;
import com.davidzhao.music.Fragment.MenuScanFragment;
import com.davidzhao.music.Fragment.RightFragment;
import com.davidzhao.music.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-7.
 */

public class MenuScanActivity extends FragmentActivity {
    public ViewPager mViewPager;
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.menu_scan);
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


        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        initViewPager();
    }

    private void initViewPager() {
        Fragment leftFragment = new LeftFragment();
        Fragment rightFragment = new RightFragment();
        Fragment menuFragment = new MenuScanFragment();

        mFragmentList.add(leftFragment);
        mFragmentList.add(menuFragment);
        mFragmentList.add(rightFragment);

        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(),
                mFragmentList));
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        mViewPager.setCurrentItem(1, true);

		/*Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mViewPager.setCurrentItem(1, true);
			}
		};
		handler.sendEmptyMessageDelayed(1, 1000);*/
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragmentList;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public MyPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragmentList = fragments;
        }

        @Override
        public Fragment getItem(int arg0) {
            return fragmentList.get(arg0);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int onPageScrolled = -1;

        // 当滑动状态改变时调用
        @Override
        public void onPageScrollStateChanged(int arg0) {
//			System.out.println("onPageScrollStateChanged--->" + arg0);
            if ((onPageScrolled == 0 || onPageScrolled == 2) && arg0 == 0) {
                setResult(1);
                finish();
            }
        }

        // 当当前页面被滑动时调用
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            onPageScrolled = arg0;
//			System.out.println("onPageScrolled--->" + "arg0=" + arg0 + " arg1="
//					+ arg1 + " arg2=" + arg2);
        }

        // 当新的页面被选中时调用
        @Override
        public void onPageSelected(int arg0) {
//			System.out.println("onPageSelected--->" + arg0);
        }
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.isShown()) {
            mViewPager.setCurrentItem(0, true);
        }
    }
}
