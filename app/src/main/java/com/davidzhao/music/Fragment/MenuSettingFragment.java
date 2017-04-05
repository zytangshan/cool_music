package com.davidzhao.music.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.activity.music.MenuSettingActivity;
import com.davidzhao.music.activity.music.OpinionActivity;
import com.davidzhao.music.interfaces.IConstant;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-7.
 */

public class MenuSettingFragment extends Fragment implements IConstant,
        View.OnClickListener{
    private LinearLayout mAdviceLayout, mAboutLayout;
    private CheckedTextView mChangeSongTv, mAutoLyricTv, mFilterSizeTv, mFilterTimeTv;
    private MusicPreferences mSp;

    private ImageButton mBackBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.menu_setting_fragment, container, false);

        mSp = new MusicPreferences(getActivity());

        initView(view);

        return view;
    }

    private void initView(View view) {
        mAboutLayout = (LinearLayout) view.findViewById(R.id.setting_about_layout);
        mAdviceLayout = (LinearLayout) view.findViewById(R.id.setting_advice_layout);
        mAboutLayout.setOnClickListener(this);
        mAdviceLayout.setOnClickListener(this);

        mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
        mBackBtn.setOnClickListener(this);

        mChangeSongTv = (CheckedTextView) view.findViewById(R.id.shake_change_song);
        mAutoLyricTv = (CheckedTextView) view.findViewById(R.id.auto_download_lyric);
        mFilterSizeTv = (CheckedTextView) view.findViewById(R.id.filter_size);
        mFilterTimeTv = (CheckedTextView) view.findViewById(R.id.filter_time);

        mChangeSongTv.setChecked(mSp.getShake());
        mAutoLyricTv.setChecked(mSp.getAutoLyric());
        mFilterSizeTv.setChecked(mSp.getFilterSize());
        mFilterTimeTv.setChecked(mSp.getFilterTime());

        mChangeSongTv.setOnClickListener(this);
        mAutoLyricTv.setOnClickListener(this);
        mFilterSizeTv.setOnClickListener(this);
        mFilterTimeTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.setting_about_layout:
                showAboutDialog();
                break;
            case R.id.setting_advice_layout:
                startOpinion();
                break;
            case R.id.shake_change_song:
                mChangeSongTv.toggle();
                mSp.saveShake(mChangeSongTv.isChecked());
                Intent intent = new Intent(BROADCAST_SHAKE);
                intent.putExtra(SHAKE_ON_OFF, mChangeSongTv.isChecked());
                getActivity().sendBroadcast(intent);
                break;
            case R.id.auto_download_lyric:
                mAutoLyricTv.toggle();
                mSp.saveAutoLyric(mAutoLyricTv.isChecked());
                break;
            case R.id.filter_size:
                mFilterSizeTv.toggle();
                mSp.saveFilterSize(mFilterSizeTv.isChecked());
                break;
            case R.id.filter_time:
                mFilterTimeTv.toggle();
                mSp.saveFilterTime(mFilterTimeTv.isChecked());
                break;
            case R.id.backBtn:
                ((MenuSettingActivity)getActivity()).mViewPager.setCurrentItem(0, true);
                break;
        }
    }
    public void showAboutDialog() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage("cool_music is opensource");
        builder.setTitle("About");
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void startOpinion() {
        Intent intent = new Intent(getActivity(),OpinionActivity.class);
        startActivity(intent);
    }

}
