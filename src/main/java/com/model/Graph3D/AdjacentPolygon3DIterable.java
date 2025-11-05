package com.model.Graph3D;

import java.util.Iterator;

public class AdjacentPolygon3DIterable implements Iterable<Polygon3D> {
    private Polygon3D polygon;
    public AdjacentPolygon3DIterable(Polygon3D polygon) {
        this.polygon = polygon;
    }

    @Override
    public Iterator<Polygon3D> iterator() {
        return new AdjacentPolygon3DIterator(this.polygon);
    }
    
}


class AdjacentPolygon3DIterator implements Iterator<Polygon3D> {
    private Polygon3D polygon;
    private int edgeIndex;
    private int maxSize;

    public AdjacentPolygon3DIterator(Polygon3D polygon) {
        this.polygon = polygon;
        this.edgeIndex = 0;
        this.maxSize = this.polygon.getEdges().size();
    }

    @Override
    public boolean hasNext() {
        return this.edgeIndex < this.maxSize;
    }

    @Override
    public Polygon3D next() {
        Polygon3D nextVal = this.polygon.getEdges().get(this.edgeIndex).other(this.polygon);
        this.edgeIndex += 1;
        return nextVal;
    }
}