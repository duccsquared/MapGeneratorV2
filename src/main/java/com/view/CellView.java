package com.view;

import com.data.Point3D;
import com.data.VoronoiCell3D;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class CellView {
    VoronoiCell3D cell;
    Polygon cellDisplay;
    double zOrder = 0;
    Renderer3DPane pane;

    public CellView(VoronoiCell3D cell, Renderer3DPane pane) {
        this.cell = cell;
        this.pane = pane;
        this.cellDisplay = new Polygon();
        for (Point3D v : cell.getPoints()) {
            double x = pane.getPointViewMap().get(v).getPointDisplay().getCenterX();
            double y = pane.getPointViewMap().get(v).getPointDisplay().getCenterY();
            // coordsList.add(xy);
            cellDisplay.getPoints().addAll(x,y);
        }
        this.cellDisplay.setFill(Color.color(Math.random(), Math.random(), Math.random(), 1));
        this.cellDisplay.setStroke(Color.BLACK);
        pane.getChildren().add(this.cellDisplay);
        this.cellDisplay.toBack();
    }

    public VoronoiCell3D getCell() {
        return cell;
    }
    public Polygon getCellDisplay() {
        return cellDisplay;
    }
    public double getzOrder() {
        return zOrder;
    }
    public void update() {
        // remove points from cellDisplay and replace them with updated points
        this.cellDisplay.getPoints().clear();
        double totalZ = 0;
        for (Point3D v : cell.getPoints()) {
            double x = pane.getPointViewMap().get(v).getPointDisplay().getCenterX();
            double y = pane.getPointViewMap().get(v).getPointDisplay().getCenterY();
            this.cellDisplay.getPoints().addAll(x,y);
            // also calculate distance from camera to cell center
            totalZ += pane.getPointViewMap().get(v).getzOrder();
        }
        this.zOrder = totalZ/cell.getPoints().size();
    }

}
