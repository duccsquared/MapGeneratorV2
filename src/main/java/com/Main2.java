package com;

import com.data.SphericalVoronoi;
import com.data.VoronoiCell3D;
import com.data.VoronoiEdge3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.data.Point3D;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
// import javafx.scene.Scene;
import javafx.stage.Stage;
// import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

public class Main2 extends Application {

    static double yaw = 0;
    static double pitch = 0;
    @Override
    public void start(Stage stage) {
        Pane pane = new Pane();

        // calculate delaunay and voronoi graphs
        SphericalVoronoi sphericalVoronoi = new SphericalVoronoi();

        List<CellView> cellViews = new ArrayList<>();
        for(VoronoiCell3D cell: sphericalVoronoi.getCells()) {
            cellViews.add(new CellView(cell, pane));
        }

        List<EdgeView> edgeViews = new ArrayList<>();
        for(VoronoiEdge3D edge: sphericalVoronoi.getEdges()) {
            edgeViews.add(new EdgeView(edge,pane));
        }

        Map<Point3D,PointView> pointViewMap = new HashMap<>();
        List<PointView> pointViews = new ArrayList<>();
        for(Point3D point: sphericalVoronoi.getVertexes().values()) {
            PointView pointView = new PointView(point,pane);
            pointViewMap.put(point, pointView);
            pointViews.add(pointView);
        }

        // for(Point3D point: sphericalVoronoi.getDelaunayVertexes()) {
        //     PointView pointView = new PointView(point,pane,Color.RED);
        //     pointViews.add(pointView);
        // }

        // scene
        Scene scene = new Scene(pane, 600, 400);
        stage.setScene(scene);
        stage.setTitle("JavaFX App");

        scene.setOnKeyPressed(event -> {
            boolean sphereMoved = false;
            if (event.getCode() == KeyCode.LEFT) {
                System.out.println("Left key pressed!");
                yaw -= 0.2;
                sphereMoved = true;
            }
            if (event.getCode() == KeyCode.RIGHT) {
                System.out.println("Right key pressed!");
                yaw += 0.2;
                sphereMoved = true;
            }
            if (event.getCode() == KeyCode.DOWN) {
                System.out.println("Down key pressed!");
                pitch -= 0.2;
                sphereMoved = true;
            } 
            if (event.getCode() == KeyCode.UP) {
                System.out.println("Up key pressed!");
                pitch += 0.2;
                sphereMoved = true;
            }

            if(sphereMoved) {
                updateAll(pointViews,edgeViews,cellViews);
            }
        });
        stage.show();
    }

    public static void updateAll(List<PointView> pointViews, List<EdgeView> edgeViews, List<CellView> cellViews) {
        for(PointView pointView: pointViews) {
            pointView.update();
        }
        for(EdgeView edgeView: edgeViews) {
            edgeView.update();
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
        Point3D rotatedPoint = rotateX(rotateZ(p, yaw), pitch);

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


    static Point3D rotateZ(Point3D p, double angle) { // yaw
        double cos = Math.cos(angle), sin = Math.sin(angle);
        double x = cos * p.x - sin * p.y;
        double y = sin * p.x + cos * p.y;
        return new Point3D(x, y, p.z);
    }

    static Point3D rotateX(Point3D p, double angle) { // pitch
        double cos = Math.cos(angle), sin = Math.sin(angle);
        double y = cos * p.y - sin * p.z;
        double z = sin * p.y + cos * p.z;
        return new Point3D(p.x, y, z);
    }

    static Point3D rotateY(Point3D p, double angle) { // roll
        double cos = Math.cos(angle), sin = Math.sin(angle);
        double x = cos * p.x + sin * p.z;
        double z = -sin * p.x + cos * p.z;
        return new Point3D(x, p.y, z);
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
    class EdgeView {
        VoronoiEdge3D edge;
        Line edgeDisplay;
        public EdgeView(VoronoiEdge3D edge, Pane pane) {
            this.edge = edge;
            double[] p1 = this.getP1Projected();
            double[] p2 = this.getP2Projected();
            this.edgeDisplay = new Line(p1[0],p1[1],p2[0],p2[1]);
            this.edgeDisplay.setStroke(Color.LIGHTGRAY);
            pane.getChildren().add(this.edgeDisplay);
        }

        public VoronoiEdge3D getEdge() {
            return edge;
        }
        public double[] getP1Projected() {
            return projectTo2D_Perspective(edge.v1, 600, 400);
        }
        public double[] getP2Projected() {
            return projectTo2D_Perspective(edge.v2, 600, 400);
        }
        public void update() {
            double[] p1 = this.getP1Projected();
            double[] p2 = this.getP2Projected();
            // update edge based on points (x,y)
            this.edgeDisplay.setStartX(p1[0]);
            this.edgeDisplay.setStartY(p1[1]);
            this.edgeDisplay.setEndX(p2[0]);
            this.edgeDisplay.setEndY(p2[1]);
        }
    }

    class CellView {
        VoronoiCell3D cell;
        Polygon cellDisplay;
        public CellView(VoronoiCell3D cell, Pane pane) {
            this.cell = cell;
            this.cellDisplay = new Polygon();
            for (Point3D v : cell.getVertices()) {
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
            for (Point3D v : cell.getVertices()) {
                double[] xy = projectTo2D_Perspective(v, 600, 400);
                this.cellDisplay.getPoints().addAll(xy[0],xy[1]);
            }
        }

    }
}