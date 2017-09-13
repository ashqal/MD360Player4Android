package com.asha.vrlib.common;

import android.graphics.PointF;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import com.asha.vrlib.model.MDDirectorSnapshot;
import com.asha.vrlib.model.MDHitPoint;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.model.MDVector3D;
import com.google.vrtoolkit.cardboard.sensors.internal.Vector3d;

/**
 * Created by hzqiujiadi on 16/3/13.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class VRUtil {

    private static final String TAG = "VRUtil";
    private static float[] sUIThreadTmp = new float[16];

    private static float[] sTruncatedVector = new float[4];
    private static boolean sIsTruncated = false;

    public static void sensorRotationVector2Matrix(SensorEvent event, int rotation, float[] output) {
        if (!sIsTruncated) {
            try {
                SensorManager.getRotationMatrixFromVector(sUIThreadTmp, event.values);
            } catch (Exception e) {
                // On some Samsung devices, SensorManager#getRotationMatrixFromVector throws an exception
                // if the rotation vector has more than 4 elements. Since only the four first elements are used,
                // we can truncate the vector without losing precision.
                Log.e(TAG, "maybe Samsung bug, will truncate vector");
                sIsTruncated = true;
            }
        }

        if (sIsTruncated){
            System.arraycopy(event.values, 0, sTruncatedVector, 0, 4);
            SensorManager.getRotationMatrixFromVector(sUIThreadTmp, sTruncatedVector);
        }

        float[] values = event.values;
        switch (rotation){
            case Surface.ROTATION_0:
                SensorManager.getRotationMatrixFromVector(output, values);
                break;
            case Surface.ROTATION_90:
                SensorManager.getRotationMatrixFromVector(sUIThreadTmp, values);
                SensorManager.remapCoordinateSystem(sUIThreadTmp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, output);
                break;
            case Surface.ROTATION_180:
                SensorManager.getRotationMatrixFromVector(sUIThreadTmp, values);
                SensorManager.remapCoordinateSystem(sUIThreadTmp, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, output);
                break;
            case Surface.ROTATION_270:
                SensorManager.getRotationMatrixFromVector(sUIThreadTmp, values);
                SensorManager.remapCoordinateSystem(sUIThreadTmp, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, output);
                break;
        }
        Matrix.rotateM(output, 0, 90.0F, 1.0F, 0.0F, 0.0F);
    }

    public static void notNull(Object object, String error){
        if (object == null) {
            throw new RuntimeException(error);
        }
    }

    public static void checkMainThread(String error){
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException(error);
        }
    }

    public static void checkGLThread(String error){
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new RuntimeException(error);
        }
    }

    public static void checkNaN(float[] mat) {
        if (Float.isNaN(mat[0]) || Float.isNaN(mat[1])) {
            throw new RuntimeException("mat not a number");
        }
    }

    public static void checkNaN(double[] mat) {
        if (Double.isNaN(mat[0]) || Double.isNaN(mat[1])) {
            throw new RuntimeException("mat not a number");
        }
    }

    public static void checkNaN(Vector3d v3d) {
        if (Double.isNaN(v3d.x) || Double.isNaN(v3d.y) || Double.isNaN(v3d.z)) {
            throw new RuntimeException("v3d not a number");
        }
    }

    public static void barrelDistortion(double paramA, double paramB, double paramC, PointF src){

        double paramD = 1.0 - paramA - paramB - paramC; // describes the linear scaling of the image

        float d = 1.0f;

        // center of dst image
        double centerX = 0f;
        double centerY = 0f;

        if (src.x == centerX && src.y == centerY){
            return;
        }

        // cartesian coordinates of the destination point (relative to the centre of the image)
        double deltaX = (src.x - centerX) / d;
        double deltaY = (src.y - centerY) / d;

        // distance or radius of dst image
        double dstR = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // distance or radius of src image (with formula)
        double srcR = (paramA * dstR * dstR * dstR + paramB * dstR * dstR + paramC * dstR + paramD) * dstR;

        // comparing old and new distance to get factor
        double factor = Math.abs(dstR / srcR);

        // coordinates in source image
        float xResult = (float) (centerX + (deltaX * factor * d));
        float yResult = (float) (centerY + (deltaY * factor * d));

        src.set(xResult,yResult);
    }

    public static MDVector3D vec3Sub(MDVector3D v1, MDVector3D v2){
        return new MDVector3D().setX(v1.getX() - v2.getX()).setY(v1.getY() - v2.getY()).setZ(v1.getZ() - v2.getZ());
    }

    public static MDVector3D vec3Cross(MDVector3D v1, MDVector3D v2){
        return new MDVector3D().setX(v1.getY() * v2.getZ() - v2.getY() * v1.getZ())
                .setY(v1.getZ() * v2.getX() - v2.getZ() * v1.getX())
                .setZ(v1.getX() * v2.getY() - v2.getX() * v1.getY());
    }

    public static float vec3Dot(MDVector3D v1, MDVector3D v2){
        return vec3Dot(v1.getX(), v1.getY(), v1.getZ(), v2.getX(), v2.getY(), v2.getZ());
    }

    public static float vec3Dot(float x1, float y1, float z1, float x2, float y2, float z2){
        return x1 * x2 + y1 * y2 + z1 * z2;
    }

    public static boolean invertM(float[] output, float[] input){
        if (input == output){
            return false;
        }

        return Matrix.invertM(output, 0, input, 0);
    }

    public static MDRay point2Ray(float x, float y, MDDirectorSnapshot info){
        checkMainThread("point2Ray must called in main Thread");
        float[] view = info.getViewMatrix();
        float[] temp = sUIThreadTmp;
        boolean success = invertM(temp, view);
        if (success){
            MDVector3D v = new MDVector3D();
            float[] projection = info.getProjectionMatrix();
            v.setX(- ( ( ( 2.0f * x ) / info.getViewportWidth() ) - 1 ) / projection[0]);
            v.setY(( ( ( 2.0f * y ) / info.getViewportHeight() ) - 1 ) / projection[5]);
            v.setZ(1.0f);

            MDVector3D vPickRayDir = new MDVector3D();
            MDVector3D vPickRayOrig = new MDVector3D();

            vPickRayDir.setX(v.getX() * temp[0] + v.getY() * temp[4] + v.getZ() * temp[8]);
            vPickRayDir.setY(v.getX() * temp[1] + v.getY() * temp[5] + v.getZ() * temp[9]);
            vPickRayDir.setZ(v.getX() * temp[2] + v.getY() * temp[6] + v.getZ() * temp[10]);
            vPickRayOrig.setX(temp[12]);
            vPickRayOrig.setY(temp[13]);
            vPickRayOrig.setZ(temp[14]);
            return new MDRay(vPickRayOrig,vPickRayDir);
        } else {
            return null;
        }
    }

    public static void intersectTriangle(MDRay ray, MDVector3D v0, MDVector3D v1, MDVector3D v2, MDHitPoint result){
        // Find vectors for two edges sharing vert0
        MDVector3D edge1 = vec3Sub(v1 , v0);
        MDVector3D edge2 = vec3Sub(v2 , v0);

        // Begin calculating determinant - also used to calculate U parameter
        MDVector3D pvec;
        pvec = vec3Cross( ray.getDir(), edge2 );

        // If determinant is near zero, ray lies in plane of triangle
        float det = vec3Dot( edge1, pvec );

        MDVector3D tvec;
        if( det > 0 ) {
            tvec = vec3Sub(ray.getOrig() , v0);
        } else {
            tvec = vec3Sub(v0 , ray.getOrig());
            det = -det;
        }

        if( det < 0.0001f ){
            result.asNotHit();
            return;
        }

        // Calculate U parameter and test bounds
        float u = vec3Dot(tvec, pvec);
        if( u < 0.0f || u > det ){
            result.asNotHit();
            return;
        }

        // Prepare to test V parameter
        MDVector3D qvec;
        qvec = vec3Cross(tvec, edge1);

        // Calculate V parameter and test bounds
        float v = vec3Dot(ray.getDir(), qvec);
        if( v < 0.0f || u + v > det ){
            result.asNotHit();
            return;
        }

        // Calculate t, scale parameters, ray intersects triangle
        float t = vec3Dot(edge2, qvec);
        float fInvDet = 1.0f / det;
        t *= fInvDet;
        u *= fInvDet;
        v *= fInvDet;

        if (t > 0){
            result.asNotHit();
            return;
        }

        result.set(t, u, v);
    }

    public static void getEulerAngles(float[] headView, float[] output) {
        float pitch = (float) Math.asin((double) headView[6]);
        float yaw;
        float roll;
        if (Math.abs(headView[6]) < 0.9999999999D) {
            yaw = (float) Math.atan2((double) (-headView[2]), (double) headView[10]);
            roll = (float) Math.atan2((double) (-headView[4]), (double) headView[5]);
        } else {
            yaw = 0.0F;
            roll = (float) Math.atan2((double) headView[1], (double) headView[0]);
        }
        output[0] = -pitch;
        output[1] = -yaw;
        output[2] = -roll;
        float pitchAngle = (float) Math.toDegrees(output[0]);
        float yawAngle = (float) Math.toDegrees(output[1]);
        float rollAngle = (float) Math.toDegrees(output[2]);

        Log.e(TAG, String.format("pitchAngle=%f, yawAngle=%f, rollAngle=%f", pitchAngle, yawAngle, rollAngle));
    }

    public static void printMatrix(float[] m){
        Log.d(TAG, "printMatrix");
        Log.d(TAG, String.format("%f, %f, %f, %f",m[0],m[1],m[2],m[3]));
        Log.d(TAG, String.format("%f, %f, %f, %f",m[4],m[5],m[6],m[7]));
        Log.d(TAG, String.format("%f, %f, %f, %f",m[8],m[9],m[10],m[11]));
        Log.d(TAG, String.format("%f, %f, %f, %f",m[12],m[13],m[14],m[15]));
    }

}
