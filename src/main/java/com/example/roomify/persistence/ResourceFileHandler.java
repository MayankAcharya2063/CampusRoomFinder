package com.example.roomify.persistence;

import java.util.List;

/**
 * Handles saving and loading campus resources.
 */
public class ResourceFileHandler {

    private static final String RESOURCE_FILE = "resources.dat";

    /**
     * Saves all resources.
     */
    public static <T> void saveResources(List<T> resources) {
        FilePersistenceEngine.saveObjects(resources, RESOURCE_FILE);
    }

    /**
     * Loads all resources.
     */
    public static <T> List<T> loadResources() {
        return FilePersistenceEngine.loadObjects(RESOURCE_FILE);
    }
}