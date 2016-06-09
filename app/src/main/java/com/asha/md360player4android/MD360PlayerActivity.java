package com.asha.md360player4android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
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
public abstract class MD360PlayerActivity extends Activity {

    public static void startVideo(Context context, Uri uri){
        start(context, uri, VideoPlayerActivity.class);
    }

    public static void startBitmap(Context context, Uri uri){
        start(context, uri, BitmapPlayerActivity.class);
    }

    private static void start(Context context, Uri uri, Class<? extends Activity> clz){
        Intent i = new Intent(context,clz);
        i.setData(uri);
        context.startActivity(i);
    }

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
        mVRLibrary = createVRLibrary();

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
                mVRLibrary.switchDisplayMode(MD360PlayerActivity.this);
                updateDisplayModeText(displayModeSwitcher);
            }
        });
        updateDisplayModeText(displayModeSwitcher);

        findViewById(R.id.button_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVRLibrary.resetPinch();
                // reset touch
                if (mVRLibrary.getInteractiveMode() == MDVRLibrary.INTERACTIVE_MODE_TOUCH){
                    mVRLibrary.resetTouch();
                }
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mVRLibrary.handleTouchEvent(event) || super.onTouchEvent(event);
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
            case MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH:
                text = "M & T";
                break;
        }
        if (!TextUtils.isEmpty(text)) button.setText(text);
    }

    abstract protected MDVRLibrary createVRLibrary();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVRLibrary.onDestroy();
    }

    protected Uri getUri() {
        Intent i = getIntent();
        if (i == null || i.getData() == null){
            return null;
        }
        return i.getData();
    }

    public void cancelBusy(){
        findViewById(R.id.progress).setVisibility(View.GONE);
    }
}