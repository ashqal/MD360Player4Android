package com.asha.vrlib;

import android.content.Context;
import android.view.MotionEvent;

import com.asha.vrlib.common.MDHandler;
import com.asha.vrlib.common.VRUtil;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.plugins.IMDHotspot;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDPluginManager;
import com.asha.vrlib.strategy.display.DisplayModeManager;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;

import java.util.List;


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

    private MDVRLibrary.IPickListener mEyePickChangedListener;

    private EyePickPoster mEyePickPoster = new EyePickPoster();

    private TouchPickPoster mTouchPickPoster = new TouchPickPoster();

    private MDVRLibrary.IGestureListener mGestureListener;

    private MDVRLibrary.IPrivateClickListener mTouchPicker = new MDVRLibrary.IPrivateClickListener() {
        @Override
        public void onClick(MotionEvent e) {
            rayPickAsTouch(e);
        }
    };

    private MDAbsPlugin mEyePicker = new MDAbsPlugin() {
        @Override
        protected void init(Context context) {

        }

        @Override
        public void renderer(int index, int width, int height, MD360Director director) {
            if (index == 0 && isEyePickEnable()){
                rayPickAsEye(width >> 1, height >> 1, director);
            }
        }

        @Override
        public void destroy() {

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
    }

    public boolean isEyePickEnable() {
        return mEyePickEnable;
    }

    public void setEyePickEnable(boolean eyePickEnable) {
        this.mEyePickEnable = eyePickEnable;
    }

    private void rayPickAsTouch(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
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

        if (ray != null && mGestureListener != null){
            mGestureListener.onClick(e, ray, hotspot);
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
        boolean hasHit = false;
        for (MDAbsPlugin plugin : plugins) {
            if (plugin instanceof IMDHotspot) {
                IMDHotspot hotspot = (IMDHotspot) plugin;
                hasHit = hotspot.hit(ray);
                if (hasHit){
                    hitHotspot = hotspot;
                    break;
                }
            }
        }

        switch (hitType) {
            case HIT_FROM_TOUCH:
                // only post the hotspot which is hit.
                if (hasHit){
                    mTouchPickPoster.setHit(hitHotspot);
                    MDHandler.sharedHandler().post(mTouchPickPoster);
                }
                break;
            case HIT_FROM_EYE:
                mEyePickPoster.setHit(hitHotspot);
                MDHandler.sharedHandler().postDelayed(mEyePickPoster, 100);
                break;
        }

        return hitHotspot;
    }

    public MDVRLibrary.IPrivateClickListener getTouchPicker() {
        return mTouchPicker;
    }

    public MDAbsPlugin getEyePicker() {
        return mEyePicker;
    }

    public static Builder with() {
        return new Builder();
    }

    public void setEyePickChangedListener(MDVRLibrary.IPickListener eyePickChangedListener) {
        this.mEyePickChangedListener = eyePickChangedListener;
    }

    public void setGestureListener(MDVRLibrary.IGestureListener mGestureListener) {
        this.mGestureListener = mGestureListener;
    }

    private class EyePickPoster implements Runnable{

        private IMDHotspot hit;

        private long timestamp;

        @Override
        public void run() {
            MDHandler.sharedHandler().removeCallbacks(this);
            if (hit != null){
                hit.onEyeHit(timestamp);
            }

            if (mEyePickChangedListener != null){
                mEyePickChangedListener.onHotspotHit(hit, timestamp);
            }
        }

        public void setHit(IMDHotspot hit) {
            if (this.hit != hit){
                timestamp = System.currentTimeMillis();
            }
            this.hit = hit;
        }
    }

    private static class TouchPickPoster implements Runnable{

        private IMDHotspot hit;

        @Override
        public void run() {
            if (hit != null){
                hit.onTouchHit();
            }
        }

        public void setHit(IMDHotspot hit) {
            this.hit = hit;
        }
    }

    public static class Builder{
        private DisplayModeManager displayModeManager;
        private ProjectionModeManager projectionModeManager;
        private MDPluginManager pluginManager;

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
    }
}
