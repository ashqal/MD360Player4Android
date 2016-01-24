package com.asha.md360player4android;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;


/**
 * Created by hzqiujiadi on 16/1/24.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MediaPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {
    private MediaPlayer mPlayer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediaplay);
        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);

        SurfaceView surface = (SurfaceView) findViewById(R.id.surface);
        SurfaceHolder surfaceHolder = surface.getHolder();
        surfaceHolder.addCallback(this);
    }

    public void onPlayButtonClicked(View view) {
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.demo);
            if ( afd == null ) return;
            mPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(), afd.getLength());
            afd.close();
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPlayer.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mPlayer != null) mPlayer.setDisplay(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( mPlayer != null ){
            if (mPlayer.isPlaying()) mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }
}
