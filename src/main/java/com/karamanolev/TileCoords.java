package com.karamanolev;

import java.util.ArrayList;

public class TileCoords {
    private int x, y, z;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public TileCoords(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode() {
        return ((Integer) this.x).hashCode() ^ ((Integer) this.y).hashCode() ^ ((Integer) this.z).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TileCoords)) {
            return false;
        }
        TileCoords other = (TileCoords) obj;
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

    @Override
    public String toString() {
        return String.format("TileCoords(%d, %d, %d)", this.x, this.y, this.z);
    }

    public static TileCoords[] getTileCoords(BoundingBox boundingBox, int zoomLevel) {
        TileCoords nwTile = TileUtils.getTileCoords(boundingBox.getNorthWest(), zoomLevel);
        TileCoords seTile = TileUtils.getTileCoords(boundingBox.getSouthEast(), zoomLevel);

        ArrayList<TileCoords> coords = new ArrayList<>();
        for (int tileX = nwTile.getX(); tileX <= seTile.getX(); tileX++) {
            for (int tileY = nwTile.getY(); tileY <= seTile.getY(); tileY++) {
                coords.add(new TileCoords(tileX, tileY, zoomLevel));
            }
        }

        return coords.toArray(new TileCoords[0]);
    }
}
