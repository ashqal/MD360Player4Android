package com.asha.vrlib.strategy.interactive;

import android.content.res.Resources;

import com.asha.vrlib.MD360Director;

/**
 * Created by hzqiujiadi on 16/6/10.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MotionWithTouchStrategy extends MotionStrategy {

    private static final float sDensity =  Resources.getSystem().getDisplayMetrics().density;

    private static final float sDamping = 0.2f;

    private UpdateDragRunnable runnable = new UpdateDragRunnable();

    public MotionWithTouchStrategy(InteractiveModeManager.Params params) {
        super(params);
    }

    @Override
    public boolean handleDrag(int distanceX, int distanceY) {
        runnable.handleDrag(distanceX, distanceY);
        getParams().mGLHandler.post(runnable);
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
                // director.setDeltaY(director.getDeltaY() - distanceY / sDensity * sDamping);
            }
        }
    }
}
