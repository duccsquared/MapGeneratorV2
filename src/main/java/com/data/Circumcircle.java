package com.data;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Circumcircle {
    private Point center;
    private double radius;
    public Circumcircle(Triangle triangle) {
        // get triangle's points
        List<Point> pp = triangle.getPointList();
        double ax = pp.get(0).getX();
        double ay = pp.get(0).getY();
        double bx = pp.get(1).getX();
        double by = pp.get(1).getY();
        double cx = pp.get(2).getX();
        double cy = pp.get(2).getY();

        // initial calculations
        double d = 2 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by));

        double ax2ay2 = ax * ax + ay * ay;
        double bx2by2 = bx * bx + by * by;
        double cx2cy2 = cx * cx + cy * cy;

        // get x and y coordinates
        double ux = (ax2ay2 * (by - cy) + bx2by2 * (cy - ay) + cx2cy2 * (ay - by)) / d;
        double uy = (ax2ay2 * (cx - bx) + bx2by2 * (ax - cx) + cx2cy2 * (bx - ax)) / d;

        // get radius
        double radius = Math.sqrt(Math.pow(ux - ax, 2) + Math.pow(uy - ay, 2));

        // set values
        this.center = new Point(ux, uy);
        this.radius = radius;
    }
    public String toString() {
        return String.format("Cc: p=%s, r=%s",center.toStringShort(),radius);
    }
}
