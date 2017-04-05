package com.davidzhao.music.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.davidzhao.music.R;
import com.davidzhao.music.SharePreferences.MusicPreferences;
import com.davidzhao.music.Utils.MusicUtils;
import com.davidzhao.music.interfaces.IConstant;
import com.davidzhao.music.model.ArtistInfo;

import java.util.List;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-6.
 */

public class ArtistUI extends CommonUI implements IConstant,
        View.OnClickListener, AdapterView.OnItemClickListener{
    private Activity mActivity;
    private UIManager mUIManager;
    private LayoutInflater mInflater;

    private ListView mListView;
    private ImageButton mBackBtn;
    private List<ArtistInfo> mArtistList;
    private MyAdapter mAdapter;

    private LinearLayout mArtistLayout;

    public ArtistUI(Activity activity, UIManager manager) {
        this.mActivity = activity;
        this.mUIManager = manager;
        this.mInflater = LayoutInflater.from(activity);
    }

    public View getView() {
        View view = mInflater.inflate(R.layout.artistbrower, null);
        initBg(view);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.artist_listview);
        mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
        mBackBtn.setOnClickListener(this);

        mAdapter = new MyAdapter();
        mArtistList = MusicUtils.queryArtist(mActivity);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    private void initBg(View view) {
        mArtistLayout = (LinearLayout) view.findViewById(R.id.main_artist_layout);
        MusicPreferences mSp = new MusicPreferences(mActivity);
        String mDefaultBgPath = mSp.getMusicPath();
        Bitmap bitmap = mUIManager.getBitmapByPath(mDefaultBgPath);
        if(bitmap != null) {
            mArtistLayout.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));
        }
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mArtistList.size();
        }

        @Override
        public ArtistInfo getItem(int position) {
            return mArtistList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ArtistInfo artist = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mActivity.getLayoutInflater().inflate(
                        R.layout.artistbrower_listitem, null);
                viewHolder.artistNameTv = (TextView) convertView
                        .findViewById(R.id.artist_name_tv);
                viewHolder.numberTv = (TextView) convertView
                        .findViewById(R.id.number_of_tracks_tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.artistNameTv.setText(artist.artist_name);
            viewHolder.numberTv.setText(artist.number_of_tracks + "");

            return convertView;
        }

        private class ViewHolder {
            TextView artistNameTv, numberTv;
        }

    }

    @Override
    public void onClick(View v) {
        if (v == mBackBtn) {
            mUIManager.setCurrentItem();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        mUIManager.setContentType(ARTIST_TO_MYMUSIC, mAdapter.getItem(position));
    }

    @Override
    protected void setBgByPath(String path) {
        Bitmap bitmap = mUIManager.getBitmapByPath(path);
        if(bitmap != null) {
            mArtistLayout.setBackground(new BitmapDrawable(mActivity.getResources(), bitmap));
        }
    }

    @Override
    public View getView(int from) {
        return null;
    }

    @Override
    public View getView(int from, Object obj) {
        return null;
    }
}
