package com.example.roomify.service;

import com.example.roomify.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        System.out.println("Session started for: " + user.getName());
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("Session ended for: " + currentUser.getName());
        }
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}