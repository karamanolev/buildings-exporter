package com.karamanolev;

import org.apache.http.client.fluent.Request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipInputStream;

public class GeoElevationData {
    private HashMap<String, String> srtm1Files, srtm3Files;
    private HashMap<String, GeoElevationFile> fileCache = new HashMap<>();
    private FileHandler fileHandler;

    public GeoElevationData(HashMap<String, String> srtm1Files, HashMap<String, String> srtm3Files,
                            FileHandler fileHandler) {
        this.srtm1Files = srtm1Files;
        this.srtm3Files = srtm3Files;
        this.fileHandler = fileHandler;
    }

    private String getFileName(double latitude, double longitude) {
        String northSouth = latitude >= 0 ? "N" : "S";
        String eastWest = longitude >= 0 ? "E" : "W";
        int latValue = Math.abs((int) Math.floor(latitude));
        int lngValue = Math.abs((int) Math.floor(longitude));
        return String.format("%s%02d%s%03d.hgt", northSouth, latValue, eastWest, lngValue);
    }

    private byte[] retrieveOrLoadFileData(String fileName) throws IOException {
        String zipName = fileName + ".zip";
        if (this.fileHandler.exists(zipName)) {
            return this.fileHandler.read(zipName);
        }

        String url;
        if (this.srtm1Files.containsKey(fileName)) {
            url = this.srtm1Files.get(fileName);
        } else if (this.srtm3Files.containsKey(fileName)) {
            url = this.srtm3Files.get(fileName);
        } else {
            throw new IOException("File not found in either srtm1 or srtm3.");
        }

        System.out.println("Downloading SRTM data file");
        byte[] content = Request.Get(url).execute().returnContent().asBytes();
        this.fileHandler.write(zipName, content);
        return content;
    }

    private byte[] unzip(byte[] zipFile) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(zipFile);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(0);

        try (ZipInputStream zipStream = new ZipInputStream(byteStream)) {
            zipStream.getNextEntry(); // File contains just 1 entry

            byte[] buf = new byte[4096];
            while (true) {
                int read = zipStream.read(buf, 0, buf.length);
                if (read == -1) {
                    break;
                }
                outputStream.write(buf, 0, read);
            }

            zipStream.closeEntry();
        }

        return outputStream.toByteArray();
    }

    private GeoElevationFile getFile(double latitude, double longitude) throws IOException {
        String fileName = this.getFileName(latitude, longitude);
        if (fileName == null) {
            return null;
        }

        if (this.fileCache.containsKey(fileName)) {
            return this.fileCache.get(fileName);
        }

        byte[] data = this.unzip(this.retrieveOrLoadFileData(fileName));
        GeoElevationFile elevationFile = new GeoElevationFile(fileName, data);
        this.fileCache.put(fileName, elevationFile);

        return elevationFile;
    }

    public short getElevation(double latitude, double longitude) {
        GeoElevationFile file;
        try {
            file = this.getFile(latitude, longitude);
        } catch (IOException e) {
            return Short.MIN_VALUE;
        }
        if (file == null) {
            return Short.MIN_VALUE;
        }
        return file.getElevation(latitude, longitude);
    }

    public short[][] getElevationGrid(LatLng southEast, LatLng northWest, int points, int samples) throws IOException {
        XY southEastXY = SphericalMercator.toXY(southEast);
        XY northWestXY = SphericalMercator.toXY(northWest);

        double startX = northWestXY.getX();
        double startY = northWestXY.getY();
        double vecX = (southEastXY.getX() - startX);
        double vecY = (southEastXY.getY() - startY);

        double pixelVecX = vecX / (points - 1);
        double pixelVecY = vecY / (points - 1);

        short[][] data = new short[points][points];

        for (int pixelY = 0; pixelY < points; pixelY++) {
            double pixelStartY = startY + vecY * pixelY / (points - 1);

            for (int pixelX = 0; pixelX < points; pixelX++) {
                double pixelStartX = startX + vecX * pixelX / (points - 1);

                int sum = 0;
                for (int sampleY = 0; sampleY < samples; sampleY++) {
                    double y = pixelStartY + pixelVecY * sampleY / samples;

                    for (int sampleX = 0; sampleX < samples; sampleX++) {
                        double x = pixelStartX + pixelVecX * sampleX / samples;
                        LatLng pixelLocation = SphericalMercator.toLatLng(new XY(x, y));
                        sum += this.getElevation(pixelLocation.getLat(), pixelLocation.getLng());
                    }
                }

                data[pixelY][pixelX] = (short) (sum / (double) (samples * samples));
            }
        }

        return data;
    }
}
