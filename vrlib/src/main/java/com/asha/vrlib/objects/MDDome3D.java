package com.asha.vrlib.objects;


import android.content.Context;

import com.asha.vrlib.common.GLUtil;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDDome3D extends MDAbsObject3D {

    @Override
    protected void executeLoad(Context context) {
        generateSphere(this);
    }

    private static void generateSphere(MDAbsObject3D object3D) {
        GLUtil.generateSphere(18,150,180f,object3D);
    }
}
