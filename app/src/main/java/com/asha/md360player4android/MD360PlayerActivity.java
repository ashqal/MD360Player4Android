package com.asha.md360player4android;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.asha.vrlib.MDVRLibrary;

/**
 * using MD360Renderer
 *
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MD360PlayerActivity extends MediaPlayerActivity {

    private static final String URL = "URL";

    public static void start(Context context, String url){
        Intent i = new Intent(context,MD360PlayerActivity.class);
        i.putExtra(URL,url);
        context.startActivity(i);
    }

    private MDVRLibrary mVRLibrary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if (i == null || TextUtils.isEmpty(i.getStringExtra(URL))){
            this.finish();
            Toast.makeText(MD360PlayerActivity.this, "url is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = i.getStringExtra(URL);


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
        openRemoteFile(url);

        // media player play!
        play();

        // interactive mode switcher
        final Button interactiveModeSwitcher = (Button) findViewById(R.id.button_interactive_mode_switcher);
        interactiveModeSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVRLibrary.switchInteractiveMode(MD360PlayerActivity.this);
                updateInteractiveModeText(interactiveModeSwitcher);
            }
        });
        updateInteractiveModeText(interactiveModeSwitcher);

        // display mode switcher
        final Button displayModeSwitcher = (Button) findViewById(R.id.button_display_mode_switcher);
        displayModeSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVRLibrary.switchDisplayMode();
                updateDisplayModeText(displayModeSwitcher);
            }
        });
        updateDisplayModeText(displayModeSwitcher);

    }

    private void updateDisplayModeText(Button button) {
        String text = null;
        switch (mVRLibrary.getDisplayMode()){
            case MDVRLibrary.DISPLAY_MODE_NORMAL:
                text = "NORMAL";
                break;
            case MDVRLibrary.DISPLAY_MODE_GLASS:
                text = "GLASS";
                break;
        }
        if (!TextUtils.isEmpty(text)) button.setText(text);
    }

    private void updateInteractiveModeText(Button button){
        String text = null;
        switch (mVRLibrary.getInteractiveMode()){
            case MDVRLibrary.INTERACTIVE_MODE_MOTION:
                text = "MOTION";
                break;
            case MDVRLibrary.INTERACTIVE_MODE_TOUCH:
                text = "TOUCH";
                break;
        }
        if (!TextUtils.isEmpty(text)) button.setText(text);
    }

    private void initVRLibrary(){
        mVRLibrary = MDVRLibrary.builder()
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .callback(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        getPlayer().setSurface(surface);
                    }
                }).build();

        mVRLibrary.initWithGLSurfaceViewIds(this,R.id.surface_view1,R.id.surface_view2);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        super.onPrepared(mp);
        findViewById(R.id.progress).setVisibility(View.GONE);
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