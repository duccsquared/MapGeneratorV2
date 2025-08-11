package com.data;

import java.util.List;

public class VoronoiCell extends Polygon {
    public VoronoiCell(List<Point> points, List<Edge> edges) {
        super(points,edges);
        
        // add this cell to the edges
        for (Edge edge : this.getEdges()) {
            edge.addPolygon(this);
        }
    }
}
