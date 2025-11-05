package com.model.Voronoi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.model.Graph2D.Edge;
import com.model.Graph2D.Point;
import com.model.Graph2D.Triangle;
import com.model.Graph3D.CellFactory;
import com.model.Graph3D.Circumsphere;
import com.model.Graph3D.Edge3D;
import com.model.Graph3D.Point3D;
import com.model.Graph3D.PointFactory;
import com.model.Graph3D.Polygon3D;
import com.model.Graph3D.Triangle3D;
import com.model.Util.EdgeKey;
import com.model.Util.Util;

public class Voronoi3DGraph<P extends Point3D, C extends Polygon3D> {

    // --- data ---
    private PointFactory<P> pointFactory;
    private CellFactory<C> cellFactory;

    private List<Triangle3D> triangles = new ArrayList<>();
    private List<Point3D> delaunayPoints = new ArrayList<>();
    private List<C> voronoiCells = new ArrayList<>();
    private List<Edge3D> voronoiEdges = new ArrayList<>();
    private List<P> voronoiPoints = new ArrayList<>();

    public List<Triangle3D> getTriangles() {return triangles;}
    public List<Point3D> getDelaunayPoints() {return delaunayPoints;}
    public List<C> getVoronoiCells() {return voronoiCells;}
    public List<Edge3D> getVoronoiEdges() {return voronoiEdges;}
    public List<P> getVoronoiPoints() {return voronoiPoints;}

    // --- Convert Lat/Lon to Cartesian ---
    static Point3D latLonToCartesian(double latDeg, double lonDeg) {
        double lat = Math.toRadians(latDeg);
        double lon = Math.toRadians(lonDeg);
        double x = Math.cos(lat) * Math.cos(lon);
        double z = Math.cos(lat) * Math.sin(lon);
        double y = Math.sin(lat);
        return new Point3D(x, y, z);
    }
    // --- project from 3D to 2D ---

    static Point projectTo2D(Point3D point3d) {
        double denom = 1.0 - point3d.z;
        if (Math.abs(denom) < 1e-12) {
            // point too close to north pole: either rotate dataset first, or perturb slightly
            throw new IllegalArgumentException("Point too close to projection pole: " + point3d);
        }
        return new Point(point3d.x / denom, point3d.y / denom);
    }

    // --- inverse project from 2D to 3D ---

    static Point3D projectTo3D(Point point) {
        double px = point.getX();
        double pz = point.getY();
        double denom = 1 + px * px + pz * pz;
        double x = 2 * px / denom;
        double z = 2 * pz / denom;
        double y = (-1 + px * px + pz * pz) / denom;
        return new Point3D(x, y, z);

    }

    // Projects circumcenter into tangent plane and computes atan2 angle
    private static double atan2AroundSite(Point3D site, Point3D e1, Point3D e2, Point3D c) {
        // Project c onto tangent plane at site:
        Point3D cNorm = Util.normalize(c);
        // Remove radial component along site
        double radial = Util.dot(cNorm, site);
        Point3D proj = new Point3D(cNorm.x - radial*site.x,
                                cNorm.y - radial*site.y,
                                cNorm.z - radial*site.z);
        double projLen = Util.length(proj);
        if (projLen < 1e-12) {
            // Degenerate: circumcenter lies (almost) on the site direction.
            return 0.0;
        }
        Point3D p = new Point3D(proj.x / projLen, proj.y / projLen, proj.z / projLen);
        double x = Util.dot(p, e1);
        double y = Util.dot(p, e2);
        return Math.atan2(y, x);
    }


    public static List<Point3D> sortAroundSite(Point3D site, Collection<Point3D> circumcenters) {
        // Basis for tangent plane
        Point3D north = new Point3D(0,1,0);
        Point3D u = Util.cross(north, site);
        if (Util.length(u) < 1e-8) { // site near north pole
            north = new Point3D(1, 0, 0); // pick a different north pole
            u = Util.cross(north, site);
        }
        Point3D e1 = Util.normalize(u);
        Point3D e2 = Util.normalize(Util.cross(site, e1)); // guaranteed orthogonal

        List<Point3D> sorted = new ArrayList<>(circumcenters);
        sorted.sort((p1, p2) -> {
            double a1 = atan2AroundSite(site, e1, e2, p1);
            double a2 = atan2AroundSite(site, e1, e2, p2);
            return Double.compare(a1, a2);
        });
        return sorted;
    }

    public Voronoi3DGraph(PointFactory<P> pointFactory, CellFactory<C> cellFactory) {
        this.pointFactory = pointFactory;
        this.cellFactory = cellFactory;
        this.calculateVoronoi();
    }

    @SuppressWarnings("unchecked")
    public Voronoi3DGraph() {
        this((x, y, z) -> (P) new Point3D(x, y, z),
            (x, y) -> (C) new Polygon3D(x,y));
    }

    public void calculateVoronoi() {

        // --- 1. generate points ---

        ArrayList<Point3D> samplePoints = new ArrayList<>();
        // for(int i = 0; i < 200; i++) {
        // samplePoints.add(Util.getRandomLatLongPoint());
        // }

        double goldenRatio = (1 + Math.pow(5,0.5))/2.0;
        double n = 500;
        double u = 0;
        double v = 0.98;
        for (int i = 0; i < n; i++) {
            samplePoints.add(Util.getPointByLatLong(u + 3*Util.randRange(-0.98/n, 0.98/n), v + 3*Util.randRange(-0.98/n, 0.98/n)));
            v -= 0.98/n;
            u += 1/goldenRatio;
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
        HashMap<Integer, Point3D> projectedPoints = new HashMap<>();
        List<Point3D> pointList = new ArrayList<>();

        for (Triangle tri : delaunayGraph.getTriangles()) {
            Point3D a;
            Point3D b;
            Point3D c;
            // point 1
            if (projectedPoints.containsKey(tri.getP1().getId())) {
                a = projectedPoints.get(tri.getP1().getId());
            } else {
                a = projectTo3D(tri.getP1());
                projectedPoints.put(tri.getP1().getId(), a);
                pointList.add(a);
            }
            // point 2
            if (projectedPoints.containsKey(tri.getP2().getId())) {
                b = projectedPoints.get(tri.getP2().getId());
            } else {
                b = projectTo3D(tri.getP2());
                projectedPoints.put(tri.getP2().getId(), b);
                pointList.add(b);
            }
            // point 3
            if (projectedPoints.containsKey(tri.getP3().getId())) {
                c = projectedPoints.get(tri.getP3().getId());
            } else {
                c = projectTo3D(tri.getP3());
                projectedPoints.put(tri.getP3().getId(), c);
                pointList.add(c);
            }
            delaunayTriangles.add(new Triangle3D(a, b, c));
        }

        // --- 5. connect north pole ---

        // get planar hull edges
        Map<EdgeKey,Edge> planarHullEdgesMap = new HashMap<>();
        for (Edge edge : delaunayGraph.getEdges()) {
            // hull edges are those with only one adjacent triangle
            if (edge.getPolygons().size() == 1) {
                planarHullEdgesMap.put(edge.getEdgeKey(), edge);
            }
        }
        List<Edge> planarHullEdges = new ArrayList<>(planarHullEdgesMap.values());
        // create triangles
        Point3D northPole = new Point3D(0, 1, 0);
        pointList.add(northPole);
        for (Edge edge : planarHullEdges) {
            Point3D a;
            Point3D b;
            // point 1
            if (projectedPoints.containsKey(edge.getP1().getId())) {
                a = projectedPoints.get(edge.getP1().getId());
            } else {
                a = projectTo3D(edge.getP1());
                projectedPoints.put(edge.getP1().getId(), a);
                pointList.add(a);
            }
            // point 2
            if (projectedPoints.containsKey(edge.getP2().getId())) {
                b = projectedPoints.get(edge.getP2().getId());
            } else {
                b = projectTo3D(edge.getP2());
                projectedPoints.put(edge.getP2().getId(), b);
                pointList.add(b);
            }
            delaunayTriangles.add(new Triangle3D(northPole, a, b));
        }

        // --- 6. calculate circumcircles for all triangles ---
        ArrayList<P> voronoiPoints = new ArrayList<>();
        for (Triangle3D triangle : delaunayTriangles) {
            if (triangle.getCenter() == null) {
                Circumsphere circumsphere = new Circumsphere(triangle);
                P point = this.pointFactory.create(circumsphere.getSphericalCenter().getX(), circumsphere.getSphericalCenter().getY(), circumsphere.getSphericalCenter().getZ());
                triangle.setCenter(point);
                voronoiPoints.add(point);
            }
            else {
                throw new IllegalStateException("Triangle centers should be null");
            }
            
        }

        // --- 7. create Voronoi edges ---
        List<C> cells = new ArrayList<>();
        for (Point3D point : pointList) {
            // cell variables
            Set<Point3D> cellpointList = new HashSet<>();
            Map<EdgeKey,Edge3D> cellEdgeMap = new HashMap<>();
            // loop through edges connected to the point
            for (Edge3D delaunayEdge : point.getEdges()) {
                if (delaunayEdge.getPolygons().size() == 2) {
                    // connect cirumcircles together and add the points and the edge
                    List<Polygon3D> triangles = delaunayEdge.getPolygons();
                    Point3D p1 = triangles.get(0).getCenter();
                    Point3D p2 = triangles.get(1).getCenter();
                    Edge3D edge = p1.connectTo(p2);
                    cellpointList.add(p1);
                    cellpointList.add(p2);
                    cellEdgeMap.put(edge.getEdgeKey(), edge);
                    
                } else {
                    // edges are supposed to only have two triangles on each side
                    throw new IllegalStateException("edge with incorrect number of triangles found");
                }
            }

            C voronoiCell = this.cellFactory.create(new ArrayList<Point3D>(sortAroundSite(point, cellpointList)),
                    new ArrayList<Edge3D>(cellEdgeMap.values()));
            cells.add(voronoiCell);

        }

        // --- 8. sanity check --- 

        // check euler characteristic for the voronoi graph (V - E + F = 2)
        int V = voronoiPoints.size();
        int E = 0;
        for (Polygon3D cell : cells) {
            E += cell.getEdges().size();
        }
        int F = cells.size();
        E = E / 2; // each edge is counted twice
        int euler = V - E + F;
        if (euler != 2) {
            throw new IllegalStateException("Euler characteristic failed: V - E + F = " + 
            V + " - " + E + " + " + F + " = " + euler + " != 2");
        }

        // calculate euler characteristic for the delaunay graph
        V = pointList.size();
        E = 0;
        for (Point3D p : pointList) {
            E += p.getEdges().size();
        }
        F = delaunayTriangles.size();
        E = E / 2; // each edge is counted twice
        euler = V - E + F;
        if (euler != 2) {
            throw new IllegalStateException("Euler characteristic failed: V - E + F = " + 
            V + " - " + E + " + " + F + " = " + euler + " != 2");
        }

        // --- 9. save variables ---

        this.triangles = delaunayTriangles;
        this.delaunayPoints = new ArrayList<>();
        this.delaunayPoints.addAll(pointList);
        this.voronoiPoints = voronoiPoints;
        this.voronoiCells.addAll(cells);
    }
}
