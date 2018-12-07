package com.karamanolev;

public class MercatorSquare {
    private XY start, vec;
    private double size;

    public double getSize() {
        return size;
    }

    public MercatorSquare(BoundingBox boundingBox, double size) {
        this.size = size;

        this.start = SphericalMercator.toXY(boundingBox.getNorthWest());

        XY southEastXY = SphericalMercator.toXY(boundingBox.getSouthEast());
        this.vec = new XY(southEastXY.getX() - this.start.getX(), southEastXY.getY() - this.start.getY());
    }

    public XY toRelative(XY absolute) {
        return new XY(
                (absolute.getX() - start.getX()) / vec.getX() * size,
                (absolute.getY() - start.getY()) / vec.getY() * size
        );
    }

    public XY toRelative(LatLng absolute) {
        return this.toRelative(SphericalMercator.toXY(absolute));
    }

    public XY toAbsolute(XY relative) {
        return new XY(
                start.getX() + relative.getX() / size * vec.getX(),
                start.getY() + relative.getY() / size * vec.getY()
        );
    }
}
