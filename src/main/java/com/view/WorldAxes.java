package com.view;

import com.data.Point3D;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class WorldAxes {
        PointView origin;
        PointView x;
        Line xLine;
        PointView y;
        Line yLine;
        PointView z;
        Line zLine;

        public WorldAxes(Renderer3DPane pane, double magnitude) {
            origin = new PointView(new Point3D(0, 0, 0), pane, Color.BLACK);

            x = new PointView(new Point3D(magnitude, 0, 0), pane, Color.RED);
            y = new PointView(new Point3D(0, magnitude, 0), pane, Color.GREEN);
            z = new PointView(new Point3D(0, 0, magnitude), pane, Color.BLUE);

            xLine = new Line(origin.getPointDisplay().getCenterX(),origin.getPointDisplay().getCenterY(),x.getPointDisplay().getCenterX(),x.getPointDisplay().getCenterY());
            yLine = new Line(origin.getPointDisplay().getCenterX(),origin.getPointDisplay().getCenterY(),y.getPointDisplay().getCenterX(),y.getPointDisplay().getCenterY());
            zLine = new Line(origin.getPointDisplay().getCenterX(),origin.getPointDisplay().getCenterY(),z.getPointDisplay().getCenterX(),z.getPointDisplay().getCenterY());
            
            xLine.setStroke(Color.RED);
            yLine.setStroke(Color.GREEN);
            zLine.setStroke(Color.BLUE);

            pane.getChildren().addAll(xLine,yLine,zLine);
        }

        public void update() {
            origin.update();
            x.update();
            y.update();
            z.update();
            // update xLine, yLine, and zLine
            xLine.setStartX(origin.getPointDisplay().getCenterX());
            xLine.setStartY(origin.getPointDisplay().getCenterY());
            xLine.setEndX(x.getPointDisplay().getCenterX());
            xLine.setEndY(x.getPointDisplay().getCenterY());

            yLine.setStartX(origin.getPointDisplay().getCenterX());
            yLine.setStartY(origin.getPointDisplay().getCenterY());
            yLine.setEndX(y.getPointDisplay().getCenterX());
            yLine.setEndY(y.getPointDisplay().getCenterY());

            zLine.setStartX(origin.getPointDisplay().getCenterX());
            zLine.setStartY(origin.getPointDisplay().getCenterY());
            zLine.setEndX(z.getPointDisplay().getCenterX());
            zLine.setEndY(z.getPointDisplay().getCenterY());
        }
    }
