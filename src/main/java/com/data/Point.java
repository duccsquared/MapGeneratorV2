package com.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.*;

@Setter
@Getter
public class Point {
    private static int seq = 0;
    private int id;
    private double x;
    private double y;
    private String name;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private OrderedUniqueList<Edge> edges = new OrderedUniqueList<>();

    public Point(double x, double y) {
        this.id = seq; seq += 1;
        this.id = (int)(Math.random()*Integer.MAX_VALUE);
        this.x = x;
        this.y = y;
    }

    public Point(double x, double y, String name) {
        this.id = seq; seq += 1;
        this.id = (int)(Math.random()*Integer.MAX_VALUE);
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public List<Edge> getEdges() {
        return this.edges.getImmutableList();
    }

    public boolean addEdge(Edge edge) {
        return this.edges.add(edge);
    }

    public boolean removeEdge(Edge edge) {
        return this.edges.remove(edge);
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

    // @Override
    // public boolean equals(Object obj) {
    //     if (!(obj instanceof Point)) return false;
    //     Point other = (Point) obj;
    //     return this.x == other.x && this.y == other.y;
    // }

    @Override
    public boolean equals (Object other) {
        if(other instanceof Point) {
            Point pOther = (Point) other;
            return this.getId() == pOther.getId();
            // return this == other || nearlyEqual(this.x, pOther.x, 1e-6) && nearlyEqual(this.y, pOther.y, 1e-6);
        }
        else {
            return false;
        }
    }

    // private boolean nearlyEqual(double a, double b, double margin) {
    //     return a >= b - margin && a <= b + margin;
    // }

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
