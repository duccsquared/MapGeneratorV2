package com.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.data.Point3D;
import com.data.SphericalVoronoi;
import com.data.Util;
import com.data.VoronoiCell3D;

import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;

public class Renderer3DPane extends Pane {
    // graph
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
    
    public Renderer3DPane() {
        super();
        initialize();
    }

    void initialize() {
        updateCameraCoords();
        // set graph
        SphericalVoronoi sphericalVoronoi = new SphericalVoronoi();
        this.sphericalVoronoi = sphericalVoronoi;

        // setup world axes
        worldAxes = new WorldAxes(this, 2);

        // calculate views
        for(Point3D point3d : sphericalVoronoi.getVertexes().values()) {
            PointView pointView = new PointView(point3d, this);
            pointViews.add(pointView);
            pointViewMap.put(point3d, pointView);
        }

        
        for(VoronoiCell3D cell3d: sphericalVoronoi.getCells()) {
            CellView cellView = new CellView(cell3d, this);
            cellViews.add(cellView);
        }

        // update to ensure correctness after initialization
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
        pause.setOnFinished(event -> updateAll());
        pause.play();

        // update on window size change
        this.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            updateAll();
        });

        this.localToSceneTransformProperty().addListener((obs, oldTransform, newTransform) -> {
            updateAll();
        });
    }
    
    public Map<Point3D, PointView> getPointViewMap() {
        return pointViewMap;
    }
    public double getPitch() {
        return pitch;
    }
    public void setPitch(double pitch) {
        this.pitch = pitch;
    }
    public double getYaw() {
        return yaw;
    }
    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public void updateAll() {
        updateCameraCoords();
        worldAxes.update();
        for(PointView pointView : pointViews) {
            pointView.update();
            pointView.setzOrder(Util.dot(pointView.getPoint(), cameraZ) + cameraTranslation.getZ());
        }

        for(CellView cellView : cellViews) {
            cellView.update();
        }
        // sort cellviews by z-order
        cellViews.sort((a,b) -> Double.compare(b.getzOrder(), a.getzOrder()));
        // move cell views and corresponding point views to front based on order
        for(CellView cellView : cellViews) {
            for(Point3D v : cellView.getCell().getPoints()) {
                pointViewMap.get(v).getPointDisplay().toBack();
            }
            cellView.getCellDisplay().toBack();
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

    public double[] calculateProjectedPosition(Point3D point3d) {

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
        double[] viewport = calculateViewport();
        double x = viewport[0] + (xProj + 1) / 2 * viewport[2];
        double y = viewport[1] + (1 - yProj) / 2 * viewport[2];

        return new double[]{x,y};
    }

    public double[] calculateViewport() {
        // get bounds
        Bounds b = this.localToScene(this.getLayoutBounds());
        // get base range
        double minX = b.getMinX();
        double minY = b.getMinY();
        double width = b.getWidth();
        double height = b.getHeight();
        // get de-facto size (ensures 1:1 aspect ratio)
        double size = Math.min(b.getWidth(),b.getHeight());
        // recalculate start points to center render on the pane
        minX += width/2 * (1 - (size/width));
        minY += height/2 * (1 - (size/height));

        return new double[]{minX, minY, size};
    }

}
