package com.karamanolev;

public class XYZ {
    private double x, y, z;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public XYZ(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return String.format("XYZ(%.5f, %.5f, %.5f)", this.x, this.y, this.z);
    }
}
