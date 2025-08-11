package com.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.*;

@Setter
@Getter
public class Point {
    private double x;
    private double y;
    private String name;
    private List<Edge> edges = new ArrayList<>();

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(double x, double y, String name) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public boolean isConnectedTo(Point other) {
        // loop through edges to find other
        for(Edge edge: this.edges) {
            if(edge.getPoints().contains(other)) {
                return true;
            }
        }
        return false;
    }

    public Edge connectTo(Point other) {
        // create new edge if it doesn't exist
        if(!isConnectedTo(other)) {
            return new Edge(this, other);
        }
        else {
            // otherwise return the current edge
            return this.getEdgeWith(other);
        }
    }

    public Edge getEdgeWith(Point other) {
        // loop through edges to find an edge that contains this point and the other point
        for (Edge edge : this.edges) {
            if (edge.getPoints().contains(this) && edge.getPoints().contains(other)) {
                return edge;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point)) return false;
        Point other = (Point) obj;
        return this.x == other.x && this.y == other.y;
    }
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public void delete() {
        // delete
        for(Edge edge: this.edges) {
            edge.delete(false);
        }
        this.edges.clear();
    }

    public String toString() {
        List<String> connectionList = new ArrayList<>();
        for(Edge edge: this.edges) {
            connectionList.add(edge.other(this).toStringShort());
        }
        return String.format("<Point %s: %s>",toStringShort(),String.join(", ",connectionList));
    }

    public String toStringShort() {
        if(this.name!=null) {
            return this.name;
        }
        else {
            return String.format("(%s, %s)",this.getX(),this.getY());
        }
    }
}
