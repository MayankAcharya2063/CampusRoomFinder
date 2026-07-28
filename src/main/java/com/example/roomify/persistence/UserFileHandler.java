package com.example.roomify.persistence;

import com.example.roomify.model.User;

import java.util.List;

/**
 * Handles saving and loading user accounts.
 */
public class UserFileHandler {

    // Binary file for user accounts
    private static final String USER_FILE = "users.dat";

    /**
     * Save all users.
     */
    public static void saveUsers(List<User> users) {
        FilePersistenceEngine.saveObjects(users, USER_FILE);
    }

    /**
     * Load all users.
     */
    public static List<User> loadUsers() {
        return FilePersistenceEngine.loadObjects(USER_FILE);
    }
}