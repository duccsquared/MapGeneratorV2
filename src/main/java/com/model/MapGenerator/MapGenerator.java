package com.model.MapGenerator;

// import java.util.HashMap;
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

    public MapGenerator(int pointCount) {
        this.graph = new Voronoi3DGraph<Point3D, MapPolygon>(pointCount, (x, y, z) -> new Point3D(x, y, z),
            (x, y) -> new MapPolygon(x,y));
    }

    public Voronoi3DGraph<Point3D, MapPolygon> getGraph() {
        return graph;
    }

    public void calculateAltitudeMap() {
        // generate noise (inputs: range, startFrequency, octaves)
        LayeredPerlinNoise3D perlinNoise = new LayeredPerlinNoise3D(3, 4, 4);
        LayeredPerlinNoise3D perlinNoise2 = new LayeredPerlinNoise3D(3, 1, 4);
        LayeredPerlinNoise3D perlinNoise3 = new LayeredPerlinNoise3D(2, 8, 4);

        // loop through polygons
        for(MapPolygon mapPolygon: this.graph.getVoronoiCells()) {
            Point3D point = mapPolygon.getCenter();

            // warp noise
            Point3D offsetPoint = new Point3D(
                perlinNoise.getNoiseValue(0.5*point.getX()+1.9, 0.5*point.getY()+1.7, 0.5*point.getZ()-1.8), 
                perlinNoise.getNoiseValue(0.5*point.getX()-1.5, 0.5*point.getY()+1.8, 0.5*point.getZ()-1.6), 
                perlinNoise.getNoiseValue(0.5*point.getX()+1.7, 0.5*point.getY()-1.5, 0.5*point.getZ()+1.8)
            );

            // warp noise 2
            Point3D offsetPoint2 = new Point3D(
                perlinNoise.getNoiseValue(0.5*point.getX()+1.2, 0.5*point.getY()-1.1, 0.5*point.getZ()-1.7), 
                perlinNoise.getNoiseValue(0.5*point.getX()-0.7, 0.5*point.getY()+1.5, 0.5*point.getZ()+1.4), 
                perlinNoise.getNoiseValue(0.5*point.getX()-1.1, 0.5*point.getY()-0.9, 0.5*point.getZ()-1.5)
            );


            // mapPolygon.setAltitude(95 * perlinNoise.getNoiseValue(Util.add(point, offsetPoint)) - 5);

            // get continent mask
            double continentMask = smoothstep(0, 0.5, perlinNoise2.getNoiseValue(Util.add(Util.scale(Util.add(point,Util.scale(offsetPoint,0.4)),0.8),new Point3D(-1.1, 0.8, 0.7))));

            // get land and sea noise
            double landNoise = perlinNoise.getNoiseValue(Util.add(point,Util.scale(offsetPoint2,0.3))) * 0.3 + 0.3;
            double seaNoise = perlinNoise2.getNoiseValue(point) * 0.5 - 0.3;

            // get altitude based on mask
            double altitude = lerp(seaNoise, landNoise, continentMask);

            // apply mountains
            double mountain = 1 - Math.abs(perlinNoise3.getNoiseValue(1.8*point.getX(), 1.8*point.getY(), 1.8*point.getZ()));
            double mountainMask = smoothstep(0.3, 0.4, altitude);
            altitude = altitude + mountain * mountainMask * 0.5;
            
            // set altitude
            mapPolygon.setContinentMask(continentMask);
            mapPolygon.setLandNoise(landNoise);
            mapPolygon.setSeaNoise(seaNoise);
            mapPolygon.setMountain(mountain);
            mapPolygon.setMountainMask(mountainMask);
            mapPolygon.setAltitude(altitude);
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
