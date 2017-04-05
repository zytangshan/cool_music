package com.davidzhao.music.Fragment;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.activity.common.MainActivity;
import com.davidzhao.music.activity.music.MenuBackgroundActivity;
import com.davidzhao.music.activity.music.MenuScanActivity;
import com.davidzhao.music.activity.music.MenuSettingActivity;
import com.davidzhao.music.application.MusicApp;
import com.davidzhao.music.interfaces.IConstant;
import com.davidzhao.music.service.ServiceManager;
import com.davidzhao.music.slidemenu.SlidingMenu;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-7.
 */

public class MenuFragment extends Fragment implements IConstant, View.OnClickListener,
        SlidingMenu.OnOpenedListener{
    private TextView mMediaCountTv;
    private TextView mScanTv, mPlayModeTv, mBackgroundTv, mSleepTv, mSettingTv,
            mExitTv;
    private MainActivity mMainActivity;

    private int mCurMode;
    private ServiceManager mServiceManager;
    private static final String modeName[] = { "列表循环", "顺序播放", "随机播放", "单曲循环" };
    private int modeDrawable[] = { R.drawable.icon_list_reapeat,
            R.drawable.icon_sequence, R.drawable.icon_shuffle,
            R.drawable.icon_single_repeat };
    private int mScreenWidth, mScreenHeight;
    public static final String ALARM_CLOCK_BROADCAST = "alarm_clock_broadcast";
    private MusicPreferences sp;
    private String type;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sp = new MusicPreferences(getContext());
        type = sp.getCurrntType();
        View view;
        if (type.equals("video")) {
             view= inflater.inflate(R.layout.video_menu, container, false);
        } else if (type.equals("image")) {
            view = inflater.inflate(R.layout.image_menu, container, false);
        } else {
            view = inflater.inflate(R.layout.frame_menu1, container, false);
        }
        initView(view);
        mServiceManager = MusicApp.mServiceManager;
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivity = (MainActivity) getActivity();
        mMainActivity.mSlidingMenu.setOnOpenedListener(this);
    }

    private void initView(View view) {
        mMediaCountTv = (TextView) view.findViewById(R.id.txt_media_count);
        mScanTv = (TextView) view.findViewById(R.id.txt_scan);
        mPlayModeTv = (TextView) view.findViewById(R.id.txt_play_mode);
        mBackgroundTv = (TextView) view.findViewById(R.id.txt_background);
        mSleepTv = (TextView) view.findViewById(R.id.txt_sleep);
        mSettingTv = (TextView) view.findViewById(R.id.preference_text);
        mExitTv = (TextView) view.findViewById(R.id.txt_exit);

        mMediaCountTv.setOnClickListener(this);
        mScanTv.setOnClickListener(this);
        mPlayModeTv.setOnClickListener(this);
        mBackgroundTv.setOnClickListener(this);
        mSleepTv.setOnClickListener(this);
        mSettingTv.setOnClickListener(this);
        mExitTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_scan:
                startActivityForResult(new Intent(getActivity(), MenuScanActivity.class), 1);
                break;
            case R.id.txt_play_mode:
                changeMode();
                break;
            case R.id.txt_background:
                startActivity(new Intent(getActivity(), MenuBackgroundActivity.class));
                break;
            case R.id.preference_text:
                startActivity(new Intent(getActivity(), MenuSettingActivity.class));
                break;
            case R.id.txt_sleep:
                showSleepDialog();
                break;
            case R.id.txt_exit:
                getActivity().finish();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            ((MainActivity)getActivity()).mMainFragment.refreshNum();
        }
    }

    private void changeMode() {
        mCurMode++;
        if (mCurMode > MPM_SINGLE_LOOP_PLAY) {
            mCurMode = MPM_LIST_LOOP_PLAY;
        }
        mServiceManager.setPlayMode(mCurMode);
        initPlayMode();
    }

    private void initPlayMode() {
        if(type.equals("music")) {
            mPlayModeTv.setText(modeName[mCurMode]);
        }
        Drawable drawable = getResources().getDrawable(modeDrawable[mCurMode]);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        mPlayModeTv.setCompoundDrawables(drawable, null, null, null);
    }

    @Override
    public void onOpened() {
        mCurMode = mServiceManager.getPlayMode();
        initPlayMode();
    }

    public void showSleepDialog() {
        cancelSleep();
        View view = View.inflate(getContext(), R.layout.sleep_time, null);
        final Dialog sleepDialog = new Dialog(getContext(), R.style.lrc_dialog);
        sleepDialog.setContentView(view);
        sleepDialog.setCanceledOnTouchOutside(false);
        Window window = sleepDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int)(mScreenWidth * 0.7);
        //lp.height = mScreenHeight;
        window.setAttributes(lp);
        sleepDialog.show();
        final Button cancel = (Button)view.findViewById(R.id.cancle_btn);
        final Button ok = (Button)view.findViewById(R.id.ok_btn);
        final EditText time = (EditText)view.findViewById(R.id.time_et);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sleepDialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sleepTime;
                sleepTime = time.getText().toString();
                if (TextUtils.isEmpty(sleepTime) ||
                        Integer.parseInt(sleepTime) == 0) {
                    Toast.makeText(getActivity(), "invalid time", Toast.LENGTH_SHORT).show();
                } else {

                    setSleep(sleepTime);
                    sleepDialog.dismiss();
                }
            }
        });

    }

    public void cancelSleep() {
        Intent intent = new Intent(ALARM_CLOCK_BROADCAST);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(),0,intent,0);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        MusicApp.mIsSleepClockSetting = false;
    }

    public void setSleep(String sleepTime) {
        Intent intent = new Intent(ALARM_CLOCK_BROADCAST);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        long time = Integer.parseInt(sleepTime) * 60 * 1000;
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + time, pendingIntent);
        MusicApp.mIsSleepClockSetting = true;
        Toast.makeText(getActivity(), "set sleep successfull", Toast.LENGTH_SHORT).show();
    }

    private BroadcastReceiver sleepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String receiver = intent.getAction().toString();
            if (receiver.equals(ALARM_CLOCK_BROADCAST))
            {
                Log.e("david" , "receiver" + receiver);
                getActivity().finish();
            }
        }
    };
}
