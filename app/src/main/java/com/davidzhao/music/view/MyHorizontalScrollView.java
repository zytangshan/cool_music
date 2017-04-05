package com.davidzhao.music.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.davidzhao.music.Adapter.HorizontalScrollViewAdapter;
import com.davidzhao.music.Utils.ImageUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-20.
 */

public class MyHorizontalScrollView extends HorizontalScrollView implements View.OnClickListener{
    /**
     * image scroll callback
     *
     */
    public interface CurrentImageChangeListener
    {
        void onCurrentImgChanged(int position, View viewIndicator);
    }

    /**
     * click
     *
     */
    public interface OnItemClickListener
    {
        void onClick(View view, int pos);
    }

    private CurrentImageChangeListener mListener;

    private OnItemClickListener mOnClickListener;

    private static final String TAG = "MyHorizontalScrollView";

    /**
     * HorizontalListView  LinearLayout
     */
    private LinearLayout mContainer;

    /**
     * width
     */
    private int mChildWidth;
    /**
     * height
     */
    private int mChildHeight;
    /**
     * current index
     */
    private int mCurrentIndex;
    /**
     * first index
     */
    private int mFristIndex;
    /**
     * first View
     */
    private View mFirstView;
    /**
     * adapter
     */
    private HorizontalScrollViewAdapter mAdapter;
    /**
     * one screen image count
     */
    private int mCountOneScreen;
    /**
     * screen width
     */
    private int mScreenWitdh;


    /**
     * key value
     */
    private Map<View, Integer> mViewPos = new HashMap<View, Integer>();

    public MyHorizontalScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWitdh = outMetrics.widthPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mContainer = (LinearLayout) getChildAt(0);
    }



    /**
     * load next
     */
    protected void loadNextImg()
    {
        if (mCurrentIndex == mAdapter.getCount() - 1)
        {
            return;
        }
        //remove first   scroll to 0
        scrollTo(0, 0);
        mViewPos.remove(mContainer.getChildAt(0));
        mContainer.removeViewAt(0);

        // next
        View view = mAdapter.getView(++mCurrentIndex, null, mContainer);
        view.setOnClickListener(this);
        mContainer.addView(view);
        mViewPos.put(view, mCurrentIndex);

        //first image
        mFristIndex++;
        if (mListener != null)
        {
            notifyCurrentImgChanged();
        }

    }
    /**
     * pre image
     */
    protected void loadPreImg()
    {
        //now the first
        if (mFristIndex == 0)
            return;
        //now first index
        int index = mCurrentIndex - mCountOneScreen;
        if (index >= 0)
        {
            //  mContainer = (LinearLayout) getChildAt(0);
            int oldViewPos = mContainer.getChildCount() - 1;
            mViewPos.remove(mContainer.getChildAt(oldViewPos));
            mContainer.removeViewAt(oldViewPos);

            View view = mAdapter.getView(index, null, mContainer);
            mViewPos.put(view, index);
            mContainer.addView(view, 0);
            view.setOnClickListener(this);

            scrollTo(mChildWidth, 0);

            mCurrentIndex--;
            mFristIndex--;

            if (mListener != null)
            {
                notifyCurrentImgChanged();

            }
        }
    }

    /**
     * scroll callback
     */
    public void notifyCurrentImgChanged()
    {
        for (int i = 0; i < mContainer.getChildCount(); i++)
        {
            mContainer.getChildAt(i).setBackgroundColor(Color.WHITE);
        }

        mListener.onCurrentImgChanged(mFristIndex, mContainer.getChildAt(0));

    }

    /**
     *init
     * @param mAdapter
     */
    public void initDatas(HorizontalScrollViewAdapter mAdapter)
    {
        Log.e("david", "time 1");
        this.mAdapter = mAdapter;
        mContainer = (LinearLayout) getChildAt(0);
        final View view = mAdapter.getView(0, null, mContainer);
        mContainer.addView(view);
        Log.e("david", "time 2");

        // view's width height
        if (mChildWidth == 0 && mChildHeight == 0)
        {
            int w = View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED);
            int h = View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED);
            view.measure(w, h);
            mChildHeight = view.getMeasuredHeight();
            mChildWidth = view.getMeasuredWidth();
            Log.e(TAG, view.getMeasuredWidth() + "," + view.getMeasuredHeight());

            //mCountOneScreen = mScreenWitdh / mChildWidth + 2;
            mCountOneScreen = ImageUtils.getImage(getContext()).size();

            Log.e(TAG, "mCountOneScreen = " + mCountOneScreen
                    + " ,mChildWidth = " + mChildWidth);


        }
        Log.e("david", "time 3");

        initFirstScreenChildren(mCountOneScreen);
        Log.e("david", "time 4");
    }

    /**
     * load View
     *
     * @param mCountOneScreen
     */
    public void initFirstScreenChildren(int mCountOneScreen)
    {
        mContainer = (LinearLayout) getChildAt(0);
        mContainer.removeAllViews();
        mViewPos.clear();
        Log.e("david", "time a");

        for (int i = 0; i < mCountOneScreen; i++)
        {
            View view = mAdapter.getView(i, null, mContainer);
            Log.e("david", "time b");
            view.setOnClickListener(this);
            mContainer.addView(view);
            mViewPos.put(view, i);
            Log.e("david", "time bb");
            mCurrentIndex = i;
        }
        Log.e("david", "time aa");

        if (mListener != null)
        {
            notifyCurrentImgChanged();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                //  Log.e(TAG, getScrollX() + "");

                int scrollX = getScrollX();
                // 如果当前scrollX为view的宽度，加载下一张，移除第一张
                Log.e("david", "scrollX" + scrollX);
                if (scrollX >= mChildWidth)
                {
                    //loadNextImg();
                }
                // 如果当前scrollX = 0， 往前设置一张，移除最后一张
                if (scrollX == 0)
                {
                    //loadPreImg();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void onClick(View v)
    {
        if (mOnClickListener != null)
        {
            for (int i = 0; i < mContainer.getChildCount(); i++)
            {
                mContainer.getChildAt(i).setBackgroundColor(Color.WHITE);
            }
            mOnClickListener.onClick(v, mViewPos.get(v));
        }
    }

    public void setOnItemClickListener(OnItemClickListener mOnClickListener)
    {
        this.mOnClickListener = mOnClickListener;
    }

    public void setCurrentImageChangeListener(
            CurrentImageChangeListener mListener)
    {
        this.mListener = mListener;
    }
}
