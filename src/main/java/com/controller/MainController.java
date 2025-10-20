package com.controller;

import java.util.HashSet;
import java.util.Set;

import com.view.Renderer3DPane;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;


public class MainController {
    @FXML private Renderer3DPane rendererPane;
    @FXML private Label statusLabel;

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    // Called from Main3D after scene creation
    public void initializeKeyTracking(Scene scene) {
        scene.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));

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
