package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.booking.BookingService;
import com.example.roomify.model.Booking;
import com.example.roomify.model.User;
import com.example.roomify.service.SessionManager;
import com.example.roomify.util.AlertFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Approval View.
 * Allows admin to approve or reject pending booking requests.
 */
public class ApprovalController implements Initializable {

    // ==================== FXML INJECTIONS ====================

    // Header
    @FXML private Label loggedInUserLabel;
    @FXML private Button logoutButton;
    @FXML private Button logoutSidebarBtn;

    // Navigation
    @FXML private Button dashboardBtn;
    @FXML private Button resourcesBtn;
    @FXML private Button usersBtn;
    @FXML private Button approvalsBtn;

    // Table
    @FXML private TableView<ApprovalRequest> pendingTable;
    @FXML private TableColumn<ApprovalRequest, String> bookingIdColumn;
    @FXML private TableColumn<ApprovalRequest, String> userColumn;
    @FXML private TableColumn<ApprovalRequest, String> resourceColumn;
    @FXML private TableColumn<ApprovalRequest, String> dateColumn;
    @FXML private TableColumn<ApprovalRequest, String> timeColumn;
    @FXML private TableColumn<ApprovalRequest, String> statusColumn;

    // Buttons
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button viewDetailsButton;
    @FXML private Button refreshButton;

    // Rejection reason
    @FXML private TextArea rejectReasonArea;

    // Status
    @FXML private Label statusLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label lastUpdateLabel;

    // ==================== SERVICE REFERENCES ====================

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final AlertFactory alertFactory = AlertFactory.getInstance();
    private final BookingService bookingService = new BookingService();

    // ==================== DATA ====================

    private ObservableList<ApprovalRequest> pendingRequests = FXCollections.observableArrayList();

    /**
     * ApprovalRequest class for table display.
     */
    public static class ApprovalRequest {
        private final String bookingId;
        private final String user;
        private final String resource;
        private final String date;
        private final String time;
        private final String status;

        public ApprovalRequest(String bookingId, String user, String resource,
                               String date, String time, String status) {
            this.bookingId = bookingId;
            this.user = user;
            this.resource = resource;
            this.date = date;
            this.time = time;
            this.status = status;
        }

        public String getBookingId() { return bookingId; }
        public String getUser() { return user; }
        public String getResource() { return resource; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getStatus() { return status; }
    }

    // ==================== INITIALIZATION ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadPendingRequests();
        setupTableSelectionListener();
        updateUserInfo();
        updateStatus("Ready");
        updateLastUpdate();
        updatePendingCount();
    }

    /**
     * Sets up table columns.
     */
    private void setupTableColumns() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        resourceColumn.setCellValueFactory(new PropertyValueFactory<>("resource"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<ApprovalRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
            }
        });
    }

    /**
     * Loads pending approval requests from the booking service.
     */
    private void loadPendingRequests() {
        pendingRequests.clear();

        // Load from booking service - get all pending bookings
        List<Booking> pendingBookings = bookingService.getPendingBookings();

        if (pendingBookings == null || pendingBookings.isEmpty()) {
            // If no bookings exist, load sample data for demo
            loadSamplePendingRequests();
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Booking booking : pendingBookings) {
            if ("PENDING".equalsIgnoreCase(booking.getStatus())) {
                String date = booking.getStartTime().format(dateFormatter);
                String time = booking.getStartTime().format(timeFormatter) + " - " +
                        booking.getEndTime().format(timeFormatter);

                ApprovalRequest request = new ApprovalRequest(
                        booking.getBookingId(),
                        booking.getRequesterName(),
                        booking.getResourceName(),
                        date,
                        time,
                        "PENDING"
                );
                pendingRequests.add(request);
            }
        }

        // If still empty, load sample data
        if (pendingRequests.isEmpty()) {
            loadSamplePendingRequests();
        } else {
            pendingTable.setItems(pendingRequests);
            updatePendingCount();
        }
    }

    /**
     * Loads sample pending requests (for demo/testing).
     */
    private void loadSamplePendingRequests() {
        // Only load samples if we have no pending requests from the service
        if (!pendingRequests.isEmpty()) {
            return;
        }


        LocalDateTime now = LocalDateTime.now();

        // Create sample bookings and save them to the service
        String[] sampleData = {
                "BKG-001|Jane Student|Study Room 3A|" + now.plusDays(1) + "|09:00|11:00",
                "BKG-002|John Staff|Computer Lab C|" + now.plusDays(2) + "|14:00|17:00",
                "BKG-003|Sarah Chen|Seminar Hall|" + now.plusDays(3) + "|10:00|12:00",
                "BKG-004|Michael Lee|Discussion Room 2B|" + now.plusDays(1) + "|13:00|14:30",
                "BKG-005|Emma Wilson|Conference Room|" + now.plusDays(4) + "|11:00|13:00"
        };

        for (String data : sampleData) {
            String[] parts = data.split("\\|");
            String bookingId = parts[0];
            String requester = parts[1];
            String resource = parts[2];
            String dateStr = parts[3];
            String startTimeStr = parts[4];
            String endTimeStr = parts[5];

            try {
                LocalDateTime start = LocalDateTime.parse(dateStr + "T" + startTimeStr + ":00");
                LocalDateTime end = LocalDateTime.parse(dateStr + "T" + endTimeStr + ":00");

                Booking booking = new Booking(
                        bookingId,
                        "RES-001",
                        resource,
                        requester,
                        start,
                        end,
                        "General booking",
                        "PENDING"
                );

                // Add to service
                try {
                    bookingService.createBooking(
                            bookingId,
                            "USER-001",
                            "RES-001",
                            start,
                            end,
                            requester
                    );
                } catch (Exception e) {
                    // Booking might already exist, that's fine
                }
            } catch (Exception e) {
                System.err.println("Error creating sample booking: " + e.getMessage());
            }
        }

        // Reload from service
        List<Booking> pendingBookings = bookingService.getPendingBookings();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Booking booking : pendingBookings) {
            if ("PENDING".equalsIgnoreCase(booking.getStatus())) {
                String date = booking.getStartTime().format(dateFormatter);
                String time = booking.getStartTime().format(timeFormatter) + " - " +
                        booking.getEndTime().format(timeFormatter);

                ApprovalRequest request = new ApprovalRequest(
                        booking.getBookingId(),
                        booking.getRequesterName(),
                        booking.getResourceName(),
                        date,
                        time,
                        "PENDING"
                );
                pendingRequests.add(request);
            }
        }

        pendingTable.setItems(pendingRequests);
        updatePendingCount();
    }

    /**
     * Sets up table selection listener.
     */
    private void setupTableSelectionListener() {
        pendingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                rejectReasonArea.clear();
            }
        });
    }

    /**
     * Updates user info label.
     */
    private void updateUserInfo() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            loggedInUserLabel.setText("Welcome, " + currentUser.getName() + " (ADMIN)");
        } else {
            loggedInUserLabel.setText("Welcome, Admin");
        }
    }

    /**
     * Updates status bar message.
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Updates last update timestamp.
     */
    private void updateLastUpdate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        lastUpdateLabel.setText("Last updated: " + now.format(formatter));
    }

    /**
     * Updates pending count label.
     */
    private void updatePendingCount() {
        pendingCountLabel.setText("Pending: " + pendingRequests.size());
    }

    // ==================== CONTEXT INITIALIZATION ====================

    /**
     * Initializes controller with user context.
     */
    public void initContext(User user) {
        if (user != null) {
            sessionManager.login(user);
            updateUserInfo();
        }
        loadPendingRequests();
        updateStatus("Approvals ready");
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Handles approve button click.
     */
    @FXML
    private void handleApprove(ActionEvent event) {
        ApprovalRequest selected = pendingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertFactory.showWarningAlert("No Selection", "Please select a booking to approve.");
            return;
        }

        boolean confirm = alertFactory.showConfirmationDialog("Approve Booking",
                "Are you sure you want to approve booking '" + selected.getBookingId() + "'?\n" +
                        "Resource: " + selected.getResource() + "\n" +
                        "User: " + selected.getUser() + "\n" +
                        "Date: " + selected.getDate() + " " + selected.getTime());

        if (confirm) {
            // Update the booking status in the service
            boolean success = bookingService.approveBooking(selected.getBookingId());

            if (success) {
                // Remove from UI list
                pendingRequests.remove(selected);
                updatePendingCount();
                updateStatus("Approved booking: " + selected.getBookingId());
                updateLastUpdate();

                // Log the action
                com.example.roomify.persistence.SystemLogger.logBookingApproved(selected.getBookingId());

                alertFactory.showSuccessAlert("Booking Approved",
                        "Booking '" + selected.getBookingId() + "' has been approved.\n" +
                                "The user has been notified.");
            } else {
                alertFactory.showErrorAlert("Error", "Failed to approve booking. Please try again.");
            }
        }
    }

    /**
     * Handles reject button click.
     */
    @FXML
    private void handleReject(ActionEvent event) {
        ApprovalRequest selected = pendingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertFactory.showWarningAlert("No Selection", "Please select a booking to reject.");
            return;
        }

        String reason = rejectReasonArea.getText().trim();
        if (reason.isEmpty()) {
            alertFactory.showWarningAlert("Reason Required",
                    "Please provide a reason for rejecting this booking.");
            return;
        }

        boolean confirm = alertFactory.showConfirmationDialog("Reject Booking",
                "Are you sure you want to reject booking '" + selected.getBookingId() + "'?\n" +
                        "Reason: " + reason);

        if (confirm) {
            // Update the booking status in the service
            boolean success = bookingService.rejectBooking(selected.getBookingId());

            if (success) {
                // Remove from UI list
                pendingRequests.remove(selected);
                updatePendingCount();
                updateStatus("Rejected booking: " + selected.getBookingId());
                updateLastUpdate();

                // Log the action
                com.example.roomify.persistence.SystemLogger.logBookingRejected(selected.getBookingId());

                alertFactory.showInfoAlert("Booking Rejected",
                        "Booking '" + selected.getBookingId() + "' has been rejected.\n" +
                                "Reason: " + reason + "\n" +
                                "The user has been notified.");
                rejectReasonArea.clear();
            } else {
                alertFactory.showErrorAlert("Error", "Failed to reject booking. Please try again.");
            }
        }
    }

    /**
     * Handles view details button click.
     */
    @FXML
    private void handleViewDetails(ActionEvent event) {
        ApprovalRequest selected = pendingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertFactory.showWarningAlert("No Selection", "Please select a booking to view details.");
            return;
        }

        alertFactory.showInfoAlert("Booking Details",
                "=== Booking Details ===\n\n" +
                        "Booking ID: " + selected.getBookingId() + "\n" +
                        "User: " + selected.getUser() + "\n" +
                        "Resource: " + selected.getResource() + "\n" +
                        "Date: " + selected.getDate() + "\n" +
                        "Time: " + selected.getTime() + "\n" +
                        "Status: " + selected.getStatus() + "\n\n" +
                        "Additional Notes:\n" +
                        "No additional notes provided.");
    }

    /**
     * Handles refresh button click - reloads from the actual data source.
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        // Force reload from the booking service
        loadPendingRequests();
        updateLastUpdate();
        updateStatus("Approvals refreshed");
        alertFactory.showInfoAlert("Refreshed", "Pending approvals have been refreshed.");
    }

    /**
     * Handles dashboard navigation.
     */
    @FXML
    private void handleDashboard(ActionEvent event) {
        Stage currentStage = (Stage) dashboardBtn.getScene().getWindow();
        User currentUser = sessionManager.getCurrentUser();
        StageCoordinator.getInstance().showAdminDashboard(currentUser, currentStage);
    }

    /**
     * Handles resources navigation.
     */
    @FXML
    private void handleResources(ActionEvent event) {
        Stage currentStage = (Stage) resourcesBtn.getScene().getWindow();
        User currentUser = sessionManager.getCurrentUser();
        StageCoordinator.getInstance().showResourceList(currentUser, currentStage);
    }

    /**
     * Handles users navigation.
     */
    @FXML
    private void handleUsers(ActionEvent event) {
        alertFactory.showInfoAlert("Manage Users", "User management will be displayed here.");
    }

    /**
     * Handles approvals navigation (current view).
     */
    @FXML
    private void handleApprovals(ActionEvent event) {
        refreshView();
    }

    /**
     * Handles logout action.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        boolean confirm = alertFactory.showConfirmationDialog("Logout", "Are you sure you want to logout?");
        if (confirm) {
            sessionManager.logout();
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            StageCoordinator.getInstance().showLogin(currentStage);
        }
    }

    /**
     * Refreshes the view.
     */
    private void refreshView() {
        loadPendingRequests();
        rejectReasonArea.clear();
        updateLastUpdate();
        updateStatus("View refreshed");
    }
}