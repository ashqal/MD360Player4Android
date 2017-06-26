package com.asha.vrlib.strategy.interactive;

import android.content.Context;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public interface IInteractiveMode {

    void onResume(Context context);

    void onPause(Context context);

    boolean handleDrag(int distanceX, int distanceY);

    void onOrientationChanged(Context context);
}
