package com.view.colours;

import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;

import javafx.scene.paint.Color;

public abstract class RendererColourPicker {
    public abstract Color getPointColor(Point3D point);

    public abstract Color getPolygonColor(Polygon3D cell);

}
