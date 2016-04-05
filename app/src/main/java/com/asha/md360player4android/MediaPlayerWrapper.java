package com.asha.md360player4android;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MediaPlayerWrapper implements MediaPlayer.OnPreparedListener {
    protected MediaPlayer mPlayer;
    private MediaPlayer.OnPreparedListener mPreparedListener;

    public void init(){
        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);
    }

    /*
    protected void openLocalFile(){
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.demo);
        if (afd == null) return;
        try {
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */

    protected void openRemoteFile(String url){
        try {
            //"http://vod.moredoo.com/u/7575/m3u8/854x480/25883d97c738b1be48d1e106ede2789c/25883d97c738b1be48d1e106ede2789c.m3u8"
            mPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public MediaPlayer getPlayer() {
        return mPlayer;
    }

    public void play() {
        stop();
        if (mPlayer == null) return;
        mPlayer.prepareAsync();
    }

    private void stop(){
        if (mPlayer == null) return;
        if (mPlayer.isPlaying()){
            mPlayer.stop();
        }
    }

    public void onStop() {
        stop();
    }

    public void onDestroy() {
        if (mPlayer != null) mPlayer.release();
        mPlayer = null;
    }

    public void setPreparedListener(MediaPlayer.OnPreparedListener mPreparedListener) {
        this.mPreparedListener = mPreparedListener;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        if (mPreparedListener != null) mPreparedListener.onPrepared(mp);
    }
}
