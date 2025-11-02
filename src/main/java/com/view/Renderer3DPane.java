package com.view;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;
import com.model.Util.Util;
import com.model.Voronoi.Voronoi3DGraph;

import javafx.application.Platform;
// import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Renderer3DPane extends Pane {
    private final Canvas canvas = new Canvas();
    // index mapping for fast access
    private Point3D[] pointsArray;
    private Map<Point3D, Integer> pointIndexMap = new HashMap<>();
    private Map<Integer,Color> pointIndexColorMap = new HashMap<>();
    // cells store indices to pointsArray
    private List<int[]> cellPointIndexLists = new ArrayList<>();
    private Map<Integer,Color> cellIndexColorMap = new HashMap<>();
    // arrays for projected results (x,y) and z-order
    private double[] projX;
    private double[] projY;
    private double[] zOrderArr;

    // graph
    List<Point3D> points;
    List<Polygon3D> cells;
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

    // Executor for optional parallel computation (uses common pool)
    private final ExecutorService exec = ForkJoinPool.commonPool();

    public Renderer3DPane() {
        super();
        getChildren().add(canvas);
        Voronoi3DGraph voronoi3dGraph = new Voronoi3DGraph();
        initialize(voronoi3dGraph.getVoronoiPoints(),voronoi3dGraph.getVoronoiCells());
    }

    public void initialize(List<Point3D> points, List<Polygon3D> cells) {
        updateCameraCoords();
        // set graph
        this.points = points;
        this.cells = cells;

        // reset variables
        pointIndexMap = new HashMap<>();
        pointIndexColorMap = new HashMap<>();
        cellPointIndexLists = new ArrayList<>();
        cellIndexColorMap = new HashMap<>();
        
        // build arrays and mappings
        pointsArray = points.toArray(new Point3D[0]);
        int n = pointsArray.length;
        projX = new double[n];
        projY = new double[n];
        zOrderArr = new double[n];

        for (int i = 0; i < n; i++) {
            pointIndexMap.put(pointsArray[i], i);
        }

        // convert cells to index arrays for cheap drawing
        for (Polygon3D cell : cells) {
            List<Point3D> point3ds = cell.getPoints();
            int[] pointIndexes = new int[point3ds.size()];
            // loop through each each point to get the corresponding ID
            for (int i = 0; i < point3ds.size(); i++) {
                Integer id = pointIndexMap.get(point3ds.get(i));
                pointIndexes[i] = (id == null) ? -1 : id;
            }
            cellPointIndexLists.add(pointIndexes);
        }

        // update to ensure correctness after initialization
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
        pause.setOnFinished(event -> updateAll());
        pause.play();

        // update on window size change
        this.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            canvas.setWidth(newBounds.getWidth());
            canvas.setHeight(newBounds.getHeight());
            updateAll();
        });
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

        // compute viewport
        double[] viewport = calculateViewport();

        // compute projection for each point (parallel when large)
        final int n = pointsArray.length;
        // choose to parallelize if array sufficiently large
        if (n > 5000) {
            IntStream.range(0, n).parallel().forEach(i -> {
                projectPointToArrays(pointsArray[i], viewport, i);
            });
        } else {
            for (int i = 0; i < n; i++) {
                projectPointToArrays(pointsArray[i], viewport, i);
            }
        }

        // compute cell z-order averages
        // (single-threaded; cheap compared to per-point work)
        final double[] cellZ = new double[cellPointIndexLists.size()];
        for (int cellIndex = 0; cellIndex < cellPointIndexLists.size(); cellIndex++) {
            int[] pointIndexes = cellPointIndexLists.get(cellIndex);
            double total = 0;
            int count = 0;
            for (int pointIndex : pointIndexes) {
                if (pointIndex >= 0) {
                    total += zOrderArr[pointIndex];
                    count++;
                }
            }
            cellZ[cellIndex] = (count > 0) ? (total / count) : Double.NEGATIVE_INFINITY;
        }

        // draw on FX thread
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            // Clear
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // draw cells sorted by z-order (back -> front)
            Integer[] order = IntStream.range(0, cellPointIndexLists.size()).boxed().toArray(Integer[]::new);
            Arrays.sort(order, Comparator.comparingDouble(i -> cellZ[i]));

            boolean[] drawn = new boolean[pointsArray.length];
            for (int cellIndex : order) {
                int[] pointIndexes = cellPointIndexLists.get(cellIndex);
                // skip invalid
                if (pointIndexes.length == 0) continue;
                
                //  backface culling
                Point3D p0 = pointsArray[pointIndexes[0]];
                Point3D p1 = pointsArray[pointIndexes[1]];
                Point3D p2 = pointsArray[pointIndexes[2]];
                Point3D u = Util.subtract(p1, p0);
                Point3D v = Util.subtract(p2, p0);
                Point3D normal = Util.cross(u, v);
                if (Util.dot(normal, cameraZ) < 0) continue; // skip backface

                // build polygon arrays
                int valid = 0;
                for (int pointIndex : pointIndexes) if (pointIndex >= 0) valid++;
                if (valid < 3) continue; // cannot draw polygon

                double[] xs = new double[valid];
                double[] ys = new double[valid];
                int p = 0;
                for (int pointIndex : pointIndexes) {
                    if (pointIndex < 0) continue;
                    xs[p] = projX[pointIndex];
                    ys[p] = projY[pointIndex];
                    p++;
                }

                // random color per cell
                // gc.setFill(Color.hsb((cellIndex * 47) % 360, 0.4, 0.95, 1.0));
                if(!cellIndexColorMap.containsKey(cellIndex)) {
                    cellIndexColorMap.put(cellIndex, Color.color(Math.random(), Math.random(), Math.random(), 1));
                }
                gc.setFill(cellIndexColorMap.get(cellIndex));
                gc.fillPolygon(xs, ys, xs.length);
                gc.setStroke(Color.BLACK);
                gc.strokePolygon(xs, ys, xs.length);

                // draw points comprising the cell
                for (int pointIndex : pointIndexes) {
                    // if (pointIndex < 0 || drawn[pointIndex]) continue;
                    double x = projX[pointIndex];
                    double y = projY[pointIndex];
                    if (!Double.isFinite(x) || !Double.isFinite(y)) continue;
                    final int r = 3;
                    if(!pointIndexColorMap.containsKey(pointIndex)) {
                        pointIndexColorMap.put(pointIndex, Color.color(Math.random()/3, Math.random()/3, Math.random()/3, 1));
                    }
                    gc.setFill(pointIndexColorMap.get(pointIndex));
                    gc.fillOval(x - r, y - r, 2*r, 2*r);
                    drawn[pointIndex] = true;
                }
            }
        });
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


    private void projectPointToArrays(Point3D point3d, double[] viewport, int pointIndex) {
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

        double x = viewport[0] + (xProj + 1) / 2 * viewport[2];
        double y = viewport[1] + (1 - yProj) / 2 * viewport[2];

        projX[pointIndex] = x;
        projY[pointIndex] = y;
        zOrderArr[pointIndex] = viewAdjustedPoint.getZ(); // use view-space z for depth (higher generally nearer)
    }

    public double[] calculateViewport() {
        // get bounds
        double minX = 0; 
        double minY = 0;
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        // get de-facto size (ensures 1:1 aspect ratio)
        double size = Math.min(width, height);
        // recalculate start points to center render on the pane
        minX += width/2 * (1 - (size/width));
        minY += height/2 * (1 - (size/height));

        return new double[]{minX, minY, size};
    }

    // Call this to shutdown executor if needed
    public void dispose() {
        exec.shutdownNow();
    }
}
