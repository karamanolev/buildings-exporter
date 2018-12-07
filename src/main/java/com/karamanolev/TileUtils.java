package com.karamanolev;

public class TileUtils {
    private static LatLng tileToCoordinates(TileCoords coords) {
        double x = coords.getX(), y = coords.getY(), z = coords.getZ();

        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return new LatLng(
                Math.toDegrees(Math.atan(Math.sinh(n))),
                x / Math.pow(2.0, z) * 360.0 - 180
        );
    }

    public static BoundingBox getBoundingBox(TileCoords coords) {
        return new BoundingBox(
                tileToCoordinates(coords),
                tileToCoordinates(new TileCoords(coords.getX() + 1, coords.getY() + 1, coords.getZ()))
        );
    }

    public static TileCoords getTileCoords(LatLng latLng, int zoom) {
        double lat = latLng.getLat(), lng = latLng.getLng();

        int tileX = (int) Math.floor((lng + 180) / 360 * (1 << zoom));
        int tileY = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        if (tileX < 0)
            tileX = 0;
        if (tileX >= (1 << zoom))
            tileX = ((1 << zoom) - 1);
        if (tileY < 0)
            tileY = 0;
        if (tileY >= (1 << zoom))
            tileY = ((1 << zoom) - 1);
        return new TileCoords(tileX, tileY, zoom);
    }

    public static TileCoords[] getTileChildren(TileCoords tileCoords) {
        int twoX = tileCoords.getX() << 1,
                twoY = tileCoords.getY() << 1,
                z = tileCoords.getZ();
        return new TileCoords[]{
                new TileCoords(twoX, twoY, z + 1),
                new TileCoords(twoX + 1, twoY, z + 1),
                new TileCoords(twoX + 1, twoY + 1, z + 1),
                new TileCoords(twoX, twoY + 1, z + 1)
        };
    }
}
