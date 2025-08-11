package com.data;

import java.util.ArrayList;
import java.util.List;

public class Edge {
    private OrderedUniqueList<Point> points;
    private OrderedUniqueList<Polygon> polygons;

    public Edge(Point p1, Point p2) {
        // define variables
        this.points = new OrderedUniqueList<>();
        this.polygons = new OrderedUniqueList<>();
        // sanity checking points
        if(p1==p2) {
            throw new IllegalArgumentException("Edge points can't use the same points");
        }
        // add points
        this.points.add(p1);
        this.points.add(p2);
        // add self to points
        p1.addEdge(this);
        p2.addEdge(this);
    }

    public Point other(Point point) {
        // loop through points; return other point
        for(Point p: this.points) {
            if(point!=p) {
                return p;
            }
        }
        return null;
    }

    public Point getP1() {return this.points.get(0);}
    public Point getP2() {return this.points.get(1);}

    public List<Point> getPoints() {return this.points.getImmutableList();}
    public List<Polygon> getPolygons() {return this.polygons.getImmutableList();}

    public boolean addPolygon(Polygon polygon) {
        // if(this.polygons.size()>=2) {
        //     throw new IllegalStateException("Edges can only have a maximum of two polygons");
        // }
        return this.polygons.add(polygon);
    }
    public boolean removePolygon(Polygon polygon) {
        return this.polygons.remove(polygon);
    }

    public void delete(boolean removePoints) {
        // remove self from edges
        for(Point point: this.points) {
            point.removeEdge(this);
            // delete points
            if(removePoints) {
                point.delete();
            }
        }
    }

    public void delete() {this.delete(false);}

    public String toString() {
        List<String> pointStrings = new ArrayList<>();
        for(Point point: this.points) {
            String pointString = String.format(point.toStringShort());
            pointStrings.add(pointString);
        }
        return "[" + String.join(", ", pointStrings) + "]";
    }


}
