package com.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import lombok.*;

@Getter
public class Edge {
    private Set<Point> points;
    private List<Polygon> polygons;

    public Edge(Point p1, Point p2) {
        // define variables
        this.points = new HashSet<>();
        this.polygons = new ArrayList<>();
        // add points
        this.points.add(p1);
        this.points.add(p2);
        // add self to points
        p1.getEdges().add(this);
        p2.getEdges().add(this);
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

    public List<Point> getPointList() {
        // convert to list, then return
        List<Point> pointList = new ArrayList<>();
        pointList.addAll(this.getPoints());
        return pointList;
    }

    public List<Polygon> getPolygonList() {
        // convert to list, then return
        List<Polygon> triangeList = new ArrayList<>();
        triangeList.addAll(this.polygons);
        return triangeList;
    }


    public void delete(boolean removePoints) {
        // remove self from edges
        for(Point point: this.points) {
            point.getEdges().remove(this);
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
