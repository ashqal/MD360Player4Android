package com.asha.md360player4android;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MediaPlayerActivity extends Activity implements MediaPlayer.OnPreparedListener {

    private static final String TAG = "MediaPlayerActivity";
    protected MediaPlayer mPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);
    }

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

    protected void openRemoteFile(){
        try {
            mPlayer.setDataSource("http://vod.moredoo.com/u/7575/m3u8/854x480/25883d97c738b1be48d1e106ede2789c/25883d97c738b1be48d1e106ede2789c.m3u8");
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

    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) mPlayer.release();
        mPlayer = null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public void onPlayButtonClicked(View view) {
        play();
    }
}