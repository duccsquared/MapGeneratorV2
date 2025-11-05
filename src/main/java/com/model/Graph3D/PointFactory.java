package com.model.Graph3D;
@FunctionalInterface
public interface PointFactory<P extends Point3D> {
    P create(double x, double y, double z);
}
