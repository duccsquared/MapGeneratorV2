package com.model.MapGenerator;

import java.util.List;

import com.model.Graph3D.Edge3D;
import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;

public class MapPolygon extends Polygon3D {

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

}
