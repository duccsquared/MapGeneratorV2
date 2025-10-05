package com.data;

import java.util.ArrayList;
import java.util.List;

public class Triangle3D extends Polygon3D {

    public Triangle3D(Point3D p1, Point3D p2, Point3D p3) {
        super();
        this.initializeTriangle(p1, p2, p3);
    }

    public Triangle3D(Edge3D edge, Point3D point) {
        super();
        // make triangle with edge and points   

        Point3D p2 = edge.getP1();
        Point3D p3 = edge.getP2();

        if (point == p2 || point == p3) {
            throw new IllegalArgumentException("Edge3D has to not include the point");
        }

        this.initializeTriangle(point,p2,p3);
    }

    private void initializeTriangle(Point3D p1, Point3D p2, Point3D p3) {
        // add points
        this.addPoint(p1);
        this.addPoint(p2);
        this.addPoint(p3);

        // connect points to each other
        Edge3D e1 = p1.connectTo(p2);
        Edge3D e2 = p2.connectTo(p3);
        Edge3D e3 = p3.connectTo(p1);

        // add edges
        this.addEdge(e1);
        this.addEdge(e2);
        this.addEdge(e3);

        // add this triangle to the edges
        for (Edge3D edge : this.getEdges()) {
            edge.addPolygon(this);
        }
    }

    public Point3D getP1() {return this.getPoints().get(0);}
    public Point3D getP2() {return this.getPoints().get(1);}
    public Point3D getP3() {return this.getPoints().get(2);}

    public boolean includes(Point3D p) {
        return this.getPoints().contains(p);
    }

    public List<Point3D> getOthers(Point3D p) {
        List<Point3D> points = new ArrayList<>();
        for(Point3D point: getPoints()) {
            if(p!=point) {
                points.add(point);
            }
        }
        return points;
    }


}
