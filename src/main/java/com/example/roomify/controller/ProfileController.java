package com.example.roomify.controller;

import com.example.roomify.model.User;
import com.example.roomify.service.AuthenticationService;
import com.example.roomify.security.PasswordEncoder;
import com.example.roomify.service.SessionManager;
import com.example.roomify.util.AlertHelper;
import com.example.roomify.validation.InputValidator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Profile Controller - Displays and manages user profile.
 */
public class ProfileController {

    @FXML private Label nameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label emailLabel;
    @FXML private Label departmentLabel;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final AuthenticationService authService = AuthenticationService.getInstance();
    private User currentUser;

    public void initContext(User user) {
        this.currentUser = user;
        displayProfile();
    }

    private void displayProfile() {
        if (currentUser != null) {
            nameLabel.setText(currentUser.getName());
            usernameLabel.setText(currentUser.getUserId());
            roleLabel.setText(currentUser.getRole().name());
            emailLabel.setText(currentUser.getEmail());
            departmentLabel.setText("N/A");
        }
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!PasswordEncoder.matches(currentPassword, currentUser.getPassword())) {
            AlertHelper.showError("Error", "Current password is incorrect.");
            return;
        }

        if (newPassword.isEmpty()) {
            AlertHelper.showError("Error", "New password cannot be empty.");
            return;
        }

        if (!InputValidator.isValidPassword(newPassword)) {
            AlertHelper.showError("Invalid Password",
                    "Password must be at least 6 characters,\ncontaining one letter and one number.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            AlertHelper.showError("Error", "New passwords do not match.");
            return;
        }

        currentUser.setPassword(PasswordEncoder.encode(newPassword));
        authService.updateUser(currentUser);

        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();

        AlertHelper.showInformation("Success", "Password changed successfully.");

        com.example.roomify.persistence.SystemLogger.logAdminAction("Password changed for: " + currentUser.getEmail());
    }

    @FXML
    private void handleRefresh() {
        displayProfile();
        AlertHelper.showInformation("Refreshed", "Profile refreshed.");
    }
}