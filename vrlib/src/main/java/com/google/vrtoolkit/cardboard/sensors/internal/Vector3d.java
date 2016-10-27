package com.google.vrtoolkit.cardboard.sensors.internal;

public class Vector3d {
    public double x;
    public double y;
    public double z;
    
    public Vector3d() {
        super();
    }
    
    public Vector3d(final double xx, final double yy, final double zz) {
        super();
        this.set(xx, yy, zz);
    }
    
    public void set(final double xx, final double yy, final double zz) {
        this.x = xx;
        this.y = yy;
        this.z = zz;
    }
    
    public void setComponent(final int i, final double val) {
        if (i == 0) {
            this.x = val;
        }
        else if (i == 1) {
            this.y = val;
        }
        else {
            this.z = val;
        }
    }
    
    public void setZero() {
        final double x = 0.0;
        this.z = x;
        this.y = x;
        this.x = x;
    }
    
    public void set(final Vector3d other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }
    
    public void scale(final double s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
    }
    
    public void normalize() {
        final double d = this.length();
        if (d != 0.0) {
            this.scale(1.0 / d);
        }
    }
    
    public static double dot(final Vector3d a, final Vector3d b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
    
    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }
    
    public boolean sameValues(final Vector3d other) {
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }
    
    public static void add(final Vector3d a, final Vector3d b, final Vector3d result) {
        result.set(a.x + b.x, a.y + b.y, a.z + b.z);
    }
    
    public static void sub(final Vector3d a, final Vector3d b, final Vector3d result) {
        result.set(a.x - b.x, a.y - b.y, a.z - b.z);
    }
    
    public static void cross(final Vector3d a, final Vector3d b, final Vector3d result) {
        result.set(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
    }
    
    public static void ortho(final Vector3d v, final Vector3d result) {
        int k = largestAbsComponent(v) - 1;
        if (k < 0) {
            k = 2;
        }
        result.setZero();
        result.setComponent(k, 1.0);
        cross(v, result, result);
        result.normalize();
    }
    
    public static int largestAbsComponent(final Vector3d v) {
        final double xAbs = Math.abs(v.x);
        final double yAbs = Math.abs(v.y);
        final double zAbs = Math.abs(v.z);
        if (xAbs > yAbs) {
            if (xAbs > zAbs) {
                return 0;
            }
            return 2;
        }
        else {
            if (yAbs > zAbs) {
                return 1;
            }
            return 2;
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder().append("{ ");
        builder.append(Double.toString(this.x));
        builder.append(", ");
        builder.append(Double.toString(this.y));
        builder.append(", ");
        builder.append(Double.toString(this.z));
        builder.append(" }");
        return builder.toString();
    }
}
