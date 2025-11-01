package com.model.Voronoi;

import com.model.Graph2D.Point;

public class GhostPoint extends Point {
    private Point point;

    public GhostPoint(Point point, double x, double y) {
        super(x, y);
        this.point = point;
    }

    public Point getPoint() {
        return point;
    }
    public void setPoint(Point point) {
        this.point = point;
    }
}
