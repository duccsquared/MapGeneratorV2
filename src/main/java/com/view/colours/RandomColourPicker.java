package com.view.colours;

import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;

import javafx.scene.paint.Color;

public class RandomColourPicker extends RendererColourPicker {

    @Override
    public Color getPointColor(Point3D point) {
        return Color.color(Math.random()/3, Math.random()/3, Math.random()/3, 1);
    }

    @Override
    public Color getPolygonColor(Polygon3D cell) {
        return Color.color(Math.random(), Math.random(), Math.random(), 1);
    }
    
}
