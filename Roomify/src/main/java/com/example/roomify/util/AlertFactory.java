package com.example.roomify.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.util.Optional;

/**
 * Factory class for creating standardized UI alerts and dialogs.
 * Provides a consistent look and feel for all error, warning,
 * information, and confirmation dialogs in the application.
 */
public class AlertFactory {

    private static AlertFactory instance;

    private AlertFactory() {
        // Private constructor for singleton pattern
    }

    public static AlertFactory getInstance() {
        if (instance == null) {
            instance = new AlertFactory();
        }
        return instance;
    }

    /**
     * Shows an error alert dialog.
     *
     * @param title The title of the alert
     * @param message The error message to display
     */
    public void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an information alert dialog.
     *
     * @param title The title of the alert
     * @param message The information message to display
     */
    public void showInfoAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert dialog.
     *
     * @param title The title of the alert
     * @param message The warning message to display
     */
    public void showWarningAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog with Yes/No buttons.
     *
     * @param title The title of the dialog
     * @param message The confirmation message
     * @return true if the user clicked Yes, false otherwise
     */
    public boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(title);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows a custom error alert with an exception message.
     *
     * @param title The title of the alert
     * @param exception The exception that was thrown
     */
    public void showExceptionAlert(String title, Exception exception) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }

    /**
     * Shows a success alert dialog.
     *
     * @param title The title of the alert
     * @param message The success message to display
     */
    public void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a validation error alert specifically for form validation.
     *
     * @param fieldName The name of the field that failed validation
     * @param errorMessage The validation error message
     */
    public void showValidationError(String fieldName, String errorMessage) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Invalid " + fieldName);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    /**
     * Shows an alert with custom styling.
     *
     * @param type The type of alert
     * @param title The title
     * @param header The header text
     * @param content The content text
     */
    public void showCustomAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog with custom button text.
     *
     * @param title The title
     * @param message The confirmation message
     * @param confirmText Text for the confirm button
     * @param cancelText Text for the cancel button
     * @return true if the user clicked confirm, false otherwise
     */
    public boolean showCustomConfirmation(String title, String message, String confirmText, String cancelText) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(title);
        alert.setContentText(message);

        ButtonType confirmButton = new ButtonType(confirmText);
        ButtonType cancelButton = new ButtonType(cancelText);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }

    /**
     * Displays an alert and automatically closes after a timeout.
     * Useful for notifications that shouldn't require user interaction.
     *
     * @param type The type of alert
     * @param title The title
     * @param message The message
     * @param timeoutMs Time in milliseconds before auto-close
     */
    public void showAutoCloseAlert(AlertType type, String title, String message, long timeoutMs) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Auto-close after timeout
        new Thread(() -> {
            try {
                Thread.sleep(timeoutMs);
                // Check if alert is still showing
                if (alert.getDialogPane().getScene() != null) {
                    alert.close();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        alert.showAndWait();
    }

    /**
     * Shows a detailed error alert with stack trace (for debugging).
     *
     * @param title The title
     * @param exception The exception with stack trace
     */
    public void showDetailedError(String title, Exception exception) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);

        String stackTrace = java.util.Arrays.toString(exception.getStackTrace());
        alert.setContentText(exception.getMessage() + "\n\n" + stackTrace);

        alert.showAndWait();
    }
}