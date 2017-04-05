package com.davidzhao.music.interfaces;

import com.davidzhao.music.aidl.IMediaService;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-6.
 */

public interface IOnServiceConnectComplete {
    public void onServiceConnectComplete(IMediaService service);
}
