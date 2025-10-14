package com.data;

public class Util {
    public static int randInt(int a, int b) {
        return a + (int) Math.floor(Math.random() * (b - a + 1));
    }

    public static double distance(Point a, Point b) {
        return Math.pow(Math.pow(b.getX() - a.getX(),2)+Math.pow(b.getY() - a.getY(),2),0.5);
    }

    public static boolean nearlyEqual(double a, double b, double margin) {
        return a >= b - margin && a <= b + margin;
    }

    public static Point3D cross(Point3D u, Point3D v) {
        return new Point3D(
                u.y * v.z - u.z * v.y,
                u.z * v.x - u.x * v.z,
                u.x * v.y - u.y * v.x);
    }

    public static double length(Point3D p) {
        return Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
    }

    public static Point3D subtract(Point3D p1, Point3D p2) {
        return new Point3D(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
    }

    public static Point3D add(Point3D p1, Point3D p2) {
        return new Point3D(p1.x + p2.x, p1.y + p2.y, p1.z + p2.z);
    }

    public static double dot(Point3D a, Point3D b) {
        return a.x*b.x + a.y*b.y + a.z*b.z;
    }

    public static Point3D scale(Point3D p, double s) {
        return new Point3D(p.x*s, p.y*s, p.z*s);
    }

    public static Point3D negative(Point3D p) {
        return new Point3D(-p.x, -p.y, -p.z);
    }

    public static Point3D normalize(Point3D p) {
        double len = length((p));
        return new Point3D(p.x / len, p.y / len, p.z / len);
    }

    public static Point3D rotateZ(Point3D p, double angle) { // roll
        double cos = Math.cos(angle), sin = Math.sin(angle);
        double x = cos * p.x - sin * p.y;
        double y = sin * p.x + cos * p.y;
        return new Point3D(x, y, p.z);
    }

    public static Point3D rotateX(Point3D p, double angle) { // pitch
        double cos = Math.cos(angle), sin = Math.sin(angle);
        double y = cos * p.y - sin * p.z;
        double z = sin * p.y + cos * p.z;
        return new Point3D(p.x, y, z);
    }

    public static Point3D rotateY(Point3D p, double angle) { // yaw
        double cos = Math.cos(angle), sin = Math.sin(angle);
        double x = cos * p.x + sin * p.z;
        double z = -sin * p.x + cos * p.z;
        return new Point3D(x, p.y, z);
    }


    public static Point3D getPointByLatLong(double u, double v) {
        double theta = 2 * Math.PI * u; // longitude
        double phi = Math.acos(2 * v - 1); // colatitude

        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);
        return new Point3D(x, y, z);
    }

    public static Point3D getRandomLatLongPoint() {
        double u = Math.random(); // in [0,1)
        double v = Math.random(); // in [0,1)
        return getPointByLatLong(u, v);
    }
}
