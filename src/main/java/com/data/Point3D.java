package com.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.*;

@Setter
@Getter
public class Point3D {
    public double x, y, z;
    private String name;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private OrderedUniqueList<Edge3D> edges = new OrderedUniqueList<>();

    public Point3D(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Point3D(double x, double y, double z, String name) {
        this.x = x; this.y = y; this.z = z; this.name = name;
    }


    @Override
    public boolean equals (Object other) {
        if(other instanceof Point3D) {
            Point3D pOther = (Point3D) other;
            return this == other || nearlyEqual(this.x, pOther.x, 1e-6) && nearlyEqual(this.y, pOther.y, 1e-6) && nearlyEqual(this.z, pOther.z, 1e-6);
        }
        else {
            return false;
        }
    }

    private boolean nearlyEqual(double a, double b, double margin) {
        return a >= b - margin && a <= b + margin;
    }

    public List<Edge3D> getEdges() {
        return this.edges.getImmutableList();
    }

    public boolean addEdge(Edge3D edge) {
        return this.edges.add(edge);
    }

    public boolean removeEdge(Edge3D edge) {
        return this.edges.remove(edge);
    }

    public boolean isConnectedTo(Point3D other) {
        // loop through edges to find other
        for(Edge3D edge: this.edges) {
            if(edge.getPoints().contains(other)) {
                return true;
            }
        }
        return false;
    }

    public Edge3D connectTo(Point3D other) {
        // create new edge if it doesn't exist
        if(!isConnectedTo(other)) {
            return new Edge3D(this, other);
        }
        else {
            // otherwise return the current edge
            return this.getEdgeWith(other);
        }
    }

    public Edge3D getEdgeWith(Point3D other) {
        // loop through edges to find an edge that contains this point and the other point
        for (Edge3D edge : this.edges) {
            if (edge.getPoints().contains(this) && edge.getPoints().contains(other)) {
                return edge;
            }
        }
        return null;
    }


    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public void delete() {
        // delete
        for(Edge3D edge: this.edges) {
            edge.delete(false);
        }
        this.edges.clear();
    }

    public String toString() {
        List<String> connectionList = new ArrayList<>();
        for(Edge3D edge: this.edges) {
            connectionList.add(edge.other(this).toStringShort());
        }
        return String.format("<Point %s: %s>",toStringShort(),String.join(", ",connectionList));
    }

    public String toStringShort() {
        if(this.name!=null) {
            return this.name;
        }
        else {
            return String.format("(%.3f, %.3f, %.3f)",this.getX(),this.getY(),this.getY());
        }
    }
}

