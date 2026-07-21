package com.example.roomify.model;

import com.example.roomify.UserRole;
import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private int adminLevel;
    private List<String> permissions;

    public Admin(String userId, String name, String email, String password, int adminLevel) {
        super(userId, name, email, password, UserRole.ADMIN);
        this.adminLevel = adminLevel;
        this.permissions = new ArrayList<>();
        initializeDefaultPermissions();
    }

    private void initializeDefaultPermissions() {
        this.permissions.add("MANAGE_RESOURCES");
        this.permissions.add("APPROVE_BOOKINGS");
        this.permissions.add("VIEW_LOGS");
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    // Getters and Setters
    public int getAdminLevel() { return adminLevel; }
    public void setAdminLevel(int adminLevel) { this.adminLevel = adminLevel; }

    public List<String> getPermissions() { return permissions; }
}