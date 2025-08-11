package com;

import java.util.List;

import com.data.DelaunayGraph;
import com.data.Edge;
import com.data.Point;
import com.data.VoronoiGraph;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
// import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
    @Override
    public void start(Stage stage) {
        Pane pane = new Pane();

        DelaunayGraph delaunayGraph = new DelaunayGraph(100, 600, 400);
        VoronoiGraph voronoiGraph = new VoronoiGraph(delaunayGraph);


        for(Edge edge: delaunayGraph.getEdges()) {
            List<Point> pp = edge.getPointList();
            Line line = new Line(pp.get(0).getX(),pp.get(0).getY(),pp.get(1).getX(),pp.get(1).getY());
            line.setStroke(Color.LIGHTGRAY);
            pane.getChildren().add(line);
        }
        for(Point point: delaunayGraph.getPoints()) {
            pane.getChildren().add(createDraggableVertex(point.getX(), point.getY(), pane, Color.GRAY));
        }


        for(Edge edge: voronoiGraph.getEdges()) {
            List<Point> pp = edge.getPointList();
            Line line = new Line(pp.get(0).getX(),pp.get(0).getY(),pp.get(1).getX(),pp.get(1).getY());
            line.setStroke(Color.BLACK);
            pane.getChildren().add(line);
        }
        for(Point point: voronoiGraph.getPoints()) {
            pane.getChildren().add(createDraggableVertex(point.getX(), point.getY(), pane, Color.CRIMSON));
        }

        Scene scene = new Scene(pane, 600, 400);
        stage.setScene(scene);
        stage.setTitle("JavaFX App");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Circle createDraggableVertex(double x, double y, Pane pane, Color color) {
        Circle vertex = new Circle(x, y, 6, color);

        vertex.setOnMousePressed(e -> {
            vertex.setUserData(new double[]{e.getX(), e.getY()});
        });

        vertex.setOnMouseDragged(e -> {
            vertex.getUserData();
            vertex.setCenterX(e.getSceneX());
            vertex.setCenterY(e.getSceneY());
        });

        return vertex;
    }

}
