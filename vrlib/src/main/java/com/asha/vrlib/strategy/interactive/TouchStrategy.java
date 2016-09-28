package com.asha.vrlib.strategy.interactive;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.asha.vrlib.MD360Director;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class TouchStrategy extends AbsInteractiveStrategy {

    private static final float sDensity =  Resources.getSystem().getDisplayMetrics().density;

    private static final float sDamping = 0.2f;

    private static final String TAG = "TouchStrategy";

    private UpdateDragRunnable runnable = new UpdateDragRunnable();

    public TouchStrategy(InteractiveModeManager.Params params) {
        super(params);
    }

    @Override
    public void onResume(Context context) {}

    @Override
    public void onPause(Context context) {}

    @Override
    public boolean handleDrag(final int distanceX, final int distanceY) {
        getParams().mGLHandler.post(new Runnable() {
            @Override
            public void run() {
                runnable.handleDrag(distanceX, distanceY);
                getParams().mGLHandler.post(runnable);
            }
        });
        return false;
    }

    private class UpdateDragRunnable implements Runnable {
        private int distanceX;
        private int distanceY;

        private void handleDrag(int distanceX, int distanceY){
            this.distanceX = distanceX;
            this.distanceY = distanceY;
        }

        @Override
        public void run() {
            for (MD360Director director : getDirectorList()){
                director.setDeltaX(director.getDeltaX() - distanceX / sDensity * sDamping);
                director.setDeltaY(director.getDeltaY() - distanceY / sDensity * sDamping);
            }
        }
    }

    @Override
    public void onOrientationChanged(Activity activity) {

    }

    @Override
    public void on(Activity activity) {
        for (MD360Director director : getDirectorList()){
            director.reset();
        }
    }

    @Override
    public void off(Activity activity) {}

    @Override
    public boolean isSupport(Activity activity) {
        return true;
    }
}
