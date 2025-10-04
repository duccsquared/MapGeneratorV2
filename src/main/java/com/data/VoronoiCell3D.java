package com.data;

import java.util.List;

public class VoronoiCell3D {
    Point3D site;
    List<Point3D> vertices; // ordered circumcenters

    public VoronoiCell3D(Point3D site, List<Point3D> vertices) {
        this.site = site;
        this.vertices = vertices;
    }
    public List<Point3D> getVertices() {return vertices;}

    @Override
    public String toString() {
        return String.format("VoronoiCell3D %s -> %s", this.site, this.vertices);
    }
}
