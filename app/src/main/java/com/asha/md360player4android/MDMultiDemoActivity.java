package com.asha.md360player4android;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.asha.vrlib.MDVRLibrary;

/**
 * using MD360Renderer
 *
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDMultiDemoActivity extends MediaPlayerActivity {

    private MDVRLibrary mVRLibrary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // set content view
        setContentView(R.layout.activity_md_multi);

        // init VR Library
        initVRLibrary();

        // open local file for media player
        openRemoteFile();

        // media player play!
        play();

        // mode switcher
        final Button button = (Button) findViewById(R.id.mode_switcher);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVRLibrary.switchMode(MDMultiDemoActivity.this);
                updateButtonText(button);
            }
        });
        updateButtonText(button);
    }

    private void updateButtonText(Button button){
        String text = null;
        switch (mVRLibrary.getCurrentMode()){
            case MDVRLibrary.MODE_MOTION:
                text = "MOTION";
                break;
            case MDVRLibrary.MODE_TOUCH:
                text = "TOUCH";
                break;
        }
        if (!TextUtils.isEmpty(text)) button.setText(text);
    }

    private void initVRLibrary(){
        mVRLibrary = new MDVRLibrary(new MDVRLibrary.IOnSurfaceReadyCallback() {
            @Override
            public void onSurfaceReady(Surface surface) {
                getPlayer().setSurface(surface);
            }
        });

        mVRLibrary.initWithGLSurfaceViewIds(this,R.id.surface_view1,R.id.surface_view2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mVRLibrary.handleTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVRLibrary.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVRLibrary.onPause(this);
    }
}