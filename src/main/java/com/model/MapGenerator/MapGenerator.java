package com.model.MapGenerator;

// import java.util.Collections;
import java.util.List;

import com.model.Graph3D.Point3D;
// import com.model.Graph3D.Polygon3D;
import com.model.Noise.LayeredPerlinNoise3D;
import com.model.Util.Util;
// import com.model.Util.Util;
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
        // for(MapPolygon mapPolygon: this.graph.getVoronoiCells()) {
        //     mapPolygon.setAltitude(Util.randInt(-100, 80));
        // }
        // // normalize between tiles
        // Collections.shuffle(this.graph.getVoronoiCells());
        // for(MapPolygon mapPolygon: this.graph.getVoronoiCells()) {
        //     int i = 1;
        //     double totalAltitude = 0;
        //     totalAltitude += mapPolygon.getAltitude();
        //     for(Polygon3D adj: mapPolygon.adjacentIterator()) {
        //         MapPolygon mapAdj = (MapPolygon) adj;
        //         totalAltitude += mapAdj.getAltitude();
        //         i += 1;
        //     }
        //     mapPolygon.setAltitude(totalAltitude/i);
        // }
        LayeredPerlinNoise3D perlinNoise = new LayeredPerlinNoise3D(3, 4, 4);
        LayeredPerlinNoise3D perlinNoise2 = new LayeredPerlinNoise3D(3, 1, 4);

        for(MapPolygon mapPolygon: this.graph.getVoronoiCells()) {
            Point3D point = mapPolygon.getCenter();

            // warp noise
            Point3D offsetPoint = new Point3D(
                perlinNoise.getNoiseValue(0.5*point.getX()+1.9, 0.5*point.getY()+1.7, 0.5*point.getZ()-1.8), 
                perlinNoise.getNoiseValue(0.5*point.getX()-1.5, 0.5*point.getY()+1.8, 0.5*point.getZ()-1.6), 
                perlinNoise.getNoiseValue(0.5*point.getX()+1.7, 0.5*point.getY()-1.5, 0.5*point.getZ()+1.8)
            );


            // mapPolygon.setAltitude(95 * perlinNoise.getNoiseValue(Util.add(point, offsetPoint)) - 5);

            double continentMask = smoothstep(0.2, 0.5, perlinNoise.getNoiseValue(Util.scale(offsetPoint, 0.2)));

            double landNoise = perlinNoise.getNoiseValue(Util.add(point, Util.scale(offsetPoint,0.3)));
            double seaNoise = perlinNoise2.getNoiseValue(point) * 0.5;
            mapPolygon.setAltitude(95*lerp(seaNoise, landNoise, continentMask)-5);
        }
    }

    public List<Point3D> getPoints() {
        return this.graph.getVoronoiPoints();
    }

    public List<MapPolygon> getCells() {
        return this.graph.getVoronoiCells();
    }

    double smoothstep(double edge0, double edge1, double x) {
        double t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3 - 2 * t);
    }

    double clamp(double val, double minVal, double maxVal) {
        return Math.min(maxVal,Math.max(minVal,val));
    }

    double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
}
