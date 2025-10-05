package com.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SphericalVoronoi2 {

    // --- Convert Lat/Lon to Cartesian ---
    static Point3D latLonToCartesian(double latDeg, double lonDeg) {
        double lat = Math.toRadians(latDeg);
        double lon = Math.toRadians(lonDeg);
        double x = Math.cos(lat) * Math.cos(lon);
        double y = Math.cos(lat) * Math.sin(lon);
        double z = Math.sin(lat);
        return new Point3D(x, y, z);
    }
    // --- project from 3D to 2D ---

    static Point projectTo2D(Point3D point3d) {
        double denom = 1 - point3d.z;
        double px = point3d.x / denom;
        double py = point3d.y / denom;
        return new Point(px, py);
    }

    // --- inverse project from 2D to 3D ---

    static Point3D projectTo3D(Point point) {
        double px = point.getX();
        double py = point.getY();
        double denom = 1 + px * px + py * py;
        double x = 2 * px / denom;
        double y = 2 * py / denom;
        double z = (-1 + px * px + py * py) / denom;
        return new Point3D(x, y, z);

    }

    // --- Vector Utilities ---
    static Point3D cross(Point3D u, Point3D v) {
        return new Point3D(
                u.y * v.z - u.z * v.y,
                u.z * v.x - u.x * v.z,
                u.x * v.y - u.y * v.x);
    }

    private static double length(Point3D p) {
        return Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
    }

    static double dot(Point3D u, Point3D v) {
        return u.x * v.x + u.y * v.y + u.z * v.z;
    }

    static Point3D normalize(Point3D p) {
        double len = Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
        return new Point3D(p.x / len, p.y / len, p.z / len);
    }

    static boolean nearlyEqual(double a, double b, double margin) {
        return a >= b - margin && a <= b + margin;
    }

    // Projects circumcenter into tangent plane and computes atan2 angle
    private static double angleAtSite(Point3D site, Point3D u, Point3D v, Point3D c) {
        // Vector from site to circumcenter
        Point3D vec = normalize(c);
        // project into tangent plane
        double x = dot(vec, u);
        double y = dot(vec, v);
        return Math.atan2(y, x);
    }

    public static List<Point3D> sortAroundSite(Point3D site, Collection<Point3D> circumcenters) {
        // Step 1: define an orthonormal basis in the tangent plane at `site`
        Point3D north = new Point3D(0, 0, 1);
        Point3D u = cross(north, site); // first tangent axis
        final Point3D newU;
        if (length(u) < 1e-6) {
            // site is too close to north pole -> pick another reference
            north = new Point3D(0, 1, 0);
            newU = normalize(cross(north, site));
        } else {
            newU = normalize(u);
        }

        Point3D v = normalize(cross(site, u)); // second tangent axis

        // Step 2: compute angle of each circumcenter around site
        List<Point3D> sorted = new ArrayList<>(circumcenters);
        sorted.sort((p1, p2) -> {
            double a1 = angleAtSite(site, newU, v, p1);
            double a2 = angleAtSite(site, newU, v, p2);
            return Double.compare(a1, a2);
        });

        return sorted;
    }

    // --- EdgeKey ---

    static class EdgeKey {
        int u, v;

        EdgeKey(int a, int b) {
            this.u = Math.min(a, b);
            this.v = Math.max(a, b);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EdgeKey))
                return false;
            EdgeKey e = (EdgeKey) o;
            return this.u == e.u && this.v == e.v;
        }

        @Override
        public int hashCode() {
            return Objects.hash(u, v);
        }
    }

    static Point3D getPoint(double u, double v) {
        double theta = 2 * Math.PI * u; // longitude
        double phi = Math.acos(2 * v - 1); // colatitude

        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);
        return new Point3D(x, y, z);
    }

    static Point3D getRandomPoint() {
        double u = Math.random(); // in [0,1)
        double v = Math.random(); // in [0,1)
        return getPoint(u, v);
    }

    // --- data ---
    private List<Triangle3D> triangles = new ArrayList<>();
    private List<Point3D> delaunayVertexes = new ArrayList<>();
    private Map<Triangle3D, Point3D> vertexes = new HashMap<>();
    private List<Edge3D> edges = new ArrayList<>();
    private List<VoronoiCell3D> cells = new ArrayList<>();

    public List<Triangle3D> getTriangles() {
        return triangles;
    }

    public List<Point3D> getDelaunayVertexes() {
        return delaunayVertexes;
    }

    public Map<Triangle3D, Point3D> getVertexes() {
        return vertexes;
    }

    public List<Edge3D> getEdges() {
        return edges;
    }

    public List<VoronoiCell3D> getCells() {
        return cells;
    }

    public SphericalVoronoi2() {
        this.calculateVoronoi();
    }

    public void calculateVoronoi() {

        // --- 1. generate points ---

        ArrayList<Point3D> samplePoints = new ArrayList<>();
        // for(int i = 0; i < 100; i++) {
        // samplePoints.add(getRandomPoint());
        // }

        double u = 0;
        double v = 0.98;
        for (int i = 0; i < 98; i++) {
            samplePoints.add(getPoint(u, v));
            v -= 0.01;
            u += 0.12;
            if (u > 1) {
                u -= 1;
            }
        }

        // --- 2. Map spherical points to a plane ---

        ArrayList<Point> points = new ArrayList<>();

        for (Point3D point3d : samplePoints) {
            points.add(projectTo2D(point3d));
        }

        // --- 3. use 2D delaunay triangulation ---

        // calculate width and height of points
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (Point p : points) {
            if (p.getX() < minX)
                minX = p.getX();
            if (p.getX() > maxX)
                maxX = p.getX();
            if (p.getY() < minY)
                minY = p.getY();
            if (p.getY() > maxY)
                maxY = p.getY();
        }

        double width = maxX - minX;
        double height = maxY - minY;

        // run delaunay

        DelaunayGraph delaunayGraph = new DelaunayGraph(points, width, height, false);

        // --- 4. map triangulation edges back onto the sphere ---

        List<Triangle3D> delaunayTriangles = new ArrayList<>();
        HashMap<Point, Point3D> projectedPoints = new HashMap<>();
        Set<Point3D> pointSet = new HashSet<>();

        for (Triangle tri : delaunayGraph.getTriangles()) {
            Point3D a;
            Point3D b;
            Point3D c;
            // point 1
            if (projectedPoints.containsKey(tri.getP1())) {
                a = projectedPoints.get(tri.getP1());
            } else {
                a = projectTo3D(tri.getP1());
                projectedPoints.put(tri.getP1(), a);
                pointSet.add(a);
            }
            // point 2
            if (projectedPoints.containsKey(tri.getP2())) {
                b = projectedPoints.get(tri.getP2());
            } else {
                b = projectTo3D(tri.getP2());
                projectedPoints.put(tri.getP2(), b);
                pointSet.add(b);
            }
            // point 3
            if (projectedPoints.containsKey(tri.getP3())) {
                c = projectedPoints.get(tri.getP3());
            } else {
                c = projectTo3D(tri.getP3());
                projectedPoints.put(tri.getP3(), c);
                pointSet.add(c);
            }
            delaunayTriangles.add(new Triangle3D(a, b, c));
        }

        // --- 5. connect north pole ---

        // get planar hull edges
        Set<Edge> planarHullEdges = new HashSet<>();
        for (Triangle triangle : delaunayGraph.getTriangles()) {
            for (Edge edge : triangle.getEdges()) {
                // hull edges are those with only one adjacent triangle
                if (edge.getPolygons().size() == 1) {
                    planarHullEdges.add(edge);
                }
            }
        }

        // create triangles
        Point3D northPole = new Point3D(0, 0, 1);
        for (Edge edge : planarHullEdges) {
            Point3D a;
            Point3D b;
            // point 1
            if (projectedPoints.containsKey(edge.getP1())) {
                a = projectedPoints.get(edge.getP1());
            } else {
                a = projectTo3D(edge.getP1());
                projectedPoints.put(edge.getP1(), a);
                pointSet.add(a);
            }
            // point 2
            if (projectedPoints.containsKey(edge.getP2())) {
                b = projectedPoints.get(edge.getP2());
            } else {
                b = projectTo3D(edge.getP2());
                projectedPoints.put(edge.getP2(), b);
                pointSet.add(b);
            }
            delaunayTriangles.add(new Triangle3D(northPole, a, b));
        }

        // --- 6. calculate circumcircles for all triangles ---
        ArrayList<Point3D> voronoiPoints = new ArrayList<>();
        for (Triangle3D triangle : delaunayTriangles) {
            if (triangle.getCenter() == null) {
                Circumsphere circumsphere = new Circumsphere(triangle);
                triangle.setCenter(circumsphere.getSphericalCenter());
            }
            voronoiPoints.add(triangle.getCenter());
        }

        // --- 7. create Voronoi edges ---
        for (Point3D point : pointSet) {
            // cell variables
            Set<Point3D> cellPointSet = new HashSet<>();
            Set<Edge3D> cellEdgeSet = new HashSet<>();
            // loop through edges connected to the point
            for (Edge3D delaunayEdge : point.getEdges()) {
                if (delaunayEdge.getPolygons().size() == 2) {
                    // connect cirumcircles together and add the points and the edge
                    List<Polygon3D> triangles = delaunayEdge.getPolygons();
                    Point3D p1 = triangles.get(0).getCenter();
                    Point3D p2 = triangles.get(1).getCenter();
                    Edge3D edge = p1.connectTo(p2);
                    cellPointSet.add(p1);
                    cellPointSet.add(p2);
                    cellEdgeSet.add(edge);
                } else {
                    // edges are supposed to only have two triangles on each side
                    throw new IllegalArgumentException("edge with incorrect number of triangles found");
                }
            }
            VoronoiCell3D voronoiCell = new VoronoiCell3D(new ArrayList<>(sortAroundSite(point, cellPointSet)),
                    new ArrayList<>(cellEdgeSet));
            this.cells.add(voronoiCell);

        }

        // --- 8. save variables ---

        this.triangles = delaunayTriangles;
        this.delaunayVertexes = new ArrayList<>();
        this.delaunayVertexes.addAll(pointSet);
        this.vertexes = new HashMap<>();
        for (Triangle3D tri : delaunayTriangles) {
            this.vertexes.put(tri, tri.getCenter());
        }
        this.cells.addAll(cells);
    }
}
