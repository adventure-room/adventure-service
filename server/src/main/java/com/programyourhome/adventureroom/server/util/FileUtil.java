package com.programyourhome.adventureroom.server.util;

import java.io.File;

public class FileUtil {

    private FileUtil() {
    }

    public static void assertFileExists(String path) {
        assertFileExists(new File(path));
    }

    public static void assertFileExists(File file) {
        if (!file.exists()) {
            throw new IllegalStateException("File: '" + file + "' does not exist");
        }
    }

    public static void assertDirectoryExists(String path) {
        assertDirectoryExists(new File(path));
    }

    public static void assertDirectoryExists(File file) {
        assertFileExists(file);
        if (!file.isDirectory()) {
            throw new IllegalStateException("File: '" + file + "' is not a directory");
        }
    }

}
