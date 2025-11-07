package com.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import com.model.Graph3D.Point3D;
import com.model.Graph3D.Polygon3D;
import com.view.colours.RandomColourPicker;
import com.view.colours.RendererColourPicker;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Renderer2DProjection extends Pane {
    private final Canvas canvas = new Canvas();
    RendererColourPicker rendererColourPicker = new RandomColourPicker();
    // index mapping for fast access
    private Point3D[] pointsArray;
    private Map<Point3D, Integer> pointIndexMap = new HashMap<>();
    private Map<Integer,Color> pointIndexColorMap = new HashMap<>();
    // cells store indices to pointsArray
    private List<int[]> cellPointIndexLists = new ArrayList<>();
    private Map<Integer,Color> cellIndexColorMap = new HashMap<>();
    // arrays for projected results (x,y)
    private double[] projX;
    private double[] projY;

    // graph
    List<Point3D> points;
    List<? extends Polygon3D> cells;

    // relative horizontal position
    double yaw = 0;

    // Executor for optional parallel computation (uses common pool)
    private final ExecutorService exec = ForkJoinPool.commonPool();

    public Renderer2DProjection() {
        super();
        getChildren().add(canvas);
    }

    public void initialize(List<Point3D> points, List<? extends Polygon3D> cells) {
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

    public double getYaw() {
        return yaw;
    }
    public void setYaw(double yaw) {
        this.yaw = yaw;
    }
    public void setRendererColourPicker(RendererColourPicker rendererColourPicker) {
        this.rendererColourPicker = rendererColourPicker;
    }

    public void updateAll() {
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

        // draw on FX thread
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            // Clear
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());


            boolean[] drawn = new boolean[pointsArray.length];
            for (int cellIndex = 0; cellIndex < cells.size(); cellIndex++) {
                int[] pointIndexes = cellPointIndexLists.get(cellIndex);
                // skip invalid
                if (pointIndexes.length == 0) continue;
                
                // build polygon arrays
                int valid = 0;
                for (int pointIndex : pointIndexes) if (pointIndex >= 0) valid++;
                if (valid < 3) continue; // cannot draw polygon


                // get difference between leftmost and rightmost point
                double minX = 999999;
                double maxX = 0;
                for(int pointIndex : pointIndexes) {
                    minX = Math.min(minX,projX[pointIndex]);
                    maxX = Math.max(maxX,projX[pointIndex]);
                }

                // determine if the polygon was split by the 2D map's edges (wraparound)
                if(maxX - minX > viewport[2]/2) {
                    // draw polygons for the left edge and the right edge
                    drawPolygon(gc,cellIndex,valid,pointIndexes,viewport,true,false);
                    drawPolygon(gc,cellIndex,valid,pointIndexes,viewport,false,true);
                }
                else {
                    // draw the full polygon normally
                    drawPolygon(gc,cellIndex,valid,pointIndexes,viewport,false,false);
                }

                // draw points comprising the cell
                for (int pointIndex : pointIndexes) {
                    // if (pointIndex < 0 || drawn[pointIndex]) continue;
                    double x = projX[pointIndex];
                    double y = projY[pointIndex];
                    if (!Double.isFinite(x) || !Double.isFinite(y)) continue;
                    final int r = 3;
                    if(!pointIndexColorMap.containsKey(pointIndex)) {
                        pointIndexColorMap.put(pointIndex, rendererColourPicker.getPointColor(points.get(pointIndex)));
                    }
                    gc.setFill(pointIndexColorMap.get(pointIndex));
                    gc.fillOval(x - r, y - r, 2*r, 2*r);
                    drawn[pointIndex] = true;
                }
            }
        });
    }

    private void drawPolygon(GraphicsContext gc, int cellIndex, int valid, int[] pointIndexes, double[] viewport, boolean leftSide, boolean rightSide) {
        double[] xs = new double[valid];
        double[] ys = new double[valid];
        int p = 0;
        for (int pointIndex : pointIndexes) {
            if (pointIndex < 0) continue;
            if(leftSide && projX[pointIndex] > viewport[0] + (viewport[2]/2)) {
                xs[p] = viewport[0];
            }
            else if(rightSide && projX[pointIndex] < viewport[0] + (viewport[2]/2)) {
                xs[p] = viewport[0] + viewport[2];
            }
            else {
                xs[p] = projX[pointIndex];
            }
            ys[p] = projY[pointIndex];
            p++;
        }

        // random color per cell
        // gc.setFill(Color.hsb((cellIndex * 47) % 360, 0.4, 0.95, 1.0));
        if(!cellIndexColorMap.containsKey(cellIndex)) {
            cellIndexColorMap.put(cellIndex, rendererColourPicker.getPolygonColor(cells.get(cellIndex)));
        }
        gc.setFill(cellIndexColorMap.get(cellIndex));
        gc.fillPolygon(xs, ys, xs.length);
        gc.setStroke(Color.BLACK);
        gc.strokePolygon(xs, ys, xs.length);
    }

    private void projectPointToArrays(Point3D point3d, double[] viewport, int pointIndex) {
        double x = point3d.getX(); double y = point3d.getY(); double z = point3d.getZ();

        // normalize
        double r = Math.sqrt(x*x + y*y + z*z);
        if (r == 0) throw new IllegalArgumentException("zero vector");
        x /= r; y /= r; z /= r;


        double phi = Math.asin(y); // latitude
        double lambda = Math.atan2(z, x);
        double lambdaRel = wrapPi(lambda - this.yaw);

        // normalize to [0,1]
        double u = (lambdaRel + Math.PI) / (2*Math.PI);  
        double v = (Math.PI/2 - phi) / Math.PI;  
        
        // normalize to screen size
        projX[pointIndex] = viewport[0] + (u * viewport[2]);
        projY[pointIndex] = viewport[1] + ((1 - v)/2 * viewport[2]);
    }

    public static double wrapPi(double a) {
        while (a <= -Math.PI) a += 2*Math.PI;
        while (a > Math.PI) a -= 2*Math.PI;
        return a;
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
        // minX += width/2 * (1 - (size/width));
        minY += height/2 * (1 - (size/height)) + size/4;
        return new double[]{minX, minY, size};
    }

    // Call this to shutdown executor if needed
    public void dispose() {
        exec.shutdownNow();
    }
}
