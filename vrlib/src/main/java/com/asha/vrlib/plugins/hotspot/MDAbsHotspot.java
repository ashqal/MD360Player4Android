package com.asha.vrlib.plugins.hotspot;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.GLES20;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.VRUtil;
import com.asha.vrlib.model.MDHitEvent;
import com.asha.vrlib.model.MDHitPoint;
import com.asha.vrlib.model.MDPluginBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.model.MDVector3D;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDObject3DHelper;
import com.asha.vrlib.objects.MDPlane;
import com.asha.vrlib.plugins.MDAbsPlugin;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 2017/4/12.
 * hzqiujiadi ashqalcn@gmail.com
 */

public abstract class MDAbsHotspot extends MDAbsPlugin implements IMDHotspot {

    private RectF size;

    private MDAbsObject3D object3D;

    MD360Program program;

    // hotspot
    private String title;

    private String tag;

    private MDVRLibrary.ITouchPickListener clickListener;

    private MDHitPoint hitPoint1 = new MDHitPoint(){
        @Override
        public float getV() {
            return 1 - super.getV();
        }
    };

    private MDHitPoint hitPoint2 = new MDHitPoint(){
        @Override
        public float getU() {
            return 1 - super.getU();
        }
    };

    private AtomicBoolean mPendingRotateToCamera = new AtomicBoolean(false);

    public MDAbsHotspot(MDPluginBuilder builder) {
        setTag(builder.tag);
        setTitle(builder.title);
        this.clickListener = builder.clickListener;
        this.size = new RectF(0, 0, builder.width, builder.height);
        setModelPosition(builder.position == null ? MDPosition.getOriginalPosition() : builder.position);
    }

    @Override
    protected void initInGL(Context context) {
        program = new MD360Program(MDVRLibrary.ContentType.BITMAP);
        program.build(context);

        // load obj
        object3D = new MDPlane(size);
        MDObject3DHelper.loadObj(context,object3D);
    }

    @Override
    public void destroyInGL() {

    }

    @Override
    public void beforeRenderer(int totalWidth, int totalHeight) {

    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {

        // Update Projection
        director.setViewport(width, height);

        // Set our per-vertex lighting program.
        program.use();
        glCheck("MDSimplePlugin mProgram use");

        object3D.uploadVerticesBufferIfNeed(program, index);

        object3D.uploadTexCoordinateBufferIfNeed(program, index);

        // Pass in the combined matrix.
        director.beforeShot();
        consumePendingRotateToCamera(director);
        director.shot(program, getModelPosition());

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        object3D.draw();
        GLES20.glDisable(GLES20.GL_BLEND);

    }

    @Override
    protected boolean removable() {
        return true;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public MDHitPoint hit(MDRay ray) {
        if (object3D == null || object3D.getVerticesBuffer(0) == null){
            return MDHitPoint.notHit();
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
        MDHitPoint hit1 = hitPoint1;
        MDHitPoint hit2 = hitPoint2;
        if (points.size() == 4){
            VRUtil.intersectTriangle(ray, points.get(0), points.get(1), points.get(2), hitPoint1);
            VRUtil.intersectTriangle(ray, points.get(3), points.get(2), points.get(1), hitPoint2);
        }

        return MDHitPoint.min(hit1, hit2);
    }

    @Override
    public void onEyeHitIn(MDHitEvent hitEvent) {

    }

    @Override
    public void onEyeHitOut(long timestamp) {

    }

    @Override
    public void onTouchHit(MDRay ray) {
        if (clickListener != null){
            clickListener.onHotspotHit(this, ray);
        }
    }

    @Override
    public void rotateToCamera(){
        mPendingRotateToCamera.set(true);
    }

    private void consumePendingRotateToCamera(MD360Director director) {
        if (mPendingRotateToCamera.get()){
            MDPosition position = getModelPosition();
            float[] rotation = director.getWorldRotationInvert();
            position.setRotationMatrix(rotation);
            mPendingRotateToCamera.set(false);
        }
    }
}
