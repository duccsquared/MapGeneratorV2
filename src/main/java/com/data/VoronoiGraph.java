package com.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.*;

@SuppressWarnings("unused")
@Data
public class VoronoiGraph {
    private double width;
    private double height;
    private List<Point> points;
    private List<VoronoiCell> cells;

    public VoronoiGraph(DelaunayGraph delaunayGraph) {
        this.points = new ArrayList<>();
        this.cells = new ArrayList<>();

        this.generateVoronoi(delaunayGraph);
    }


    public List<Edge> getEdges() {
        Set<Edge> edgeSet = new HashSet<>();
        for (Point point : this.points) {
            edgeSet.addAll(point.getEdges());
        }
        return new ArrayList<>(edgeSet);
    }


    public void generateVoronoi(DelaunayGraph delaunayGraph) {
        try {
            // Step 1: calculate circumcircles for all triangles
            for(Triangle triangle: delaunayGraph.getTriangles()) {
                if(triangle.getCenter()==null) {
                    Circumcircle circumcircle = new Circumcircle(triangle);
                    triangle.setCenter(circumcircle.getCenter());
                }
                this.points.add(triangle.getCenter());
            }
            // Step 2: create Voronoi edges based on Delaunay edges
            for(Point point: delaunayGraph.getPoints()) {
                // cell variables
                Set<Point> cellPointSet = new HashSet<>();
                Set<Edge> cellEdgeSet = new HashSet<>();
                // loop through edges connected to the point
                for(Edge delaunayEdge: point.getEdges()) {
                    if(delaunayEdge.getPolygons().size()==2) {
                        // connect cirumcircles together and add the points and the edge
                        List<Polygon> triangles = delaunayEdge.getPolygons();
                        Point p1 = triangles.get(0).getCenter();
                        Point p2 = triangles.get(1).getCenter();
                        Edge edge = p1.connectTo(p2);
                        cellPointSet.add(p1);
                        cellPointSet.add(p2);
                        cellEdgeSet.add(edge);
                    }
                    else {
                        // edges are supposed to only have two triangles on each side
                        throw new IllegalArgumentException("edge with incorrect number of triangles found");
                    }
                }
                VoronoiCell voronoiCell = new VoronoiCell(new ArrayList<>(cellPointSet), new ArrayList<>(cellEdgeSet));
                cells.add(voronoiCell);



            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
