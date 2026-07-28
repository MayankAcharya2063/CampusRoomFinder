package com.example.roomify.controller;

import com.example.roomify.booking.BookingService;
import com.example.roomify.model.Booking;
import com.example.roomify.model.User;
import com.example.roomify.persistence.SystemLogger;
import com.example.roomify.service.SessionManager;
import com.example.roomify.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * My Bookings Controller - Displays user's bookings with filtering and cancellation.
 */
public class MyBookingsController {

    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, String> bookingIdColumn;
    @FXML private TableColumn<Booking, String> resourceColumn;
    @FXML private TableColumn<Booking, String> dateColumn;
    @FXML private TableColumn<Booking, String> startColumn;
    @FXML private TableColumn<Booking, String> endColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, String> createdDateColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button refreshButton;
    @FXML private Button cancelButton;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final BookingService bookingService = BookingService.getInstance();
    private User currentUser;
    private ObservableList<Booking> userBookings = FXCollections.observableArrayList();
    private FilteredList<Booking> filteredBookings;

    public void initContext(User user) {
        this.currentUser = user;
        setupTableColumns();
        setupStatusFilter();
        loadBookings();
    }

    private void setupTableColumns() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        resourceColumn.setCellValueFactory(new PropertyValueFactory<>("resourceName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startTimeDisplay"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endTimeDisplay"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));

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
                } else if ("CANCELLED".equals(item)) {
                    setStyle("-fx-text-fill: #6B7280; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void setupStatusFilter() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "PENDING", "APPROVED", "REJECTED", "CANCELLED"
        ));
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.setOnAction(e -> applyFilters());
    }

    private void loadBookings() {
        bookingService.refreshBookings(); // reload from disk so Refresh actually picks up new bookings
        userBookings.clear();
        List<Booking> allBookings = bookingService.getBookings();

        if (allBookings != null && currentUser != null) {
            for (Booking booking : allBookings) {
                if (booking.getRequesterName().equals(currentUser.getName())) {
                    userBookings.add(booking);
                }
            }
        }

        filteredBookings = new FilteredList<>(userBookings, p -> true);
        bookingsTable.setItems(filteredBookings);
        applyFilters();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();

        filteredBookings.setPredicate(booking -> {
            if (!searchText.isEmpty()) {
                boolean matches = booking.getBookingId().toLowerCase().contains(searchText) ||
                        booking.getResourceName().toLowerCase().contains(searchText);
                if (!matches) return false;
            }

            if (status != null && !status.equals("All") && !booking.getStatus().equals(status)) {
                return false;
            }

            return true;
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFilters();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadBookings();
        AlertHelper.showInformation("Refreshed", "Bookings refreshed.");
    }

    @FXML
    private void handleCancelBooking(ActionEvent event) {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a booking to cancel.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(selected.getStatus())) {
            AlertHelper.showError("Cannot Cancel", "Only PENDING bookings can be cancelled.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation("Cancel Booking",
                "Are you sure you want to cancel booking '" + selected.getBookingId() + "'?\n" +
                        "Resource: " + selected.getResourceName());

        if (confirm) {
            boolean success = bookingService.cancelBooking(selected.getBookingId());
            if (success) {
                loadBookings();
                SystemLogger.logBookingCancelled(selected.getBookingId());
                AlertHelper.showInformation("Success", "Booking cancelled successfully.");
            } else {
                AlertHelper.showError("Error", "Failed to cancel booking.");
            }
        }
    }

    @FXML
    private void handleClearFilters(ActionEvent event) {
        searchField.clear();
        statusFilter.getSelectionModel().selectFirst();
        applyFilters();
    }
}