package com.model.Graph3D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.model.Util.EdgeKey;
import com.model.Util.OrderedUniqueList;

public class Edge3D {

    private OrderedUniqueList<Point3D> points;
    private OrderedUniqueList<Polygon3D> polygons;

    Edge3D(Point3D p1, Point3D p2) {
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

    public Point3D other(Point3D point) {
        // loop through points; return other point
        for(Point3D p: this.points) {
            if(point!=p) {
                return p;
            }
        }
        return null;
    }

    public Point3D getP1() {return this.points.get(0);}
    public Point3D getP2() {return this.points.get(1);}

    public List<Point3D> getPoints() {return this.points.getImmutableList();}
    public List<Polygon3D> getPolygons() {return this.polygons.getImmutableList();}

    public boolean addPolygon(Polygon3D polygon) {
        // if(this.polygons.size()>=2) {
        //     throw new IllegalStateException("Edges can only have a maximum of two polygons");
        // }
        return this.polygons.add(polygon);
    }
    public boolean removePolygon(Polygon3D polygon) {
        return this.polygons.remove(polygon);
    }

    public void delete(boolean removePoints) {
        // remove self from edges
        for(Point3D point: this.points) {
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
        for(Point3D point: this.points) {
            String pointString = String.format(point.toStringShort());
            pointStrings.add(pointString);
        }
        return "[" + String.join(", ", pointStrings) + "]";
    }

    @Override 
    public boolean equals(Object other) {
        if(other instanceof Edge3D) {
            Edge3D vOther = (Edge3D) other;
            return this.getEdgeKey().equals(vOther.getEdgeKey());
            // return (this.getP1().equals(vOther.getP1()) && this.getP2().equals(vOther.getP2())) || (this.getP1().equals(vOther.getP2()) && this.getP2().equals(vOther.getP1()));
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.getEdgeKey().hashCode();
    }

    public EdgeKey getEdgeKey() {
        return new EdgeKey(this.getP1().getId(), this.getP2().getId());
    }

}