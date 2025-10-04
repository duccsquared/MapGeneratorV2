package com.data;

import java.util.*;
import java.lang.Math;
import com.github.quickhull3d.Point3d;
import com.github.quickhull3d.QuickHull3D;

public class SphericalVoronoi {

    // --- Basic Point Structures ---

    static class Triangle3D {
        Point3D a, b, c;
        Triangle3D(Point3D a, Point3D b, Point3D c) {
            this.a = a; this.b = b; this.c = c;
        }

        public boolean includes(Point3D p) {
            return this.a.equals(p) ||  this.b.equals(p) ||  this.c.equals(p);
        }

        public List<Point3D> getOthers(Point3D p) {
            List<Point3D> points = new ArrayList<>();
            if(!this.a.equals(p)) {points.add(this.a);}
            if(!this.b.equals(p)) {points.add(this.b);}
            if(!this.c.equals(p)) {points.add(this.c);}
            return points;
        }
    }

    static class EdgeKey {
        int u, v;
        EdgeKey(int a, int b) {
            this.u = Math.min(a, b);
            this.v = Math.max(a, b);
        }
        @Override public boolean equals(Object o) {
            if (!(o instanceof EdgeKey)) return false;
            EdgeKey e = (EdgeKey)o;
            return this.u == e.u && this.v == e.v;
        }
        @Override public int hashCode() {
            return Objects.hash(u, v);
        }
    }

    // --- Vector Utilities ---
    static Point3D cross(Point3D u, Point3D v) {
        return new Point3D(
            u.y * v.z - u.z * v.y,
            u.z * v.x - u.x * v.z,
            u.x * v.y - u.y * v.x
        );
    }

    static double dot(Point3D u, Point3D v) {
        return u.x*v.x + u.y*v.y + u.z*v.z;
    }

    static Point3D normalize(Point3D p) {
        double len = Math.sqrt(p.x*p.x + p.y*p.y + p.z*p.z);
        return new Point3D(p.x/len, p.y/len, p.z/len);
    }

    static boolean nearlyEqual(double a, double b, double margin) {
        return a >= b - margin && a <= b + margin;
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
        }
        else {
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

    // Projects circumcenter into tangent plane and computes atan2 angle
    private static double angleAtSite(Point3D site, Point3D u, Point3D v, Point3D c) {
        // Vector from site to circumcenter
        Point3D vec = normalize(c);
        // project into tangent plane
        double x = dot(vec, u);
        double y = dot(vec, v);
        return Math.atan2(y, x);
    }


    private static double length(Point3D p) {
        return Math.sqrt(p.x*p.x + p.y*p.y + p.z*p.z);
    }

    // --- Convert Lat/Lon to Cartesian ---
    static Point3D latLonToCartesian(double latDeg, double lonDeg) {
        double lat = Math.toRadians(latDeg);
        double lon = Math.toRadians(lonDeg);
        double x = Math.cos(lat) * Math.cos(lon);
        double y = Math.cos(lat) * Math.sin(lon);
        double z = Math.sin(lat);
        return new Point3D(x, y, z);
    }

    // --- Circumcenter of spherical triangle ---
    static Point3D sphericalCircumcenter(Point3D a, Point3D b, Point3D c) {
        Point3D ab = cross(a, b);
        Point3D bc = cross(b, c);
        Point3D n = cross(ab, bc); // intersection
        return normalize(n);
    }

    private List<Triangle3D> triangles = new ArrayList<>();
    private List<Point3D> delaunayVertexes = new ArrayList<>();
    private Map<Triangle3D, Point3D> vertexes = new HashMap<>();
    private List<VoronoiEdge3D> edges = new ArrayList<>();
    private List<VoronoiCell3D> cells = new ArrayList<>();

    public List<Triangle3D> getTriangles() {return triangles;}
    public List<Point3D> getDelaunayVertexes() {return delaunayVertexes;}
    public Map<Triangle3D, Point3D> getVertexes() {return vertexes;}
    public List<VoronoiEdge3D> getEdges() {return edges;}
    public List<VoronoiCell3D> getCells() {return cells;}

    public SphericalVoronoi() {
        this.calculateVoronoi();
    }
    public void calculateVoronoi() {
        // 1. Example points (lat, lon)
        // double[][] latLonPoints = {
        //     {0, 0}, {0, 90}, {0, 180}, {0, -90},
        //     {45, 45}, {-45, -45}, {60, 120}, {-60, -120}
        // };

        ArrayList<double[]> latLons = new ArrayList<>();
        for(int i = 0; i < 60; i++) {
            latLons.add(new double[]{Util.randInt(-180, 180), Util.randInt(-180, 180)});
        }
        // convert double[] arraylist to double[][]
        double[][] latLonPoints = new double[latLons.size()][2];
        for (int i = 0; i < latLons.size(); i++) {
            latLonPoints[i] = latLons.get(i);
        }


        // 2. Convert to 3D
        Point3D[] points = new Point3D[latLonPoints.length];
        double[][] ptsArray = new double[latLonPoints.length][3];
        for (int i = 0; i < latLonPoints.length; i++) {
            Point3D p = latLonToCartesian(latLonPoints[i][0], latLonPoints[i][1]);
            points[i] = p;
            ptsArray[i][0] = p.x;
            ptsArray[i][1] = p.y;
            ptsArray[i][2] = p.z;
        }

        // 3. Run QuickHull3D
        Point3d[] points3d = new Point3d[points.length];
        for (int i = 0; i < points.length; i++) {
            points3d[i] = new Point3d(points[i].x, points[i].y, points[i].z);
        }
        QuickHull3D hull = new QuickHull3D();
        hull.build(points3d);
        int[][] faceIndices = hull.getFaces();

        // 4. Extract Delaunay triangles
        List<Triangle3D> delaunayTriangles = new ArrayList<>();
        for (int[] face : faceIndices) {
            System.out.println(face[0] + " " + face[1] + " " + face[2]);
            Point3D a = points[face[0]];
            Point3D b = points[face[1]];
            Point3D c = points[face[2]];
            delaunayTriangles.add(new Triangle3D(a, b, c));
        }

        System.out.println("Delaunay Triangles:");
        for (Triangle3D tri : delaunayTriangles) {
            System.out.println(tri.a + " " + tri.b + " " + tri.c);
        }

        // 5. Compute Voronoi vertices (circumcenters)
        Map<Triangle3D, Point3D> circumcenters = new HashMap<>();
        for (Triangle3D tri : delaunayTriangles) {
            Point3D center = sphericalCircumcenter(tri.a, tri.b, tri.c);
            circumcenters.put(tri, center);
        }

        System.out.println("\nVoronoi Vertices (Circumcenters):");
        for (Point3D c : circumcenters.values()) {
            System.out.println(c);
        }

        // 7. store circumcenters
        Map<Integer, Point3D> faceCircumcenters = new HashMap<>();
        for (int f = 0; f < faceIndices.length; f++) {
            int[] face = faceIndices[f];
            Point3D a = points[face[0]];
            Point3D b = points[face[1]];
            Point3D c = points[face[2]];
            faceCircumcenters.put(f, sphericalCircumcenter(a, b, c));
        }

        // 7. build an edge -> faces map
        Map<EdgeKey, List<Integer>> edgeToFaces = new HashMap<>();

        for (int f = 0; f < faceIndices.length; f++) {
            int[] face = faceIndices[f];
            for (int i = 0; i < 3; i++) {
                int a = face[i];
                int b = face[(i+1)%3];
                EdgeKey ek = new EdgeKey(a, b);
                edgeToFaces.computeIfAbsent(ek, k -> new ArrayList<>()).add(f);
            }
        }

        // 8. build voronoi edges
        List<VoronoiEdge3D> voronoiEdges = new ArrayList<>();

        for (Map.Entry<EdgeKey, List<Integer>> entry : edgeToFaces.entrySet()) {
            List<Integer> faces = entry.getValue();
            if (faces.size() == 2) {
                Point3D c1 = faceCircumcenters.get(faces.get(0));
                Point3D c2 = faceCircumcenters.get(faces.get(1));
                if (c1.equals(c2)) continue;
                VoronoiEdge3D edge = new VoronoiEdge3D(c1, c2);
                if(voronoiEdges.contains(edge)) continue;
                voronoiEdges.add(edge);
            }
        }

        // 9. build voronoi cells
        List<VoronoiCell3D> voronoiCells = new ArrayList<>();

        for (Point3D point: points) {
            Set<Point3D> cellSet = new HashSet<>();
            for (Triangle3D tri : delaunayTriangles) {
                if(tri.includes(point)) {
                    cellSet.add(circumcenters.get(tri));
                }
            }
            List<Point3D> removeList = new ArrayList<>();
            for(Point3D other: cellSet) {
                if(point.equals(other)) {
                    removeList.add(other);
                }
            }
            for(Point3D other: removeList) {
                cellSet.remove(other);
            }
            List<Point3D> cellPoints = sortAroundSite(point, cellSet);
            voronoiCells.add(new VoronoiCell3D(point, cellPoints));
        }

        System.out.println("\nVoronoi Edges:");
        for (VoronoiEdge3D e : voronoiEdges) {
            System.out.println(e);
        }
        System.out.println("Count: " + voronoiEdges.size());

        System.out.println("\nVoronoi Cells:");
        for (VoronoiCell3D cell : voronoiCells) {
            System.out.println(cell);
        }

        // save to variables
        this.triangles = delaunayTriangles;
        this.delaunayVertexes = new ArrayList<Point3D>();
        this.vertexes = circumcenters;
        this.edges = voronoiEdges;
        this.cells = voronoiCells;

        for(Point3D point : points) {
            this.delaunayVertexes.add(point);
        }
    }
}
