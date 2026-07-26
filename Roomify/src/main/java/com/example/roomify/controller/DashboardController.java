package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.booking.BookingService;
import com.example.roomify.model.Booking;
import com.example.roomify.model.Resource;
import com.example.roomify.model.User;
import com.example.roomify.persistence.ResourceFileHandler;
import com.example.roomify.service.AuthenticationService;
import com.example.roomify.service.SessionManager;
import com.example.roomify.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard Controller - Shared dashboard view for students and staff.
 */
public class DashboardController {

    @FXML private Label availableResourcesLabel;
    @FXML private Label pendingBookingsLabel;
    @FXML private Label approvedBookingsLabel;
    @FXML private Label rejectedBookingsLabel;
    @FXML private Label todayBookingsLabel;

    @FXML private TableView<Booking> recentBookingsTable;
    @FXML private TableColumn<Booking, String> resourceColumn;
    @FXML private TableColumn<Booking, String> dateColumn;
    @FXML private TableColumn<Booking, String> startColumn;
    @FXML private TableColumn<Booking, String> endColumn;
    @FXML private TableColumn<Booking, String> statusColumn;

    @FXML private Button refreshButton;
    @FXML private Button searchResourcesBtn;
    @FXML private Button myBookingsBtn;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final BookingService bookingService = new BookingService();
    private final AuthenticationService authService = AuthenticationService.getInstance();
    private User currentUser;

    public void initContext(User user) {
        this.currentUser = user;
        System.out.println("DashboardController.initContext() called for: " + (user != null ? user.getName() : "null"));
        setupTableColumns();
        loadDashboardData();
    }

    @FXML
    public void initialize() {
        System.out.println("DashboardController.initialize() called");
        setupTableColumns();
    }

    private void setupTableColumns() {
        try {
            resourceColumn.setCellValueFactory(new PropertyValueFactory<>("resourceName"));
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
            startColumn.setCellValueFactory(new PropertyValueFactory<>("startTimeDisplay"));
            endColumn.setCellValueFactory(new PropertyValueFactory<>("endTimeDisplay"));
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
                    } else if ("CANCELLED".equals(item)) {
                        setStyle("-fx-text-fill: #6B7280; -fx-font-weight: bold;");
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error setting up table columns: " + e.getMessage());
        }
    }

    private void loadDashboardData() {
        try {
            System.out.println("Loading dashboard data for user: " + (currentUser != null ? currentUser.getName() : "null"));

            // Force refresh from file
            bookingService.refreshBookings();

            // Load resources count
            List<Resource> resources = ResourceFileHandler.loadResources();
            long availableCount = 0;
            if (resources != null) {
                availableCount = resources.stream()
                        .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                        .count();
            }
            if (availableResourcesLabel != null) {
                availableResourcesLabel.setText(String.valueOf(availableCount));
            }

            // Load bookings
            List<Booking> allBookings = bookingService.getBookings();
            System.out.println("Total bookings loaded: " + (allBookings != null ? allBookings.size() : 0));

            long pending = 0, approved = 0, rejected = 0, today = 0;

            if (allBookings != null && currentUser != null) {
                LocalDateTime now = LocalDateTime.now();
                for (Booking booking : allBookings) {
                    // Show all bookings for admin view, or filter by user for student/staff
                    if (currentUser.getRole().name().equals("ADMIN")) {
                        String status = booking.getStatus();
                        if ("PENDING".equalsIgnoreCase(status)) pending++;
                        else if ("APPROVED".equalsIgnoreCase(status)) approved++;
                        else if ("REJECTED".equalsIgnoreCase(status)) rejected++;
                    } else {
                        if (booking.getRequesterName().equals(currentUser.getName())) {
                            String status = booking.getStatus();
                            if ("PENDING".equalsIgnoreCase(status)) pending++;
                            else if ("APPROVED".equalsIgnoreCase(status)) approved++;
                            else if ("REJECTED".equalsIgnoreCase(status)) rejected++;
                        }
                    }

                    if (booking.getStartTime().toLocalDate().equals(now.toLocalDate())) {
                        today++;
                    }
                }
            }

            if (pendingBookingsLabel != null) pendingBookingsLabel.setText(String.valueOf(pending));
            if (approvedBookingsLabel != null) approvedBookingsLabel.setText(String.valueOf(approved));
            if (rejectedBookingsLabel != null) rejectedBookingsLabel.setText(String.valueOf(rejected));
            if (todayBookingsLabel != null) todayBookingsLabel.setText(String.valueOf(today));

            // Load recent bookings
            if (recentBookingsTable != null) {
                recentBookingsTable.getItems().clear();
                if (allBookings != null && currentUser != null) {
                    // Filter by user for student/staff, show all for admin
                    if (currentUser.getRole().name().equals("ADMIN")) {
                        allBookings.stream()
                                .limit(10)
                                .forEach(recentBookingsTable.getItems()::add);
                    } else {
                        allBookings.stream()
                                .filter(b -> b.getRequesterName().equals(currentUser.getName()))
                                .limit(10)
                                .forEach(recentBookingsTable.getItems()::add);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        System.out.println("Dashboard - Refresh clicked");
        loadDashboardData();
        AlertHelper.showInformation("Refreshed", "Dashboard data refreshed.");
    }

    @FXML
    private void handleSearchResources(ActionEvent event) {
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        if (currentUser != null) {
            if (currentUser.getRole().name().equals("STUDENT")) {
                StageCoordinator.getInstance().showStudentDashboard(currentUser, currentStage);
            } else {
                StageCoordinator.getInstance().showStaffDashboard(currentUser, currentStage);
            }
        }
    }

    @FXML
    private void handleMyBookings(ActionEvent event) {
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        if (currentUser != null) {
            if (currentUser.getRole().name().equals("STUDENT")) {
                StageCoordinator.getInstance().showStudentDashboard(currentUser, currentStage);
            } else {
                StageCoordinator.getInstance().showStaffDashboard(currentUser, currentStage);
            }
        }
    }
}