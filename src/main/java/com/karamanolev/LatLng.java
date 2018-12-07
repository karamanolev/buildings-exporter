package com.karamanolev;

public class LatLng {
    private double lat, lng;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public LatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public String toString() {
        return String.format("LatLng(%.5f, %.5f)", this.lat, this.lng);
    }
}
