package com.view.colours;

import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;
import com.model.MapGenerator.MapPolygon;

import javafx.scene.paint.Color;

public class MapPolygonColourPicker extends RendererColourPicker {

    // Color stops: elevation, R, G, B
    private static final double[][] COLOR_MAP = {
        {-1.00, 39, 74, 143}, 
        {-0.75, 64, 110, 170},
        {-0.50, 112, 150, 204},
        {-0.25, 184, 210, 230}, 
        { 0.00, 245, 245, 245}, 
        { 0.25, 255, 225, 190}, 
        { 0.50, 255, 170, 110},
        { 0.75, 230, 110, 60}, 
        { 1.00, 190, 45, 30} 
    };

    private String viewMode;

    public MapPolygonColourPicker(String viewMode) {
        this.viewMode = viewMode;
    }

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

    public double getValue(MapPolygon cell) {
        if(this.viewMode=="Continent Mask") {
            return cell.getContinentMask()*2-1;
        }
        else if(this.viewMode=="Land") {
            return cell.getLandNoise();
        }
        else if(this.viewMode=="Sea") {
            return cell.getSeaNoise();
        }
        else if(this.viewMode=="Mountain") {
            return cell.getMountain();
        }
        else if(this.viewMode=="Mountain Mask") {
            return cell.getMountainMask()*2-1;
        }
        else {
            return 0;
        }   
    }

    @Override
    public Color getPointColor(Point3D point) {
        return Color.color(0.5, 0.5, 0.5, 0);
    }

    @Override
    public Color getPolygonColor(Polygon3D cell) {
        if(cell instanceof MapPolygon) {
            MapPolygon mapPolygon = (MapPolygon) cell;
            //
            double value = getValue(mapPolygon);

            // double brightnessMultiplier = (value+1)/2;
            // return Color.color(brightnessMultiplier, brightnessMultiplier, brightnessMultiplier);
            return getColor(value);
        }
        else {
            return Color.color(0, 0, 0, 1);
        }
    }


    double clamp(double val, double minVal, double maxVal) {
        return Math.min(maxVal,Math.max(minVal,val));
    }
}
