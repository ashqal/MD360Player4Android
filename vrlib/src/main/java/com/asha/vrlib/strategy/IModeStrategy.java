package com.asha.vrlib.strategy;

import android.content.Context;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public interface IModeStrategy {

    void turnOnInGL(Context context);

    void turnOffInGL(Context context);
    
    boolean isSupport(Context context);

    void onResume(Context context);

    void onPause(Context context);

}
