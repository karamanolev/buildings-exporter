package com.karamanolev;

public class XY {
    private double x, y;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public XY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("XY(%.5f, %.5f)", this.x, this.y);
    }
}
