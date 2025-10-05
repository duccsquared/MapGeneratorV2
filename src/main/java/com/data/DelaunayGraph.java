package com.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.*;

@Data
public class DelaunayGraph {
    private boolean usePeriodicDuplicates;
    private double width;
    private double height;
    private List<Point> points;
    private List<Triangle> triangles;

    public DelaunayGraph(List<Point> points, double width, double height, boolean usePeriodicDuplicates) {
        // initialize variables
        this.triangles = new ArrayList<>();
        this.points = points;
        this.width = width;
        this.height = height;
        this.usePeriodicDuplicates = usePeriodicDuplicates;

        // generate delaunay triangulation
        this.generateDelaunay();
    }

    public DelaunayGraph(List<Point> points, double width, double height) {
        // initialize variables
        this.triangles = new ArrayList<>();
        this.points = points;
        this.width = width;
        this.height = height;
        this.usePeriodicDuplicates = true;

        // generate delaunay triangulation
        this.generateDelaunay();
    }

    public DelaunayGraph(int pointCount, double width, double height) {
        // initialize variables
        this.triangles = new ArrayList<>();
        this.points = new ArrayList<>();

        // padding (to avoid points right at the edge)
        int pad = 5;

        // calculate average distance
        double averageDistance = Math.pow(((width-2*pad) * (height-2*pad)) / ((double) pointCount), 0.5);

        // place points in a grid with random nudges 
        for (int x = pad; x < width - pad; x += averageDistance) {
            for (int y = pad; y < height - pad; y += averageDistance) {
                this.points.add(new Point(x + Util.randInt(-(int) averageDistance / 3, (int) averageDistance / 3),
                        y + Util.randInt(-(int) averageDistance / 3, (int) averageDistance / 3)));
            }
        }

        // set more variables
        this.width = width;
        this.height = height;
        this.usePeriodicDuplicates = true;

        // generate delaunay triangulation
        this.generateDelaunay();
    }

    public List<Edge> getEdges() {
        Set<Edge> edgeSet = new HashSet<>();
        // loop through points to get their edges
        for (Point point : this.points) {
            edgeSet.addAll(point.getEdges());
        }
        return new ArrayList<>(edgeSet);
    }

    public void generateDelaunay() {
        try {
            this.triangles.clear();

            // --- step 1: create initial super triangle ---
            Triangle superTriangle = createSuperTriangle(this.width*3, this.height*3);
            // set names for debug purposes
            superTriangle.getP1().setName("ST1");
            superTriangle.getP2().setName("ST2");
            superTriangle.getP3().setName("ST3");

            List<Triangle> triangulation = new ArrayList<>();
            triangulation.add(superTriangle);

            // --- step 2: create periodic duplicates ---
            List<Point> periodicPoints = new ArrayList<>();
            // loop through points
            if(usePeriodicDuplicates) {
                for (Point point : points) {
                    // loop through all adjacent locations
                    for (double dx : new double[] { -this.width, 0, this.width }) {
                        for (double dy : new double[] { -this.height, 0, this.height }) {
                            // use a ghost point instead of a regular point if it's not an original point
                            if (dx == 0 && dy == 0) {
                                periodicPoints.add(point);
                            } else {
                                periodicPoints.add(new GhostPoint(point, point.getX() + dx, point.getY() + dy));
                            }
                        }
                    }
                }
            }
            // duplicate points list
            else {
                periodicPoints.addAll(points);
            }
            


            // --- step 3: insert points into the graph ---
            List<Triangle> fullBadTriangles = new ArrayList<>();

            for (Point point : periodicPoints) {
                // list of bad triangles
                List<Triangle> badTriangles = new ArrayList<>();

                // - step 3a: find triangles whose circumcircle contains p -
                for (Triangle triangle : triangulation) {
                    Circumcircle circumcircle = new Circumcircle(triangle);
                    triangle.setCenter(circumcircle.getCenter());
                    // check if the triangle shouldn't exist
                    if (Util.distance(circumcircle.getCenter(), point) < circumcircle.getRadius()) {
                        badTriangles.add(triangle);
                    }

                }

                // - step 3b: find polygonal hole boundary -

                List<Edge> polygon = new ArrayList<>();
                Set<Edge> badEdges = new HashSet<>();

                // get all the edges of the bad triangles
                for (Triangle triangle : badTriangles) {
                    for (Edge edge : triangle.getEdges()) {
                        badEdges.add(edge);
                    }
                }

                // find edges that aren't used by multiple bad triangles
                // (aka they can be used to form a triangle with the current point)
                for (Edge edge : badEdges) {
                    int sharedCount = 0;
                    for (Triangle triangle : badTriangles) {
                        if (triangle.getEdges().contains(edge)) {
                            sharedCount += 1;
                        }
                    }
                    if (sharedCount <= 1) {
                        polygon.add(edge);
                    }
                }

                // get rid of bad triangles
                for (Triangle triangle : badTriangles) {
                    triangulation.remove(triangle);
                    fullBadTriangles.add(triangle);
                }

                // - step 3c: retriangulate hole with new point p -

                // create new triangles
                for (Edge edge : polygon) {
                    triangulation.add(new Triangle(edge, point));
                }
            }
            // --- step 4: remove triangles connected to the super triangle ---
            // remove bad triangles (other than the super-triangle)
            for (Triangle triangle : fullBadTriangles) {
                if (!triangle.equals(superTriangle)) {
                    triangle.delete(true, false);
                }
            }

            // variables
            List<Triangle> clearedTriangulation = new ArrayList<>();
            Set<Point> superPoints = new HashSet<>(superTriangle.getPoints());
            
            // remove triangles that use any of the super-triangle's points
            for (Triangle triangle : triangulation) {
                if (triangle.getPoints().stream().anyMatch(superPoints::contains)) {
                    triangle.delete(true, false);
                    continue;
                }
                clearedTriangulation.add(triangle);
            }

            // remove super-triangle
            superTriangle.delete(true, false);


            // --- step 5: filter triangles outside the original domain---

            if(usePeriodicDuplicates) {
                // only include triangles that include at least one regular point
                for (Triangle triangle : clearedTriangulation) {
                    int ghostCount = 0;
                    for (Point point : triangle.getPoints()) {
                        if (point instanceof GhostPoint) {
                            ghostCount += 1;
                        }
                    }
                    if (ghostCount < 3) {
                        this.triangles.add(triangle);
                    }
                }
            }
            else {
                // duplicate triangles
                this.triangles.addAll(clearedTriangulation);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Triangle createSuperTriangle(double width, double height) {
        // create a triangle large enough to fit all the points
        return new Triangle(new Point(-10 * width, -10 * height), new Point(3 * width, -10 * height),
                new Point(width / 2, 10 * height));
    }

}
