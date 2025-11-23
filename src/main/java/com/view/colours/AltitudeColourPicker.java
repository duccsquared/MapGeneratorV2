package com.view.colours;

import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;
import com.model.MapGenerator.MapPolygon;

import javafx.scene.paint.Color;

public class AltitudeColourPicker extends RendererColourPicker {

    // Color stops: elevation, R, G, B
    private static final double[][] COLOR_MAP = {
        {-1.0,   0,   0, 139},
        {-0.5,   0, 105, 148},
        {-0.25,  0, 149, 182},
        {0.0,  20, 150, 162},
        {0.0, 0, 201, 50},
        {0.08, 30, 211, 104},
        {0.15, 95, 224, 116},
        {0.23, 163, 235, 130},
        {0.31, 223, 248, 146},
        {0.38, 245, 228, 148},
        {0.46, 197, 175, 116},
        {0.54, 163, 128, 95},
        {0.62, 144, 98, 85},
        {0.69, 162, 124, 115},
        {0.77, 178, 150, 139},
        {0.85, 199, 175, 169},
        {0.92, 219, 205, 202},
        {1.0, 236, 228, 226},
        
    };

    public Color getColor(double altitude) {
        // Clamp altitude to valid range
        altitude = Math.max(-1.0, Math.min(1.0, altitude));
        
        // Find the two color stops to interpolate between
        int lowerIndex = 0;
        int upperIndex = COLOR_MAP.length - 1;
        
        for (int i = 0; i < COLOR_MAP.length - 1; i++) {
            if (altitude >= COLOR_MAP[i][0] && altitude <= COLOR_MAP[i + 1][0]) {
                lowerIndex = i;
                upperIndex = i + 1;
                break;
            }
        }
        
        // Get the color stops
        double lowerElevation = COLOR_MAP[lowerIndex][0];
        double upperElevation = COLOR_MAP[upperIndex][0];
        
        // Calculate interpolation factor
        double t = (altitude - lowerElevation) / (upperElevation - lowerElevation);
        
        // Interpolate RGB values
        double[] color = new double[3];
        for (int i = 0; i < 3; i++) {
            double lowerColor = COLOR_MAP[lowerIndex][i + 1];
            double upperColor = COLOR_MAP[upperIndex][i + 1];
            color[i] = lowerColor + t * (upperColor - lowerColor);
        }
        
        return Color.color(color[0]/255.0, color[1]/255.0, color[2]/255.0);
    }

    static double[] cartesianToLatLon(Point3D point) {
        double lat = Math.asin(point.y);
        double lon = Math.atan2(point.z, point.x);
        return new double[] {lat/Math.PI+0.5, lon/(2*Math.PI)+0.5};
    }

    @Override
    public Color getPointColor(Point3D point) {
        return Color.color(0.5, 0.5, 0.5, 0);
    }

    @Override
    public Color getPolygonColor(Polygon3D cell) {
        if(cell instanceof MapPolygon) {
            MapPolygon mapPolygon = (MapPolygon) cell;
            double altitude = clamp(mapPolygon.getAltitude(),-1,1); // -1 to 1
            double brightnessMultiplier = (altitude+1)/2;
            double[] latLong = cartesianToLatLon(cell.getCenter());
            if(altitude > -40 && (latLong[0] < 1/18.0 || latLong[0] > 17/18.0)) {
                return Color.color(1*brightnessMultiplier, 1*brightnessMultiplier, 1*brightnessMultiplier, 1);
            }
            else {
                return this.getColor(altitude);
            }
        }
        else {
            return Color.color(0, 0, 0, 1);
        }
    }


    double clamp(double val, double minVal, double maxVal) {
        return Math.min(maxVal,Math.max(minVal,val));
    }
}
