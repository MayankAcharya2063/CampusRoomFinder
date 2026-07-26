package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.exception.UnauthorizedAccessException;
import com.example.roomify.model.Booking;
import com.example.roomify.model.User;
import com.example.roomify.security.AuthorizationGuard;
import com.example.roomify.service.SessionManager;
import com.example.roomify.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;

/**
 * Controller for admin-dashboard-view.fxml.
 */
public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> resourceColumn;
    @FXML private TableColumn<Booking, String> requesterColumn;
    @FXML private TableColumn<Booking, LocalDateTime> startColumn;
    @FXML private TableColumn<Booking, LocalDateTime> endColumn;
    @FXML private TableColumn<Booking, String> statusColumn;

    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button viewResourcesButton;
    @FXML private Button logoutButton;

    private User currentUser;
    private final ObservableList<Booking> bookings = FXCollections.observableArrayList();

    public void initContext(User user) {
        this.currentUser = user;
        System.out.println("AdminDashboardController.initContext() called for: " + (user != null ? user.getName() : "null"));

        try {
            AuthorizationGuard.requireAdmin(user.getRole());
        } catch (UnauthorizedAccessException e) {
            AlertHelper.showError("Access Denied", e.getMessage());
            return;
        }

        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getName());
        }

        loadSampleBookings();
        bookingTable.setItems(bookings);
        updatePendingCount();
        System.out.println("AdminDashboard initialized successfully");
    }

    @FXML
    public void initialize() {
        System.out.println("AdminDashboardController.initialize() called");
        try {
            resourceColumn.setCellValueFactory(new PropertyValueFactory<>("resourceName"));
            requesterColumn.setCellValueFactory(new PropertyValueFactory<>("requesterName"));
            startColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
            endColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            System.out.println("Table columns initialized");
        } catch (Exception e) {
            System.err.println("Error initializing table columns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * TEMPORARY: sample pending bookings.
     */
    private void loadSampleBookings() {
        bookings.setAll(
                new Booking("B-101", "R-001", "Study Room A", "Jane Student",
                        LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                        LocalDateTime.now().plusDays(1).withHour(11).withMinute(0),
                        "Group project meeting", "PENDING"),
                new Booking("B-102", "R-003", "Computer Lab 1", "Sam Staff",
                        LocalDateTime.now().plusDays(2).withHour(14).withMinute(0),
                        LocalDateTime.now().plusDays(2).withHour(16).withMinute(0),
                        "Workshop session", "PENDING")
        );
    }

    private void updatePendingCount() {
        long count = bookings.stream().filter(b -> "PENDING".equalsIgnoreCase(b.getStatus())).count();
        if (pendingCountLabel != null) {
            pendingCountLabel.setText("Pending: " + count);
        }
    }

    @FXML
    void handleApprove(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a booking to approve.");
            return;
        }
        selected.setStatus("APPROVED");
        bookingTable.refresh();
        updatePendingCount();
        AlertHelper.showInformation("Booking Approved", selected.getResourceName() + " booking approved.");
    }

    @FXML
    void handleReject(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a booking to reject.");
            return;
        }
        selected.setStatus("REJECTED");
        bookingTable.refresh();
        updatePendingCount();
        AlertHelper.showInformation("Booking Rejected", selected.getResourceName() + " booking rejected.");
    }

    @FXML
    void handleViewResources(ActionEvent event) {
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        StageCoordinator.getInstance().showResourceList(currentUser, currentStage);
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadSampleBookings();
        bookingTable.refresh();
        updatePendingCount();
        if (statusLabel != null) {
            statusLabel.setText("Table Refreshed");
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        StageCoordinator.getInstance().showLogin(currentStage);
    }
}