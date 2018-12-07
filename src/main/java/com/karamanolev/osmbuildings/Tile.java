package com.karamanolev.osmbuildings;

import com.karamanolev.TileCoords;
import org.json.JSONObject;

import java.util.ArrayList;

public class Tile {
    private TileCoords coords;
    private Feature[] features;

    public TileCoords getCoords() {
        return coords;
    }

    public Feature[] getFeatures() {
        return features;
    }

    public Tile(TileCoords coords) {
        this.coords = coords;
        this.features = new Feature[0];
    }

    public Tile(TileCoords coords, JSONObject data) {
        this.coords = coords;

        assert data.getString("type").equals("FeatureCollection");
        data.remove("type");

        ArrayList<Feature> features = new ArrayList<>();
        int i = 0;
        for (Object obj : data.getJSONArray("features")) {
            features.add(new Feature((JSONObject) obj));
        }
        this.features = features.toArray(new Feature[0]);
        data.remove("features");

        assert data.length() == 0;
    }
}