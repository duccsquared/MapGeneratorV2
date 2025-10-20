package com;


import java.util.HashSet;
import java.util.Set;

import com.data.SphericalVoronoi;
import com.view.Renderer3DPane;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Main3D extends Application {

    private Renderer3DPane pane;
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    @Override
    public void start(Stage stage) throws Exception {
        SphericalVoronoi sphericalVoronoi = new SphericalVoronoi();
        pane = new Renderer3DPane(sphericalVoronoi);

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
            pane.setYaw(pane.getYaw()-0.025);
            sphereMoved = true;
        }
        if (pressedKeys.contains(KeyCode.RIGHT)) {
            pane.setYaw(pane.getYaw()+0.025);
            sphereMoved = true;
        }
        if (pressedKeys.contains(KeyCode.UP)) {
            pane.setPitch(pane.getPitch()+0.025);
            sphereMoved = true;
        }
        if (pressedKeys.contains(KeyCode.DOWN)) {
            pane.setPitch(pane.getPitch()-0.025);
            sphereMoved = true;
        }

        if (sphereMoved) {
            // System.out.printf("yaw=%.2f, pitch=%.2f%n", yaw, pitch);
            // updateAll(pointViews,cellViews);
            pane.updateAll();
        }
    }

}
