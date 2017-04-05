package com.davidzhao.music.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidzhao.music.Fragment.MainFragment;
import com.davidzhao.music.R;
import com.davidzhao.music.interfaces.IConstant;
import com.davidzhao.music.model.ImageInfo;

import java.util.ArrayList;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-17.
 */

public class ImageAdapter extends BaseAdapter implements IConstant{

    private ArrayList<String> name;
    private ArrayList<ImageInfo> imageInfos;
    LayoutInflater layoutInflater;

    public ImageAdapter(Context context, ArrayList<ImageInfo> imageInfos) {
        this.imageInfos = imageInfos;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return imageInfos.size();
    }

    @Override
    public Object getItem(int arg0) {
        return imageInfos.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(
                    R.layout.image_gridview, null);
            holder.iv = (ImageView) convertView
                    .findViewById(R.id.gridview_item_iv);
            holder.nameTv = (TextView) convertView
                    .findViewById(R.id.gridview_item_name);
            holder.numTv = (TextView) convertView
                    .findViewById(R.id.gridview_item_num);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        /*convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (position) {

                }
            }
        });*/

        holder.iv.setImageBitmap(imageInfos.get(position).bitmap);
        holder.nameTv.setText(imageInfos.get(position).imageName);
        //holder.numTv.setText(imageInfos.get(position).artist);
        holder.numTv.setVisibility(View.GONE);

        return convertView;
    }

    private class ViewHolder {
        ImageView iv;
        TextView nameTv, numTv;
    }
}
