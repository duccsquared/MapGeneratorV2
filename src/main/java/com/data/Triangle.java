package com.data;

import java.util.List;

public class Triangle extends Polygon {

    public Triangle(Point p1, Point p2, Point p3) {
        super();
        this.initializeTriangle(p1, p2, p3);
    }

    public Triangle(Edge edge, Point point) {
        super();
        // make triangle with edge and points   
        List<Point> pp = edge.getPointList();

        Point p2 = pp.get(0);
        Point p3 = pp.get(1);

        if (point == p2 || point == p3) {
            throw new IllegalArgumentException("Edge has to not include the point");
        }

        this.initializeTriangle(point,p2,p3);
    }

    private void initializeTriangle(Point p1, Point p2, Point p3) {
        // add points
        this.getPoints().add(p1);
        this.getPoints().add(p2);
        this.getPoints().add(p3);

        // connect points to each other
        Edge e1 = p1.connectTo(p2);
        Edge e2 = p2.connectTo(p3);
        Edge e3 = p3.connectTo(p1);

        // add edges
        this.getEdges().add(e1);
        this.getEdges().add(e2);
        this.getEdges().add(e3);

        // add this triangle to the edges
        for (Edge edge : this.getEdges()) {
            edge.getPolygons().add(this);
        }
    }
}
