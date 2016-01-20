package com.wowwee.snappetssampleproject.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

public class SimpleAudioPlayer {

    private static SimpleAudioPlayer instance = null;

    private Hashtable<String, MediaPlayer> audioMap;

    private Context activityContext;

    private int musicId;

    private AudioManager audioManager = null;

    protected SimpleAudioPlayer() {
        super();
        audioMap = new Hashtable<String, MediaPlayer>();
    }

    public static SimpleAudioPlayer getInstance() {
        if (instance == null) {
            instance = new SimpleAudioPlayer();
        }
        return instance;
    }

    public void setActivityContext(Context context) {
        activityContext = context;
        audioManager = (AudioManager) activityContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public void clearCache() {
        Iterator<Entry<String, MediaPlayer>> it = audioMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, MediaPlayer> pairs = it.next();
            MediaPlayer mPlayer = pairs.getValue();
            mPlayer.stop();
            mPlayer.release();
//	        it.remove(); // avoids a ConcurrentModificationException
        }
        audioMap.clear();
    }

    public void removeFromCache(int resId) {
        MediaPlayer mPlayer = audioMap.get("" + resId);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            audioMap.remove("" + resId);
            mPlayer = null;
        }
    }

    public void playAudio(int resId) {
        playAudio(resId, true);
    }

    public void playAudio(final int resId, boolean cleanUp) {
        MediaPlayer mPlayer;
        if (cleanUp) {
            mPlayer = MediaPlayer.create(activityContext, resId);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    audioMap.remove("" + resId);
                }
            });
            audioMap.put("" + resId, mPlayer);
        } else {
            mPlayer = audioMap.get("" + resId);
            if (mPlayer == null) {
                mPlayer = MediaPlayer.create(activityContext, resId);
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                audioMap.put("" + resId, mPlayer);
            }
            mPlayer.seekTo(0);
        }
        int vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mPlayer.setVolume(vol, vol);
        mPlayer.start();
    }

    public void playMusic(int resId, boolean looping) {
        if (musicId != resId) {
            stopMusic(true);
            musicId = resId;
        }
        MediaPlayer mPlayer = audioMap.get("" + resId);
        if (mPlayer == null) {
            mPlayer = MediaPlayer.create(activityContext, resId);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setLooping(looping);
            audioMap.put("" + resId, mPlayer);
        }
        int vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mPlayer.setVolume(vol, vol);
        mPlayer.start();
    }

    public void stopMusic(boolean cleanup) {
        MediaPlayer mPlayer = audioMap.get("" + musicId);
        if (mPlayer != null) {
            mPlayer.pause();
            if (cleanup) {
                mPlayer.release();
                audioMap.remove("" + musicId);
            }
        }
    }

    public void stopAll() {
        Iterator<Entry<String, MediaPlayer>> it = audioMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, MediaPlayer> pairs = it.next();
            MediaPlayer mPlayer = pairs.getValue();
            mPlayer.stop();
//	        it.remove(); // avoids a ConcurrentModificationException
        }
    }

}
