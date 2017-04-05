package com.davidzhao.music.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.davidzhao.music.Adapter.ImageAdapter;
import com.davidzhao.music.Adapter.VideoAdapter;
import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.Utils.ImageUtils;
import com.davidzhao.music.Utils.VideoUtils;
import com.davidzhao.music.activity.picture.ImageActivity;
import com.davidzhao.music.interfaces.IConstant;
import com.davidzhao.music.model.ImageInfo;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-17.
 */

public class ImageGridFragment extends Fragment implements IConstant {
    private ImageButton back;
    private GridView imageList;
    private RelativeLayout mMainLayout;
    private ChangeBgReceiver changeBgReceiver;
    private Context mContext;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.myimage, container, false);
        back = (ImageButton)view.findViewById(R.id.image_backBtn);
        back.setOnClickListener(new ViewOnClickListener());
        imageList = (GridView)view.findViewById(R.id.image_gridview);
        final ImageAdapter imageAdapter = new ImageAdapter(getContext(),
                ImageUtils.getImage(getContext()));
        mContext = getContext();
        //Log.e("david", "image list " + ImageUtils.getImage(getContext()).get(0).imageName.toString());
        imageList.setAdapter(imageAdapter);
        imageList.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("david", "image click");
                ImageInfo temp = (ImageInfo) imageAdapter.getItem(i);
                Log.e("david", "click image" + temp.artist);
                Intent intent = new Intent(getActivity(), ImageActivity.class);
                intent.putExtra("path", temp.artist);
                startActivity(intent);

            }
        });
        mMainLayout = (RelativeLayout) view.findViewById(R.id.main_mymusic_layout);
        IntentFilter filter = new IntentFilter(BROADCAST_CHANGEBG);
        changeBgReceiver = new ChangeBgReceiver();
        getActivity().registerReceiver(changeBgReceiver, filter);
        MusicPreferences mSp = new MusicPreferences(getActivity());
        String mDefaultBgPath = mSp.getImagePath();
        Bitmap bitmap = getBitmapByPath(mDefaultBgPath);
        if(bitmap != null) {
            mMainLayout.setBackground(new BitmapDrawable(getActivity().getResources(), bitmap));
        }

        //如果第一次进来 SharedPreference中没有数据
        if(TextUtils.isEmpty(mDefaultBgPath)) {
            mSp.saveVideoPath("004.jpg");
        }

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private class ViewOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.image_backBtn:
                    Log.e("david", "image back button");
                    //getActivity().getSupportFragmentManager().beginTransaction().remove(new VideoListFragment()).commit();
                    Intent intent = new Intent("Image Back Press");
                    intent.putExtra("Back", "Image");
                    getActivity().sendBroadcast(intent);
                    break;
                default:
                    break;
            }

        }
    }
    private class ChangeBgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicPreferences sp = new MusicPreferences(context);
            String type = sp.getCurrntType();
            if (type.equals("image")) {
                String path = intent.getStringExtra("path");
                Bitmap bitmap = getBitmapByPath(path);
                if (bitmap != null) {
                    mMainLayout.setBackground(new BitmapDrawable(mContext.getResources(), bitmap));
                }
                /*if (commonUI != null) {
                    commonUI.setBgByPath(path);
                }*/
            }
        }
    }
    public Bitmap getBitmapByPath(String path) {
        AssetManager am = mContext.getAssets();
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
}
