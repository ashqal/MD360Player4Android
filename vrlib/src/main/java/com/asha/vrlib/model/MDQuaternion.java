package com.asha.vrlib.model;

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.badlogic.gdx.math.MathUtils;

/**
 * Generic Quaternion
 * Written for maximum portability between desktop and Android
 * Not in performance critical sections
 *
 * copy from package com.example.android.rs.vr.engine.Quaternion;
 */
public class MDQuaternion {
    private final float[] q = new float[4]; // w,x,y,z,

    private void set(float w, float x, float y, float z) {
        this.q[0] = w;
        this.q[1] = x;
        this.q[2] = y;
        this.q[3] = z;
    }

    private void set(float[] v1, float[] v2) {
        float[] vec1 = normal(v1);
        float[] vec2 = normal(v2);
        float[] axis = normal(cross(vec1, vec2));
        float angle = (float) Math.acos(dot(vec1, vec2));
        set(angle, axis);
    }

    private void set(float angle, float[] axis) {
        q[0] = (float) Math.cos(angle / 2);
        float sin = (float) Math.sin(angle / 2);
        q[1] = axis[0] * sin;
        q[2] = axis[1] * sin;
        q[3] = axis[2] * sin;
    }

    public void clone(MDQuaternion src) {
        System.arraycopy(src.q, 0, q, 0, q.length);
    }

    public void idt(){
        set(1, 0, 0, 0);
    }

    private static float[] cross(float[] a, float[] b) {
        float out0 = a[1] * b[2] - b[1] * a[2];
        float out1 = a[2] * b[0] - b[2] * a[0];
        float out2 = a[0] * b[1] - b[0] * a[1];
        return new float[]{out0, out1, out2};
    }

    private static float dot(float[] a, float[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static float[] normal(float[] a) {
        float norm = (float) Math.sqrt(dot(a, a));
        return new float[]{a[0] / norm, a[1] / norm, a[2] / norm};
    }

    public static float calcAngle(float[] v1, float[] v2) {
        float[] vec1 = normal(v1);
        float[] vec2 = normal(v2);
        return (float) Math.acos(Math.min(dot(vec1, vec2), 1));
    }

    public static float[] calcAxis(float[] v1, float[] v2) {
        float[] vec1 = normal(v1);
        float[] vec2 = normal(v2);
        return normal(cross(vec1, vec2));
    }

    public MDQuaternion(float x0, float x1, float x2, float x3) {
        q[0] = x0;
        q[1] = x1;
        q[2] = x2;
        q[3] = x3;
    }

    public MDQuaternion() {
        idt();
    }

    public MDQuaternion conjugate() {
        return new MDQuaternion(q[0], -q[1], -q[2], -q[3]);
    }

    public MDQuaternion plus(MDQuaternion b) {
        MDQuaternion a = this;
        return new MDQuaternion(a.q[0] + b.q[0], a.q[1] + b.q[1], a.q[2] + b.q[2], a.q[3] + b.q[3]);
    }

    public MDQuaternion times(MDQuaternion b) {
        MDQuaternion a = this;
        float y0 = a.q[0] * b.q[0] - a.q[1] * b.q[1] - a.q[2] * b.q[2] - a.q[3] * b.q[3];
        float y1 = a.q[0] * b.q[1] + a.q[1] * b.q[0] + a.q[2] * b.q[3] - a.q[3] * b.q[2];
        float y2 = a.q[0] * b.q[2] - a.q[1] * b.q[3] + a.q[2] * b.q[0] + a.q[3] * b.q[1];
        float y3 = a.q[0] * b.q[3] + a.q[1] * b.q[2] - a.q[2] * b.q[1] + a.q[3] * b.q[0];
        return new MDQuaternion(y0, y1, y2, y3);
    }

    public MDQuaternion inverse() {
        float d = q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3];
        return new MDQuaternion(q[0] / d, -q[1] / d, -q[2] / d, -q[3] / d);
    }

    public MDQuaternion divides(MDQuaternion b) {
        MDQuaternion a = this;
        return a.inverse().times(b);
    }

    public float[] rotateVec(float[] v) {

        float v0 = v[0];
        float v1 = v[1];
        float v2 = v[2];

        float s = q[1] * v0 + q[2] * v1 + q[3] * v2;

        float n0 = 2 * (q[0] * (v0 * q[0] - (q[2] * v2 - q[3] * v1)) + s * q[1]) - v0;
        float n1 = 2 * (q[0] * (v1 * q[0] - (q[3] * v0 - q[1] * v2)) + s * q[2]) - v1;
        float n2 = 2 * (q[0] * (v2 * q[0] - (q[1] * v1 - q[2] * v0)) + s * q[3]) - v2;

        return new float[]{n0, n1, n2};

    }

    public void toMatrix(float[] m) {
        float xx = q[1] * q[1];
        float xy = q[1] * q[2];
        float xz = q[1] * q[3];
        float xw = q[1] * q[0];

        float yy = q[2] * q[2];
        float yz = q[2] * q[3];
        float yw = q[2] * q[0];

        float zz = q[3] * q[3];
        float zw = q[3] * q[0];
        m[0] = 1 - 2 * (yy + zz);
        m[1] = 2 * (xy - zw);
        m[2] = 2 * (xz + yw);

        m[4] = 2 * (xy + zw);
        m[5] = 1 - 2 * (xx + zz);
        m[6] = 2 * (yz - xw);

        m[8] = 2 * (xz - yw);
        m[9] = 2 * (yz + xw);
        m[10] = 1 - 2 * (xx + yy);

        m[3] = m[7] = m[11] = m[12] = m[13] = m[14] = 0;
        m[15] = 1;
    }

    public void fromMatrix(float[] matrix) {
        setFromAxes(false, matrix[0], matrix[1], matrix[2],
                matrix[4], matrix[5], matrix[6],
                matrix[8], matrix[9], matrix[10]);
    }

    /** Sets the quaternion components from the given axis and angle around that axis.
     * @param x X direction of the axis
     * @param y Y direction of the axis
     * @param z Z direction of the axis
     * @param degrees The angle in degrees
     * @return This quaternion for chaining. */
    public void setFromAxis (final float x, final float y, final float z, final float degrees) {
        setFromAxisRad(x, y, z, degrees * MathUtils.degreesToRadians);
    }

    /** Sets the quaternion components from the given axis and angle around that axis.
     * @param x X direction of the axis
     * @param y Y direction of the axis
     * @param z Z direction of the axis
     * @param radians The angle in radians
     * @return This quaternion for chaining. */
    public void setFromAxisRad (final float x, final float y, final float z, final float radians) {
        float d = MDVector3D.len(x, y, z);
        if (d == 0f){
            idt();
            return;
        }

        d = 1f / d;
        float l_ang = radians < 0 ? MathUtils.PI2 - (-radians % MathUtils.PI2) : radians % MathUtils.PI2;
        float l_sin = (float)Math.sin(l_ang / 2);
        float l_cos = (float)Math.cos(l_ang / 2);
        this.set(l_cos, d * x * l_sin, d * y * l_sin, d * z * l_sin);
        this.nor();
    }

    /** <p>
     * Sets the Quaternion from the given x-, y- and z-axis.
     * </p>
     *
     * <p>
     * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics Gem code at
     * ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z.
     * </p>
     *
     * @param normalizeAxes whether to normalize the axes (necessary when they contain scaling)
     * @param xx x-axis x-coordinate
     * @param xy x-axis y-coordinate
     * @param xz x-axis z-coordinate
     * @param yx y-axis x-coordinate
     * @param yy y-axis y-coordinate
     * @param yz y-axis z-coordinate
     * @param zx z-axis x-coordinate
     * @param zy z-axis y-coordinate
     * @param zz z-axis z-coordinate */
    private void setFromAxes (boolean normalizeAxes, float xx, float xy, float xz, float yx, float yy, float yz, float zx,
                                   float zy, float zz) {
        float w,x,y,z;
        if (normalizeAxes) {
            final float lx = 1f / MDVector3D.len(xx, xy, xz);
            final float ly = 1f / MDVector3D.len(yx, yy, yz);
            final float lz = 1f / MDVector3D.len(zx, zy, zz);
            xx *= lx;
            xy *= lx;
            xz *= lx;
            yx *= ly;
            yy *= ly;
            yz *= ly;
            zx *= lz;
            zy *= lz;
            zz *= lz;
        }
        // the trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html
        final float t = xx + yy + zz;

        // we protect the division by s by ensuring that s>=1
        if (t >= 0) { // |w| >= .5
            float s = (float)Math.sqrt(t + 1); // |s|>=1 ...
            w = 0.5f * s;
            s = 0.5f / s; // so this division isn't bad
            x = (zy - yz) * s;
            y = (xz - zx) * s;
            z = (yx - xy) * s;
        } else if ((xx > yy) && (xx > zz)) {
            float s = (float)Math.sqrt(1.0 + xx - yy - zz); // |s|>=1
            x = s * 0.5f; // |x| >= .5
            s = 0.5f / s;
            y = (yx + xy) * s;
            z = (xz + zx) * s;
            w = (zy - yz) * s;
        } else if (yy > zz) {
            float s = (float)Math.sqrt(1.0 + yy - xx - zz); // |s|>=1
            y = s * 0.5f; // |y| >= .5
            s = 0.5f / s;
            x = (yx + xy) * s;
            z = (zy + yz) * s;
            w = (xz - zx) * s;
        } else {
            float s = (float)Math.sqrt(1.0 + zz - xx - yy); // |s|>=1
            z = s * 0.5f; // |z| >= .5
            s = 0.5f / s;
            x = (xz + zx) * s;
            y = (zy + yz) * s;
            w = (yx - xy) * s;
        }

        set(w, x, y, z);
    }

    public void setEulerAngles (float pitch, float yaw, float roll) {
        setEulerAnglesRad(pitch * MathUtils.degreesToRadians, yaw * MathUtils.degreesToRadians, roll * MathUtils.degreesToRadians);
    }

    /** Sets the quaternion to the given euler angles in radians.
     * @param pitch the rotation around the x axis in radians
     * @param yaw the rotation around the y axis in radians
     * @param roll the rotation around the z axis in radians
     * @return this quaternion */
    public void setEulerAnglesRad (float pitch, float yaw, float roll) {
        final float hr = roll * 0.5f;
        final float shr = (float)Math.sin(hr);
        final float chr = (float)Math.cos(hr);
        final float hp = pitch * 0.5f;
        final float shp = (float)Math.sin(hp);
        final float chp = (float)Math.cos(hp);
        final float hy = yaw * 0.5f;
        final float shy = (float)Math.sin(hy);
        final float chy = (float)Math.cos(hy);
        final float chy_shp = chy * shp;
        final float shy_chp = shy * chp;
        final float chy_chp = chy * chp;
        final float shy_shp = shy * shp;

        q[1] = (chy_shp * chr) + (shy_chp * shr); // cos(yaw/2) * sin(pitch/2) * cos(roll/2) + sin(yaw/2) * cos(pitch/2) * sin(roll/2)
        q[2] = (shy_chp * chr) - (chy_shp * shr); // sin(yaw/2) * cos(pitch/2) * cos(roll/2) - cos(yaw/2) * sin(pitch/2) * sin(roll/2)
        q[3] = (chy_chp * shr) - (shy_shp * chr); // cos(yaw/2) * cos(pitch/2) * sin(roll/2) - sin(yaw/2) * sin(pitch/2) * cos(roll/2)

        // w
        q[0] = (chy_chp * chr) + (shy_shp * shr); // cos(yaw/2) * cos(pitch/2) * cos(roll/2) + sin(yaw/2) * sin(pitch/2) * sin(roll/2)
    }

    /** Get the pole of the gimbal lock, if any.
     * @return positive (+1) for north pole, negative (-1) for south pole, zero (0) when no gimbal lock */
    public int getGimbalPole () {
        float w = q[0];
        float x = q[1];
        float y = q[2];
        float z = q[3];

        final float t = y * x + z * w;
        return t > 0.499f ? 1 : (t < -0.499f ? -1 : 0);
    }

    /** Get the roll euler angle in radians, which is the rotation around the z axis. Requires that this quaternion is normalized.
     * @return the rotation around the z axis in radians (between -PI and +PI) */
    public float getRollRad () {
        float w = q[0];
        float x = q[1];
        float y = q[2];
        float z = q[3];

        final int pole = getGimbalPole();
        return pole == 0 ? MathUtils.atan2(2f * (w * z + y * x), 1f - 2f * (x * x + z * z)) : (float)pole * 2f
                * MathUtils.atan2(y, w);
    }

    /** Get the roll euler angle in degrees, which is the rotation around the z axis. Requires that this quaternion is normalized.
     * @return the rotation around the z axis in degrees (between -180 and +180) */
    public float getRoll () {
        return getRollRad() * MathUtils.radiansToDegrees;
    }

    /** Get the pitch euler angle in radians, which is the rotation around the x axis. Requires that this quaternion is normalized.
     * @return the rotation around the x axis in radians (between -(PI/2) and +(PI/2)) */
    public float getPitchRad () {
        float w = q[0];
        float x = q[1];
        float y = q[2];
        float z = q[3];

        final int pole = getGimbalPole();
        return pole == 0 ? (float)Math.asin(MathUtils.clamp(2f * (w * x - z * y), -1f, 1f)) : (float)pole * MathUtils.PI * 0.5f;
    }

    /** Get the pitch euler angle in degrees, which is the rotation around the x axis. Requires that this quaternion is normalized.
     * @return the rotation around the x axis in degrees (between -90 and +90) */
    public float getPitch () {
        return getPitchRad() * MathUtils.radiansToDegrees;
    }

    /** Get the yaw euler angle in radians, which is the rotation around the y axis. Requires that this quaternion is normalized.
     * @return the rotation around the y axis in radians (between -PI and +PI) */
    public float getYawRad () {
        float w = q[0];
        float x = q[1];
        float y = q[2];
        float z = q[3];

        return getGimbalPole() == 0 ? MathUtils.atan2(2f * (y * w + x * z), 1f - 2f * (y * y + x * x)) : 0f;
    }

    /** Get the yaw euler angle in degrees, which is the rotation around the y axis. Requires that this quaternion is normalized.
     * @return the rotation around the y axis in degrees (between -180 and +180) */
    public float getYaw () {
        return getYawRad() * MathUtils.radiansToDegrees;
    }

    public void nor () {
        float w = q[0];
        float x = q[1];
        float y = q[2];
        float z = q[3];

        float len = x * x + y * y + z * z + w * w;
        if (len != 0.f && !MathUtils.isEqual(len, 1f)) {
            len = (float)Math.sqrt(len);
            w /= len;
            x /= len;
            y /= len;
            z /= len;
        }
        set(w, x, y, z);
    }


    @Override
    public String toString() {
        return String.format("MDQuaternion w=%f x=%f, y=%f, z=%f", q[0], q[1], q[2], q[3]);
    }
}
