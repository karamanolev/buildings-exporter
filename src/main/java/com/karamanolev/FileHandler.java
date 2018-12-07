package com.karamanolev;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler implements IFileHandler {
    private static Path getCacheDirectory(String subcache, String fileName) {
        return Paths.get(System.getProperty("user.home"), "terrainerCache").resolve(subcache).resolve(fileName);
    }

    private String subcache;

    public FileHandler(String subcache) {
        this.subcache = subcache;
    }

    private void ensureExists() throws IOException {
        Path cacheDir = getCacheDirectory(this.subcache, "");
        if (!Files.isDirectory(cacheDir)) {
            Files.createDirectories(cacheDir);
        }
    }

    @Override
    public boolean exists(String fileName) {
        return Files.exists(getCacheDirectory(this.subcache, fileName));
    }

    @Override
    public byte[] read(String fileName) throws IOException {
        return Files.readAllBytes(getCacheDirectory(this.subcache, fileName));
    }

    public void write(String fileName, byte[] contents) throws IOException {
        this.ensureExists();
        Files.write(getCacheDirectory(this.subcache, fileName), contents);
    }
}
