package com.asha.md360player4android;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360Renderer;

import java.io.IOException;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MD360DemoActivity extends Activity implements MediaPlayer.OnPreparedListener{

    private static final String TAG = "MD360DemoActivity";

    /** Hold a reference to our GLSurfaceView */
	private GLSurfaceView mGLSurfaceView;
    private MediaPlayer mPlayer;
    protected MD360Director mDirector;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);
        mDirector = new MD360Director();
	}

    public void play(){
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
    protected void onDestroy() {
        super.onDestroy();
        if ( mPlayer != null ){
            if (mPlayer.isPlaying()) mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    protected void initOpenGL(int glSurfaceViewResId){
        mGLSurfaceView = (GLSurfaceView) findViewById(glSurfaceViewResId);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2){
            // Request an OpenGL ES 2.0 compatible context.
            mGLSurfaceView.setEGLContextClientVersion(2);

            // Create MD360Renderer now!
            GLSurfaceView.Renderer renderer = MD360Renderer.with(this)
                    .attachPlayer(mPlayer)
                    .attachDirector(mDirector)
                    .build();

            // Set the renderer to our demo renderer, defined below.
            mGLSurfaceView.setRenderer(renderer);
        } else {
            mGLSurfaceView.setVisibility(View.GONE);
            Toast.makeText(MD360DemoActivity.this, "OpenGLES2 not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
	protected void onResume(){
		// The activity must call the GL surface view's onResume() on activity onResume().
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause(){
		// The activity must call the GL surface view's onPause() on activity onPause().
		super.onPause();
		mGLSurfaceView.onPause();
	}

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public void onPlayButtonClicked(View view) {
        play();
    }
}