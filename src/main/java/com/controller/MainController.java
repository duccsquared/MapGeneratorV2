package com.controller;

import java.util.HashSet;
import java.util.Set;

import com.model.MapGenerator.MapGenerator;
import com.view.Renderer2DProjection;
import com.view.Renderer3DPane;
import com.view.colours.*;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class MainController {
    @FXML private Renderer3DPane rendererPane;
    @FXML private Renderer2DProjection projectionPane;
    @FXML private Label statusLabel;

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public void init(Scene scene) {
        projectionPane.setVisible(false);
        rendererPane.setVisible(true);

        MapGenerator mapGenerator = new MapGenerator();
        mapGenerator.calculateAltitudeMap();

        rendererPane.setRendererColourPicker(new AltitudeColourPicker());
        rendererPane.initialize(mapGenerator.getPoints(),mapGenerator.getCells());

        projectionPane.setRendererColourPicker(new AltitudeColourPicker());
        projectionPane.initialize(mapGenerator.getPoints(),mapGenerator.getCells());
        
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

        projectionPane.setFocusTraversable(true);
        projectionPane.requestFocus();
        projectionPane.setOnMouseClicked(event -> projectionPane.requestFocus());
        projectionPane.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
        projectionPane.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
        projectionPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
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
        projectionPane.setYaw(projectionPane.getYaw()-0.025);
        statusLabel.setText("Rotated left");
        rendererPane.updateAll();
        projectionPane.updateAll();
    }

    @FXML
    private void rotateRight() {
        rendererPane.setYaw(rendererPane.getYaw()+0.025);
        projectionPane.setYaw(projectionPane.getYaw()+0.025);
        statusLabel.setText("Rotated right");
        rendererPane.updateAll();
        projectionPane.updateAll();
    }

    @FXML
    private void reloadGraph() {
        MapGenerator mapGenerator = new MapGenerator();
        mapGenerator.calculateAltitudeMap();
        rendererPane.initialize(mapGenerator.getPoints(),mapGenerator.getCells());
        projectionPane.initialize(mapGenerator.getPoints(),mapGenerator.getCells());
    }

    @FXML
    private void view2DPane() {
        rendererPane.setVisible(false);
        projectionPane.setVisible(true);
    }

    @FXML
    private void view3DPane() {
        projectionPane.setVisible(false);
        rendererPane.setVisible(true);
    }

    private void handleKeys() {
        boolean sphereMoved = false;

        if (pressedKeys.contains(KeyCode.LEFT)) {
            rendererPane.setYaw(rendererPane.getYaw()-0.025);
            projectionPane.setYaw(projectionPane.getYaw()-0.025);
            statusLabel.setText("Rotated left");
            sphereMoved = true;
        }
        if (pressedKeys.contains(KeyCode.RIGHT)) {
            rendererPane.setYaw(rendererPane.getYaw()+0.025);
            projectionPane.setYaw(projectionPane.getYaw()+0.025);
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
            projectionPane.updateAll();
        }
    }


}
