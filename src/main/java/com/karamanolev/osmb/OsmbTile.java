package com.karamanolev.osmb;

import com.karamanolev.TileCoords;
import org.json.JSONObject;

import java.util.ArrayList;

public class OsmbTile {
    private TileCoords coords;
    private OsmbFeature[] features;

    public TileCoords getCoords() {
        return coords;
    }

    public OsmbFeature[] getFeatures() {
        return features;
    }

    public OsmbTile(TileCoords coords) {
        this.coords = coords;
        this.features = new OsmbFeature[0];
    }

    public OsmbTile(TileCoords coords, JSONObject data) {
        this.coords = coords;

        assert data.getString("type").equals("FeatureCollection");
        data.remove("type");

        ArrayList<OsmbFeature> features = new ArrayList<>();
        int i = 0;
        for (Object obj : data.getJSONArray("features")) {
            features.add(new OsmbFeature((JSONObject) obj));
        }
        this.features = features.toArray(new OsmbFeature[0]);
        data.remove("features");

        assert data.length() == 0;
    }
}