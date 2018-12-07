package com.karamanolev;

import java.io.IOException;

public interface IFileHandler {
    boolean exists(String fileName);

    byte[] read(String fileName) throws IOException;

    void write(String fileName, byte[] contents) throws IOException;
}
