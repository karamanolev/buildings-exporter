package com.karamanolev.geojsontiles;

import com.karamanolev.LatLng;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class GeoJsonTiles {
    private HashMap<Integer, ArrayList<LatLng>> tiles = new HashMap<>();

    public GeoJsonTiles(String path) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        JSONObject root = new JSONObject(content);
        for (Object featureObj : root.getJSONArray("features")) {
            JSONObject feature = (JSONObject) featureObj;
            int id = feature.getJSONObject("properties").getInt("pkuid");
            JSONObject geometry = feature.getJSONObject("geometry");
            if (!geometry.getString("type").equals("Polygon")) {
                throw new RuntimeException("Unexpected feature type: " + geometry.getString("type"));
            }

            ArrayList<LatLng> latLngs = new ArrayList<>();
            for (Object coordObj : (JSONArray) (geometry.getJSONArray("coordinates").get(0))) {
                JSONArray coords = (JSONArray) coordObj;
                latLngs.add(new LatLng(coords.getDouble(1), coords.getDouble(0)));
            }
            tiles.put(id, latLngs);
        }
    }

    public LatLng[] getPolygon(int id) {
        return tiles.get(id).toArray(new LatLng[0]);
    }
}
