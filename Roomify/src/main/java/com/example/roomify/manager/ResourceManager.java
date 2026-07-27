package com.example.roomify.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic Resource Manager.
 * Stores and manages campus resources.
 */
public class ResourceManager<T> {

    // Stores resources using ArrayList
    private final List<T> resources = new ArrayList<>();

    // Stores resources using HashMap
    private final Map<String, T> resourceMap = new HashMap<>();

    /**
     * Add a resource.
     */
    public void addResource(String id, T resource) {

        resources.add(resource);
        resourceMap.put(id, resource);

    }

    /**
     * Remove resource.
     */
    public boolean removeResource(String id) {

        T resource = resourceMap.remove(id);

        if(resource != null){

            resources.remove(resource);

            return true;

        }

        return false;

    }

    /**
     * Find resource by ID.
     */
    public T getResource(String id){

        return resourceMap.get(id);

    }

    /**
     * Returns all resources.
     */
    public List<T> getAllResources(){

        return resources;

    }

    /**
     * Check if resource exists.
     */
    public boolean containsResource(String id){

        return resourceMap.containsKey(id);

    }

    /**
     * Display all resources.
     */
    public void displayResources(){

        if(resources.isEmpty()){

            System.out.println("No resources available.");

            return;

        }

        for(T resource : resources){

            System.out.println(resource);

        }

    }

}