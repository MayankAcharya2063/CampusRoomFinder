package com.example.roomify.service;
import com.example.roomify.model.User;

public class AuthenticationService {

    private User currentUser;

    public User login(String email, String password) {

        // load users from file later

        // compare encoded passwords

        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}