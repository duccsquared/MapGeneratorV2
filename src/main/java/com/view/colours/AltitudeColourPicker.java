package com.view.colours;

import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;
import com.model.MapGenerator.MapPolygon;

import javafx.scene.paint.Color;

public class AltitudeColourPicker extends RendererColourPicker {
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
            double altitude = mapPolygon.getAltitude();
            double brightnessMultiplier = (altitude + 100)/200;
            double[] latLong = cartesianToLatLon(cell.getPoints().get(0));
            if(altitude > -20 && (latLong[0] < 0.15 || latLong[0] > 0.85)) {
                return Color.color(1*brightnessMultiplier, 1*brightnessMultiplier, 1*brightnessMultiplier, 1);
            }
            else if(altitude>0) {
                return Color.color(0.2*brightnessMultiplier, 1*brightnessMultiplier, 0.2*brightnessMultiplier, 1);
            }
            else {
                return Color.color(0.2*brightnessMultiplier, 0.2*brightnessMultiplier, 1*brightnessMultiplier, 1);
            }
        }
        else {
            return Color.color(0, 0, 0, 1);
        }
        
    }
}
