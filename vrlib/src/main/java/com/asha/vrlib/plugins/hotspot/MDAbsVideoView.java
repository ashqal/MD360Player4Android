package com.asha.vrlib.plugins.hotspot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDHitEvent;
import com.asha.vrlib.model.MDHitPoint;
import com.asha.vrlib.model.MDHotspotBuilder;
import com.asha.vrlib.model.MDVideoHotspotBuilder;
import com.asha.vrlib.model.MDViewBuilder;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;
import com.asha.vrlib.texture.MD360VideoTexture;

import static com.asha.vrlib.common.VRUtil.checkMainThread;
import static com.asha.vrlib.common.VRUtil.notNull;

/**
 * Created by hzqiujiadi on 2017/4/12.
 * hzqiujiadi ashqalcn@gmail.com
 */

public abstract class MDAbsVideoView extends MDAbsHotspot {

    private boolean mInvalidate;

    private MDLayoutParams mLayoutParams;

    private TouchStatus mTouchStatus;

    private MD360VideoTexture mTexture;

    private MDVRLibrary.IOnSurfaceReadyCallback callback;

    private enum TouchStatus{
        NOP, DOWN
    }

    public MDAbsVideoView(MDVideoHotspotBuilder builder) {
        super(builder.builderDelegate);
        this.mLayoutParams = builder.layoutParams;
        this.callback = builder.callback;
    }

    @Override
    protected void initInGL(Context context) {
        super.initInGL(context);

        mTexture = new MD360VideoTexture(callback);
        mTexture.create();
    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {
        if (mTexture == null){
            return;
        }

        if (mInvalidate){
            mInvalidate = false;
            mTexture.notifyChanged();
        }

        mTexture.texture(program);

        if (mTexture.isReady()){
            super.renderer(index, width, height, director);
        }
    }
}
