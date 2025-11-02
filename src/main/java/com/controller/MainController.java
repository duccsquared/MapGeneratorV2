package com.controller;

import java.util.HashSet;
import java.util.Set;

import com.model.Voronoi.Voronoi3DGraph;
import com.view.Renderer3DPane;
import com.view.colours.RandomColourPicker;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class MainController {
    @FXML private Renderer3DPane rendererPane;
    @FXML private Label statusLabel;

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public void init(Scene scene) {
        rendererPane.setRendererColourPicker(new RandomColourPicker());
        Voronoi3DGraph voronoi3dGraph = new Voronoi3DGraph();
        rendererPane.initialize(voronoi3dGraph.getVoronoiPoints(),voronoi3dGraph.getVoronoiCells());
        this.initializeKeyTracking(scene);
    }

    public void initializeKeyTracking(Scene scene) {

        AnimationTimer loop = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 8_000_000) { // ~30 FPS
                    handleKeys();
                    lastUpdate = now;
                }
            }
        };

        rendererPane.setFocusTraversable(true);
        rendererPane.requestFocus();
        rendererPane.setOnMouseClicked(event -> rendererPane.requestFocus());
        rendererPane.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
        rendererPane.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
        rendererPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().isArrowKey()) {
                // detect key pressed vs key released
                if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                    pressedKeys.add(event.getCode());
                } else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
                    pressedKeys.remove(event.getCode());
                }
                event.consume(); // prevent focus traversal
            }
        });
        loop.start();
    }

    @FXML
    private void rotateLeft() {
        rendererPane.setYaw(rendererPane.getYaw()-0.025);
        statusLabel.setText("Rotated left");
        rendererPane.updateAll();
    }

    @FXML
    private void rotateRight() {
        rendererPane.setYaw(rendererPane.getYaw()+0.025);
        statusLabel.setText("Rotated right");
        rendererPane.updateAll();
    }

    @FXML
    private void reloadGraph() {
        Voronoi3DGraph voronoi3dGraph = new Voronoi3DGraph();
        rendererPane.initialize(voronoi3dGraph.getVoronoiPoints(),voronoi3dGraph.getVoronoiCells());
    }

    private void handleKeys() {
        boolean sphereMoved = false;

        if (pressedKeys.contains(KeyCode.LEFT)) {
            rendererPane.setYaw(rendererPane.getYaw()-0.025);
            statusLabel.setText("Rotated left");
            sphereMoved = true;
        }
        if (pressedKeys.contains(KeyCode.RIGHT)) {
            rendererPane.setYaw(rendererPane.getYaw()+0.025);
            statusLabel.setText("Rotated right");
            sphereMoved = true;
        }
        if (pressedKeys.contains(KeyCode.UP)) {
            rendererPane.setPitch(rendererPane.getPitch()+0.025);
            statusLabel.setText("Rotated up");
            sphereMoved = true;
        }
        if (pressedKeys.contains(KeyCode.DOWN)) {
            rendererPane.setPitch(rendererPane.getPitch()-0.025);
            statusLabel.setText("Rotated down");
            sphereMoved = true;
        }

        if (sphereMoved) {
            // System.out.printf("yaw=%.2f, pitch=%.2f%n", yaw, pitch);
            // updateAll(pointViews,cellViews);
            rendererPane.updateAll();
        }
    }


}
