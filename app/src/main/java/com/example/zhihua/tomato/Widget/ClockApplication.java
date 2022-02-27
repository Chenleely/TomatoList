package com.example.zhihua.tomato.Widget;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.zhihua.tomato.Utils.SPUtils;

import java.util.concurrent.TimeUnit;
/*
* 用于保存番茄运行的状态、场景、运行时间、结束时间、剩余时间
* */
public class ClockApplication extends Application {
    public static final int DEFAULT_WORK_LENGTH = 25;
    public static final int DEFAULT_SHORT_BREAK = 5;
    public static final int DEFAULT_LONG_BREAK  = 20;
    public static final int DEFAULT_LONG_BREAK_FREQUENCY = 4; // 默认 4 次开始长休息

    // 场景
    public static final int SCENE_WORK = 0;
    public static final int SCENE_SHORT_BREAK = 1;
    public static final int SCENE_LONG_BREAK = 2;

    // 当前状态
    public static final int STATE_WAIT = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_FINISH = 3;

//    结束的时间
    private long mStopTimeInFuture;
//    总执行时间
    private long mMillisInTotal;
//    剩余的时间
    private long mMillisUntilFinished;

    private int mTimes;
    private int mState;

    @Override
    public void onCreate() {
        super.onCreate();

        mState = STATE_WAIT;
    }

    public void reload() {
        switch(mState) {
            case STATE_WAIT:
            case STATE_FINISH:
                //将Clock预设的时间传入
                mMillisInTotal = TimeUnit.MINUTES.toMillis(getMinutesInTotal());
                mMillisUntilFinished = mMillisInTotal;
                break;
            case STATE_RUNNING:
                //实际运行时间超过了结束时间，结束这次计时
                if (SystemClock.elapsedRealtime() > mStopTimeInFuture) {
                    finish();
                }
                break;
        }
    }

    public void start() {
        setState(STATE_RUNNING);
        //结束时间=boot时间+设置的总时间
        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInTotal;
    }

    public void pause() {
        setState(STATE_PAUSE);
        //剩余时间=结束时间-boot时间
        mMillisUntilFinished = mStopTimeInFuture - SystemClock.elapsedRealtime();
    }

    public void resume() {
        setState(STATE_RUNNING);
        //结束时间=boot时间+剩余时间
        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisUntilFinished;
    }

    public void stop() {
        setState(STATE_WAIT);
        reload();
    }

    public void skip() {
        setState(STATE_WAIT);
        setTimes();
        reload();
    }

    public void finish() {
        setState(STATE_FINISH);
        setTimes();
        reload();
    }

    public void exit() {
        setState(STATE_WAIT);
        mTimes = 0;
        reload();
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    private void setTimes() {
        mTimes++; // 注意这里不能在 activity 中使用, 如果睡眠中就不能保证会运行
    }

    public int getScene() {
        int frequency = (int)SPUtils
                .get(this,"pref_key_long_break_frequency", DEFAULT_LONG_BREAK_FREQUENCY);
        frequency = frequency * 2; // 工作/短休息/工作/短休息/工作/短休息/工作/长休息

        if (mTimes % 2  == 1) { // 偶数：工作, 奇数：休息

            if ((mTimes + 1 ) % frequency == 0) { // 长休息
                return SCENE_LONG_BREAK;
            }

            return SCENE_SHORT_BREAK;
        }

        return SCENE_WORK;
    }

    //根据当前场景返回对应的总时间
    public int getMinutesInTotal() {
        int minutes = 0;

        switch (getScene()) {
            case SCENE_WORK:
                minutes = (int) SPUtils
                        .get(this,"pref_key_work_length", DEFAULT_WORK_LENGTH);
                break;
            case SCENE_SHORT_BREAK:
                minutes = (int)SPUtils
                        .get(this,"pref_key_short_break", DEFAULT_SHORT_BREAK);
                break;
            case SCENE_LONG_BREAK:
                minutes = (int)SPUtils
                        .get(this,"pref_key_long_break", DEFAULT_LONG_BREAK);
                break;
        }

        return minutes;
    }

    public long getMillisInTotal() {
        return mMillisInTotal;
    }

    public void setMillisUntilFinished(long millisUntilFinished) {
        mMillisUntilFinished = millisUntilFinished;
    }

    public long getMillisUntilFinished() {
        if (mState == STATE_RUNNING) {
            return mStopTimeInFuture - SystemClock.elapsedRealtime();
        }

        return mMillisUntilFinished;
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }
}
