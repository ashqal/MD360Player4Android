package com.asha.md360player4android;

import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class VideoPlayerActivity extends MD360PlayerActivity {

    private static final String TAG = "VideoPlayerActivity";
    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                cancelBusy();
                if (getVRLibrary() != null){
                    getVRLibrary().notifyPlayerChanged();
                }
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                String error = String.format("Play Error what=%d extra=%d",what,extra);
                Toast.makeText(VideoPlayerActivity.this, error, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                getVRLibrary().onTextureResize(width, height);
            }
        });

        Uri uri = getUri();
        if (uri != null){
            mMediaPlayerWrapper.openRemoteFile(uri.toString());
            mMediaPlayerWrapper.prepare();
        }

        findViewById(R.id.control_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayerWrapper.pause();
                mMediaPlayerWrapper.destroy();
                mMediaPlayerWrapper.init();
                mMediaPlayerWrapper.openRemoteFile(DemoActivity.sPath + "video_31b451b7ca49710719b19d22e19d9e60.mp4");
                mMediaPlayerWrapper.prepare();
            }
        });

    }

    @Override
    protected MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mMediaPlayerWrapper.setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                        Toast.makeText(VideoPlayerActivity.this, tip, Toast.LENGTH_SHORT).show();
                    }
                })
                .pinchConfig(new MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.1f))
                .pinchEnabled(true)
                .directorFactory(new MD360DirectorFactory() {
                    @Override
                    public MD360Director createDirector(int index) {
                        return MD360Director.builder().setPitch(90).build();
                    }
                })
                .projectionFactory(new CustomProjectionFactory())
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build((GLSurfaceView) findViewById(R.id.gl_view));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayerWrapper.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaPlayerWrapper.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayerWrapper.resume();
    }
}
