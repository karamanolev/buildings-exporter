package com.karamanolev;


import com.karamanolev.jts.JtsHelper;
import com.karamanolev.openscad.exporter.*;
import com.karamanolev.osmb.OsmbFeature;
import com.karamanolev.osmb.OsmbTile;
import com.karamanolev.osmb.OsmbTileManager;
import com.karamanolev.osmtiles.OsmTileManager;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class BuildingsExporter {
    public class Result {
        private String openScad, svg;

        public String getOpenScad() {
            return openScad;
        }

        public String getSvg() {
            return svg;
        }

        public Result(String openScad, String svg) {
            this.openScad = openScad;
            this.svg = svg;
        }
    }

    static class FeatureItem {
        OsmbFeature feature;
        Polygon polygon;
        int numBuffered;

        FeatureItem(OsmbFeature feature, Polygon polygon) {
            this.feature = feature;
            this.polygon = polygon;
        }
    }

    private static final double SCALE = 1.0 / 5000.0;
    private static final double MM_SCALE = 1000.0 * SCALE;
    private static final double EXTRA_BUILDING_SCALE = 8;
    private static final double MM_BUILDING_SCALE = 1000.0 * SCALE * EXTRA_BUILDING_SCALE;
    private static final double BASE_RECT_HEIGHT = 0.8;
    private static final double SLIDER_RECT_HEIGHT = 0.5;

    private OsmTileManager osmTileManager;
    private OsmbTileManager osmbTileManager;
    private GeoElevationData elevationData;
    private LatLng[] area;
    private String additionalInfo;

    private MercatorSquare mercatorSquare;
    private Polygon areaPolygon;
    private Envelope areaEnvelope;
    private BoundingBox boundingBox;
    private FeatureItem[] items;

    public BuildingsExporter(OsmTileManager osmTileManager,
                             OsmbTileManager osmbTileManager,
                             GeoElevationData elevationData,
                             LatLng[] area,
                             String additionalInfo) {
        this.osmTileManager = osmTileManager;
        this.osmbTileManager = osmbTileManager;
        this.elevationData = elevationData;
        this.area = area;
        this.additionalInfo = additionalInfo;
    }

    private OsmbFeature[] getOsmbFeatures() throws IOException {
        ArrayList<OsmbFeature> osmbFeatures = new ArrayList<>();
        for (TileCoords tileCoords : TileCoords.getTileCoords(this.boundingBox, OsmbTileManager.ZOOM)) {
            OsmbTile tile = this.osmbTileManager.getTile(tileCoords);
            osmbFeatures.addAll(Arrays.asList(tile.getFeatures()));
        }
        return osmbFeatures.toArray(new OsmbFeature[0]);
    }

    private void collectFeatures() throws IOException {
        // Scale is meters:meters, OpenScad units are assumed mm
        this.boundingBox = new BoundingBox(this.area);
        this.mercatorSquare = new MercatorSquare(this.boundingBox, MM_SCALE);

        this.areaPolygon = JtsHelper.INSTANCE.toPolygon(mercatorSquare, new LatLng[][]{area});
        this.areaEnvelope = this.areaPolygon.getEnvelopeInternal();

        ArrayList<FeatureItem> features = new ArrayList<>();
        OsmbFeature[] osmbFeatures = this.getOsmbFeatures();
        for (OsmbFeature osmbFeature : osmbFeatures) {
            Polygon polygon = JtsHelper.INSTANCE.toPolygon(mercatorSquare, osmbFeature.getContours());

            Geometry intersection = areaPolygon.intersection(polygon);
            if (!intersection.isEmpty()) {
                // If the intersection is the entire input polygon, it's entirely contained
                if (intersection.equalsNorm(polygon)) {
                    FeatureItem featureItem = new FeatureItem(osmbFeature, polygon);

//                if (!feature.getId().equals("299355116") && !feature.getId().equals("299355123") && !feature.getId().equals("303540640")) {
                    if (!osmbFeature.getId().equals("297430606") && !osmbFeature.getId().equals("297430603")) {
//                    continue;
                    }
                    features.add(featureItem);
                } else {
                    throw new RuntimeException("Partial intersection!");
                }
            }
        }
        this.items = features.toArray(new FeatureItem[0]);

        System.out.println(String.format("Collected %d / %d features", this.items.length, features.size()));
    }

    private void quantizePolygons() {
        for (FeatureItem item : this.items) {
            item.polygon = JtsHelper.INSTANCE.quantizePolygon(item.polygon);
        }
    }

    private void fixItems() {
        System.out.println("Start fix features");
        long start = System.currentTimeMillis();

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

        System.out.println("Fix features: " + (System.currentTimeMillis() - start));
    }

    private String exportOpenScad() {
        ScadRoot scadRoot = new ScadRoot();

        for (FeatureItem featureItem : items) {
            Polygon polygon = featureItem.polygon;
            OsmbFeature feature = featureItem.feature;

            double heightMeters = feature.getComputedHeight();
//                LatLng buildingLocation = feature.getContours()[0][0];
//                height += elevationData.getElevation(buildingLocation.getLat(), buildingLocation.getLng());

            ScadPolygon scadPolygon = new ScadPolygon(JtsHelper.INSTANCE.toXY(this.areaEnvelope.getMaxY(), polygon));
            ScadLinearExtrude extruded = new ScadLinearExtrude(
                    BASE_RECT_HEIGHT + heightMeters * MM_BUILDING_SCALE, scadPolygon);
            extruded.setComment("id: " + feature.getId());
            scadRoot.addChild(extruded);
        }

        XY[][] areaContours = JtsHelper.INSTANCE.toXY(this.areaEnvelope.getMaxY(), this.areaPolygon);

        scadRoot.addChild(new ScadLinearExtrude(BASE_RECT_HEIGHT, new ScadPolygon(areaContours)));

        scadRoot.setComment(String.format(
                "%s\nScale: 1 / %f\nBuildings Scale: %f\nBase Height: %f",
                this.additionalInfo,
                1 / SCALE,
                EXTRA_BUILDING_SCALE,
                BASE_RECT_HEIGHT
        ));

        return scadRoot.toString();
    }

    private String exportOpenScadSlider() {
        XY[][] areaContours = JtsHelper.INSTANCE.toXY(this.areaEnvelope.getMaxY(), this.areaPolygon);
        ScadLinearExtrude areaBase = new ScadLinearExtrude(SLIDER_RECT_HEIGHT, new ScadPolygon(areaContours));
        ScadDifference difference = new ScadDifference(areaBase);
        ScadRoot scadRoot = new ScadRoot(difference);

        for (FeatureItem featureItem : items) {
            Polygon buffered = JtsHelper.INSTANCE.buffer(featureItem.polygon, 0.1);

//                LatLng buildingLocation = feature.getContours()[0][0];
//                height += elevationData.getElevation(buildingLocation.getLat(), buildingLocation.getLng());

            ScadPolygon scadPolygon = new ScadPolygon(JtsHelper.INSTANCE.toXY(this.areaEnvelope.getMaxY(), buffered));
            ScadLinearExtrude extruded = new ScadLinearExtrude(SLIDER_RECT_HEIGHT * 3, scadPolygon);
            ScadTranslate translate = new ScadTranslate(0, 0, -SLIDER_RECT_HEIGHT, extruded);
            translate.setComment("id: " + featureItem.feature.getId());
            difference.addChild(translate);
        }


        scadRoot.setComment(String.format(
                "%s\nScale: 1 / %f\nBuildings Scale: %f\nBase Height: %f",
                this.additionalInfo,
                1 / SCALE,
                EXTRA_BUILDING_SCALE,
                BASE_RECT_HEIGHT
        ));

        return scadRoot.toString();
    }

    private String exportSvg() throws IOException {
        SVGGraphics2D g2 = new SVGGraphics2D(
                (int) Math.ceil(this.areaEnvelope.getMaxX()),
                (int) Math.ceil(this.areaEnvelope.getMaxY())
        );

        for (TileCoords tileCoords : TileCoords.getTileCoords(boundingBox, 17)) {
            BoundingBox boundingBox = TileUtils.getBoundingBox(tileCoords);
            XY northWest = mercatorSquare.toRelative(boundingBox.getNorthWest());
            XY southEast = mercatorSquare.toRelative(boundingBox.getSouthEast());

            double x = northWest.getX();
            double y = northWest.getY();
            double width = southEast.getX() - northWest.getX();
            double height = southEast.getY() - northWest.getY();

            byte[] tileImageBytes = this.osmTileManager.getTile(tileCoords);
            BufferedImage tileImage = ImageIO.read(new ByteArrayInputStream(tileImageBytes));
            AffineTransform transform = AffineTransform.getScaleInstance(
                    width / tileImage.getWidth(),
                    height / tileImage.getHeight()
            );
            transform.preConcatenate(AffineTransform.getTranslateInstance(x, y));
            g2.drawImage(tileImage, transform, null);
        }

        Geometry polygonUnion = JtsHelper.FACTORY.createGeometryCollection();
        for (FeatureItem featureItem : items) {
            Polygon buffered = JtsHelper.INSTANCE.buffer(featureItem.polygon, 0.1);
            polygonUnion = polygonUnion.union(buffered);
        }

        MultiPolygon multiPolygon = (MultiPolygon) polygonUnion;
        g2.setPaint(Color.BLACK);
        g2.setStroke(new BasicStroke(0.5f));
        for (int numPolygon = 0, numPolygons = multiPolygon.getNumGeometries(); numPolygon < numPolygons; numPolygon++) {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(numPolygon);
            Path2D path = JtsHelper.INSTANCE.toPath2D(polygon.getExteriorRing());
            g2.draw(path);
        }

        g2.draw(JtsHelper.INSTANCE.toPath2D(this.areaPolygon.getExteriorRing()));

        return g2.getSVGElement();
    }

    public BuildingsExporter.Result export() throws IOException {
        this.collectFeatures();
        this.quantizePolygons();
        this.fixItems();

        return new BuildingsExporter.Result(
                this.exportOpenScadSlider(),
                this.exportSvg()
        );
    }

    private static Polygon bufferProcess(Polygon polygon) {
        Polygon buffered = JtsHelper.INSTANCE.buffer(polygon, 0.05);
        buffered = JtsHelper.INSTANCE.quantizePolygon(buffered);

//        TopologyPreservingSimplifier simplifier = new TopologyPreservingSimplifier(buffered);
//        simplifier.setDistanceTolerance(0.05);
//        buffered = (Polygon) simplifier.getResultGeometry();

        return buffered;
    }
}