package com.davidzhao.music.ui;

import android.view.View;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-6.
 */

public abstract class CommonUI {
    protected abstract void setBgByPath(String path);
    public abstract View getView();
    public abstract View getView(int from);
    public abstract View getView(int from, Object obj);
}
