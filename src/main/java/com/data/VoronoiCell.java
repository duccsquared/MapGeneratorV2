package com.data;

import java.util.Set;

public class VoronoiCell extends Polygon {
    public VoronoiCell(Set<Point> points, Set<Edge> edges) {
        this.setPoints(points);
        this.setEdges(edges);
        
        // add this cell to the edges
        for (Edge edge : this.getEdges()) {
            edge.getPolygons().add(this);
        }
    }
}
