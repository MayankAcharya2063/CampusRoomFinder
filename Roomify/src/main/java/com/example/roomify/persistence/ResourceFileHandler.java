package com.example.roomify.persistence;

import com.example.roomify.model.Resource;

import java.util.List;

/**
 * Handles saving and loading campus resources.
 */
public class ResourceFileHandler {

    private static final String RESOURCE_FILE = "resources.dat";

    /**
     * Saves all resources.
     */
    public static void saveResources(List<Resource> resources) {
        FilePersistenceEngine.saveObjects(resources, RESOURCE_FILE);
    }

    /**
     * Loads all resources.
     */
    @SuppressWarnings("unchecked")
    public static List<Resource> loadResources() {
        return FilePersistenceEngine.loadObjects(RESOURCE_FILE);
    }

    /**
     * Save resources to text file (for readability).
     */
    public static void saveResourcesToText(List<Resource> resources) {
        StringBuilder sb = new StringBuilder();
        for (Resource resource : resources) {
            sb.append(resource.getResourceId()).append("|")
                    .append(resource.getName()).append("|")
                    .append(resource.getType()).append("|")
                    .append(resource.getLocation()).append("|")
                    .append(resource.getCapacity()).append("|")
                    .append(resource.getStatus())
                    .append("\n");
        }
        FilePersistenceEngine.writeText("resources.txt", sb.toString());
    }
}