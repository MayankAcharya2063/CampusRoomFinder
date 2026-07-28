package com.example.roomify.controller;

import com.example.roomify.UserRole;
import com.example.roomify.booking.BookingService;
import com.example.roomify.model.Booking;
import com.example.roomify.model.User;
import com.example.roomify.persistence.FilePersistenceEngine;
import com.example.roomify.persistence.ResourceFileHandler;
import com.example.roomify.service.AuthenticationService;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    // ==================== FXML INJECTIONS ====================
    @FXML private Label totalResourcesLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label pendingBookingsLabel;
    @FXML private Label todayBookingsLabel;

    // Stat card containers (for role-based visibility)
    @FXML private VBox pendingBookingsCard;
    @FXML private VBox todayBookingsCard;

    @FXML private TableView<LogEntry> recentActivitiesTable;
    @FXML private TableColumn<LogEntry, String> timestampColumn;
    @FXML private TableColumn<LogEntry, String> actionColumn;
    @FXML private TableColumn<LogEntry, String> userColumn;
    @FXML private TableColumn<LogEntry, String> descriptionColumn;

    // Quick-action buttons (admin-only section)
    @FXML private HBox quickActionsBox;
    // Log section container
    @FXML private VBox recentActivitiesBox;

    // ==================== DATA ====================
    private final ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
    private User currentUser;

    // ==================== INITIALIZATION ====================
    public void initContext(User user) {
        this.currentUser = user;
        setupTableColumns();
        loadDashboardData();
        loadRecentActivities();
    }

    @FXML
    public void initialize() {
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
            // Silent fail - UI will show empty table
        }
    }

    private void loadDashboardData() {
        try {
            List<Booking> bookings = BookingService.getInstance().getBookings();

            long pendingCount = bookings.stream()
                    .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()))
                    .count();

            LocalDate today = LocalDate.now();
            long todayCount = bookings.stream()
                    .filter(b -> b.getStartTime() != null && b.getStartTime().toLocalDate().equals(today))
                    .count();

            int userCount = AuthenticationService.getInstance().getUserCount();
            int resourceCount = ResourceFileHandler.loadResources().size();

            totalResourcesLabel.setText(String.valueOf(resourceCount));
            totalUsersLabel.setText(String.valueOf(userCount));
            pendingBookingsLabel.setText(String.valueOf(pendingCount));
            todayBookingsLabel.setText(String.valueOf(todayCount));
        } catch (Exception e) {
            // Silent fail - keep default values
        }
    }

    private void loadRecentActivities() {
        // Only ADMIN users may see system logs, pending bookings card, and quick actions
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            // Hide the Pending Bookings card — it shows system-wide data, admin only
            if (pendingBookingsCard != null) {
                pendingBookingsCard.setVisible(false);
                pendingBookingsCard.setManaged(false);
                // Shift Today's Bookings left to fill the empty column slot
                if (todayBookingsCard != null) {
                    javafx.scene.layout.GridPane.setColumnIndex(todayBookingsCard, 0);
                }
            }
            if (recentActivitiesBox != null) {
                recentActivitiesBox.setVisible(false);
                recentActivitiesBox.setManaged(false);
            }
            if (quickActionsBox != null) {
                quickActionsBox.setVisible(false);
                quickActionsBox.setManaged(false);
            }
            return;
        }

        try {
            logEntries.clear();

            List<String> lines = FilePersistenceEngine.readText("system_log.txt");

            // Show the most recent 10 entries, newest first
            int start = Math.max(0, lines.size() - 10);
            for (int i = lines.size() - 1; i >= start; i--) {
                LogEntry entry = parseLogLine(lines.get(i));
                if (entry != null) {
                    logEntries.add(entry);
                }
            }

            recentActivitiesTable.setItems(logEntries);
        } catch (Exception e) {
            // Silent fail - show empty table
        }
    }

    /**
     * Parses a line like "[2026-07-26 22:33:10] User Login : admin@roomify.com"
     * into a LogEntry for display.
     */
    private LogEntry parseLogLine(String line) {
        if (line == null || !line.startsWith("[")) {
            return null;
        }

        int closeBracket = line.indexOf(']');
        if (closeBracket < 0) {
            return null;
        }

        String timestamp = line.substring(1, closeBracket);
        String message = line.substring(closeBracket + 1).trim();

        String action = message;
        String detail = "";
        int separator = message.indexOf(" : ");
        if (separator >= 0) {
            action = message.substring(0, separator);
            detail = message.substring(separator + 3);
        }

        return new LogEntry(timestamp, action, detail, message);
    }

    // ==================== EVENT HANDLERS ====================
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadDashboardData();
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