package com.data;

import java.util.ArrayList;
import java.util.List;

public class VoronoiCell3D extends Polygon3D {
    public VoronoiCell3D(List<Point3D> points, List<Edge3D> edges) {
        super(points,edges);
        
        // add this cell to the edges
        for (Edge3D edge : this.getEdges()) {
            edge.addPolygon(this);
        }
    }

    public VoronoiCell3D(Point3D site, List<Point3D> vertices) {
        super(vertices,calculateEdgesByVertices(vertices));
        this.setCenter(site);
    }

    static private List<Edge3D> calculateEdgesByVertices(List<Point3D> vertices) {
        List<Edge3D> edges = new ArrayList<>();
        for(Point3D vertex: vertices) {
            for(Edge3D edge : vertex.getEdges()) {
                if(vertices.contains(edge.getP1()) && vertices.contains(edge.getP2())) {
                    edges.add(edge);
                }
            }
        }
        return edges;
    }
}
