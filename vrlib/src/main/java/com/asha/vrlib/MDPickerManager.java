package com.asha.vrlib;

import android.view.MotionEvent;

import com.asha.vrlib.common.MDMainHandler;
import com.asha.vrlib.common.VRUtil;
import com.asha.vrlib.model.MDDirectorSnapshot;
import com.asha.vrlib.model.MDHitEvent;
import com.asha.vrlib.model.MDHitPoint;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDPluginAdapter;
import com.asha.vrlib.plugins.MDPluginManager;
import com.asha.vrlib.plugins.hotspot.IMDHotspot;
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

    private MDAbsPlugin mEyePicker = new MDPluginAdapter() {

        private long pickTs;

        // gl thread
        @Override
        public void beforeRenderer(int totalWidth, int totalHeight) {
            synchronized (mDirectorLock){
                mDirectorContext.snapshot(mProjectionModeManager.getDirectors());
            }

            if (isEyePickEnable()){
                long current = System.currentTimeMillis();
                if (current - pickTs > 100){
                    MDMainHandler.sharedHandler().post(mRayPickAsEyeRunnable);
                    pickTs = current;
                }
            }
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

        MDDirectorSnapshot snapshot = directorContext.getSnapshot(0);
        if (snapshot == null){
            return;
        }

        int itemWidth = (int) snapshot.getViewportWidth();

        int index = (int) (x / itemWidth);
        if (index >= size){
            return;
        }

        snapshot = directorContext.getSnapshot(index);
        if (snapshot == null){
            return;
        }

        MDRay ray = VRUtil.point2Ray(x - itemWidth * index, y, snapshot);

        pick(ray, HIT_FROM_TOUCH);
    }

    // main thread.
    private void rayPickAsEye(DirectorContext mDirectorContext) {
        MDDirectorSnapshot snapshot = mDirectorContext.getSnapshot(0);
        if (snapshot == null){
            return;
        }
        MDRay ray = VRUtil.point2Ray(snapshot.getViewportWidth() / 2, snapshot.getViewportHeight() / 2, snapshot);

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
                    mTouchPickPoster.fire(hitHotspot, ray, currentDistance);
                }
                break;

            case HIT_FROM_EYE:
                mEyePickPoster.fire(hitHotspot, ray, currentDistance);
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
            synchronized (mDirectorLock){
                rayPickAsEye(mDirectorContext);
            }
        }
    }

    private class EyePickPoster {

        private IMDHotspot hit;

        private long timestamp;

        void fire(IMDHotspot hit, MDRay ray, MDHitPoint hitPoint) {
            setHit(hit);

            MDHitEvent event = MDHitEvent.obtain();
            event.setHotspot(hit);
            event.setRay(ray);
            event.setTimestamp(timestamp);
            event.setHitPoint(hitPoint);

            if (this.hit != null){
                this.hit.onEyeHitIn(event);
            }

            if (mEyePickChangedListener != null){
                mEyePickChangedListener.onHotspotHit(event);
            }

            MDHitEvent.recycle(event);
        }

        void setHit(IMDHotspot hit){

            if (this.hit != hit){
                if (this.hit != null){
                    this.hit.onEyeHitOut(timestamp);
                }

                timestamp = System.currentTimeMillis();
            }

            this.hit = hit;
        }
    }

    private class TouchPickPoster {

        void fire(IMDHotspot hitHotspot, MDRay ray, MDHitPoint hitPoint) {

            if (mTouchPickListener != null){
                MDHitEvent event = MDHitEvent.obtain();
                event.setHotspot(hitHotspot);
                event.setRay(ray);
                event.setTimestamp(System.currentTimeMillis());
                event.setHitPoint(hitPoint);
                mTouchPickListener.onHotspotHit(event);
                MDHitEvent.recycle(event);
            }
        }
    }

    private static class DirectorContext {

        private int size;

        private List<MDDirectorSnapshot> list = new LinkedList<>();

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
                list.add(new MDDirectorSnapshot());
            }
        }

        public MDDirectorSnapshot getSnapshot(int i) {
            if (i < size){
                return list.get(0);
            }

            return null;
        }
    }
}
