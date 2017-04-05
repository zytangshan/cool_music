package com.davidzhao.music.Lyric;

import android.annotation.SuppressLint;
import android.util.Log;

import com.davidzhao.music.model.LyricSentence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by SPREADTRUM\david.zhao on 17-3-8.
 */

public class LyricLoadHelper {
    public interface LyricListener {

        /**
         *
         * @param lyricSentences
         * @param indexOfCurSentence
         */
        public abstract void onLyricLoaded(List<LyricSentence> lyricSentences,
                                           int indexOfCurSentence);

        /**
         *         *
         * @param indexOfCurSentence
         * */
        public abstract void onLyricSentenceChanged(int indexOfCurSentence);
    }

    private static final String TAG = LyricLoadHelper.class.getSimpleName();

    private ArrayList<LyricSentence> mLyricSentences = new ArrayList<LyricSentence>();

    private LyricListener mLyricListener = null;

    private boolean mHasLyric = false;

    private int mIndexOfCurrentSentence = -1;

    private final Pattern mBracketPattern = Pattern
            .compile("(?<=\\[).*?(?=\\])");
    private final Pattern mTimePattern = Pattern
            .compile("(?<=\\[)(\\d{2}:\\d{2}\\.?\\d{0,3})(?=\\])");

    private final String mEncoding = "utf-8";

    public List<LyricSentence> getLyricSentences() {
        return mLyricSentences;
    }

    public void setLyricListener(LyricListener listener) {
        this.mLyricListener = listener;
    }

    public void setIndexOfCurrentSentence(int index) {
        mIndexOfCurrentSentence = index;
    }

    public int getIndexOfCurrentSentence() {
        return mIndexOfCurrentSentence;
    }

    /**
     *
     * @param lyricPath
     * @return
     * */
    public boolean loadLyric(String lyricPath) {
        Log.e(TAG, "LoadLyric begin,path is:" + lyricPath);
        mHasLyric = false;
        mLyricSentences.clear();

        if (lyricPath != null) {
            File file = new File(lyricPath);
            if (file.exists()) {
                mHasLyric = true;
                try {
                    FileInputStream fr = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fr, mEncoding);
                    BufferedReader br = new BufferedReader(isr);
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        Log.e(TAG, "lyric line:" + line);
                        parseLine(line);
                    }
                    Collections.sort(mLyricSentences,
                            new Comparator<LyricSentence>() {
                                public int compare(LyricSentence object1,
                                                   LyricSentence object2) {
                                    if (object1.getStartTime() > object2
                                            .getStartTime()) {
                                        return 1;
                                    } else if (object1.getStartTime() < object2
                                            .getStartTime()) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                }
                            });

                    for (int i = 0; i < mLyricSentences.size() - 1; i++) {
                        mLyricSentences.get(i).setDuringTime(
                                mLyricSentences.get(i + 1).getStartTime());
                    }
                    mLyricSentences.get(mLyricSentences.size() - 1)
                            .setDuringTime(Integer.MAX_VALUE);
                    fr.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            } else {
                Log.e(TAG, " lyric not exist");
            }
        }
        if (mLyricListener != null) {
            mLyricListener.onLyricLoaded(mLyricSentences,
                    mIndexOfCurrentSentence);
        }
        if (mHasLyric) {
            Log.e(TAG, "Lyric file existed.Lyric has " + mLyricSentences.size()
                    + " Sentences");
        } else {
            Log.e(TAG, "Lyric file does not existed");
        }
        return mHasLyric;
    }

    /**
     *
     * @param millisecond
     */
    public void notifyTime(long millisecond) {
        if (mHasLyric && mLyricSentences != null && mLyricSentences.size() != 0) {
            int newLyricIndex = seekSentenceIndex(millisecond);
            if (newLyricIndex != -1 && newLyricIndex != mIndexOfCurrentSentence) {
                if (mLyricListener != null) {
                    mLyricListener.onLyricSentenceChanged(newLyricIndex);
                }
                mIndexOfCurrentSentence = newLyricIndex;
            }
        }
    }

    private int seekSentenceIndex(long millisecond) {
        int findStart = 0;
        if (mIndexOfCurrentSentence >= 0) {
            findStart = mIndexOfCurrentSentence;
        }

        try {
            long lyricTime = mLyricSentences.get(findStart).getStartTime();

            if (millisecond > lyricTime) {
                if (findStart == (mLyricSentences.size() - 1)) {
                    return findStart;
                }
                int new_index = findStart + 1;
                while (new_index < mLyricSentences.size()
                        && mLyricSentences.get(new_index).getStartTime() <= millisecond) {
                    ++new_index;
                }
                return new_index - 1;
            } else if (millisecond < lyricTime) {
                if (findStart == 0)
                    return 0;

                int new_index = findStart - 1;
                while (new_index > 0
                        && mLyricSentences.get(new_index).getStartTime() > millisecond) {
                    --new_index;
                }
                return new_index;
            } else {
                return findStart;
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            Log.i(TAG, "new lyric");
            return 0;
        }
    }

    /** parse lyric, one line maybe contain not one time */
    private void parseLine(String line) {
        if (line.equals("")) {
            return;
        }
        String content = null;
        int timeLength = 0;
        int index = 0;
        Matcher matcher = mTimePattern.matcher(line);
        int lastIndex = -1;
        int lastLength = -1;

        // such as“[01:02.3][01:11:22.33]xxxxxxxxxxxxxxxx”
        // such as“[01:02.3]xxxxxxxxxxxxxx[01:02:22.33]xxxxxxxxxxxxxx”
        List<String> times = new ArrayList<String>();

        while (matcher.find()) {
            // match 01:02.3，01:11:22.33

            String s = matcher.group();
            index = line.indexOf("[" + s + "]");
            if (lastIndex != -1 && index - lastIndex > lastLength + 2) {
                //  such as“[01:02.3]xxxxxxxxxxxxxx[01:02:22.33]xxxxxxxxxxxxxx”
                content = trimBracket(line.substring(
                        lastIndex + lastLength + 2, index));
                for (String string : times) {
                    long t = parseTime(string);
                    if (t != -1) {
                        Log.i(TAG, "line content match-->" + content);
                        mLyricSentences.add(new LyricSentence(t, content));
                    }
                }
                times.clear();
            }
            times.add(s);
            lastIndex = index;
            lastLength = s.length();

            Log.i(TAG, "time match--->" + s);
        }
        if (times.isEmpty()) {
            return;
        }

        timeLength = lastLength + 2 + lastIndex;
        if (timeLength > line.length()) {
            content = trimBracket(line.substring(line.length()));
        } else {
            content = trimBracket(line.substring(timeLength));
        }
        Log.i(TAG, "line content match-->" + content);
        for (String s : times) {
            long t = parseTime(s);
            if (t != -1) {
                mLyricSentences.add(new LyricSentence(t, content));
            }
        }
    }

    /** remove [XXX] */
    private String trimBracket(String content) {
        String s = null;
        String result = content;
        Matcher matcher = mBracketPattern.matcher(content);
        while (matcher.find()) {
            s = matcher.group();
            result = result.replace("[" + s + "]", "");
        }
        return result;
    }

    /** 00:01:23.45 */
    @SuppressLint("DefaultLocale")
    private long parseTime(String strTime) {
        String beforeDot = new String("00:00:00");
        String afterDot = new String("0");

        int dotIndex = strTime.indexOf(".");
        if (dotIndex < 0) {
            beforeDot = strTime;
        } else if (dotIndex == 0) {
            afterDot = strTime.substring(1);
        } else {
            beforeDot = strTime.substring(0, dotIndex);// 00:01:23
            afterDot = strTime.substring(dotIndex + 1); // 45
        }

        long intSeconds = 0;
        int counter = 0;
        while (beforeDot.length() > 0) {
            int colonPos = beforeDot.indexOf(":");
            try {
                if (colonPos > 0) {
                    intSeconds *= 60;
                    intSeconds += Integer.valueOf(beforeDot.substring(0,
                            colonPos));
                    beforeDot = beforeDot.substring(colonPos + 1);
                } else if (colonPos < 0) {
                    intSeconds *= 60;
                    intSeconds += Integer.valueOf(beforeDot);
                    beforeDot = "";
                } else {
                    return -1;
                }
            } catch (NumberFormatException e) {
                return -1;
            }
            ++counter;
            if (counter > 3) {
                return -1;
            }
        }
        // intSeconds=83

        String totalTime = String.format("%d.%s", intSeconds, afterDot);// totaoTimer
        // =
        // "83.45"
        Double doubleSeconds = Double.valueOf(totalTime);
        return (long) (doubleSeconds * 1000);
    }
}
