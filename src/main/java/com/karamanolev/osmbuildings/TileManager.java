package com.karamanolev.osmbuildings;

import com.karamanolev.FileHandler;
import com.karamanolev.TileCoords;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class TileManager {
    private static int ZOOM = 15;
    private static String[] SERVERS = new String[]{"a", "b", "c", "d"};

    private int currentServer = 0;
    private FileHandler fileHandler;
    private HashMap<TileCoords, Tile> tileCache;

    public TileManager() {
        this.fileHandler = new FileHandler("osmBuildings");
        this.tileCache = new HashMap<>();
    }

    private Tile createTileFromData(TileCoords tileCoords, byte[] data) {
        if (data.length == 0) {
            return new Tile(tileCoords);
        } else {
            JSONObject jsonData = new JSONObject(new String(data, StandardCharsets.UTF_8));
            return new Tile(tileCoords, jsonData);
        }
    }

    private byte[] downloadTile(TileCoords tileCoords) throws IOException {
        String server = SERVERS[this.currentServer];
        this.currentServer = (this.currentServer + 1) % SERVERS.length;

        String url = String.format(
                "https://%s.data.osmbuildings.org/0.2/ph2apjye/tile/%d/%d/%d.json",
                server, tileCoords.getZ(), tileCoords.getX(), tileCoords.getY()
        );

        System.out.println("Downloading tile from OsmBuildings: " + url);
        Request request = Request.Get(url);
        request = request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36")
                .addHeader("Origin", "https://osmbuildings.org");
        Response response = request.execute();

        // Sleep for 1s after the request is executed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        HttpResponse httpResponse = response.returnResponse();
        if (httpResponse.getStatusLine().getStatusCode() == 204) {
            return new byte[0];
        } else {
            return EntityUtils.toByteArray(httpResponse.getEntity());
        }
    }

    public Tile getTile(TileCoords tileCoords) throws IOException {
        if (tileCoords.getZ() != ZOOM) {
            throw new RuntimeException("OsmBuildings only supports zoom 15");
        }

        if (tileCache.containsKey(tileCoords)) {
            return tileCache.get(tileCoords);
        }

        String fileName = tileCoords.getZ() + "-" + tileCoords.getX() + "-" + tileCoords.getY() + ".json";
        byte[] data;

        if (fileHandler.exists(fileName)) {
            data = fileHandler.read(fileName);
        } else {
            data = this.downloadTile(tileCoords);
            fileHandler.write(fileName, data);
        }

        Tile tile = this.createTileFromData(tileCoords, data);
        this.tileCache.put(tileCoords, tile);
        return tile;
    }

    public Tile[] getTiles(TileCoords tileCoords) throws IOException {
        int left = tileCoords.getX();
        int top = tileCoords.getY();
        int right = left + 1;
        int bottom = top + 1;

        for (int i = tileCoords.getZ(); i < 15; i++) {
            left *= 2;
            top *= 2;
            right *= 2;
            bottom *= 2;
        }

        ArrayList<Tile> tiles = new ArrayList<Tile>();
        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                tiles.add(this.getTile(new TileCoords(x, y, ZOOM)));
            }
        }

        return tiles.toArray(new Tile[0]);
    }
}
