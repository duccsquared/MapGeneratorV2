package com.view.mapProjections;

public class EquirectangularProjection extends MapProjection {
    public double getX(double latVal, double longVal) {
        double x = longVal;
        return (x + Math.PI) / (2*Math.PI);
    }
    
    public double getY(double latVal, double longVal) {
        double y = latVal;
        return (Math.PI/2 + y) / Math.PI; 
    }
}
