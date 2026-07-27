package com.example.roomify.service;

import com.example.roomify.UserRole;
import com.example.roomify.model.Admin;
import com.example.roomify.model.Staff;
import com.example.roomify.model.Student;
import com.example.roomify.model.User;
import com.example.roomify.persistence.UserFileHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthenticationService {
    private Map<String, User> userDatabase;

    // Singleton pattern (optional but good practice)
    private static AuthenticationService instance;
    private AuthenticationService() {
        this.userDatabase = new HashMap<>();
        loadUsers();
    }

    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Loads users from the persisted users.dat file.
     * If no file exists yet (first run), falls back to seeding
     * default accounts and immediately saves them to disk.
     */
    private void loadUsers() {
        List<User> storedUsers = UserFileHandler.loadUsers();

        if (storedUsers.isEmpty()) {
            loadDummyUsers();
            persistUsers();
        } else {
            for (User user : storedUsers) {
                userDatabase.put(user.getEmail().toLowerCase(), user);
            }
        }
    }

    // Default seed data used only on first run, before users.dat exists
    private void loadDummyUsers() {
        userDatabase.put("admin@roomify.com", new Admin("A001", "Admin User", "admin@roomify.com", "admin123", 1));
        userDatabase.put("staff@roomify.com", new Staff("S001", "Staff User", "staff@roomify.com", "staff123", "STF01", "Engineering"));
        userDatabase.put("student@roomify.com", new Student("ST001", "Student User", "student@roomify.com", "student123", "ST01", "Computer Science"));
    }

    /**
     * Saves the current in-memory user database to disk.
     */
    private void persistUsers() {
        UserFileHandler.saveUsers(new java.util.ArrayList<>(userDatabase.values()));
    }

    /**
     * Registers a new user and persists the updated user list to file.
     */
    public void registerUser(User user) {
        userDatabase.put(user.getEmail().toLowerCase(), user);
        persistUsers();
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

    /**
     * Returns the total number of registered users.
     */
    public int getUserCount() {
        return userDatabase.size();
    }
}