package com.karamanolev;

import com.karamanolev.geojsontiles.GeoJsonTiles;
import com.karamanolev.osmb.OsmbTile;
import com.karamanolev.osmb.OsmbTileManager;
import com.karamanolev.osmtiles.OsmTileManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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

        OsmbTileManager buildingsTileManager = new OsmbTileManager();

        OsmbTile[] tiles = new OsmbTile[]{
//                tileManager.getTile(new TileCoords(18507, 12078, 15)),
//                tileManager.getTile(new TileCoords(18508, 12078, 15)),
//                tileManager.getTile(new TileCoords(18507, 12079, 15)),
                buildingsTileManager.getTile(new TileCoords(18499, 12084, 15)) // Boyana
//                tileManager.getTile(new TileCoords(18508, 12079, 15)) // Yavorov / Borisova
        };

        Integer tileNumber = 1;
        GeoJsonTiles geoJsonTiles = new GeoJsonTiles(
                "/Users/ivailo/repos/buildings-exporter/tiles-project/tile-polygons.geojson");
        LatLng[] buildingsArea = geoJsonTiles.getPolygon(tileNumber);

        OsmTileManager osmTileManager = new OsmTileManager();

        BuildingsExporter exporter = new BuildingsExporter(
                osmTileManager, buildingsTileManager, elevationData, buildingsArea, "OsmbTile: " + tileNumber);
        BuildingsExporter.Result result = exporter.export();
        Files.write(Paths.get("/Users/ivailo/Downloads/tile1.svg"), result.getSvg().getBytes(StandardCharsets.UTF_8));
        Utils.setClipboard(result.getOpenScad());
        System.out.println("Copied to clipboard!");

//        Utils.setClipboard(
//        System.out.println(
//                OpenScadUtils.exportTerrainBlock(elevationData, boundingBox.getSouthEast(), boundingBox.getNorthWest(), 64)
//        );
    }
}
