package com.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.*;

@Getter
@Setter
public class Polygon {
    private Point center;
    private Set<Point> points;
    private Set<Edge> edges;

    public Polygon() {
        this.points = new HashSet<>();
        this.edges = new HashSet<>();
    }

    public List<Point> getPointList() {
        // convert to list, then return
        List<Point> pointList = new ArrayList<>();
        pointList.addAll(this.getPoints());
        return pointList;   
    }

    public void delete(boolean removeEdges, boolean removePoints) {
        // loop through points
        ArrayList<String> pointList = new ArrayList<>();
        for(Point point: this.getPoints()) {
            pointList.add(point.toStringShort());
        }

        // loop through edges to remove
        for (Edge edge : this.edges) {
            edge.getPolygons().remove(this);
            // remove edges if the edges aren't connected to other polygons
            if (removeEdges && edge.getPolygons().isEmpty()) {
                edge.delete(removePoints);
            }

        }

        this.points.clear();
        this.edges.clear();
    }

    public void delete() {
        this.delete(false, false);
    }

    public String toString() {
        List<String> pointStrings = new ArrayList<>();
        for (Point point : this.points) {
            String pointString = String.format(point.toStringShort());
            pointStrings.add(pointString);
        }
        return "[" + String.join(", ", pointStrings) + "]";
    }
}
