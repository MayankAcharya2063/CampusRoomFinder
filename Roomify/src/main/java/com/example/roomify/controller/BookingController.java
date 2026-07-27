package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.booking.BookingService;
import com.example.roomify.exception.InvalidBookingDurationException;
import com.example.roomify.exception.ResourceUnavailableException;
import com.example.roomify.model.Booking;
import com.example.roomify.model.Resource;
import com.example.roomify.model.User;
import com.example.roomify.persistence.SystemLogger;
import com.example.roomify.service.SessionManager;
import com.example.roomify.util.AlertHelper;
import com.example.roomify.validation.InputValidator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Booking Controller - Handles booking creation.
 */
public class BookingController {

    @FXML private Label resourceNameLabel;
    @FXML private Label locationLabel;
    @FXML private Label capacityLabel;
    @FXML private Label statusLabel;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> startHourCombo;
    @FXML private ComboBox<Integer> startMinuteCombo;
    @FXML private ComboBox<Integer> endHourCombo;
    @FXML private ComboBox<Integer> endMinuteCombo;
    @FXML private TextArea purposeField;

    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private User currentUser;
    private Resource selectedResource;
    private final BookingService bookingService = new BookingService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public void initContext(User user, Resource resource) {
        this.currentUser = user;
        this.selectedResource = resource;

        if (resource != null) {
            resourceNameLabel.setText(resource.getName());
            locationLabel.setText("Location: " + resource.getLocation());
            capacityLabel.setText("Capacity: " + resource.getCapacity());
            String status = resource.getStatus();
            statusLabel.setText("Status: " + status);
            if ("AVAILABLE".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            }
        }

        datePicker.setValue(LocalDate.now());
        setupTimeComboBoxes();
    }

    @FXML
    public void initialize() {
        setupTimeComboBoxes();
        datePicker.setValue(LocalDate.now());
    }

    private void setupTimeComboBoxes() {
        // Hours 8 AM to 8 PM
        startHourCombo.setItems(FXCollections.observableArrayList(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20));
        endHourCombo.setItems(FXCollections.observableArrayList(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20));

        // Minutes - 0 and 30
        startMinuteCombo.setItems(FXCollections.observableArrayList(0, 30));
        endMinuteCombo.setItems(FXCollections.observableArrayList(0, 30));

        startHourCombo.setValue(9);
        startMinuteCombo.setValue(0);
        endHourCombo.setValue(10);
        endMinuteCombo.setValue(0);
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

        if (startHourCombo.getValue() == null || startMinuteCombo.getValue() == null ||
                endHourCombo.getValue() == null || endMinuteCombo.getValue() == null) {
            AlertHelper.showError("Missing Time", "Please select start and end times.");
            return;
        }

        if (InputValidator.isNullOrEmpty(purposeField.getText())) {
            AlertHelper.showError("Missing Purpose", "Please describe the purpose of this booking.");
            return;
        }

        // Build start and end times
        LocalDate date = datePicker.getValue();
        LocalDateTime start = date.atTime(startHourCombo.getValue(), startMinuteCombo.getValue());
        LocalDateTime end = date.atTime(endHourCombo.getValue(), endMinuteCombo.getValue());

        // Validate time range
        if (!start.isBefore(end)) {
            AlertHelper.showError("Invalid Time", "Start time must be before end time.");
            return;
        }

        // Check if date is in the past
        if (start.isBefore(LocalDateTime.now())) {
            AlertHelper.showError("Past Date", "Cannot book in the past.");
            return;
        }

        // Check if resource is available
        if (!"AVAILABLE".equals(selectedResource.getStatus())) {
            AlertHelper.showError("Unavailable", "This resource is not available for booking.");
            return;
        }

        // Check for double booking
        List<Booking> existingBookings = bookingService.getBookings();
        for (Booking booking : existingBookings) {
            if (booking.getResourceName().equals(selectedResource.getName())) {
                boolean overlap = !(end.isBefore(booking.getStartTime()) || start.isAfter(booking.getEndTime()));
                if (overlap && !"CANCELLED".equals(booking.getStatus())) {
                    AlertHelper.showError("Conflict", "This resource is already booked for the selected time slot.");
                    return;
                }
            }
        }

        try {
            String bookingId = "BKG-" + String.format("%03d", bookingService.getBookings().size() + 1);
            bookingService.createBooking(
                    bookingId,
                    currentUser.getUserId(),
                    selectedResource.getResourceId(),
                    start,
                    end,
                    currentUser.getName()
            );

            // Log booking
            SystemLogger.logBookingCreated(bookingId);

            AlertHelper.showInformation("Success",
                    "Booking created successfully!\n" +
                            "Booking ID: " + bookingId + "\n" +
                            "Resource: " + selectedResource.getName() + "\n" +
                            "Date: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                            "Time: " + start.format(DateTimeFormatter.ofPattern("HH:mm")) +
                            " - " + end.format(DateTimeFormatter.ofPattern("HH:mm")));

            // Navigate back to search resources
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            if (currentUser.getRole().name().equals("STUDENT")) {
                StageCoordinator.getInstance().showStudentDashboard(currentUser, currentStage);
            } else {
                StageCoordinator.getInstance().showStaffDashboard(currentUser, currentStage);
            }

        } catch (InvalidBookingDurationException e) {
            AlertHelper.showError("Invalid Duration", e.getMessage());
        } catch (ResourceUnavailableException e) {
            AlertHelper.showError("Resource Unavailable", e.getMessage());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to create booking: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        if (currentUser.getRole().name().equals("STUDENT")) {
            StageCoordinator.getInstance().showStudentDashboard(currentUser, currentStage);
        } else {
            StageCoordinator.getInstance().showStaffDashboard(currentUser, currentStage);
        }
    }

    @FXML
    private void handleRefresh() {
        if (selectedResource != null) {
            String status = selectedResource.getStatus();
            statusLabel.setText("Status: " + status);
            if ("AVAILABLE".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            }
        }
    }
}