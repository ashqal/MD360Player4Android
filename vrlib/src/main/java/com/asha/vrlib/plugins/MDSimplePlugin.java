package com.asha.vrlib.plugins;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.GLES20;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.VRUtil;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.model.MDVector3D;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDObject3DHelper;
import com.asha.vrlib.objects.MDPlane;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 16/8/2.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDSimplePlugin extends MDAbsPlugin implements IMDHotspot{

    private static final String TAG = "MDSimplePlugin";

    MDAbsObject3D object3D;

    MD360Program program;

    MD360Texture texture;

    RectF size;

    public MDSimplePlugin(MDVRLibrary.IBitmapProvider provider) {
        this(2.0f, 2.0f, provider);
    }

    public MDSimplePlugin(float width, float height, MDVRLibrary.IBitmapProvider provider) {
        texture = new MD360BitmapTexture(provider);
        size = new RectF(0,0,width,height);
    }

    @Override
    public void init(Context context) {

        program = new MD360Program(MDVRLibrary.ContentType.BITMAP);
        program.build(context);

        texture.create();

        object3D = new MDPlane(size);
        MDObject3DHelper.loadObj(context,object3D);

    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {

        texture.texture(program);

        if (texture.isReady()){
            // Update Projection
            director.updateViewport(width, height);

            // Set our per-vertex lighting program.
            program.use();
            glCheck("MDSimplePlugin mProgram use");

            object3D.uploadVerticesBufferIfNeed(program, index);

            object3D.uploadTexCoordinateBufferIfNeed(program, index);

            // Pass in the combined matrix.
            director.shot(program, getModelPosition());

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


            object3D.draw();
            GLES20.glDisable(GLES20.GL_BLEND);
        }

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean hit(MDRay ray) {
        if (object3D == null || object3D.getVerticesBuffer(0) == null){
            return false;
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

        boolean hit = false;
        if (points.size() == 4){
            hit = VRUtil.intersectTriangle(ray, points.get(0), points.get(1), points.get(2));
            hit |= VRUtil.intersectTriangle(ray,points.get(1),points.get(2),points.get(3));
        }

        // Log.d(TAG,"Ray:" + ray);
        // Log.e(TAG,"MDSimplePlugin hit:" + hit);

        return hit;
    }

    @Override
    public void onHit() {
    }
}
