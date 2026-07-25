package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.exception.InvalidBookingDurationException;
import com.example.roomify.exception.ResourceUnavailableException;
import com.example.roomify.model.Resource;
import com.example.roomify.model.User;
import com.example.roomify.util.AlertHelper;
import com.example.roomify.validation.InputValidator;
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
 * <p>
 * Lets a Student or Staff member request a booking for the resource they
 * selected on the Resource List screen. Performs client-side input
 * validation (reusing Member 3's InputValidator) before attempting to
 * create the booking.
 * <p>
 * NOTE: Actual booking creation / double-booking conflict detection is
 * owned by Member 5 (BookingService, ConflictDetectorEngine). The
 * {@link #createBooking} method below is a local stand-in that performs
 * the same duration check and throws the shared custom exceptions
 * (Member 3's module) so the try/catch wiring here won't need to change
 * once the real BookingService is dropped in — just replace the body of
 * createBooking with a call to BookingService.createBooking(...).
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

        resourceNameLabel.setText(resource.getName());
        capacityLabel.setText("Capacity: " + resource.getCapacity());
        datePicker.setValue(LocalDate.now());
    }

    @FXML
    public void initialize() {
        // Hours the booking system accepts (8 AM - 8 PM)
        startHourCombo.setItems(javafx.collections.FXCollections.observableArrayList(
                List.of(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)));
        // Duration options in hours; InputValidator currently allows 1-4
        durationCombo.setItems(javafx.collections.FXCollections.observableArrayList(1, 2, 3, 4));
    }

    @FXML
    void handleConfirmBooking(ActionEvent event) {
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
            AlertHelper.showInfo("Booking Requested",
                    selectedResource.getName() + " requested for " + start + " to " + end + ".");

            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            StageCoordinator.getInstance().showResourceList(currentUser, currentStage);

        } catch (InvalidBookingDurationException e) {
            AlertHelper.showError("Invalid Duration", e.getMessage());
        } catch (ResourceUnavailableException e) {
            AlertHelper.showError("Resource Unavailable", e.getMessage());
        }
    }

    /**
     * TEMPORARY stand-in for Member 5's BookingService.createBooking(...).
     * Performs the same duration validation locally and reuses the shared
     * custom exceptions so this method can be deleted and replaced with a
     * one-line delegate call once BookingService exists.
     */
    private void createBooking(Resource resource, User user, LocalDateTime start, LocalDateTime end, String purpose)
            throws InvalidBookingDurationException, ResourceUnavailableException {

        if (!"AVAILABLE".equals(resource.getStatus())) {
            throw new ResourceUnavailableException(resource.getName() + " is not available for booking.");
        }
        long hours = java.time.Duration.between(start, end).toHours();
        if (!InputValidator.isValidDuration((int) hours)) {
            throw new InvalidBookingDurationException("Booking duration must be between 1 and 4 hours.");
        }
        // Real implementation (Member 5) would persist the Booking and run
        // ConflictDetectorEngine here before returning.
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        StageCoordinator.getInstance().showResourceList(currentUser, currentStage);
    }
}
