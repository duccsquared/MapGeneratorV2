package com.data;

public class VoronoiEdge3D {
    public Point3D v1, v2;
    VoronoiEdge3D(Point3D v1, Point3D v2) {
        this.v1 = v1; this.v2 = v2;
    }
    @Override
    public String toString() {
        return String.format("VoronoiEdge %s -> %s", v1, v2);
    }

    @Override 
    public boolean equals(Object other) {
        if(other instanceof VoronoiEdge3D) {
            VoronoiEdge3D vOther = (VoronoiEdge3D) other;
            return (this.v1.equals(vOther.v1) && this.v2.equals(vOther.v2)) || (this.v1.equals(vOther.v2) && this.v2.equals(vOther.v1));
        }
        else {
            return false;
        }
    }
}