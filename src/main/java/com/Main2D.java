package com;

import com.model.Graph2D.Edge;
import com.model.Graph2D.Point;
import com.model.Voronoi.DelaunayGraph;
import com.model.Voronoi.VoronoiGraph;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
// import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main2D extends Application {
    @Override
    public void start(Stage stage) {
        Pane pane = new Pane();

        // calculate delaunay and voronoi graphs
        DelaunayGraph delaunayGraph = new DelaunayGraph(100, 600, 400);
        VoronoiGraph voronoiGraph = new VoronoiGraph(delaunayGraph);


        // delaunay graph
        for(Edge edge: delaunayGraph.getEdges()) {
            Line line = new Line(edge.getP1().getX(),edge.getP1().getY(),edge.getP2().getX(),edge.getP2().getY());
            line.setStroke(Color.LIGHTGRAY);
            pane.getChildren().add(line);
        }
        for(Point point: delaunayGraph.getPoints()) {
            pane.getChildren().add(new Circle(point.getX(), point.getY(), 6, Color.GRAY));
        }

        // voronoi graph
        for(Edge edge: voronoiGraph.getEdges()) {
            Line line = new Line(edge.getP1().getX(),edge.getP1().getY(),edge.getP2().getX(),edge.getP2().getY());
            line.setStroke(Color.BLACK);
            pane.getChildren().add(line);
        }
        for(Point point: voronoiGraph.getPoints()) {
            pane.getChildren().add(new Circle(point.getX(), point.getY(), 6, Color.CRIMSON));
        }

        // scene
        Scene scene = new Scene(pane, 600, 400);
        stage.setScene(scene);
        stage.setTitle("JavaFX App");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
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
