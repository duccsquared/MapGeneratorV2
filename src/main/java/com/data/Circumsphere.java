package com.data;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Circumsphere {
    private Point3D euclideanCenter;   // Center in 3D space
    private Point3D sphericalCenter;   // Normalized (on unit sphere)
    private double euclideanRadius;    // Euclidean radius
    private double angularRadius;      // Spherical (great-circle) radius, in radians

    public Circumsphere(Triangle3D triangle) {
        Point3D A = triangle.getP1();
        Point3D B = triangle.getP2();
        Point3D C = triangle.getP3();

        // --- Step 1: Compute Euclidean circumcenter (same as before) ---
        Point3D AB = subtract(B, A);
        Point3D AC = subtract(C, A);
        Point3D n = cross(AB, AC);
        double n2 = dot(n, n);

        if (n2 < 1e-16) {
            // degenerate - handle specially
            throw new IllegalArgumentException("Triangle points are collinear; circumcenter is undefined.");
        }

        double ab2 = dot(AB, AB);
        double ac2 = dot(AC, AC);

        Point3D term1 = cross(AC, n);
        term1 = scale(term1, ab2);
        Point3D term2 = cross(n, AB);
        term2 = scale(term2, ac2);

        Point3D offset = scale(add(term1, term2), 1.0 / (2.0 * n2));
        Point3D center = add(A, offset);
        double radius = length(subtract(center, A));

        // --- Step 2: Compute spherical version ---
        Point3D sphereCenter = normalize(center);

        // Great-circle radius (angle between A and spherical center)
        double angular = Math.acos(dot(normalize(A), sphereCenter));

        // --- Step 3: Store values ---
        this.euclideanCenter = center;
        this.sphericalCenter = sphereCenter;
        this.euclideanRadius = radius;
        this.angularRadius = angular;
    }

    // --- Utility methods ---

    private static Point3D subtract(Point3D p1, Point3D p2) {
        return new Point3D(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
    }

    private static Point3D add(Point3D p1, Point3D p2) {
        return new Point3D(p1.x + p2.x, p1.y + p2.y, p1.z + p2.z);
    }

    private static Point3D cross(Point3D a, Point3D b) {
        return new Point3D(
            a.y*b.z - a.z*b.y,
            a.z*b.x - a.x*b.z,
            a.x*b.y - a.y*b.x
        );
    }

    private static double dot(Point3D a, Point3D b) {
        return a.x*b.x + a.y*b.y + a.z*b.z;
    }

    private static Point3D scale(Point3D p, double s) {
        return new Point3D(p.x*s, p.y*s, p.z*s);
    }

    private static double length(Point3D p) {
        return Math.sqrt(p.x*p.x + p.y*p.y + p.z*p.z);
    }

    private static Point3D normalize(Point3D p) {
        double len = length(p);
        return new Point3D(p.x/len, p.y/len, p.z/len);
    }

    @Override
    public String toString() {
        return String.format(
            "Circumsphere:\n  Euclidean center: %s\n  Euclidean radius: %.6f\n  " +
            "Spherical center: %s\n  Angular radius (rad): %.6f\n",
            euclideanCenter.toString(),
            euclideanRadius,
            sphericalCenter.toString(),
            angularRadius
        );
    }
}
