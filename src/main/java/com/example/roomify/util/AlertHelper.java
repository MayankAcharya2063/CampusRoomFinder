package com.example.roomify.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class AlertHelper {

    /**
     * Displays an Information alert popup with a title and content message.
     *
     * @param title   The text displayed in the dialog's title bar.
     * @param message The detailed message displayed in the dialog body.
     */
    public static void showInformation(String title, String message) {
        showInformation(title, null, message);
    }

    /**
     * Overload to display an Information alert with a header.
     *
     * @param title   The text displayed in the dialog's title bar.
     * @param header  The header text (can be null for no header).
     * @param message The detailed message displayed in the dialog body.
     */
    public static void showInformation(String title, String header, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper for displaying Error alerts (used alongside showInformation).
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    /**
     * Shows a confirmation dialog.
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}