package com.example.roomify.service;

import com.example.roomify.UserRole;
import com.example.roomify.model.Admin;
import com.example.roomify.model.Staff;
import com.example.roomify.model.Student;
import com.example.roomify.model.User;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationService {
    private Map<String, User> userDatabase;

    // Singleton pattern (optional but good practice)
    private static AuthenticationService instance;
    private AuthenticationService() {
        this.userDatabase = new HashMap<>();
        loadDummyUsers();
    }

    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    // Temporary dummy data for testing
    private void loadDummyUsers() {
        // Hardcoded users to match the LoginController's original logic
        userDatabase.put("admin@roomify.com", new Admin("A001", "Admin User", "admin@roomify.com", "admin123", 1));
        userDatabase.put("staff@roomify.com", new Staff("S001", "Staff User", "staff@roomify.com", "staff123", "STF01", "Engineering"));
        userDatabase.put("student@roomify.com", new Student("ST001", "Student User", "student@roomify.com", "student123", "ST01", "Computer Science"));
    }

    public User authenticate(String email, String password) {
        if (email == null || password == null) {
            return null;
        }

        // Case-insensitive email lookup
        User user = userDatabase.get(email.toLowerCase());

        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    // This method will be used later to load users from a file
    // public void loadUsersFromFile(String filePath) { ... }
}