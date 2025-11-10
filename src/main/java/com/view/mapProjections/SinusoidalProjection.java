package com.view.mapProjections;

public class SinusoidalProjection extends MapProjection {
    public double getX(double latVal, double longVal) {
        double x = longVal * Math.cos(latVal);
        return (x + Math.PI) / (2*Math.PI);
    }
    
    public double getY(double latVal, double longVal) {
        double y = latVal;
        return (Math.PI/2 + y) / Math.PI; 
    }
}
