package com.view.mapProjections;

public class MercatorProjection extends MapProjection {
    public double getX(double latVal, double longVal) {
        double x = longVal;
        return (x + Math.PI) / (2*Math.PI);
    }
    
    public double getY(double latVal, double longVal) {
        double clampedLatVal = Math.max((-Math.PI/2)*(17.0/18),Math.min((Math.PI/2)*(17.0/18),latVal));
        double y = Math.log(Math.tan(Math.PI/4 + clampedLatVal/2));
        double maxY = Math.log(Math.tan(Math.PI/4 + ((Math.PI/2)*(17.0/18))/2));
        return (y + maxY) / (2*maxY);  
    }
    
}
