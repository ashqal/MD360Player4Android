package com.asha.vrlib.objects;

import android.content.Context;

/**
 * Created by hzqiujiadi on 16/4/24.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDObject3DHelper {

    public interface LoadComplete{
        void onComplete(MDAbsObject3D object3D);
    }

    public static void loadObj(final Context context, final MDAbsObject3D object3D){
        loadObj(context, object3D, null);
    }

    public static void loadObj(final Context context, final MDAbsObject3D object3D, final LoadComplete loadComplete){
        new Thread(new Runnable() {
            @Override
            public void run() {
                object3D.executeLoad(context);
                if (loadComplete != null)
                    loadComplete.onComplete(object3D);
            }
        }).start();
    }
}
