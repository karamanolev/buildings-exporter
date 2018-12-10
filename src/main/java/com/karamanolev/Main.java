package com.karamanolev;

import com.karamanolev.geojsontiles.GeoJsonTiles;
import com.karamanolev.openscad.BuildingsExporter;
import com.karamanolev.osmbuildings.Feature;
import com.karamanolev.osmbuildings.Tile;
import com.karamanolev.osmbuildings.TileManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void interpolateImage(GeoElevationData elevationData, LatLng southEast, LatLng northWest) throws IOException {
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        int segments = 128;
        int pps = image.getWidth() / segments; // pixels per segment
        short minElevation = 200;
        short maxElevation = 2000;

        System.out.println("Start generating...");

        short[][] elevations = elevationData.getElevationGrid(southEast, northWest, segments, 4);

        for (int y = 0; y < segments; y++) {
            for (int x = 0; x < segments; x++) {
                int brightness = (int) (255.0 * (elevations[y][x] - minElevation) / (maxElevation - minElevation));
                brightness = Math.max(0, Math.min(255, brightness));
                int rgb = (brightness << 16) | (brightness << 8) | brightness;

                for (int yOffset = 0; yOffset < pps; yOffset++) {
                    for (int xOffset = 0; xOffset < pps; xOffset++) {
                        image.setRGB(x * pps + xOffset, y * pps + yOffset, rgb);
                    }
                }
            }
        }

        System.out.println("Finished generating...");
        ImageIO.write(image, "png", new File("C:\\Users\\Ivailo\\IdeaProjects\\SrtmExperiments\\out\\artifacts\\SrtmExperiments_jar\\image.png"));
    }

    public static void main(String[] args) throws IOException {
        GeoElevationData elevationData = GeoElevationDataFactory.createGeoElevationData(null);

        LatLng lozen = new LatLng(42.601600, 23.502900);
        LatLng cherniVrah = new LatLng(42.5637, 23.2784);
        LatLng stadium = new LatLng(42.687544, 23.335296);

        TileCoords tileCoords = new TileCoords(18506, 12079, 15); // City center
//        TileCoords tileCoords = new TileCoords(578, 377, 10); // Sofia at large
        BoundingBox boundingBox = TileUtils.getBoundingBox(tileCoords);
        System.out.println(boundingBox.getSouthEast().getLat() + ", " + boundingBox.getSouthEast().getLng());
        System.out.println(boundingBox.getNorthWest().getLat() + ", " + boundingBox.getNorthWest().getLng());

//        interpolateImage(elevationData, boundingBox.getSouthEast(), boundingBox.getNorthWest());
//        System.exit(0);

        TileManager tileManager = new TileManager();

        Tile[] tiles = new Tile[]{
//                tileManager.getTile(new TileCoords(18507, 12078, 15)),
//                tileManager.getTile(new TileCoords(18508, 12078, 15)),
//                tileManager.getTile(new TileCoords(18507, 12079, 15)),
                tileManager.getTile(new TileCoords(18499, 12084, 15)) // Boyana
//                tileManager.getTile(new TileCoords(18508, 12079, 15)) // Yavorov / Borisova
        };

        GeoJsonTiles geoJsonTiles = new GeoJsonTiles(
                "/Users/ivailo/repos/buildings-exporter/tiles-project/tile-polygons.geojson");
        LatLng[] buildingsArea = geoJsonTiles.getPolygon(1);

        BuildingsExporter exporter = new BuildingsExporter(tileManager, elevationData, buildingsArea);
//        BuildingsExporter exporter = new BuildingsExporter(elevationData, buildingsArea, buildingsAreaFeatures);
        Utils.setClipboard(exporter.export());
//        System.out.println(scadCode);
        System.out.println("Copied to clipboard!");

//        Utils.setClipboard(
//        System.out.println(
//                OpenScadUtils.exportTerrainBlock(elevationData, boundingBox.getSouthEast(), boundingBox.getNorthWest(), 64)
//        );
    }
}
