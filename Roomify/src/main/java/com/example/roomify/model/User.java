package com.example.roomify.model;

import com.example.roomify.UserRole;

import java.io.Serializable;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String name;
    private String email;
    private String password;
    private UserRole role;

    public User(String userId, String name, String email, String password, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Common operational methods required by the Use Case blueprint
    public boolean login(String inputEmail, String inputPassword) {
        return this.email.equalsIgnoreCase(inputEmail) && this.password.equals(inputPassword);
    }

    public void logout() {
        System.out.println(name + " logged out successfully.");
    }

    // Getters and Setters for encapsulation
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}