package com.karamanolev.openscad;


import com.karamanolev.*;
import com.karamanolev.jts.JtsHelper;
import com.karamanolev.openscad.exporter.ScadLinearExtrude;
import com.karamanolev.openscad.exporter.ScadPolygon;
import com.karamanolev.openscad.exporter.ScadRoot;
import com.karamanolev.osmbuildings.Feature;
import com.karamanolev.osmbuildings.Tile;
import com.karamanolev.osmbuildings.TileManager;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;

import java.io.IOException;
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

    private static double SCALE = 1.0 / 5000;
    private static double BASE_HEIGHT = 1;

    private TileManager tileManager;
    private GeoElevationData elevationData;
    private LatLng[] area;
    private Polygon areaPolygon;
    private FeatureItem[] items;

    public BuildingsExporter(TileManager tileManager, GeoElevationData elevationData, LatLng[] area) {
        this.tileManager = tileManager;
        this.elevationData = elevationData;
        this.area = area;
    }

    private void collectFeatures() throws IOException {
        BoundingBox boundingBox = new BoundingBox(this.area);
        TileCoords nwTile = TileUtils.getTileCoords(boundingBox.getNorthWest(), 15);
        TileCoords seTile = TileUtils.getTileCoords(boundingBox.getSouthEast(), 15);
        // Scale is meters:meters, OpenScad units are assumed mm
        MercatorSquare square = new MercatorSquare(boundingBox, SCALE * 1000);

        areaPolygon = JtsHelper.INSTANCE.toPolygon(square, new LatLng[][]{area});

        int totalFeatures = 0;
        ArrayList<FeatureItem> features = new ArrayList<>();
        for (int tileX = nwTile.getX(); tileX <= seTile.getX(); tileX++) {
            for (int tileY = nwTile.getY(); tileY <= seTile.getY(); tileY++) {
                Tile tile = this.tileManager.getTile(new TileCoords(tileX, tileY, nwTile.getZ()));

                for (Feature feature : tile.getFeatures()) {
                    Polygon polygon = JtsHelper.INSTANCE.toPolygon(square, feature.getContours());

                    Geometry intersection = areaPolygon.intersection(polygon);
                    if (!intersection.isEmpty()) {
                        // If the intersection is the entire input polygon, it's entirely contained
                        if (intersection.equalsNorm(polygon)) {
                            FeatureItem featureItem = new FeatureItem(
                                    feature, polygon);

//                if (!feature.getId().equals("299355116") && !feature.getId().equals("299355123") && !feature.getId().equals("303540640")) {
                            if (!feature.getId().equals("297430606") && !feature.getId().equals("297430603")) {
//                    continue;
                            }
                            features.add(featureItem);
                        } else {
                            throw new RuntimeException("Partial intersection!");
                        }
                    }

                    totalFeatures++;
                }
            }
        }
        this.items = features.toArray(new FeatureItem[0]);

        System.out.println(String.format("Collected %d / %d features", this.items.length, totalFeatures));
    }

    private Polygon bufferProcess(Polygon polygon) {
        Polygon buffered = (Polygon) BufferOp.bufferOp(polygon, 0.05, new BufferParameters(
                1,
                BufferParameters.CAP_SQUARE
        ));
        buffered = JtsHelper.INSTANCE.quantizePolygon(buffered);

//        TopologyPreservingSimplifier simplifier = new TopologyPreservingSimplifier(buffered);
//        simplifier.setDistanceTolerance(0.05);
//        buffered = (Polygon) simplifier.getResultGeometry();

        return buffered;
    }

    private void quantizePolygons() {
        for (FeatureItem item : this.items) {
            item.polygon = JtsHelper.INSTANCE.quantizePolygon(item.polygon);
        }
    }

    private void fixItems() {
        outerloop:
        while (true) {
            for (int i = 0; i < items.length; i++) {
                Polygon first = items[i].polygon;

                for (int j = i + 1; j < items.length; j++) {
                    Polygon second = items[j].polygon;

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

    public String export() throws IOException {
        this.collectFeatures();
        this.quantizePolygons();

        System.out.println("Start fix features");
        long start = System.currentTimeMillis();
        this.fixItems();
        System.out.println("Fix features: " + (System.currentTimeMillis() - start));

        Envelope envelope = this.areaPolygon.getEnvelopeInternal();
        ScadRoot scadRoot = new ScadRoot();

        int maxFeatures = 100;
        for (FeatureItem featureItem : items) {
            Polygon polygon = featureItem.polygon;
            Feature feature = featureItem.feature;

//            if (maxFeatures-- <= 0) break;

            if (
                    !feature.getId().equals("297430606") && !feature.getId().equals("297430603") &&
                            !feature.getId().equals("299281697") && !feature.getId().equals("299281699") &&
                            !feature.getId().equals("166213453") && !feature.getId().equals("256093026")
            ) {
//                continue;
            }

            double height = featureItem.feature.getComputedHeight();
//                LatLng buildingLocation = feature.getContours()[0][0];
//                height += elevationData.getElevation(buildingLocation.getLat(), buildingLocation.getLng());

            ScadPolygon scadPolygon = new ScadPolygon(JtsHelper.INSTANCE.toXY(envelope.getMaxY(), polygon));
            ScadLinearExtrude extruded = new ScadLinearExtrude(BASE_HEIGHT + height, scadPolygon);
            extruded.setComment("id: " + feature.getId());
            scadRoot.addChild(extruded);
        }

        XY[][] areaContours = JtsHelper.INSTANCE.toXY(envelope.getMaxY(), this.areaPolygon);

        scadRoot.addChild(new ScadLinearExtrude(BASE_HEIGHT, new ScadPolygon(areaContours)));

        return scadRoot.toString();
    }
}