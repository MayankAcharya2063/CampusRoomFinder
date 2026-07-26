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
    private boolean usersLoaded = false;

    // Singleton pattern (optional but good practice)
    private static AuthenticationService instance;

    private AuthenticationService() {
        this.userDatabase = new HashMap<>();
        loadUsersFromFile();
        if (userDatabase.isEmpty()) {
            loadDummyUsers();
            saveUsersToFile();
        }
    }

    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Load users from the persistent file.
     */
    private void loadUsersFromFile() {
        List<User> users = UserFileHandler.loadUsers();
        if (users != null && !users.isEmpty()) {
            for (User user : users) {
                userDatabase.put(user.getEmail().toLowerCase(), user);
            }
            usersLoaded = true;
            System.out.println("Loaded " + users.size() + " users from file.");
        }
    }

    /**
     * Save users to the persistent file.
     */
    public void saveUsersToFile() {
        List<User> users = List.copyOf(userDatabase.values());
        UserFileHandler.saveUsers(users);
        UserFileHandler.saveUsersToText(users);
        System.out.println("Saved " + users.size() + " users to file.");
    }

    /**
     * Temporary dummy data for testing.
     */
    private void loadDummyUsers() {
        // Hardcoded users to match the LoginController's original logic
        userDatabase.put("admin@roomify.com", new Admin("A001", "Admin User",
                "admin@roomify.com", "admin123", 1));
        userDatabase.put("staff@roomify.com", new Staff("S001", "Staff User",
                "staff@roomify.com", "staff123", "STF01", "Engineering"));
        userDatabase.put("student@roomify.com", new Student("ST001", "Student User",
                "student@roomify.com", "student123", "ST01", "Computer Science"));
    }

    public User authenticate(String email, String password) {
        if (email == null || password == null) {
            return null;
        }

        // Ensure users are loaded
        if (!usersLoaded) {
            loadUsersFromFile();
            usersLoaded = true;
        }

        // Case-insensitive email lookup
        User user = userDatabase.get(email.toLowerCase());

        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Add a new user to the database.
     */
    public boolean addUser(User user) {
        if (user == null) {
            return false;
        }

        String email = user.getEmail().toLowerCase();
        if (userDatabase.containsKey(email)) {
            return false;
        }

        userDatabase.put(email, user);
        saveUsersToFile();
        return true;
    }

    /**
     * Update an existing user.
     */
    public boolean updateUser(User user) {
        if (user == null) {
            return false;
        }

        String email = user.getEmail().toLowerCase();
        if (!userDatabase.containsKey(email)) {
            return false;
        }

        userDatabase.put(email, user);
        saveUsersToFile();
        return true;
    }

    /**
     * Delete a user.
     */
    public boolean deleteUser(String email) {
        if (email == null) {
            return false;
        }

        String emailLower = email.toLowerCase();
        if (!userDatabase.containsKey(emailLower)) {
            return false;
        }

        userDatabase.remove(emailLower);
        saveUsersToFile();
        return true;
    }

    /**
     * Get all users.
     */
    public Map<String, User> getAllUsers() {
        return new HashMap<>(userDatabase);
    }
}