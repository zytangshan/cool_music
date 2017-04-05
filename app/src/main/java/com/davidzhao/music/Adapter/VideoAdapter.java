package com.davidzhao.music.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidzhao.music.R;
import com.davidzhao.music.model.VideoInfo;

import java.util.ArrayList;

import static com.davidzhao.music.Utils.VideoUtils.makeTimeString;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-17.
 */

public class VideoAdapter extends BaseAdapter {
    private ArrayList<VideoInfo> videoList;
    private LayoutInflater mlayoutInflater;

    public VideoAdapter(Context context, ArrayList<VideoInfo> videoList) {
        //this.videoList = videoList;
        this.videoList = new ArrayList<VideoInfo>();
        this.videoList.addAll(videoList);
        mlayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return videoList.size();
    }

    @Override
    public Object getItem(int i) {
        return videoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        final VideoInfo videoInfo = (VideoInfo) getItem(i);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = mlayoutInflater.inflate(R.layout.videolist_layout, null);
            viewHolder.videoName = (TextView) view.findViewById(R.id.video_name);
            viewHolder.duration = (TextView) view.findViewById(R.id.video_duration);
            viewHolder.playState = (ImageView) view.findViewById(R.id.playstate_iv);
            viewHolder.video_time = (TextView) view.findViewById(R.id.video_time);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.videoName.setText(videoInfo.videoName.toString());
        viewHolder.duration.setText(makeTimeString(videoInfo.duration));
        viewHolder.playState.setImageBitmap(videoInfo.bitmap);
        viewHolder.video_time.setText(videoInfo.data);
        Log.e("david", "video adapter " + videoInfo.bitmap.toString());
        return view;
    }

    public void setVideoData(ArrayList<VideoInfo> videoInfoArrayList) {
        videoList.addAll(videoInfoArrayList);
    }

    class ViewHolder {
        TextView videoName, duration, video_time;
        ImageView playState, favorite;
    }
}
