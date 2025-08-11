package com.data;

public class Util {
    static int randInt(int a, int b) {
        return a + (int) Math.floor(Math.random() * (b - a + 1));
    }

    static double distance(Point a, Point b) {
        return Math.pow(Math.pow(b.getX() - a.getX(),2)+Math.pow(b.getY() - a.getY(),2),0.5);
    }
}
