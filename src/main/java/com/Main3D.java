package com;

import com.data.SphericalVoronoi;
import com.data.VoronoiCell3D;
// import com.data.Edge3D;
import com.data.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

import com.data.Point3D;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
// import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
// import javafx.scene.Scene;
import javafx.stage.Stage;
// import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

public class Main3D extends Application {

    static double yaw = 0;
    static double pitch = 0;
    private List<PointView> pointViews;
    private List<CellView> cellViews;
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    
    @Override
    public void start(Stage stage) {
        Pane pane = new Pane();

        // calculate delaunay and voronoi graphs
        SphericalVoronoi sphericalVoronoi = new SphericalVoronoi();

        cellViews = new ArrayList<>();
        for(VoronoiCell3D cell: sphericalVoronoi.getCells()) {
            cellViews.add(new CellView(cell, pane));
        }

        Map<Point3D,PointView> pointViewMap = new HashMap<>();
        pointViews = new ArrayList<>();
        for(Point3D point: sphericalVoronoi.getVertexes().values()) {
            PointView pointView = new PointView(point,pane);
            pointViewMap.put(point, pointView);
            pointViews.add(pointView);
        }

        // for(Point3D point: sphericalVoronoi.getDelaunayVertexes()) {
        //     PointView pointView = new PointView(point,pane);
        //     pointViews.add(pointView);
        //     pointViewMap.put(point, pointView);
        // }

        // scene
        Scene scene = new Scene(pane, 600, 400);
        stage.setScene(scene);
        stage.setTitle("JavaFX App");


        // Track pressed keys
        scene.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));

        // Run a tracking loop to check key states
        AnimationTimer loop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 8_000_000) { // 30 FPS
                    handleKeys();
                    lastUpdate = now;
                }
            }
        };
        loop.start();

        stage.show();
    }

    private void handleKeys() {
        boolean sphereMoved = false;

        if (pressedKeys.contains(KeyCode.LEFT)) {
            yaw -= 0.025;
            sphereMoved = true;
        }
        if (pressedKeys.contains(KeyCode.RIGHT)) {
            yaw += 0.025;
            sphereMoved = true;
        }
        if (pressedKeys.contains(KeyCode.UP)) {
            pitch += 0.025;
            sphereMoved = true;
        }
        if (pressedKeys.contains(KeyCode.DOWN)) {
            pitch -= 0.025;
            sphereMoved = true;
        }

        if (sphereMoved) {
            System.out.printf("yaw=%.2f, pitch=%.2f%n", yaw, pitch);
            updateAll(pointViews,cellViews);
        }
    }

    public static void updateAll(List<PointView> pointViews, List<CellView> cellViews) {
        for(PointView pointView: pointViews) {
            pointView.update();
        }
        for(CellView cellView: cellViews) {
            cellView.update();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Projects 3D point into 2D screen space with simple perspective
    static double[] projectTo2D_Perspective(Point3D p, double width, double height) {
        double cameraZ = 3; // camera distance > 1
        double fov = 400;
        Point3D rotatedPoint = Util.rotateX(Util.rotateZ(p, yaw), pitch);

        // perspective division
        double x2d = width / 2.0 + fov * (rotatedPoint.x / (cameraZ - rotatedPoint.z));
        double y2d = height / 2.0 - fov * (rotatedPoint.y / (cameraZ - rotatedPoint.z));

        return new double[]{x2d, y2d};
    }

    // Projects 3D point on unit sphere into equirectangular 2D coordinates
    static double[] projectTo2D_Equirect(Point3D p, double width, double height) {
        double lon = Math.atan2(p.y, p.x); // [-π, π]
        double lat = Math.asin(p.z);       // [-π/2, π/2]

        double x = (lon + Math.PI) / (2 * Math.PI) * width;
        double y = (Math.PI/2 - lat) / Math.PI * height;

        return new double[]{x, y};
    }

    class PointView {
        Point3D point;
        Circle pointDisplay;

        public PointView(Point3D point, Pane pane, Color color) {
            this.point = point;
            double[] p = this.getPointProjected();
            this.pointDisplay = new Circle(p[0], p[1], 4, color);
            pane.getChildren().add(this.pointDisplay);
        }

        public PointView(Point3D point, Pane pane) {
            this.point = point;
            double[] p = this.getPointProjected();
            this.pointDisplay = new Circle(p[0], p[1], 4, Color.color(Math.random()/3, Math.random()/3, Math.random()/3,0.7));
            pane.getChildren().add(this.pointDisplay);
        }

        public Point3D getPoint() {
            return point;
        }
        public double[] getPointProjected() {
            return projectTo2D_Perspective(this.point,600,400);
        }
        public void update() {
            double[] p = this.getPointProjected();
            this.pointDisplay.setCenterX(p[0]);
            this.pointDisplay.setCenterY(p[1]);
        }
    }   

    class CellView {
        VoronoiCell3D cell;
        Polygon cellDisplay;
        public CellView(VoronoiCell3D cell, Pane pane) {
            this.cell = cell;
            this.cellDisplay = new Polygon();
            for (Point3D v : cell.getPoints()) {
                double[] xy = projectTo2D_Perspective(v, 600, 400);
                // coordsList.add(xy);
                cellDisplay.getPoints().addAll(xy[0],xy[1]);
            }
            this.cellDisplay.setFill(Color.color(Math.random(), Math.random(), Math.random(), 0.4));
            this.cellDisplay.setStroke(Color.BLACK);
            pane.getChildren().add(this.cellDisplay);
        }

        public VoronoiCell3D getCell() {
            return cell;
        }
        public void update() {
            // remove points from cellDisplay and replace them with updated points
            this.cellDisplay.getPoints().clear();
            for (Point3D v : cell.getPoints()) {
                double[] xy = projectTo2D_Perspective(v, 600, 400);
                this.cellDisplay.getPoints().addAll(xy[0],xy[1]);
            }
        }

    }
}