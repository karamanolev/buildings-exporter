package com.karamanolev.openscad;


import com.karamanolev.*;
import com.karamanolev.jts.JtsHelper;
import com.karamanolev.osmbuildings.Feature;
import com.karamanolev.osmbuildings.Tile;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;

import java.util.ArrayList;

public class BuildingsExporter {
    static class FeatureItem {
        Feature feature;
        Polygon polygon;
        int numBuffered;

        FeatureItem(Feature feature, Polygon polygon) {
            this.feature = feature;
            this.polygon = polygon;
        }
    }

    private static double TERRAIN_BLOCK_SIZE = 200;

    private GeometryFactory factory = new GeometryFactory();
    private JtsHelper geom = new JtsHelper(factory);
    private GeoElevationData elevationData;
    private Tile[] tiles;
    private FeatureItem[] items;

    public BuildingsExporter(GeoElevationData elevationData, Tile[] tiles) {
        this.elevationData = elevationData;
        this.tiles = tiles;
    }

    private void collectFeatures() {
        LatLng northWest = TileUtils.getBoundingBox(tiles[0].getCoords()).getNorthWest();
        LatLng southEast = TileUtils.getBoundingBox(tiles[tiles.length - 1].getCoords()).getSouthEast();
        MercatorSquare square = new MercatorSquare(new BoundingBox(northWest, southEast), TERRAIN_BLOCK_SIZE);

        ArrayList<FeatureItem> features = new ArrayList<>();
        for (Tile tile : tiles) {
            for (Feature feature : tile.getFeatures()) {
                FeatureItem featureItem = new FeatureItem(feature, geom.toPolygon(square, feature.getContours()));
//                if (!feature.getId().equals("299355116") && !feature.getId().equals("299355123") && !feature.getId().equals("303540640")) {
                if (!feature.getId().equals("297430606") && !feature.getId().equals("297430603")) {
//                    continue;
                }
                features.add(featureItem);
            }
        }
        this.items = features.toArray(new FeatureItem[0]);
    }

    private Polygon bufferProcess(Polygon polygon) {
        Polygon buffered = (Polygon) BufferOp.bufferOp(polygon, 0.05, new BufferParameters(
                1,
                BufferParameters.CAP_SQUARE
        ));
        buffered = this.geom.quantizePolygon(buffered);

//        TopologyPreservingSimplifier simplifier = new TopologyPreservingSimplifier(buffered);
//        simplifier.setDistanceTolerance(0.05);
//        buffered = (Polygon) simplifier.getResultGeometry();

        return buffered;
    }

    private void quantizePolygons() {
        for (FeatureItem item : this.items) {
            item.polygon = this.geom.quantizePolygon(item.polygon);
        }
    }

    private void pruneExternalPolygons() {
        Polygon basePolygon = this.factory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(TERRAIN_BLOCK_SIZE, 0),
                new Coordinate(TERRAIN_BLOCK_SIZE, TERRAIN_BLOCK_SIZE),
                new Coordinate(0, TERRAIN_BLOCK_SIZE),
                new Coordinate(0, 0),
        });

        int pruned = 0;
        for (FeatureItem item : this.items) {
            if (item.feature.getId().equals("158759064")) {
                pruned++;
            }
            double intersectionArea = item.polygon.intersection(basePolygon).getArea();
            if (intersectionArea < 3) {
                System.out.println("Pruning " + item.feature.getId() +
                        String.format(" for small area: %.5f", intersectionArea));
                item.polygon = null;
                pruned++;
            }
        }
        System.out.println("Pruned " + pruned + " features");
    }

    private void fixItems() {
        outerloop:
        while (true) {
            for (int i = 0; i < items.length; i++) {
                Polygon first = items[i].polygon;
                if (first == null) continue;

                for (int j = i + 1; j < items.length; j++) {
                    Polygon second = items[j].polygon;
                    if (second == null) continue;

                    boolean intersects = first.intersects(second);
                    boolean needsBuffer = !intersects && first.isWithinDistance(second, 0.05);
                    needsBuffer |= intersects && first.intersection(second).getArea() < 0.1;

                    if (needsBuffer) {
                        if (items[i].numBuffered <= items[j].numBuffered) {
                            items[i].polygon = this.bufferProcess(first);
                            items[i].numBuffered++;
                        }
                        if (items[j].numBuffered < items[i].numBuffered) {
                            items[j].polygon = this.bufferProcess(second);
                            items[j].numBuffered++;
                        }
                        continue outerloop;
                    }
                }
            }
            break;
        }
    }

    private ArrayList<XY> toXY(LineString ring) {
        ArrayList<XY> xys = new ArrayList<>();
        for (int i = 0; i < ring.getNumPoints(); i++) {
            Coordinate coord = ring.getPointN(i).getCoordinate();
            xys.add(new XY(coord.getX(), TERRAIN_BLOCK_SIZE - coord.getY()));
        }
        return xys;
    }

    public String export() {
        this.collectFeatures();
        this.quantizePolygons();
        this.pruneExternalPolygons();

        System.out.println("Start fix features");
        long start = System.currentTimeMillis();
        this.fixItems();
        System.out.println("Fix features: " + (System.currentTimeMillis() - start));

        StringBuilder builder = new StringBuilder();

        int maxFeatures = 100;
        for (FeatureItem featureItem : items) {
            Polygon polygon = featureItem.polygon;
            Feature feature = featureItem.feature;

            if (polygon == null) continue;
//            if (maxFeatures-- <= 0) break;

            if (
                    !feature.getId().equals("297430606") && !feature.getId().equals("297430603") &&
                            !feature.getId().equals("299281697") && !feature.getId().equals("299281699") &&
                            !feature.getId().equals("166213453") && !feature.getId().equals("256093026")
            ) {
//                continue;
            }

            ArrayList<ArrayList<XY>> contours = new ArrayList<>();
            contours.add(toXY(featureItem.polygon.getExteriorRing()));
            for (int ring = 0; ring < featureItem.polygon.getNumInteriorRing(); ring++) {
                contours.add(toXY(featureItem.polygon.getInteriorRingN(ring)));
            }

            double height = featureItem.feature.getComputedHeight();
//                LatLng buildingLocation = feature.getContours()[0][0];
//                height += elevationData.getElevation(buildingLocation.getLat(), buildingLocation.getLng());

            builder.append(String.format(
                    "linear_extrude(height=%f) %s; // id: %s\n",
                    height,
                    OpenScadUtils.exportPolygon(contours),
                    feature.getId()
            ));
        }

        return builder.toString();
    }
}