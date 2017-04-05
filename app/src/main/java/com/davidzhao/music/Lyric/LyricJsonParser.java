package com.davidzhao.music.Lyric;

import android.util.Log;
import android.view.GestureDetector;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-24.
 * json parse for gecimi
 */

public class LyricJsonParser {
    public static byte[] readParse(InputStream in) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = in.read(data)) != -1) {
                outStream.write(data, 0, len);

            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outStream.toByteArray();
    }

    //public static List<Song> getListPerson(InputStream in) throws Exception {
    public static String getLriList(InputStream in) throws Exception {

        List<Song> mlists = new ArrayList<Song>();

        byte[] data = readParse(in);
        int name;
        String address = null;
        JSONObject object = new JSONObject(new String(data));
        int count = object.optInt("count");
        if (count == 0) {
            Log.e("david", "no lric");
            name = 0;
            address = null;
        }else {
            JSONArray array = object.optJSONArray("result");
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                name = item.getInt("aid");
                address = item.getString("lrc");
                mlists.add(new Song(name, address));
                Log.e("david", "lrc address" + address );
                if (address != null) {
                    return address;
                }

            }
        }


        //return mlists;
        return address;

    }
    public static class Song{
        public static int id;
        public static String lrcAdress;

        public Song(int id, String lrcAdress) {
            this.lrcAdress = lrcAdress;
            this.id = id;
        }
    }
}
