package com.davidzhao.music.activity.picture;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Gallery;
import android.widget.ImageView;

import com.davidzhao.music.Adapter.HorizontalScrollViewAdapter;
import com.davidzhao.music.R;
import com.davidzhao.music.Utils.ImageUtils;
import com.davidzhao.music.model.ImageInfo;
import com.davidzhao.music.view.MyHorizontalScrollView;

import java.util.ArrayList;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-20.
 */

public class ImageActivity extends Activity {
    //private HorizontalScrollView horizontalScrollView;
    private Gallery gallery;
    private ArrayList<ImageInfo> imageInfos;
    private ImageView mImg;
    MyHorizontalScrollView mHorizontalScrollView;
    private Handler handler;
    private HorizontalScrollViewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image_scroll_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mHorizontalScrollView.initDatas(adapter);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                init();

                handler.sendMessage(new Message());
            }
        }).start();
    }



    public void init() {
        Log.e("david", "image create");
        imageInfos = new ArrayList<>();
        imageInfos.addAll(ImageUtils.getImage(getApplicationContext()));
        adapter = new HorizontalScrollViewAdapter(
                getApplicationContext(), imageInfos);
        String path = getIntent().getStringExtra("path").toString();

        Log.e("david", "image create 2");
        mImg = (ImageView) findViewById(R.id.id_content);

        mHorizontalScrollView = (MyHorizontalScrollView) findViewById(R.id.id_horizontalScrollView);
        //callback
        mHorizontalScrollView
                .setCurrentImageChangeListener(new MyHorizontalScrollView.CurrentImageChangeListener()
                {
                    @Override
                    public void onCurrentImgChanged(int position,
                                                    View viewIndicator)
                    {
                        mImg.setImageBitmap(imageInfos.get(position).bitmap);
                        viewIndicator.setBackgroundColor(Color.GRAY);
                    }
                });
        //callback
        mHorizontalScrollView.setOnItemClickListener(new MyHorizontalScrollView.OnItemClickListener()
        {

            @Override
            public void onClick(View view, int position)
            {
                mImg.setImageBitmap(imageInfos.get(position).bitmap);
                view.setBackgroundColor(Color.GRAY);
            }
        });
        Log.e("david", "image create 3");
        //adapter
        //mHorizontalScrollView.initDatas(adapter);
        Log.e("david", "image create 4");
    }


}
