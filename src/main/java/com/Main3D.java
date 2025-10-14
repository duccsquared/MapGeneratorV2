package com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.data.Point3D;
import com.data.SphericalVoronoi;
import com.data.Util;
import com.data.VoronoiCell3D;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

public class Main3D extends Application {

    SphericalVoronoi sphericalVoronoi;
    // point views
    List<PointView> pointViews = new ArrayList<>();
    Map<Point3D,PointView> pointViewMap = new HashMap<>();
    // cell views
    List<CellView> cellViews = new ArrayList<>();
    // location of camera in the worldspace
    Point3D cameraPosition = new Point3D(0,0,4);
    // location the camera is looking at
    Point3D cameraTarget = new Point3D(0,0,0);
    // up direction
    Point3D upDir = new Point3D(0, 1, 0);
    // camera directions
    Point3D cameraX; Point3D cameraY; Point3D cameraZ; Point3D cameraTranslation;
    // camera rotations relative to starting location
    double yaw = 0; double pitch = 0;
    // World Axes
    WorldAxes worldAxes;

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    @Override
    public void start(Stage stage) throws Exception {
        Pane pane = new Pane();

        updateCameraCoords();
        // calculate delaunay and voronoi graphs
        sphericalVoronoi = new SphericalVoronoi();

        // setup world axes
        worldAxes = new WorldAxes(pane, 2);

        // calculate views
        for(Point3D point3d : sphericalVoronoi.getVertexes().values()) {
            PointView pointView = new PointView(point3d, pane);
            pointViews.add(pointView);
            pointViewMap.put(point3d, pointView);
        }

        
        for(VoronoiCell3D cell3d: sphericalVoronoi.getCells()) {
            CellView cellView = new CellView(cell3d, pane);
            cellViews.add(cellView);
        }

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
            // System.out.printf("yaw=%.2f, pitch=%.2f%n", yaw, pitch);
            // updateAll(pointViews,cellViews);
            updateAll();
        }
    }

    void updateAll() {
        updateCameraCoords();
        worldAxes.update();
        for(PointView pointView : pointViews) {
            pointView.update();
        }

        for(CellView cellView : cellViews) {
            cellView.update();
        }
    }

    void updateCameraCoords() {
        // clamp pitch
        pitch = Math.max(-Math.PI / 2 + 0.01, Math.min(Math.PI / 2 - 0.01, pitch));

        // Distance from camera to target (orbit radius)
        double radius = Util.length(Util.subtract(cameraPosition, cameraTarget));

        // \new camera position based on yaw and pitch
        double x = radius * Math.cos(pitch) * Math.sin(yaw);
        double y = radius * Math.sin(pitch);
        double z = radius * Math.cos(pitch) * Math.cos(yaw);

        Point3D adjustedCameraPosition = new Point3D(x, y, z);

        // calculate forwards
        cameraZ = Util.normalize(Util.subtract(cameraTarget, adjustedCameraPosition));
        // calculate right
        cameraX = Util.normalize(Util.cross(cameraZ, upDir));
        // calculate up
        cameraY = Util.normalize(Util.cross(cameraX, cameraZ));
        // calculate translation vector
        cameraTranslation = new Point3D(-Util.dot(cameraX,adjustedCameraPosition), -Util.dot(cameraY,adjustedCameraPosition), Util.dot(cameraZ,adjustedCameraPosition));
    }

    double[] calculateProjectedPosition(Point3D point3d) {

        // 1) view transformation
        Point3D viewAdjustedPoint = new Point3D(
            Util.dot(point3d,cameraX) + cameraTranslation.getX(),
            Util.dot(point3d,cameraY) + cameraTranslation.getY(),
            Util.dot(point3d,cameraZ) + cameraTranslation.getZ()
        );

        // 2) perspective projection
        double fovScale = 1 / Math.tan(Math.toRadians(45) / 2);
        double xProj = (viewAdjustedPoint.getX() * fovScale) / -viewAdjustedPoint.getZ();
        double yProj = (viewAdjustedPoint.getY() * fovScale) / -viewAdjustedPoint.getZ();


        // 3) viewport mapping
        double x = 100 + (xProj + 1)/2 * 400;
        double y = (1 - yProj)/2 * 400;

        return new double[]{x,y};
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
        public Circle getPointDisplay() {
            return pointDisplay;
        }
        public double[] getPointProjected() {
            return calculateProjectedPosition(this.point);
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
                double x = pointViewMap.get(v).getPointDisplay().getCenterX();
                double y = pointViewMap.get(v).getPointDisplay().getCenterY();
                // coordsList.add(xy);
                cellDisplay.getPoints().addAll(x,y);
            }
            this.cellDisplay.setFill(Color.color(Math.random(), Math.random(), Math.random(), 0.4));
            this.cellDisplay.setStroke(Color.BLACK);
            pane.getChildren().add(this.cellDisplay);
            this.cellDisplay.toBack();
        }

        public VoronoiCell3D getCell() {
            return cell;
        }
        public void update() {
            // remove points from cellDisplay and replace them with updated points
            this.cellDisplay.getPoints().clear();
            for (Point3D v : cell.getPoints()) {
                double x = pointViewMap.get(v).getPointDisplay().getCenterX();
                double y = pointViewMap.get(v).getPointDisplay().getCenterY();
                this.cellDisplay.getPoints().addAll(x,y);
            }
        }

    }

    class WorldAxes {
        PointView origin;
        PointView x;
        Line xLine;
        PointView y;
        Line yLine;
        PointView z;
        Line zLine;

        public WorldAxes(Pane pane, double magnitude) {
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
}
