package com.davidzhao.music.Lyric;

import android.content.Context;
import android.util.Log;

import com.davidzhao.music.application.MusicApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-8.
 */

public class LyricDownloadManager {
    private static final String TAG = LyricDownloadManager.class
            .getSimpleName();
    public static final String GB2312 = "GB2312";
    public static final String UTF_8 = "utf-8";
    private final int mTimeOut = 10 * 1000;
    private LyricXMLParser mLyricXMLParser = new LyricXMLParser();
    private URL mUrl = null;
    private int mDownloadLyricId = -1;
    private String lricAddress;

//	private Context mContext = null;

    public LyricDownloadManager(Context c) {
//		mContext = c;
    }

    /*
     * get lrc from web
     */
    public String searchLyricFromWeb(String musicName, String singerName, String oldMusicName) {
        // UTF_8 for chinese
        try {
            musicName = URLEncoder.encode(musicName, UTF_8);
            musicName = musicName.replaceAll("\\+",  " ");
            singerName = URLEncoder.encode(singerName, UTF_8);
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }

        String strUrl = "http://geci.me/api/lyric/" + musicName;

        try {
            mUrl = new URL(strUrl);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        try {
            HttpURLConnection httpConn = (HttpURLConnection) mUrl
                    .openConnection();
            //httpConn.setRequestMethod("get");
            httpConn.setReadTimeout(mTimeOut);
            if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "http  fail");
                return null;
            }
            httpConn.connect();
            Log.e(TAG, "http  success");

            //test json data
            /*InputStreamReader in = new InputStreamReader(httpConn.getInputStream());
            BufferedReader bu = new BufferedReader(in);
            String string;
            while((string = bu.readLine()) != null) {
                Log.e("david", string);

            }*/
            lricAddress = LyricJsonParser.getLriList(httpConn.getInputStream());

            httpConn.disconnect();
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.e(TAG, "http success , IO exception");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "XML parse error");
            return null;
        }
        return fetchLyricContent(musicName, singerName, oldMusicName);
    }

    /**
     * download lric
     * */
    private String fetchLyricContent(String musicName, String singerName, String oldMusicName) {
        if (lricAddress == null) {
            Log.e("david", "there no lric");
        }
        BufferedReader br = null;
        StringBuilder content = null;
        String temp = null;
        String lyricURL = lricAddress;
        Log.e(TAG, "down lric address:" + lyricURL);

        try {
            mUrl = new URL(lyricURL);
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
        }

        try {
            br = new BufferedReader(new InputStreamReader(mUrl.openStream()));
            if (br != null) {
                content = new StringBuilder();
                while ((temp = br.readLine()) != null) {
                    content.append(temp);
                    Log.e(TAG, "<Lyric>" + temp);
                }
                br.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.e(TAG, " get lric fail");
        }

        try {
            musicName = URLDecoder.decode(musicName, UTF_8);
            singerName = URLDecoder.decode(singerName, UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (content != null) {
//			String folderPath = PreferenceManager.getDefaultSharedPreferences(
//					mContext).getString(SettingFragment.KEY_LYRIC_SAVE_PATH,
//					Constant.LYRIC_SAVE_FOLDER_PATH);
            String folderPath = MusicApp.lrcPath;

            File savefolder = new File(folderPath);
            if (!savefolder.exists()) {
                savefolder.mkdirs();
            }
            String savePath = folderPath + File.separator + oldMusicName
                    + ".lrc";
//			String savePath = folderPath + File.separator + musicName + ".lrc";
            Log.e(TAG, "lric path:" + savePath);

            saveLyric(content.toString(), savePath);

            return savePath;
        } else {
            return null;
        }

    }

    /** save lric */
    private void saveLyric(String content, String filePath) {
        File file = new File(filePath);
        try {
            OutputStream outstream = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(outstream);
            out.write(content);
            out.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Log.i(TAG, " io error");
        }
        Log.i(TAG, "save success");
    }
}
