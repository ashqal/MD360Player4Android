package com.asha.md360player4android;

import android.os.Bundle;
import android.view.Surface;
import android.view.View;

import com.asha.vrlib.MD360Renderer;
import com.asha.vrlib.MD360Surface;
import com.asha.vrlib.MDTextureView;

/**
 * using MDGLSurfaceView
 *
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDTextureViewDemoActivity extends MediaPlayerActivity {

    private MDTextureView mMDTextureView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_md_texture_surface);

        mMDTextureView = (MDTextureView) findViewById(R.id.md_texture_view);
        MD360Renderer renderer = MD360Renderer.with(this).defaultSurface(new MD360Surface.IOnSurfaceReadyListener() {
            @Override
            public void onSurfaceReady(Surface surface) {
                getPlayer().setSurface(surface);
            }
        }).build();
        mMDTextureView.init(renderer);

        openLocalFile();

        // play button
        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
    }

}