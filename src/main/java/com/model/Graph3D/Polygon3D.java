package com.model.Graph3D;

import java.util.ArrayList;
import java.util.List;

import com.model.Util.OrderedUniqueList;

import lombok.*;


public class Polygon3D {
    @Getter
    @Setter
    private Point3D center;
    private OrderedUniqueList<Point3D> points;
    private OrderedUniqueList<Edge3D> edges;

    public Polygon3D() {
        this.points = new OrderedUniqueList<>();
        this.edges = new OrderedUniqueList<>();
    }

    public Polygon3D(List<Point3D> points, List<Edge3D> edges) {
        this();
        for (Point3D point : points) {
            this.points.add(point);
        }
        for (Edge3D edge : edges) {
            this.edges.add(edge);
        }
    }

    public List<Point3D> getPoints() {return points.getImmutableList();}
    public List<Edge3D> getEdges() {return edges.getImmutableList();}
    public boolean addPoint(Point3D point) {return this.points.add(point);}
    // public boolean removePoint(Point3D point) {return this.points.remove(point);}
    public boolean addEdge(Edge3D edge) {return this.edges.add(edge);}
    // public boolean removeEdge(Edge3D edge) {return this.edges.remove(edge);}

    public void delete(boolean removeEdges, boolean removePoints) {
        // loop through points
        ArrayList<String> pointList = new ArrayList<>();
        for(Point3D point: this.getPoints()) {
            pointList.add(point.toStringShort());
        }

        // loop through edges to remove
        for (Edge3D edge : this.edges) {
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
        for (Point3D point : this.points) {
            String pointString = String.format(point.toStringShort());
            pointStrings.add(pointString);
        }
        return "[" + String.join(", ", pointStrings) + "]";
    }
}
