package com.example.roomify.controller;

import com.example.roomify.model.Booking;
import com.example.roomify.model.User;
import com.example.roomify.service.SessionManager;
import com.example.roomify.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Booking Approval Controller - Admin approves or rejects booking requests.
 */
public class BookingApprovalController {

    // ==================== FXML INJECTIONS ====================
    @FXML private Label pendingCountLabel;
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> bookingIdColumn;
    @FXML private TableColumn<Booking, String> requesterColumn;
    @FXML private TableColumn<Booking, String> resourceColumn;
    @FXML private TableColumn<Booking, String> dateColumn;
    @FXML private TableColumn<Booking, String> startTimeColumn;
    @FXML private TableColumn<Booking, String> endTimeColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TextField rejectReasonField;

    // ==================== DATA ====================
    private final ObservableList<Booking> bookings = FXCollections.observableArrayList();
    private User currentUser;

    // ==================== INITIALIZATION ====================
    public void initContext(User user) {
        this.currentUser = user;
        setupTableColumns();
        loadSampleBookings();
        updatePendingCount();
    }

    // Add this to your BookingApprovalController.java initialize() method
    @FXML
    public void initialize() {
        setupTableColumns();
        loadSampleBookings();
        updatePendingCount();

        // Add row selection styling
        bookingTable.setRowFactory(tv -> new TableRow<Booking>() {
            @Override
            protected void updateItem(Booking item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if (isSelected()) {
                        setStyle("-fx-background-color: #DBEAFE;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void setupTableColumns() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        requesterColumn.setCellValueFactory(new PropertyValueFactory<>("requesterName"));
        resourceColumn.setCellValueFactory(new PropertyValueFactory<>("resourceName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTimeDisplay"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTimeDisplay"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusColumn.setCellFactory(column -> new TableCell<Booking, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                if ("PENDING".equals(item)) {
                    setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                } else if ("APPROVED".equals(item)) {
                    setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                } else if ("REJECTED".equals(item)) {
                    setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void loadSampleBookings() {
        bookings.setAll(
                createBooking("BKG-001", "Jane Student", "Study Room 3A",
                        LocalDateTime.now().plusDays(1).withHour(9).withMinute(0),
                        LocalDateTime.now().plusDays(1).withHour(11).withMinute(0)),
                createBooking("BKG-002", "John Staff", "Computer Lab C",
                        LocalDateTime.now().plusDays(2).withHour(14).withMinute(0),
                        LocalDateTime.now().plusDays(2).withHour(16).withMinute(0)),
                createBooking("BKG-003", "Sarah Chen", "Main Auditorium",
                        LocalDateTime.now().plusDays(3).withHour(10).withMinute(0),
                        LocalDateTime.now().plusDays(3).withHour(12).withMinute(0)),
                createBooking("BKG-004", "Michael Lee", "Discussion Room 2B",
                        LocalDateTime.now().plusDays(1).withHour(13).withMinute(0),
                        LocalDateTime.now().plusDays(1).withHour(14).withMinute(30))
        );
        bookingTable.setItems(bookings);
    }

    private Booking createBooking(String id, String requester, String resource, LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        Booking booking = new Booking(
                id, "RES-001", resource, requester, start, end, "General booking", "PENDING"
        );
        return booking;
    }

    private void updatePendingCount() {
        long count = bookings.stream()
                .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()))
                .count();
        pendingCountLabel.setText(String.valueOf(count));
    }

    private void refreshTable() {
        bookingTable.refresh();
        updatePendingCount();
    }

    // ==================== EVENT HANDLERS ====================
    @FXML
    private void handleApprove(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a booking to approve.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(selected.getStatus())) {
            AlertHelper.showError("Invalid Status", "Only PENDING bookings can be approved.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation("Approve Booking",
                "Are you sure you want to approve booking '" + selected.getBookingId() + "'?\n" +
                        "Resource: " + selected.getResourceName() + "\n" +
                        "Requester: " + selected.getRequesterName());

        if (confirm) {
            selected.setStatus("APPROVED");
            refreshTable();
            AlertHelper.showInformation("Booking Approved",
                    "Booking '" + selected.getBookingId() + "' has been approved.");

            // Log the action
            System.out.println("Booking " + selected.getBookingId() + " approved by admin");
        }
    }

    @FXML
    private void handleReject(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a booking to reject.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(selected.getStatus())) {
            AlertHelper.showError("Invalid Status", "Only PENDING bookings can be rejected.");
            return;
        }

        String reason = rejectReasonField.getText().trim();
        if (reason.isEmpty()) {
            AlertHelper.showError("Reason Required", "Please provide a reason for rejection.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation("Reject Booking",
                "Are you sure you want to reject booking '" + selected.getBookingId() + "'?\n" +
                        "Reason: " + reason);

        if (confirm) {
            selected.setStatus("REJECTED");
            refreshTable();
            rejectReasonField.clear();
            AlertHelper.showInformation("Booking Rejected",
                    "Booking '" + selected.getBookingId() + "' has been rejected.\n" +
                            "Reason: " + reason);

            // Log the action
            System.out.println("Booking " + selected.getBookingId() + " rejected by admin. Reason: " + reason);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadSampleBookings();
        refreshTable();
        AlertHelper.showInformation("Refreshed", "Booking list refreshed.");
    }
}