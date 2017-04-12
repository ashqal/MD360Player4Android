package com.asha.vrlib.plugins.hotspot;

import android.content.Context;
import android.graphics.RectF;

import com.asha.vrlib.common.VRUtil;
import com.asha.vrlib.model.MDHotspotBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.model.MDVector3D;
import com.asha.vrlib.objects.MDObject3DHelper;
import com.asha.vrlib.objects.MDPlane;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import static com.asha.vrlib.common.VRUtil.sNotHit;

/**
 * Created by hzqiujiadi on 2017/4/12.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDSimpleHotspot extends MDAbsHotspot {

    private static final String TAG = "MDSimplePlugin";

    // plugin
    private RectF size;

    public MDSimpleHotspot(MDHotspotBuilder builder) {
        super(builder);
        size = new RectF(0, 0, builder.width, builder.height);
    }

    @Override
    public void initInGL(Context context) {
        super.initInGL(context);

        object3D = new MDPlane(size);
        MDObject3DHelper.loadObj(context,object3D);
    }

    @Override
    public void destroyInGL() {
        super.destroyInGL();
    }

    @Override
    public float hit(MDRay ray) {
        if (object3D == null || object3D.getVerticesBuffer(0) == null){
            return sNotHit;
        }

        MDPosition position = getModelPosition();
        float[] model = position.getMatrix();

        List<MDVector3D> points = new LinkedList<>();

        FloatBuffer buffer = object3D.getVerticesBuffer(0);
        int numPoints = buffer.capacity() / 3;

        for (int i = 0; i < numPoints; i++){
            MDVector3D v = new MDVector3D();
            v.setX(buffer.get(i * 3)).setY(buffer.get(i * 3 + 1)).setZ(buffer.get(i * 3 + 2));
            v.multiplyMV(model);
            points.add(v);
        }
        float hit1 = sNotHit;
        float hit2 = sNotHit;
        if (points.size() == 4){
            hit1 = VRUtil.intersectTriangle(ray, points.get(0), points.get(1), points.get(2));
            hit2 = VRUtil.intersectTriangle(ray,points.get(1), points.get(2), points.get(3));
        }

        return Math.min(hit1,hit2);
    }

}
