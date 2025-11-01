package com.model.Graph2D;

public class Triangle extends Polygon {

    public Triangle(Point p1, Point p2, Point p3) {
        super();
        this.initializeTriangle(p1, p2, p3);
    }

    public Triangle(Edge edge, Point point) {
        super();
        // make triangle with edge and points   

        Point p2 = edge.getP1();
        Point p3 = edge.getP2();

        if (point == p2 || point == p3) {
            throw new IllegalArgumentException("Edge has to not include the point");
        }

        this.initializeTriangle(point,p2,p3);
    }

    private void initializeTriangle(Point p1, Point p2, Point p3) {
        // add points
        this.addPoint(p1);
        this.addPoint(p2);
        this.addPoint(p3);

        // connect points to each other
        Edge e1 = p1.connectTo(p2);
        Edge e2 = p2.connectTo(p3);
        Edge e3 = p3.connectTo(p1);

        // add edges
        this.addEdge(e1);
        this.addEdge(e2);
        this.addEdge(e3);

        // add this triangle to the edges
        for (Edge edge : this.getEdges()) {
            edge.addPolygon(this);
        }
    }

    public Point getP1() {return this.getPoints().get(0);}
    public Point getP2() {return this.getPoints().get(1);}
    public Point getP3() {return this.getPoints().get(2);}
}
