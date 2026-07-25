package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.exception.UnauthorizedAccessException;
import com.example.roomify.model.Booking;
import com.example.roomify.model.User;
import com.example.roomify.security.AuthorizationGuard;
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
 * Controller for AdminDashboardView.fxml.
 * <p>
 * Landing screen for Admins after login. Lists pending booking requests
 * and lets an Admin approve or reject them. Guards access using
 * Member 3's AuthorizationGuard, and delegates back to the shared
 * Resource List for day-to-day resource browsing.
 * <p>
 * NOTE: Approving/rejecting a booking here only updates the local table
 * for demo purposes. Member 4 owns ApprovalWorkflowService/AdminService,
 * which should be called from {@link #handleApprove} / {@link #handleReject}
 * once available, in place of the direct status mutation.
 */
public class AdminDashboardController {

    @FXML private Label welcomeLabel;

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

        try {
            AuthorizationGuard.requireAdmin(user.getRole());
        } catch (UnauthorizedAccessException e) {
            AlertHelper.showError("Access Denied", e.getMessage());
            return;
        }

        welcomeLabel.setText("Admin Dashboard - " + user.getName());
        loadSampleBookings();
        bookingTable.setItems(bookings);
    }

    @FXML
    public void initialize() {
        resourceColumn.setCellValueFactory(new PropertyValueFactory<>("resourceName"));
        requesterColumn.setCellValueFactory(new PropertyValueFactory<>("requesterName"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    /**
     * TEMPORARY: sample pending bookings standing in for Member 5/6's
     * persisted booking records. Replace with something like
     * {@code BookingService.getInstance().getPendingBookings()}.
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

    @FXML
    void handleApprove(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a booking to approve.");
            return;
        }
        // TODO (integration): replace with ApprovalWorkflowService.approve(selected)
        selected.setStatus("APPROVED");
        bookingTable.refresh();
        AlertHelper.showInfo("Booking Approved", selected.getResourceName() + " booking approved.");
    }

    @FXML
    void handleReject(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a booking to reject.");
            return;
        }
        // TODO (integration): replace with ApprovalWorkflowService.reject(selected)
        selected.setStatus("REJECTED");
        bookingTable.refresh();
        AlertHelper.showInfo("Booking Rejected", selected.getResourceName() + " booking rejected.");
    }

    @FXML
    void handleViewResources(ActionEvent event) {
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        StageCoordinator.getInstance().showResourceList(currentUser, currentStage);
    }

    @FXML
    void handleLogout(ActionEvent event) {
        currentUser.logout();
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        StageCoordinator.getInstance().showLogin(currentStage);
    }
}
