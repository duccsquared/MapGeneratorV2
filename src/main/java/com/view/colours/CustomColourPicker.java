package com.view.colours;

import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;

import javafx.scene.paint.Color;

public class CustomColourPicker extends RendererColourPicker {
    // Convert Cartesian to Lat/Lon 
    static double[] cartesianToLatLon(Point3D point) {
        double lat = Math.asin(point.y);
        double lon = Math.atan2(point.z, point.x);
        return new double[] {lat/Math.PI+0.5, lon/(2*Math.PI)+0.5};
    }

    // calculate surface distance between two lat/lon points (assume points are normalized to [0,1])
    static double surfaceDistance(double[] latLon1, double[] latLon2) {
        double R = 1; // unit sphere
        double dLat = (latLon2[0] - latLon1[0]) * Math.PI;
        double dLon = (latLon2[1] - latLon1[1]) * 2 * Math.PI;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(latLon1[0] * Math.PI) * Math.cos(latLon2[0] * Math.PI) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public Color getPointColor(Point3D point) {
        double[] latLong = cartesianToLatLon(point);
        return Color.color(latLong[0]/3, 0, latLong[1]/3, 1);
    }

    @Override
    public Color getPolygonColor(Polygon3D cell) {
        double[] latLong = cartesianToLatLon(cell.getCenter());
        return Color.color(latLong[0], 0, latLong[1], 1);
    }
}
