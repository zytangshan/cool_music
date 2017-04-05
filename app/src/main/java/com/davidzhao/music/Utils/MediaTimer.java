package com.davidzhao.music.Utils;

import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-6.
 */

public class MediaTimer {
    public final static int REFRESH_PROGRESS_EVENT = 0x100;

    private static final int INTERVAL_TIME = 1000;
    private Handler[] mHandler;
    private Timer mTimer;
    private TimerTask mTimerTask;

    private int what;
    private boolean mTimerStart = false;

    public MediaTimer(Handler... handler) {
        this.mHandler = handler;
        this.what = REFRESH_PROGRESS_EVENT;

        mTimer = new Timer();
    }

    public void startTimer() {
        if (mHandler == null || mTimerStart) {
            return;
        }
        mTimerTask = new MyTimerTask();
        mTimer.schedule(mTimerTask, 0 , INTERVAL_TIME);
        mTimerStart = true;
    }

    public void stopTimer() {
        if (!mTimerStart) {
            return;
        }
        mTimerStart = false;
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            if (mHandler != null) {
                for (Handler handler : mHandler) {
                    Message msg = handler.obtainMessage(what);
                    msg.sendToTarget();
                }
            }
        }

    }
}
