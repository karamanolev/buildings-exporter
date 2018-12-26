package com.karamanolev.jts;

import com.karamanolev.LatLng;
import com.karamanolev.MercatorSquare;
import com.karamanolev.XY;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;

import java.awt.geom.Path2D;
import java.util.ArrayList;

public class JtsHelper {
    public static final GeometryFactory FACTORY = new GeometryFactory();
    public static final JtsHelper INSTANCE = new JtsHelper(FACTORY);

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


    public XY[] toXY(double maxY, LineString ring) {
        ArrayList<XY> xys = new ArrayList<>();
        for (int i = 0; i < ring.getNumPoints(); i++) {
            Coordinate coord = ring.getPointN(i).getCoordinate();
            xys.add(new XY(coord.getX(), maxY - coord.getY()));
        }
        return xys.toArray(new XY[0]);
    }

    public XY[][] toXY(double maxY, Polygon polygon) {
        ArrayList<XY[]> contours = new ArrayList<>();
        contours.add(toXY(maxY, polygon.getExteriorRing()));
        for (int ring = 0; ring < polygon.getNumInteriorRing(); ring++) {
            contours.add(toXY(maxY, polygon.getInteriorRingN(ring)));
        }
        return contours.toArray(new XY[0][]);
    }

    public Path2D toPath2D(LineString lineString) {
        Path2D path = new Path2D.Double();
        Coordinate coords = lineString.getPointN(0).getCoordinate();
        path.moveTo(coords.getX(), coords.getY());

        for (int i = 1, points = lineString.getNumPoints(); i < points; i++) {
            Coordinate iCoords = lineString.getPointN(i).getCoordinate();
            path.lineTo(iCoords.getX(), iCoords.getY());
        }

        return path;
    }

    public Polygon buffer(Polygon polygon, double distance) {
        return (Polygon) BufferOp.bufferOp(polygon, distance, new BufferParameters(
                1,
                BufferParameters.CAP_SQUARE
        ));
    }
}
