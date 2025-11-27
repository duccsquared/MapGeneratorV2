package com.view.colours;

import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;
import com.model.MapGenerator.MapPolygon;

import javafx.scene.paint.Color;

public class MapPolygonColourPicker extends RendererColourPicker {

    private String viewMode;

    public MapPolygonColourPicker(String viewMode) {
        this.viewMode = viewMode;
    }

    public double getValue(MapPolygon cell) {
        if(this.viewMode=="Continent Mask") {
            return cell.getContinentMask();
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
            return cell.getMountainMask();
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

            double brightnessMultiplier = (value+1)/2;
            return Color.color(brightnessMultiplier, brightnessMultiplier, brightnessMultiplier);
        }
        else {
            return Color.color(0, 0, 0, 1);
        }
    }


    double clamp(double val, double minVal, double maxVal) {
        return Math.min(maxVal,Math.max(minVal,val));
    }
}
