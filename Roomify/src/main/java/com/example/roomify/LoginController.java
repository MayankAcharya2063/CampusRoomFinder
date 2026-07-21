package com.example.roomify;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    void handleLoginSubmit(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showErrorAlert("Validation Error", "Please fill in both the username/email and password fields.");
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