package com.karamanolev;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoElevationFile {
    private static class RowAndColumn {
        private int row, column;

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        public RowAndColumn(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }

    private String fileName;
    private ByteBuffer data;
    private double latitude, longitude;
    private double resolution;
    private int squareSide;

    public GeoElevationFile(String fileName, byte[] data) {
        this.fileName = fileName;
        this.data = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        this.parseFileNameStartingPosition();
        double squareSide = Math.sqrt(this.data.limit() / 2.0);
        if (squareSide != (int) squareSide) {
            throw new NumberFormatException("Invalid file size for file");
        }
        this.squareSide = (int) squareSide;
        this.resolution = 1.0 / (squareSide - 1);
    }

    private void parseFileNameStartingPosition() {
        System.out.println(this.fileName);
        Matcher matcher = Pattern.compile("^([NS])(\\d+)([EW])(\\d+)\\.hgt$").matcher(this.fileName);
        if (!matcher.matches()) {
            throw new NumberFormatException("Invalid file name for elevation file");
        }

        this.latitude = Double.parseDouble(matcher.group(2));
        if (!matcher.group(1).equals("N")) {
            this.latitude = -this.latitude;
        }

        this.longitude = Double.parseDouble(matcher.group(4));
        if (!matcher.group(3).equals("E")) {
            this.longitude = -this.longitude;
        }
    }

    private RowAndColumn getRowAndColumn(double latitude, double longitude) {
        return new RowAndColumn(
                (int) Math.floor((this.latitude + 1 - latitude) * (this.squareSide - 1)),
                (int) Math.floor((longitude - this.longitude) * (this.squareSide - 1))
        );
    }

    private short getElevationFromRowAndColumn(RowAndColumn rowAndColumn) {
        int pos = (rowAndColumn.getRow() * this.squareSide + rowAndColumn.getColumn()) * 2;
        assert pos < this.data.limit() - 1; // Need 2 bytes
        return this.data.getShort(pos);
    }

    public short getElevation(double latitude, double longitude) {
        if (this.latitude - this.resolution > latitude || this.latitude >= this.latitude + 1) {
            throw new NumberFormatException("Invalid latitude for file");
        }
        if (this.longitude > longitude || longitude >= this.longitude + 1 + this.resolution) {
            throw new NumberFormatException("Invalid longitude for file");
        }
        RowAndColumn rowAndColumn = this.getRowAndColumn(latitude, longitude);
        return this.getElevationFromRowAndColumn(rowAndColumn);
    }
}
