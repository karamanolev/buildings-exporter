package com.karamanolev.osmb;

import com.karamanolev.LatLng;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OsmbFeature {
    private String id;
    private LatLng[][] contours;
    private HashMap<String, Object> properties;

    public String getId() {
        return id;
    }

    public LatLng[][] getContours() {
        return contours;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public double getComputedHeight() throws ClassCastException {
        Object height = this.properties.get("height");
        if (height != null) {
            if (height instanceof Integer) {
                return (double) (Integer) height;
            } else if (height instanceof Double) {
                return (Double) height;
            } else {
                throw new ClassCastException("Height is neither Integer nor Double");
            }
        }
        return (Integer) this.properties.getOrDefault("levels", 2);
    }

    public OsmbFeature(JSONObject data) {
        assert data.getString("type").equals("OsmbFeature");
        data.remove("type");

        this.id = data.getString("id");
        data.remove("id");

        this.parseGeometry(data.getJSONObject("geometry"));
        data.remove("geometry");

        this.properties = new HashMap<>();
        Map<String, Object> properties = data.getJSONObject("properties").toMap();
        for (String key : properties.keySet()) {
            this.properties.put(key, properties.get(key));
        }
        data.remove("properties");

        assert data.length() == 0;
    }

    private void parseGeometry(JSONObject data) {
        assert data.getString("type").equals("Polygon");
        data.remove("type");

        JSONArray jsonCoords = data.getJSONArray("coordinates");
        data.remove("coordinates");

        ArrayList<LatLng[]> contours = new ArrayList<>();
        for (Object jsonContour : jsonCoords) {
            ArrayList<LatLng> coordinates = new ArrayList<>();
            for (Object item : (JSONArray) jsonContour) {
                JSONArray jsonLatLng = (JSONArray) item;
                coordinates.add(new LatLng(jsonLatLng.getDouble(1), jsonLatLng.getDouble(0)));
            }
            contours.add(coordinates.toArray(new LatLng[0]));
        }
        this.contours = contours.toArray(new LatLng[0][]);

        assert data.length() == 0;
    }
}