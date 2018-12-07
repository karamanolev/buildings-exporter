package com.karamanolev;

public class SphericalMercator {
    public static final double RADIUS = 6378137.0; /* in meters on the equator */

    public static LatLng toLatLng(XY xy) {
        return new LatLng(
                Math.toDegrees(Math.atan(Math.exp(xy.getY() / RADIUS)) * 2 - Math.PI / 2),
                Math.toDegrees(xy.getX() / RADIUS)
        );
    }

    /* These functions take their angle parameter in degrees and return a length in meters */

    public static XY toXY(LatLng latLng) {
        return new XY(
                Math.toRadians(latLng.getLng()) * RADIUS,
                Math.log(Math.tan(Math.PI / 4 + Math.toRadians(latLng.getLat()) / 2)) * RADIUS
        );
    }


}