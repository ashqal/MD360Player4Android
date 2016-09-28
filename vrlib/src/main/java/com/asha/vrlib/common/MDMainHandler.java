package com.asha.vrlib.common;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by hzqiujiadi on 16/8/4.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDMainHandler {

    private static Handler sMainHandler;

    public static void init(){
        if (sMainHandler == null){
            sMainHandler = new Handler(Looper.getMainLooper());
        }
    }

    public static Handler sharedHandler(){
        return sMainHandler;
    }
}
