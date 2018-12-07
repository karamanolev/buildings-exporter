package com.karamanolev.openscad;

import com.karamanolev.GeoElevationData;
import com.karamanolev.LatLng;
import com.karamanolev.XY;
import com.karamanolev.XYZ;

import java.io.IOException;
import java.util.ArrayList;

public class OpenScadUtils {
    static class Formatter {
        private static String format(int[] ints) {
            StringBuilder result = new StringBuilder("[");
            for (int value : ints) {
                result.append(value);
                result.append(", ");
            }
            result.append("]");
            return result.toString();
        }

        private static String format(XY xy) {
            return String.format("[%.5f, %.5f]", xy.getX(), xy.getY());
        }

        private static String format(XYZ xyz) {
            return String.format("[%.5f, %.5f, %.5f]", xyz.getX(), xyz.getY(), xyz.getZ());
        }

        private static String formatXYs(Iterable<XY> xys) {
            StringBuilder result = new StringBuilder("[");
            for (XY xy : xys) {
                result.append(format(xy));
                result.append(", ");
            }
            result.append("]");
            return result.toString();
        }

        private static String formatXYZs(Iterable<XYZ> xyzs) {
            StringBuilder result = new StringBuilder("[");
            for (XYZ xyz : xyzs) {
                result.append(format(xyz));
                result.append(", ");
            }
            result.append("]");
            return result.toString();
        }
    }

    public static String exportPolygon(ArrayList<ArrayList<XY>> contours) {
        StringBuilder builder = new StringBuilder();
        builder.append("polygon(points=[");
        for (ArrayList<XY> contour : contours) {
            for (XY xy : contour) {
                builder.append(Formatter.format(xy));
                builder.append(", ");
            }
        }

        builder.append("], paths=[");
        int point = 0;
        for (ArrayList<XY> contour : contours) {
            builder.append("[");
            for (XY xy : contour) {
                builder.append(point++);
                builder.append(", ");
            }
            builder.append("], ");
        }
        builder.append("])");
        return builder.toString();
    }

    public static String exportTerrainBlock(GeoElevationData elevationData, LatLng southEast, LatLng northWest, int points) throws IOException {
        int segments = points - 1;
        double terrainBlockSize = 200;
        double terrainSegmentSize = terrainBlockSize / segments;

        short[][] elevationGrid = elevationData.getElevationGrid(southEast, northWest, points, 4); // 1 more point than segments

        ArrayList<XYZ> blockPoints = new ArrayList<>();
        ArrayList<int[]> blockFaces = new ArrayList<>();

        for (int pixelY = 0; pixelY < points; pixelY++) {
            for (int pixelX = 0; pixelX < points; pixelX++) {
                double blockX = terrainSegmentSize * pixelX;
                double blockY = terrainBlockSize - terrainSegmentSize * pixelY; // invert Y axis
                double blockZ = elevationGrid[pixelY][pixelX] - 400;

                blockPoints.add(new XYZ(blockX, blockY, blockZ));
            }
        }

        for (int blockY = 0; blockY < segments; blockY++) {
            for (int blockX = 0; blockX < segments; blockX++) {
                blockFaces.add(new int[]{
                        blockY * points + blockX,
                        blockY * points + blockX + 1,
                        (blockY + 1) * points + blockX
                });
                blockFaces.add(new int[]{
                        blockY * points + blockX + 1,
                        (blockY + 1) * points + blockX + 1,
                        (blockY + 1) * points + blockX,
                });
            }
        }

        int basePointOne = points * points;
        blockPoints.add(new XYZ(0, terrainBlockSize, 0));
        blockPoints.add(new XYZ(terrainBlockSize, terrainBlockSize, 0));
        blockPoints.add(new XYZ(terrainBlockSize, 0, 0));
        blockPoints.add(new XYZ(0, 0, 0));

        int[] frontFace = new int[points + 2], rightFace = new int[points + 2], backFace = new int[points + 2],
                leftFace = new int[points + 2];

        for (int i = 0; i < points; i++) {
            frontFace[i] = (points - 1) * points + i;
            rightFace[i] = (points - 1 - i) * points + points - 1;
            backFace[i] = points - 1 - i;
            leftFace[i] = points * i;
        }

        frontFace[points] = basePointOne + 2;
        frontFace[points + 1] = basePointOne + 3;
        rightFace[points] = basePointOne + 1;
        rightFace[points + 1] = basePointOne + 2;
        backFace[points] = basePointOne;
        backFace[points + 1] = basePointOne + 1;
        leftFace[points] = basePointOne + 3;
        leftFace[points + 1] = basePointOne;

        blockFaces.add(frontFace);
        blockFaces.add(rightFace);
        blockFaces.add(backFace);
        blockFaces.add(leftFace);

        // Add bottom face
        blockFaces.add(new int[]{basePointOne + 3, basePointOne + 2, basePointOne + 1, basePointOne});

        StringBuilder result = new StringBuilder("polyhedron(points = ");
        result.append(Formatter.formatXYZs(blockPoints));
        result.append(", faces = [");
        for (int[] face : blockFaces) {
            result.append(Formatter.format(face));
            result.append(", ");
        }
        result.append("])");

        return result.toString();
    }
}
