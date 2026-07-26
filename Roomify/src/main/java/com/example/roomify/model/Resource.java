package com.example.roomify.model;

import java.io.Serializable;

/**
 * Resource model for campus resources.
 */
public class Resource implements Serializable {
    private static final long serialVersionUID = 1L;

    private String resourceId;
    private String name;
    private String type;
    private int capacity;
    private String status;
    private String location;

    // Constructor with location
    public Resource(String resourceId, String name, String type, int capacity, String status, String location) {
        this.resourceId = resourceId;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.status = status;
        this.location = location;
    }

    // Legacy constructor for backward compatibility
    public Resource(String resourceId, String name, String type, int capacity, String status) {
        this(resourceId, name, type, capacity, status, "N/A");
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

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public String toString() {
        return name + " (" + type + ", capacity " + capacity + ")";
    }
}