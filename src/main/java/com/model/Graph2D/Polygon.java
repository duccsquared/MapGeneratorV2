package com.model.Graph2D;

import java.util.ArrayList;
import java.util.List;

import com.model.Util.OrderedUniqueList;

import lombok.*;


public class Polygon {
    @Getter
    @Setter
    private Point center;
    private OrderedUniqueList<Point> points;
    private OrderedUniqueList<Edge> edges;

    public Polygon() {
        this.points = new OrderedUniqueList<>();
        this.edges = new OrderedUniqueList<>();
    }

    public Polygon(List<Point> points, List<Edge> edges) {
        this();
        for (Point point : points) {
            this.points.add(point);
        }
        for (Edge edge : edges) {
            this.edges.add(edge);
        }
    }

    public List<Point> getPoints() {return points.getImmutableList();}
    public List<Edge> getEdges() {return edges.getImmutableList();}
    public boolean addPoint(Point point) {return this.points.add(point);}
    // public boolean removePoint(Point point) {return this.points.remove(point);}
    public boolean addEdge(Edge edge) {return this.edges.add(edge);}
    // public boolean removeEdge(Edge edge) {return this.edges.remove(edge);}

    public void delete(boolean removeEdges, boolean removePoints) {
        // loop through points
        ArrayList<String> pointList = new ArrayList<>();
        for(Point point: this.getPoints()) {
            pointList.add(point.toStringShort());
        }

        // loop through edges to remove
        for (Edge edge : this.edges) {
            edge.removePolygon(this);
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
