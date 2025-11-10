package com.view.mapProjections;

public class GallPetersProjection extends MapProjection {
    public double getX(double latVal, double longVal) {
        double x = longVal * (Math.sqrt(2)/2);
        return (x+Math.PI)/(2*Math.PI);
    }
    
    public double getY(double latVal, double longVal) {
        double y = Math.sin(latVal) * Math.sqrt(2);
        return (y+Math.sqrt(2))/(2*Math.sqrt(2));
    }
}
