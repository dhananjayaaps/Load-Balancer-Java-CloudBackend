package com.cloudbackend.frontend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
    public static byte[] fileToBytes(String filePath) throws IOException {
        Path path = new File(filePath).toPath();
        return Files.readAllBytes(path);
    }
}
