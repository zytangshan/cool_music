package com.davidzhao.music.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davidzhao.music.R;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-7.
 */

public class RightFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.viewpager_trans_layout, container, false);
    }
}
