package com.example.roomify.service;

import com.example.roomify.exception.UnauthorizedAccessException;
import com.example.roomify.model.ResourceStatus;
import com.example.roomify.model.User;
import com.example.roomify.security.AuthorizationGuard;

import java.util.HashMap;
import java.util.Map;

public class ResourceStatusManager {

    private static ResourceStatusManager instance;

    // resourceId -> current status
    private Map<String, ResourceStatus> resourceStatusMap;

    private ResourceStatusManager() {
        resourceStatusMap = new HashMap<>();
    }

    public static ResourceStatusManager getInstance() {
        if (instance == null) {
            instance = new ResourceStatusManager();
        }
        return instance;
    }

    public void updateStatus(String resourceId, ResourceStatus newStatus, User adminUser)
            throws UnauthorizedAccessException {

        AuthorizationGuard.requireAdmin(adminUser.getRole());

        resourceStatusMap.put(resourceId, newStatus);
        System.out.println("Resource " + resourceId + " status changed to " + newStatus
                + " by " + adminUser.getName());
    }

    public ResourceStatus getStatus(String resourceId) {
        return resourceStatusMap.getOrDefault(resourceId, ResourceStatus.AVAILABLE);
    }

    public boolean isAvailable(String resourceId) {
        return getStatus(resourceId) == ResourceStatus.AVAILABLE;
    }
}
