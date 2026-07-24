package com.example.roomify.controller;

import com.example.roomify.UserRole;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.example.roomify.validation.InputValidator;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    void handleLoginSubmit(ActionEvent event) {// UI Guard: disable button during processing
        loginButton.setDisable(true);

        try {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            // Empty field validation
            if (InputValidator.isNullOrEmpty(email) ||
                    InputValidator.isNullOrEmpty(password)) {

                showErrorAlert(
                        "Validation Error",
                        "Both email and password are required.");
                return;
            }

            // Email format validation
            if (!InputValidator.isValidEmail(email)) {
                showErrorAlert(
                        "Invalid Email",
                        "Please enter a valid Roomify email (example@roomify.com).");
                return;
            }

            // Password strength validation
            if (!InputValidator.isValidPassword(password)) {
                showErrorAlert(
                        "Weak Password",
                        "Password must contain at least 6 characters, including one letter and one number.");
                return;
            }
        //Authentication and Role Verification
        // Dibisha's persistence layer will eventually replace this by reading files.
        UserRole authenticatedRole = authenticateUser(email, password);

        if (authenticatedRole != null) {
            showSuccessAlert("Login Successful", "Welcome back! Redirecting to your dashboard.");

            // 3. Central Stage Routing Manager (Abrila's Module)
            navigateToDashboard(authenticatedRole, event);
        } else {
            showErrorAlert("Authentication Failed", "Invalid username or password. Please try again.");
        }
        } finally {
            // Re-enable button
            loginButton.setDisable(false);
        }
    }
    @FXML
    public void initialize() {

        // Limit email length
        emailField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.length() > 50) {
                emailField.setText(oldValue);
            }
        });

        // Limit password length
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.length() > 20) {
                passwordField.setText(oldValue);
            }
        });
    }

    /**
     * Temporary hardcoded credential checker for testing UI workflows.
     * This fulfills the system requirements for verifying Student, Staff, and Admin paths.
     */
    private UserRole authenticateUser(String email, String password) {
        if (email.equalsIgnoreCase("admin@roomify.com") && password.equals("admin123")) {
            return UserRole.ADMIN;
        } else if (email.equalsIgnoreCase("staff@roomify.com") && password.equals("staff123")) {
            return UserRole.STAFF;
        } else if (email.equalsIgnoreCase("student@roomify.com") && password.equals("student123")) {
            return UserRole.STUDENT;
        }
        return null; // Explicit failure if credentials don't match any profile
    }

    /**
     * Stub for Abrila's routing framework to load different dashboards based on roles.
     */
    private void navigateToDashboard(UserRole role, ActionEvent event) {
        System.out.println("Routing user to the " + role + " Dashboard...");

        switch (role) {
            case ADMIN:
                // TODO: Abrila will load the Admin Dashboard FXML file here
                break;
            case STAFF:
                // TODO: Load Staff Booking view
                break;
            case STUDENT:
                // TODO: Load Student Resource Explorer view
                break;
        }
    }
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}