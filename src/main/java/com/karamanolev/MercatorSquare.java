package com.karamanolev;

public class MercatorSquare {
    private XY start, vec;
    private double scale;

    public double getScale() {
        return scale;
    }

    public MercatorSquare(BoundingBox boundingBox, double scale) {
        this.scale = scale;

        this.start = SphericalMercator.toXY(boundingBox.getNorthWest());
    }

    public XY toRelative(XY absolute) {
        return new XY(
                (absolute.getX() - start.getX()) * scale,
                (absolute.getY() - start.getY()) * -scale
        );
    }

    public XY toRelative(LatLng absolute) {
        return this.toRelative(SphericalMercator.toXY(absolute));
    }
}
