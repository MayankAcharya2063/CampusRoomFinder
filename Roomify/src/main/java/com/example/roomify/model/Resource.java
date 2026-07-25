package com.example.roomify.model;

import java.io.Serializable;

/**
 * TEMPORARY placeholder until the real Resource class exists.
 * Field names match what the GUI needs, so swapping later should be easy.
 */
public class Resource implements Serializable {
    private static final long serialVersionUID = 1L;

    private String resourceId;
    private String name;
    private String type;      // e.g. "Study Room", "Lab", "Auditorium"
    private int capacity;
    private String status;    // e.g. "AVAILABLE", "UNAVAILABLE", "MAINTENANCE"

    public Resource(String resourceId, String name, String type, int capacity, String status) {
        this.resourceId = resourceId;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.status = status;
    }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return name + " (" + type + ", capacity " + capacity + ")";
    }
}
