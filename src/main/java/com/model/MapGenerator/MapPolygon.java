package com.model.MapGenerator;

import java.util.List;

import com.model.Graph3D.Edge3D;
import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;

public class MapPolygon extends Polygon3D {

    private double continentMask;
    private double landNoise;
    private double seaNoise;
    private double mountain;
    private double mountainMask;
    private double altitude;


    public MapPolygon(List<Point3D> points, List<Edge3D> edges) {
        super(points,edges);
        this.altitude = 0;
    }

    public MapPolygon(Point3D site, List<Point3D> vertices) {
        super(site,vertices);
        this.altitude = 0;
    }

    public double getAltitude() {return altitude;}
    public void setAltitude(double altitude) {this.altitude = altitude;}
    public double getContinentMask() {return continentMask;}
    public void setContinentMask(double continentMask) {this.continentMask = continentMask;}
    public double getLandNoise() {return landNoise;}
    public void setLandNoise(double landNoise) {this.landNoise = landNoise;}
    public double getSeaNoise() {return seaNoise;}
    public void setSeaNoise(double seaNoise) {this.seaNoise = seaNoise;}
    public double getMountain() {return mountain;}
    public void setMountain(double mountain) {this.mountain = mountain;}
    public double getMountainMask() {return mountainMask;}
    public void setMountainMask(double mountainMask) {this.mountainMask = mountainMask;}

}
