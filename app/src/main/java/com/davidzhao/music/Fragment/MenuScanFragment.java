package com.davidzhao.music.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.davidzhao.music.Database.DatabaseHelper;
import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.Utils.ImageUtils;
import com.davidzhao.music.Utils.MusicUtils;
import com.davidzhao.music.Utils.VideoUtils;
import com.davidzhao.music.activity.music.MenuScanActivity;
import com.davidzhao.music.interfaces.IConstant;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-7.
 */

public class MenuScanFragment extends Fragment implements IConstant,
        View.OnClickListener{
    private Button mScanBtn;
    private ImageButton mBackBtn;
    private Handler mHandler;
    private DatabaseHelper mHelper;
    private ProgressDialog mProgress;
    private MusicPreferences sp;
    private String type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = new MusicPreferences(getActivity());
        mHelper = new DatabaseHelper(getActivity());
        type = sp.getCurrntType();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.menu_scan_fragment, container,
                false);
        mScanBtn = (Button) view.findViewById(R.id.scanBtn);
        mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
        mScanBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    Thread.sleep(1000);
                }catch (Exception e) {}
                mProgress.dismiss();
                ((MenuScanActivity)getActivity()).mViewPager.setCurrentItem(0, true);
            }
        };

        return view;
    }

    private void getData() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                if (type.equals("music")) {
                    mHelper.deleteTables(getActivity());
                    MusicUtils.queryMusic(getActivity(), START_FROM_LOCAL);
                    MusicUtils.queryAlbums(getActivity());
                    MusicUtils.queryArtist(getActivity());
                    MusicUtils.queryFolder(getActivity());
                } else if (type.equals("video")) {
                    VideoUtils.getVideo(getActivity());
                    VideoUtils.queryFolder(getActivity());
                } else if (type.equals("image")) {
                    ImageUtils.getImage(getActivity());
                    ImageUtils.queryFolder(getActivity());
                }
                mHandler.sendEmptyMessage(1);
                Looper.loop();
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        if(v == mScanBtn) {
            mProgress = new ProgressDialog(getActivity());
            if (type.equals("music")) {
                mProgress.setMessage("正在扫描歌曲，请勿退出软件！");
            } else if (type.equals("video")) {
                mProgress.setMessage("正在扫描视频，请勿退出软件！");
            } else if (type.equals("image")) {
                mProgress.setMessage("正在扫图片，请勿退出软件！");
            }
            mProgress.setCancelable(false);
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
            getData();
        } else if(v == mBackBtn) {
            ((MenuScanActivity)getActivity()).mViewPager.setCurrentItem(0, true);
        }
    }
}
