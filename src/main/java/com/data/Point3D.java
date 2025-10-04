package com.data;

public class Point3D {
    public double x, y, z;
    public Point3D(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }
    @Override
    public String toString() {
        return String.format("(%.3f, %.3f, %.3f)", x, y, z);
    }

    @Override
    public boolean equals (Object other) {
        if(other instanceof Point3D) {
            Point3D pOther = (Point3D) other;
            return nearlyEqual(this.x, pOther.x, 1e-6) && nearlyEqual(this.y, pOther.y, 1e-6) && nearlyEqual(this.z, pOther.z, 1e-6);
        }
        else {
            return false;
        }
    }

    private boolean nearlyEqual(double a, double b, double margin) {
        return a >= b - margin && a <= b + margin;
    }
}

