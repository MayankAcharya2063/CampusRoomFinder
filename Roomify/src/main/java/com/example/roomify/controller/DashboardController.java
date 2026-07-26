package com.example.roomify.controller;

import com.example.roomify.model.User;
import com.example.roomify.service.SessionManager;
import com.example.roomify.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {
    public DashboardController() {
        System.out.println("DashboardController CONSTRUCTOR called");
    }
    // ==================== FXML INJECTIONS ====================
    @FXML private Label totalResourcesLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label pendingBookingsLabel;
    @FXML private Label todayBookingsLabel;

    @FXML private TableView<LogEntry> recentActivitiesTable;
    @FXML private TableColumn<LogEntry, String> timestampColumn;
    @FXML private TableColumn<LogEntry, String> actionColumn;
    @FXML private TableColumn<LogEntry, String> userColumn;
    @FXML private TableColumn<LogEntry, String> descriptionColumn;

    // ==================== DATA ====================
    private final ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
    private User currentUser;

    // ==================== INITIALIZATION ====================
    public void initContext(User user) {
        System.out.println("DashboardController.initContext() START");
        this.currentUser = user;
        setupTableColumns();
        loadDashboardData();
        loadRecentActivities();
        System.out.println("DashboardController.initContext() END - Data loaded: " + logEntries.size() + " entries");
    }

    @FXML
    public void initialize() {
        System.out.println("DashboardController.initialize() called");
        // Force column setup even if called before initContext
        setupTableColumns();
        loadDashboardData();
        loadRecentActivities();
    }

    private void setupTableColumns() {
        try {
            timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
            actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
            userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        } catch (Exception e) {
            System.err.println("Error setting up table columns: " + e.getMessage());
        }
    }

    private void loadDashboardData() {
        // In production, these would come from services
        totalResourcesLabel.setText("12");
        totalUsersLabel.setText("45");
        pendingBookingsLabel.setText("5");
        todayBookingsLabel.setText("8");
    }

    private void loadRecentActivities() {
        logEntries.clear();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        logEntries.addAll(
                new LogEntry(now.format(formatter), "Login", "Admin User", "Admin logged in"),
                new LogEntry(now.minusMinutes(5).format(formatter), "Booking Approved", "Admin User", "Booking B-101 approved"),
                new LogEntry(now.minusMinutes(12).format(formatter), "Resource Added", "Admin User", "Study Room 3B added"),
                new LogEntry(now.minusHours(1).format(formatter), "Booking Request", "Jane Student", "Study Room A requested"),
                new LogEntry(now.minusHours(2).format(formatter), "User Created", "Admin User", "New student account created")
        );

        recentActivitiesTable.setItems(logEntries);
    }

    // ==================== EVENT HANDLERS ====================
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadRecentActivities();
        AlertHelper.showInformation("Refreshed", "Dashboard data refreshed.");
    }

    @FXML
    private void handleQuickAddResource(ActionEvent event) {
        AlertHelper.showInformation("Add Resource", "Navigate to Manage Resources to add a new resource.");
    }

    @FXML
    private void handleQuickAddUser(ActionEvent event) {
        AlertHelper.showInformation("Add User", "Navigate to Manage Users to add a new user.");
    }

    @FXML
    private void handleQuickViewApprovals(ActionEvent event) {
        AlertHelper.showInformation("Pending Approvals", "Navigate to Booking Approvals to review pending requests.");
    }

    // ==================== INNER CLASS ====================
    public static class LogEntry {
        private final String timestamp;
        private final String action;
        private final String user;
        private final String description;

        public LogEntry(String timestamp, String action, String user, String description) {
            this.timestamp = timestamp;
            this.action = action;
            this.user = user;
            this.description = description;
        }

        public String getTimestamp() { return timestamp; }
        public String getAction() { return action; }
        public String getUser() { return user; }
        public String getDescription() { return description; }
    }
}