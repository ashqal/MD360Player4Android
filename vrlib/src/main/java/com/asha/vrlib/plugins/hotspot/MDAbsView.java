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
import com.asha.vrlib.model.MDViewBuilder;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;

import static com.asha.vrlib.common.VRUtil.checkMainThread;
import static com.asha.vrlib.common.VRUtil.notNull;

/**
 * Created by hzqiujiadi on 2017/4/12.
 * hzqiujiadi ashqalcn@gmail.com
 */

public abstract class MDAbsView extends MDAbsHotspot {

    private boolean mInvalidate;

    private MD360Texture mTexture;

    private View mAttachedView;

    private MDLayoutParams mLayoutParams;

    private Canvas mCanvas;

    private Bitmap mBitmap;

    private TouchStatus mTouchStatus;

    private enum TouchStatus{
        NOP, DOWN
    }

    public MDAbsView(MDViewBuilder builder) {
        super(builder.builderDelegate);
        this.mAttachedView = builder.attachedView;
        this.mLayoutParams = builder.layoutParams;
        this.mAttachedView.setLayoutParams(this.mLayoutParams);

        try {
            this.mBitmap = Bitmap.createBitmap(mLayoutParams.width, mLayoutParams.height, Bitmap.Config.ARGB_8888);
            this.mCanvas = new Canvas(mBitmap);
        } catch (Exception e){
            e.printStackTrace();
        }

        requestLayout();
    }

    public void invalidate(){
        if (mBitmap == null){
            return;
        }

        checkMainThread("invalidate must called in main thread.");
        notNull(mLayoutParams, "layout params can't be null");
        notNull(mAttachedView, "attached view can't be null");

        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mAttachedView.draw(mCanvas);
        mInvalidate = true;
    }

    public void requestLayout(){
        if (mBitmap == null){
            return;
        }

        checkMainThread("requestLayout must called in main thread.");
        notNull(mLayoutParams, "layout params can't be null");
        notNull(mAttachedView, "attached view can't be null");

        mAttachedView.measure(
                View.MeasureSpec.makeMeasureSpec(mLayoutParams.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(mLayoutParams.height, View.MeasureSpec.EXACTLY)
        );
        mAttachedView.layout(0, 0, mAttachedView.getMeasuredWidth(), mAttachedView.getMeasuredHeight());

        invalidate();
    }

    @Override
    protected void initInGL(Context context) {
        super.initInGL(context);

        mTexture = new MD360BitmapTexture(new MDVRLibrary.IBitmapProvider() {
            @Override
            public void onProvideBitmap(MD360BitmapTexture.Callback callback) {
                if (mBitmap != null){
                    callback.texture(mBitmap);
                }
            }
        });
        mTexture.create();
    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {
        if (mTexture == null || mBitmap == null){
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

    @Override
    public void onEyeHitIn(MDHitEvent hitEvent) {
        super.onEyeHitIn(hitEvent);

        MDHitPoint point = hitEvent.getHitPoint();
        if (point == null || mAttachedView == null) {
            return;
        }
        int action = mTouchStatus == TouchStatus.NOP ? MotionEvent.ACTION_HOVER_ENTER : MotionEvent.ACTION_HOVER_MOVE;
        float x = mAttachedView.getLeft() + mAttachedView.getWidth() * point.getU();
        float y = mAttachedView.getTop() + mAttachedView.getHeight() * point.getV();

        MotionEvent motionEvent = MotionEvent.obtain(hitEvent.getTimestamp(), System.currentTimeMillis(), action, x, y, 0);
        motionEvent.setSource(InputDevice.SOURCE_CLASS_POINTER);
        mAttachedView.dispatchGenericMotionEvent(motionEvent);
        motionEvent.recycle();
        mTouchStatus = TouchStatus.DOWN;

        invalidate();
    }

    @Override
    public void onEyeHitOut(long timestamp) {
        super.onEyeHitOut(timestamp);
        if (mTouchStatus == TouchStatus.DOWN){
            MotionEvent motionEvent = MotionEvent.obtain(timestamp, System.currentTimeMillis(), MotionEvent.ACTION_HOVER_EXIT, 0, 0, 0);
            motionEvent.setSource(InputDevice.SOURCE_CLASS_POINTER);
            mAttachedView.dispatchGenericMotionEvent(motionEvent);
            motionEvent.recycle();
        }
        mTouchStatus = TouchStatus.NOP;

        invalidate();
    }

    public <T extends View> T castAttachedView(Class<T> clazz){
        notNull(clazz, "param clz can't be null.");
        return clazz.cast(mAttachedView);
    }

    public View getAttachedView() {
        return mAttachedView;
    }
}
