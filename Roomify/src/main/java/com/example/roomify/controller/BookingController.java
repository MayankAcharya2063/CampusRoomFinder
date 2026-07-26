package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.exception.InvalidBookingDurationException;
import com.example.roomify.exception.ResourceUnavailableException;
import com.example.roomify.model.Resource;
import com.example.roomify.model.User;
import com.example.roomify.util.AlertHelper;
import com.example.roomify.validation.InputValidator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for BookingView.fxml.
 */
public class BookingController {

    @FXML private Label resourceNameLabel;
    @FXML private Label capacityLabel;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> startHourCombo;
    @FXML private ComboBox<Integer> durationCombo;
    @FXML private TextArea purposeField;

    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private User currentUser;
    private Resource selectedResource;

    /**
     * Called by StageCoordinator right after this controller's FXML loads.
     */
    public void initContext(User user, Resource resource) {
        this.currentUser = user;
        this.selectedResource = resource;

        if (resource != null) {
            if (resourceNameLabel != null) resourceNameLabel.setText(resource.getName());
            if (capacityLabel != null) capacityLabel.setText("Capacity: " + resource.getCapacity());
        }
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }
    }

    @FXML
    public void initialize() {
        // Hours the booking system accepts (8 AM - 8 PM)
        startHourCombo.setItems(FXCollections.observableArrayList(
                List.of(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)));
        // Duration options in hours (1-4 hours)
        durationCombo.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
    }

    @FXML
    void handleConfirmBooking(ActionEvent event) {
        if (selectedResource == null) {
            AlertHelper.showError("System Error", "No resource selected for booking.");
            return;
        }
        if (datePicker.getValue() == null) {
            AlertHelper.showError("Missing Date", "Please choose a booking date.");
            return;
        }
        if (startHourCombo.getValue() == null || durationCombo.getValue() == null) {
            AlertHelper.showError("Missing Time", "Please choose a start time and duration.");
            return;
        }
        if (InputValidator.isNullOrEmpty(purposeField.getText())) {
            AlertHelper.showError("Missing Purpose", "Please describe the purpose of this booking.");
            return;
        }

        int durationHours = durationCombo.getValue();
        if (!InputValidator.isValidDuration(durationHours)) {
            AlertHelper.showError("Invalid Duration", "Bookings must be between 1 and 4 hours.");
            return;
        }

        LocalDateTime start = datePicker.getValue().atTime(startHourCombo.getValue(), 0);
        LocalDateTime end = start.plusHours(durationHours);

        try {
            createBooking(selectedResource, currentUser, start, end, purposeField.getText().trim());

            // Fixed method name: showInformation instead of showInfo
            AlertHelper.showInformation(
                    "Booking Requested",
                    selectedResource.getName() + " requested for " + start + " to " + end + "."
            );

            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            StageCoordinator.getInstance().showResourceList(currentUser, currentStage);

        } catch (InvalidBookingDurationException e) {
            AlertHelper.showError("Invalid Duration", e.getMessage());
        } catch (ResourceUnavailableException e) {
            AlertHelper.showError("Resource Unavailable", e.getMessage());
        }
    }

    /**
     * Stand-in method for BookingService logic.
     */
    private void createBooking(Resource resource, User user, LocalDateTime start, LocalDateTime end, String purpose)
            throws InvalidBookingDurationException, ResourceUnavailableException {

        if (resource == null || !"AVAILABLE".equalsIgnoreCase(resource.getStatus())) {
            throw new ResourceUnavailableException(
                    (resource != null ? resource.getName() : "Resource") + " is not available for booking."
            );
        }
        long hours = java.time.Duration.between(start, end).toHours();
        if (!InputValidator.isValidDuration((int) hours)) {
            throw new InvalidBookingDurationException("Booking duration must be between 1 and 4 hours.");
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        StageCoordinator.getInstance().showResourceList(currentUser, currentStage);
    }
}