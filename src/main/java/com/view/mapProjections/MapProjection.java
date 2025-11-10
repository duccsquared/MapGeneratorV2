package com.view.mapProjections;

public abstract class MapProjection {
    public abstract double getX(double latVal, double longVal);
    public abstract double getY(double latVal, double longVal);
    
    public double getLongitudinalDistance(double latVal) {
        return getX(latVal,Math.PI) - getX(latVal,-Math.PI);
    }

}