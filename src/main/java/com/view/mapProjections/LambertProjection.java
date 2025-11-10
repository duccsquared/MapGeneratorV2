package com.view.mapProjections;

public class LambertProjection extends MapProjection {
    public double getX(double latVal, double longVal) {
        double x = longVal * Math.cos(0);
        return (x + Math.PI) / (2*Math.PI);
    }
    
    public double getY(double latVal, double longVal) {
        double y =  (Math.sin(latVal)/Math.cos(0));
        return (Math.PI/2 + y) / Math.PI; 
    }
}
