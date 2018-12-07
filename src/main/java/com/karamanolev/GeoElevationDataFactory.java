package com.karamanolev;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class GeoElevationDataFactory {
    public static GeoElevationData createGeoElevationData(FileHandler fileHandler) throws IOException {
        if (fileHandler == null) {
            fileHandler = new FileHandler("srtm");
        }

        HashMap<String, String> srtm1Files = new HashMap<>(), srtm3Files = new HashMap<>();
        try (InputStreamReader reader = new InputStreamReader(
                GeoElevationDataFactory.class.getResourceAsStream("/list.json"))) {
            JSONObject data = new JSONObject(new JSONTokener(reader));

            JSONObject srtm1Object = data.getJSONObject("srtm1");
            for (String key : srtm1Object.keySet()) {
                String value = srtm1Object.getString(key);
                srtm1Files.put(key, value);
            }

            JSONObject srtm3Object = data.getJSONObject("srtm3");
            for (String key : srtm3Object.keySet()) {
                String value = srtm3Object.getString(key);
                srtm3Files.put(key, value);
            }
        }

        return new GeoElevationData(srtm1Files, srtm3Files, fileHandler);
    }
}
