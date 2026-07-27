package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.UserRole;
import com.example.roomify.model.User;
import com.example.roomify.persistence.SystemLogger;
import com.example.roomify.service.AuthenticationService;
import com.example.roomify.service.SessionManager;
import com.example.roomify.util.AlertHelper;
import com.example.roomify.validation.InputValidator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    // Services
    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    void handleLoginSubmit(ActionEvent event) {
        // UI Guard: disable button during processing
        loginButton.setDisable(true);

        try {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            // 1. Empty field validation
            if (InputValidator.isNullOrEmpty(email) || InputValidator.isNullOrEmpty(password)) {
                AlertHelper.showError("Validation Error", "Both email and password are required.");
                return;
            }

            // 2. Email format validation
            if (!InputValidator.isValidEmail(email)) {
                AlertHelper.showError("Invalid Email", "Please enter a valid Roomify email (e.g., example@roomify.com).");
                return;
            }

            // 3. Password strength validation
            if (!InputValidator.isValidPassword(password)) {
                AlertHelper.showError("Weak Password", "Password must contain at least 6 characters, including one letter and one number.");
                return;
            }

            // 4. Authentication and Role Verification
            User authenticatedUser = authService.authenticate(email, password);

            if (authenticatedUser != null) {

                // 5. Start Session
                sessionManager.login(authenticatedUser);

                // Log successful login
                SystemLogger.logLogin(authenticatedUser.getEmail());

                AlertHelper.showInformation(
                        "Login Successful",
                        "Welcome back, " + authenticatedUser.getName() + "!"
                );

                // Navigate to dashboard
                navigateToDashboard(authenticatedUser, event);

            } else {
                AlertHelper.showError("Authentication Failed", "Invalid username or password. Please try again.");
            }
        } finally {
            // Re-enable button
            loginButton.setDisable(false);
        }
    }

    /**
     * Routes different user roles to their respective views via StageCoordinator.
     */
    private void navigateToDashboard(User user, ActionEvent event) {
        UserRole role = user.getRole();
        System.out.println("Routing user \"" + user.getName() + "\" to the " + role + " Dashboard...");

        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

        switch (role) {
            case ADMIN:
                System.out.println("Loading Admin Dashboard...");
                StageCoordinator.getInstance().showAdminDashboard(user, currentStage);
                break;
            case STAFF:
                System.out.println("Loading Staff Dashboard...");
                StageCoordinator.getInstance().showStaffDashboard(user, currentStage);
                break;
            case STUDENT:
                System.out.println("Loading Student Dashboard...");
                StageCoordinator.getInstance().showStudentDashboard(user, currentStage);
                break;
            default:
                AlertHelper.showError("Navigation Error", "Unknown user role. Cannot load dashboard.");
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
}