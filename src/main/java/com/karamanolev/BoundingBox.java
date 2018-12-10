package com.karamanolev;

public class BoundingBox {
    private LatLng northWest, southEast;

    public LatLng getNorthWest() {
        return northWest;
    }

    public LatLng getSouthEast() {
        return southEast;
    }

    public BoundingBox(LatLng northWest, LatLng southEast) {
        if (northWest.getLat() < southEast.getLat() || northWest.getLng() > southEast.getLng()) {
            throw new RuntimeException("Inverted bounding box coords");
        }
        this.northWest = northWest;
        this.southEast = southEast;
    }

    public BoundingBox(LatLng[] coords) {
        double nwLat = Double.MIN_VALUE,
                nwLng = Double.MAX_VALUE,
                seLat = Double.MAX_VALUE,
                seLng = Double.MIN_VALUE;
        for (LatLng coord : coords) {
            nwLat = Math.max(nwLat, coord.getLat());
            nwLng = Math.min(nwLng, coord.getLng());

            seLat = Math.min(seLat, coord.getLat());
            seLng = Math.max(seLng, coord.getLng());
        }

        this.northWest = new LatLng(nwLat, nwLng);
        this.southEast = new LatLng(seLat, seLng);
    }
}