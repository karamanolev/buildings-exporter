package com.karamanolev.osmtiles;

import com.karamanolev.FileHandler;
import com.karamanolev.TileCoords;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;

public class OsmTileManager {
    private static final String[] SERVERS = new String[]{"a", "b", "c"};

    private int currentServer = 0;
    private FileHandler fileHandler;
    private HashMap<TileCoords, byte[]> tileCache;

    public OsmTileManager() {
        this.fileHandler = new FileHandler("osmTiles");
        this.tileCache = new HashMap<>();
    }

    private byte[] downloadTile(TileCoords tileCoords) throws IOException {
        String server = SERVERS[this.currentServer];
        this.currentServer = (this.currentServer + 1) % SERVERS.length;

        String url = String.format(
                "https://%s.tile.openstreetmap.org/%d/%d/%d.png",
                server, tileCoords.getZ(), tileCoords.getX(), tileCoords.getY()
        );

        System.out.println("Downloading tile image from OSM: " + url);
        Request request = Request.Get(url);
        request = request.addHeader("User-Agent", "Java/Karamanolev");
        Response response = request.execute();

        // Sleep for 1s after the request is executed
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }

        HttpResponse httpResponse = response.returnResponse();
        return EntityUtils.toByteArray(httpResponse.getEntity());
    }

    public byte[] getTile(TileCoords tileCoords) throws IOException {
        if (tileCache.containsKey(tileCoords)) {
            return tileCache.get(tileCoords);
        }

        String fileName = tileCoords.getZ() + "-" + tileCoords.getX() + "-" + tileCoords.getY() + ".png";
        byte[] data;

        if (fileHandler.exists(fileName)) {
            data = fileHandler.read(fileName);
        } else {
            data = this.downloadTile(tileCoords);
            fileHandler.write(fileName, data);
        }

        return data;
    }
}
