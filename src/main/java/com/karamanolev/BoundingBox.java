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
        this.northWest = northWest;
        this.southEast = southEast;
    }
}