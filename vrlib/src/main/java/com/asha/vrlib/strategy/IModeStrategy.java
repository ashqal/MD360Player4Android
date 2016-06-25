package com.asha.vrlib.strategy;

import android.app.Activity;
import android.content.Context;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public interface IModeStrategy {

    void on(Activity activity);

    void off(Activity activity);
    
    boolean isSupport(Activity activity);

    void onResume(Context context);

    void onPause(Context context);

}
