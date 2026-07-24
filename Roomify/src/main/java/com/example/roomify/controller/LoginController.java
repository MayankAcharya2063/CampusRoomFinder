package com.example.roomify.controller;

import com.example.roomify.UserRole;
import com.example.roomify.model.User;
import com.example.roomify.service.AuthenticationService;
import com.example.roomify.service.SessionManager;
import com.example.roomify.validation.InputValidator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
                showErrorAlert("Validation Error", "Both email and password are required.");
                return;
            }

            // 2. Email format validation
            if (!InputValidator.isValidEmail(email)) {
                showErrorAlert("Invalid Email", "Please enter a valid Roomify email (e.g., example@roomify.com).");
                return;
            }

            // 3. Password strength validation
            if (!InputValidator.isValidPassword(password)) {
                showErrorAlert("Weak Password", "Password must contain at least 6 characters, including one letter and one number.");
                return;
            }

            // 4. Authentication and Role Verification
            User authenticatedUser = authService.authenticate(email, password);

            if (authenticatedUser != null) {
                // 5. Start Session
                sessionManager.login(authenticatedUser);
                showSuccessAlert("Login Successful", "Welcome back, " + authenticatedUser.getName() + "!");

                // 6. Central Stage Routing Manager (Abrila's Module)
                // NOTE: StageCoordinator is expected to be implemented later.
                // The call below is structured to work with it seamlessly.
                navigateToDashboard(authenticatedUser, event);
            } else {
                showErrorAlert("Authentication Failed", "Invalid username or password. Please try again.");
            }
        } finally {
            // Re-enable button
            loginButton.setDisable(false);
        }
    }

    /**
     * Stub for Abrila's routing framework to load different dashboards based on roles.
     * This method now uses the User object to retrieve the role.
     */
    private void navigateToDashboard(User user, ActionEvent event) {
        UserRole role = user.getRole();
        System.out.println("Routing user \"" + user.getName() + "\" to the " + role + " Dashboard...");

        // Get the current Stage from the event source
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

        // This is the placeholder for Abrila's StageCoordinator.
        // When implemented, it will replace this switch statement with something like:
        // StageCoordinator.getInstance().showDashboard(user, currentStage);
        switch (role) {
            case ADMIN:
                System.out.println("Loading Admin Dashboard...");
                // TODO: Abrila will load the Admin Dashboard FXML file here
                // Example: StageCoordinator.getInstance().loadAdminDashboard(currentStage);
                break;
            case STAFF:
                System.out.println("Loading Staff Dashboard...");
                // TODO: Load Staff Booking view
                // Example: StageCoordinator.getInstance().loadStaffDashboard(currentStage);
                break;
            case STUDENT:
                System.out.println("Loading Student Dashboard...");
                // TODO: Load Student Resource Explorer view
                // Example: StageCoordinator.getInstance().loadStudentDashboard(currentStage);
                break;
            default:
                showErrorAlert("Navigation Error", "Unknown user role. Cannot load dashboard.");
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