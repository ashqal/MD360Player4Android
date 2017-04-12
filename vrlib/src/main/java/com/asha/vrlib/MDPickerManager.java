package com.asha.vrlib;

import android.content.Context;
import android.view.MotionEvent;

import com.asha.vrlib.common.MDGLHandler;
import com.asha.vrlib.common.MDMainHandler;
import com.asha.vrlib.common.VRUtil;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.plugins.IMDHotspot;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDPluginManager;
import com.asha.vrlib.strategy.display.DisplayModeManager;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;

import java.util.List;

import static com.asha.vrlib.common.VRUtil.sNotHit;


/**
 * Created by hzqiujiadi on 16/8/7.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDPickerManager {

    private static final String TAG = "MDPickerManager";

    private static final int HIT_FROM_EYE = 1;

    private static final int HIT_FROM_TOUCH = 2;

    private boolean mEyePickEnable;

    private DisplayModeManager mDisplayModeManager;

    private ProjectionModeManager mProjectionModeManager;

    private MDPluginManager mPluginManager;

    private MDGLHandler mGLHandler;

    private MDVRLibrary.IEyePickListener mEyePickChangedListener;

    private MDVRLibrary.ITouchPickListener mTouchPickListener;

    private EyePickPoster mEyePickPoster = new EyePickPoster();

    private TouchPickPoster mTouchPickPoster = new TouchPickPoster();

    private RayPickAsTouchGLTask mRayPickAsTouchRunnable = new RayPickAsTouchGLTask();

    private MDVRLibrary.IGestureListener mTouchPicker = new MDVRLibrary.IGestureListener() {
        @Override
        public void onClick(MotionEvent e) {
            mRayPickAsTouchRunnable.setEvent(e.getX(), e.getY());
            mGLHandler.post(mRayPickAsTouchRunnable);
        }
    };

    private MDAbsPlugin mEyePicker = new MDAbsPlugin() {
        @Override
        protected void initInGL(Context context) {

        }

        @Override
        public void beforeRenderer(int totalWidth, int totalHeight) {

        }

        // gl thread
        @Override
        public void renderer(int index, int width, int height, MD360Director director) {
            if (index == 0 && isEyePickEnable()){
                rayPickAsEye(width >> 1, height >> 1, director);
            }
        }

        @Override
        public void destroyInGL() {

        }

        @Override
        protected boolean removable() {
            return false;
        }
    };

    private MDPickerManager(Builder params) {
        this.mDisplayModeManager = params.displayModeManager;
        this.mProjectionModeManager = params.projectionModeManager;
        this.mPluginManager = params.pluginManager;
        this.mGLHandler = params.glHandler;
    }

    public boolean isEyePickEnable() {
        return mEyePickEnable;
    }

    public void setEyePickEnable(boolean eyePickEnable) {
        this.mEyePickEnable = eyePickEnable;
    }

    // gl thread.
    private void rayPickAsTouch(float  x, float y) {
        int size = mDisplayModeManager.getVisibleSize();
        if (size == 0){
            return;
        }

        int itemWidth = mProjectionModeManager.getDirectors().get(0).getViewportWidth();

        int index = (int) (x / itemWidth);
        if (index >= size){
            return;
        }
        MDRay ray = VRUtil.point2Ray(x - itemWidth * index, y, mProjectionModeManager.getDirectors().get(index));

        IMDHotspot hotspot = pick(ray, HIT_FROM_TOUCH);

        if (ray != null && mTouchPickListener != null){
            mTouchPickListener.onHotspotHit(hotspot, ray);
        }
    }

    private void rayPickAsEye(float x, float y, MD360Director director) {
        MDRay ray = VRUtil.point2Ray(x, y, director);
        pick(ray, HIT_FROM_EYE);
    }

    private IMDHotspot pick(MDRay ray, int hitType){
        if (ray == null) return null;
        return hitTest(ray, hitType);
    }

    private IMDHotspot hitTest(MDRay ray, int hitType) {
        List<MDAbsPlugin> plugins = mPluginManager.getPlugins();
        IMDHotspot hitHotspot = null;
        float currentDistance = sNotHit;

        for (MDAbsPlugin plugin : plugins) {
            if (plugin instanceof IMDHotspot) {
                IMDHotspot hotspot = (IMDHotspot) plugin;
                float tmpDistance = hotspot.hit(ray);
                if (tmpDistance != sNotHit && tmpDistance <= currentDistance){
                    hitHotspot = hotspot;
                    currentDistance = tmpDistance;
                }
            }
        }

        switch (hitType) {
            case HIT_FROM_TOUCH:
                // only post the hotspot which is hit.
                if (currentDistance != sNotHit){
                    mTouchPickPoster.setRay(ray);
                    mTouchPickPoster.setHit(hitHotspot);
                    MDMainHandler.sharedHandler().post(mTouchPickPoster);
                }
                break;
            case HIT_FROM_EYE:
                mEyePickPoster.setHit(hitHotspot);
                MDMainHandler.sharedHandler().postDelayed(mEyePickPoster, 100);
                break;
        }

        return hitHotspot;
    }

    public MDVRLibrary.IGestureListener getTouchPicker() {
        return mTouchPicker;
    }

    public MDAbsPlugin getEyePicker() {
        return mEyePicker;
    }

    public static Builder with() {
        return new Builder();
    }

    public void setEyePickChangedListener(MDVRLibrary.IEyePickListener eyePickChangedListener) {
        this.mEyePickChangedListener = eyePickChangedListener;
    }

    public void setTouchPickListener(MDVRLibrary.ITouchPickListener touchPickListener) {
        this.mTouchPickListener = touchPickListener;
    }

    private class EyePickPoster implements Runnable{

        private IMDHotspot hit;

        private long timestamp;

        @Override
        public void run() {
            MDMainHandler.sharedHandler().removeCallbacks(this);

            if (mEyePickChangedListener != null){
                mEyePickChangedListener.onHotspotHit(hit, timestamp);
            }
        }

        public void setHit(IMDHotspot hit) {
            if (this.hit != hit){
                timestamp = System.currentTimeMillis();

                if (this.hit != null){
                    this.hit.onEyeHitOut();
                }
            }

            this.hit = hit;

            if (this.hit != null){
                this.hit.onEyeHitIn(timestamp);
            }
        }
    }

    private static class TouchPickPoster implements Runnable{

        private IMDHotspot hit;

        private MDRay ray;

        @Override
        public void run() {
            if (hit != null){
                hit.onTouchHit(ray);
            }
        }

        public void setRay(MDRay ray) {
            this.ray = ray;
        }

        public void setHit(IMDHotspot hit) {
            this.hit = hit;
        }
    }

    public void resetEyePick(){
        if (mEyePickPoster != null){
            mEyePickPoster.setHit(null);
        }
    }

    public static class Builder{
        private DisplayModeManager displayModeManager;
        private ProjectionModeManager projectionModeManager;
        private MDPluginManager pluginManager;
        private MDGLHandler glHandler;

        private Builder() {
        }

        public MDPickerManager build(){
            return new MDPickerManager(this);
        }

        public Builder setPluginManager(MDPluginManager pluginManager) {
            this.pluginManager = pluginManager;
            return this;
        }

        public Builder setDisplayModeManager(DisplayModeManager displayModeManager) {
            this.displayModeManager = displayModeManager;
            return this;
        }

        public Builder setProjectionModeManager(ProjectionModeManager projectionModeManager) {
            this.projectionModeManager = projectionModeManager;
            return this;
        }

        public Builder setGLHandler(MDGLHandler glHandler) {
            this.glHandler = glHandler;
            return this;
        }
    }

    private class RayPickAsTouchGLTask implements Runnable {
        float x;
        float y;

        public void setEvent(float x, float y){
            this.x = x;
            this.y = y;
        }

        @Override
        public void run() {
            rayPickAsTouch(x, y);
        }
    }
}
