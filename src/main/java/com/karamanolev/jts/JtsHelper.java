package com.karamanolev.jts;

import com.karamanolev.LatLng;
import com.karamanolev.MercatorSquare;
import com.karamanolev.XY;
import org.locationtech.jts.geom.*;

public class JtsHelper {
    private GeometryFactory factory;

    public JtsHelper(GeometryFactory factory) {
        this.factory = factory;
    }

    private LinearRing toLinearRing(MercatorSquare square, LatLng[] contour) {
        Coordinate[] coords = new Coordinate[contour.length];
        for (int i = 0; i < contour.length; i++) {
            XY xy = square.toRelative(contour[i]);
            coords[i] = new Coordinate(xy.getX(), xy.getY());
        }
        return factory.createLinearRing(coords);
    }

    public Polygon toPolygon(MercatorSquare square, LatLng[][] contours) {
        LinearRing shell = toLinearRing(square, contours[0]);
        LinearRing[] holes = new LinearRing[contours.length - 1];
        for (int i = 1; i < contours.length; i++) {
            holes[i - 1] = toLinearRing(square, contours[i]);
        }

        return factory.createPolygon(shell, holes);
    }

    private LinearRing quantizeLineString(LineString s) {
        Coordinate[] coords = new Coordinate[s.getNumPoints()];
        for (int i = 0; i < s.getNumPoints(); i++) {
            Coordinate c = s.getPointN(i).getCoordinate();
            coords[i] = new Coordinate(
                    Math.round(c.getX() * 100.0) / 100.0,
                    Math.round(c.getY() * 100.0) / 100.0
            );
        }

        return this.factory.createLinearRing(coords);
    }

    public Polygon quantizePolygon(Polygon polygon) {
        LinearRing exteriorRing = quantizeLineString(polygon.getExteriorRing());
        LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            holes[i] = quantizeLineString(polygon.getInteriorRingN(i));
        }
        return this.factory.createPolygon(exteriorRing, holes);
    }
}
