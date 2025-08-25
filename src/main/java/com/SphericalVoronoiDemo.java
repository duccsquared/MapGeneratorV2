package com;

import java.util.*;
import java.lang.Math;

import com.github.quickhull3d.Point3d;
import com.github.quickhull3d.QuickHull3D;

public class SphericalVoronoiDemo {

    // --- Basic Point Structures ---
    static class Point3D {
        double x, y, z;
        Point3D(double x, double y, double z) {
            this.x = x; this.y = y; this.z = z;
        }
        @Override
        public String toString() {
            return String.format("(%.3f, %.3f, %.3f)", x, y, z);
        }

        @Override
        public boolean equals (Object other) {
            if(other instanceof Point3D) {
                Point3D pOther = (Point3D) other;
                return nearlyEqual(this.x, pOther.x, 1e-6) && nearlyEqual(this.y, pOther.y, 1e-6) && nearlyEqual(this.z, pOther.z, 1e-6);
            }
            else {
                return false;
            }
        }
    }

    static class Triangle3D {
        Point3D a, b, c;
        Triangle3D(Point3D a, Point3D b, Point3D c) {
            this.a = a; this.b = b; this.c = c;
        }
    }

    static class VoronoiEdge {
        Point3D v1, v2;
        VoronoiEdge(Point3D v1, Point3D v2) {
            this.v1 = v1; this.v2 = v2;
        }
        @Override
        public String toString() {
            return String.format("VoronoiEdge %s -> %s", v1, v2);
        }

        @Override 
        public boolean equals(Object other) {
            if(other instanceof VoronoiEdge) {
                VoronoiEdge vOther = (VoronoiEdge) other;
                return (this.v1.equals(vOther.v1) && this.v2.equals(vOther.v2)) || (this.v1.equals(vOther.v2) && this.v2.equals(vOther.v1));
            }
            else {
                return false;
            }
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

    public static void main(String[] args) {
        // 1. Example points (lat, lon)
        double[][] latLonPoints = {
            {0, 0}, {0, 90}, {0, 180}, {0, -90},
            {45, 45}, {-45, -45}, {60, 120}, {-60, -120}
        };

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

        // 8. build voronoi circumcenters
        List<VoronoiEdge> voronoiEdges = new ArrayList<>();

        for (Map.Entry<EdgeKey, List<Integer>> entry : edgeToFaces.entrySet()) {
            List<Integer> faces = entry.getValue();
            if (faces.size() == 2) {
                Point3D c1 = faceCircumcenters.get(faces.get(0));
                Point3D c2 = faceCircumcenters.get(faces.get(1));
                if (c1.equals(c2)) continue;
                VoronoiEdge edge = new VoronoiEdge(c1, c2);
                if(voronoiEdges.contains(edge)) continue;
                voronoiEdges.add(edge);
            }
        }

        System.out.println("\nVoronoi Edges:");
        for (VoronoiEdge e : voronoiEdges) {
            System.out.println(e);
        }
        System.out.println("Count: " + voronoiEdges.size());
    }
}
