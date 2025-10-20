package com.view;

import com.data.Point3D;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PointView {
    Point3D point;
    Circle pointDisplay;
    double zOrder = 0;
    Renderer3DPane pane;

    public PointView(Point3D point, Renderer3DPane pane, Color color) {
        this.point = point;
        this.pane = pane;
        double[] p = this.getPointProjected();
        this.pointDisplay = new Circle(p[0], p[1], 4, color);
        pane.getChildren().add(this.pointDisplay);
    }

    public PointView(Point3D point, Renderer3DPane pane) {
        this.point = point;
        this.pane = pane;
        double[] p = this.getPointProjected();
        this.pointDisplay = new Circle(p[0], p[1], 4, Color.color(Math.random()/3, Math.random()/3, Math.random()/3,0.7));
        pane.getChildren().add(this.pointDisplay);
    }

    public Point3D getPoint() {
        return point;
    }
    public Circle getPointDisplay() {
        return pointDisplay;
    }
    public double getzOrder() {
        return zOrder;
    }
    public void setzOrder(double zOrder) {
        this.zOrder = zOrder;
    }
    public double[] getPointProjected() {
        return pane.calculateProjectedPosition(this.point);
    }
    public void update() {
        double[] p = this.getPointProjected();
        this.pointDisplay.setCenterX(p[0]);
        this.pointDisplay.setCenterY(p[1]);
    }
}   
