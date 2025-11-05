package com.model.MapGenerator;

import java.util.Collections;
import java.util.List;

import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;
import com.model.Util.Util;
import com.model.Voronoi.Voronoi3DGraph;

public class MapGenerator {
    Voronoi3DGraph<Point3D, MapPolygon> graph;

    public MapGenerator() {
        this.graph = new Voronoi3DGraph<Point3D, MapPolygon>((x, y, z) -> new Point3D(x, y, z),
            (x, y) -> new MapPolygon(x,y));
    }

    public Voronoi3DGraph<Point3D, MapPolygon> getGraph() {
        return graph;
    }

    public void calculateAltitudeMap() {
        for(MapPolygon mapPolygon: this.graph.getVoronoiCells()) {
            mapPolygon.setAltitude(Util.randInt(-100, 80));
        }
        // normalize between tiles
        Collections.shuffle(this.graph.getVoronoiCells());
        for(MapPolygon mapPolygon: this.graph.getVoronoiCells()) {
            int i = 1;
            double totalAltitude = 0;
            totalAltitude += mapPolygon.getAltitude();
            for(Polygon3D adj: mapPolygon.adjacentIterator()) {
                MapPolygon mapAdj = (MapPolygon) adj;
                totalAltitude += mapAdj.getAltitude();
                i += 1;
            }
            mapPolygon.setAltitude(totalAltitude/i);
        }
    }

    public List<Point3D> getPoints() {
        return this.graph.getVoronoiPoints();
    }

    public List<MapPolygon> getCells() {
        return this.graph.getVoronoiCells();
    }

}
