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
        Point3D AB = Util.subtract(B, A);
        Point3D AC = Util.subtract(C, A);
        Point3D n = Util.cross(AB, AC);
        double n2 = Util.dot(n, n);

        if (n2 < 1e-16) {
            // degenerate - handle specially
            throw new IllegalArgumentException("Triangle points are collinear; circumcenter is undefined.");
        }

        double ab2 = Util.dot(AB, AB);
        double ac2 = Util.dot(AC, AC);

        Point3D term1 = Util.cross(AC, n);
        term1 = Util.scale(term1, ab2);
        Point3D term2 = Util.cross(n, AB);
        term2 = Util.scale(term2, ac2);

        Point3D offset = Util.scale(Util.add(term1, term2), 1.0 / (2.0 * n2));
        Point3D center = Util.add(A, offset);
        double radius = Util.length(Util.subtract(center, A));

        // --- Step 2: Compute spherical version ---
        Point3D sphereCenter = Util.normalize(center);

        // Great-circle radius (angle between A and spherical center)
        double angular = Math.acos(Util.dot(Util.normalize(A), sphereCenter));

        // --- Step 3: Store values ---
        this.euclideanCenter = center;
        this.sphericalCenter = sphereCenter;
        this.euclideanRadius = radius;
        this.angularRadius = angular;
    }

    // --- Utility methods ---

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
