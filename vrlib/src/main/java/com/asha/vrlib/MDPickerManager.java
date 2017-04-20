package com.asha.vrlib;

import android.content.Context;
import android.view.MotionEvent;

import com.asha.vrlib.common.MDMainHandler;
import com.asha.vrlib.common.VRUtil;
import com.asha.vrlib.model.MDDirectorBrief;
import com.asha.vrlib.model.MDHitEvent;
import com.asha.vrlib.model.MDHitPoint;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.plugins.hotspot.IMDHotspot;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDPluginManager;
import com.asha.vrlib.strategy.display.DisplayModeManager;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;

import java.util.LinkedList;
import java.util.List;

import static com.asha.vrlib.common.VRUtil.checkGLThread;
import static com.asha.vrlib.common.VRUtil.checkMainThread;


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

    private MDVRLibrary.IEyePickListener2 mEyePickChangedListener;

    private MDVRLibrary.ITouchPickListener2 mTouchPickListener;

    private EyePickPoster mEyePickPoster = new EyePickPoster();

    private TouchPickPoster mTouchPickPoster = new TouchPickPoster();

    private RayPickAsTouchMainTask mRayPickAsTouchRunnable = new RayPickAsTouchMainTask();

    private RayPickAsEyeMainTask mRayPickAsEyeRunnable = new RayPickAsEyeMainTask();

    private DirectorContext mDirectorContext = new DirectorContext();

    private final Object mDirectorLock = new Object();

    private MDVRLibrary.IGestureListener mTouchPicker = new MDVRLibrary.IGestureListener() {
        @Override
        public void onClick(MotionEvent e) {
            mRayPickAsTouchRunnable.setEvent(e.getX(), e.getY());
            mRayPickAsTouchRunnable.run();
        }
    };

    private MDAbsPlugin mEyePicker = new MDAbsPlugin() {
        @Override
        protected void initInGL(Context context) {

        }

        // gl thread
        @Override
        public void beforeRenderer(int totalWidth, int totalHeight) {
            synchronized (mDirectorLock){
                mDirectorContext.snapshot(mProjectionModeManager.getDirectors());
            }

            if (isEyePickEnable()){
                MDMainHandler.sharedHandler().postDelayed(mRayPickAsEyeRunnable, 100);
            }
        }

        @Override
        public void renderer(int index, int width, int height, MD360Director director) {
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
    }

    public boolean isEyePickEnable() {
        return mEyePickEnable;
    }

    public void setEyePickEnable(boolean eyePickEnable) {
        this.mEyePickEnable = eyePickEnable;
    }

    // main thread.
    private void rayPickAsTouch(float  x, float y, DirectorContext directorContext) {
        int size = mDisplayModeManager.getVisibleSize();
        if (size == 0){
            return;
        }

        MDDirectorBrief brief = directorContext.getBrief(0);
        if (brief == null){
            return;
        }

        int itemWidth = (int) brief.getViewportWidth();

        int index = (int) (x / itemWidth);
        if (index >= size){
            return;
        }

        brief = directorContext.getBrief(index);
        if (brief == null){
            return;
        }

        MDRay ray = VRUtil.point2Ray(x - itemWidth * index, y, brief);

        pick(ray, HIT_FROM_TOUCH);
    }

    // main thread.
    private void rayPickAsEye(DirectorContext mDirectorContext) {
        MDDirectorBrief brief = mDirectorContext.getBrief(0);
        if (brief == null){
            return;
        }

        MDRay ray = VRUtil.point2Ray(brief.getViewportWidth() / 2, brief.getViewportHeight() / 2, brief);
        pick(ray, HIT_FROM_EYE);
    }

    private IMDHotspot pick(MDRay ray, int hitType){
        if (ray == null) return null;
        return hitTest(ray, hitType);
    }

    private IMDHotspot hitTest(MDRay ray, int hitType) {
        // main thread
        checkMainThread("hitTest must in main thread");

        List<MDAbsPlugin> plugins = mPluginManager.getPlugins();
        IMDHotspot hitHotspot = null;
        MDHitPoint currentDistance = MDHitPoint.notHit();

        for (MDAbsPlugin plugin : plugins) {
            if (plugin instanceof IMDHotspot) {
                IMDHotspot hotspot = (IMDHotspot) plugin;
                MDHitPoint tmpDistance = hotspot.hit(ray);
                if (!tmpDistance.isNotHit() && tmpDistance.nearThen(currentDistance)){
                    hitHotspot = hotspot;
                    currentDistance = tmpDistance;
                }
            }
        }

        switch (hitType) {
            case HIT_FROM_TOUCH:
                // only post the hotspot which is hit.
                if (hitHotspot != null && !currentDistance.isNotHit()){
                    hitHotspot.onTouchHit(ray);
                    mTouchPickPoster.fire(hitHotspot, ray);
                }
                break;

            case HIT_FROM_EYE:
                mEyePickPoster.fire(hitHotspot, ray);
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

    public void setEyePickChangedListener(MDVRLibrary.IEyePickListener2 eyePickChangedListener) {
        this.mEyePickChangedListener = eyePickChangedListener;
    }

    public void setTouchPickListener(MDVRLibrary.ITouchPickListener2 touchPickListener) {
        this.mTouchPickListener = touchPickListener;
    }

    private class EyePickPoster {

        private IMDHotspot hit;

        private long timestamp;

        void fire(IMDHotspot hit, MDRay ray) {
            setHit(hit);
            if (mEyePickChangedListener != null){
                MDHitEvent event = MDHitEvent.obtain();
                event.setHotspot(hit);
                event.setRay(ray);
                event.setTimestamp(timestamp);
                mEyePickChangedListener.onHotspotHit(event);
                MDHitEvent.recycle(event);
            }
        }

        void setHit(IMDHotspot hit){
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

    private class TouchPickPoster {

        void fire(IMDHotspot hitHotspot, MDRay ray) {

            if (mTouchPickListener != null){
                MDHitEvent event = MDHitEvent.obtain();
                event.setHotspot(hitHotspot);
                event.setRay(ray);
                event.setTimestamp(System.currentTimeMillis());
                mTouchPickListener.onHotspotHit(event);
                MDHitEvent.recycle(event);
            }
        }
    }

    void resetEyePick(){
        if (mEyePickPoster != null){
            mEyePickPoster.setHit(null);
        }
    }

    public static class Builder {
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

    private class RayPickAsTouchMainTask implements Runnable {
        float x;
        float y;

        public void setEvent(float x, float y){
            this.x = x;
            this.y = y;
        }

        @Override
        public void run() {
            synchronized (mDirectorLock){
                rayPickAsTouch(x, y, mDirectorContext);
            }
        }
    }

    private class RayPickAsEyeMainTask implements Runnable {

        @Override
        public void run() {
            MDMainHandler.sharedHandler().removeCallbacks(mRayPickAsEyeRunnable);

            synchronized (mDirectorLock){
                rayPickAsEye(mDirectorContext);
            }

        }
    }

    private static class DirectorContext {

        private int size;

        private List<MDDirectorBrief> list = new LinkedList<>();

        public void snapshot(List<MD360Director> directorList){
            checkGLThread("snapshot must in gl thread!");

            ensureSize(directorList.size());
            for (int i = 0; i < directorList.size(); i++){
                list.get(i).copy(directorList.get(i));
            }
        }

        private void ensureSize(int size){
            this.size = size;

            while (list.size() < size){
                list.add(new MDDirectorBrief());
            }
        }

        public MDDirectorBrief getBrief(int i) {
            if (i < size){
                return list.get(0);
            }

            return null;
        }
    }
}
