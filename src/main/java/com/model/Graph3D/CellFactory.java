package com.model.Graph3D;

import java.util.List;

@FunctionalInterface
public interface CellFactory<C extends Polygon3D>  {
    C create(List<Point3D> points, List<Edge3D> edges);
    // C create(Point3D site, List<Point3D> vertices);

}
