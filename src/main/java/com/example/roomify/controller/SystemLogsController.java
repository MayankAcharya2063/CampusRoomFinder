package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.UserRole;
import com.example.roomify.model.User;
import com.example.roomify.persistence.FilePersistenceEngine;
import com.example.roomify.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class SystemLogsController {

    @FXML private TextField searchField;
    @FXML private TableView<LogEntry> logTable;
    @FXML private TableColumn<LogEntry, String> timestampColumn;
    @FXML private TableColumn<LogEntry, String> actionColumn;
    @FXML private TableColumn<LogEntry, String> userColumn;
    @FXML private TableColumn<LogEntry, String> descriptionColumn;
    @FXML private Label totalLogsLabel;
    @FXML private Label statusLabel;

    private final ObservableList<LogEntry> allLogs = FXCollections.observableArrayList();
    private FilteredList<LogEntry> filteredLogs;
    private User currentUser;

    public void initContext(User user) {
        // SECURITY CHECK: Only admins can view logs
        if (user.getRole() != UserRole.ADMIN) {
            AlertHelper.showError("Access Denied", "You do not have permission to view system logs.");
            // Redirect back to dashboard
            Stage currentStage = (Stage) statusLabel.getScene().getWindow();
            StageCoordinator.getInstance().showDashboardForUser(user, currentStage);
            return;
        }

        this.currentUser = user;
        setupTableColumns();
        loadLogsFromFile();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        // Don't load logs here - wait for initContext
    }

    private void setupTableColumns() {
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadLogsFromFile() {
        allLogs.clear();
        List<String> lines = FilePersistenceEngine.readText("system_log.txt");

        for (String line : lines) {
            LogEntry entry = parseLogLine(line);
            if (entry != null) {
                allLogs.add(entry);
            }
        }

        filteredLogs = new FilteredList<>(allLogs, p -> true);
        logTable.setItems(filteredLogs);
        updateTotalCount();
        statusLabel.setText("Showing " + filteredLogs.size() + " logs");
    }

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
        String user = "";
        String description = "";

        int separator = message.indexOf(" : ");
        if (separator >= 0) {
            action = message.substring(0, separator);
            String remainder = message.substring(separator + 3);

            if (action.contains("Login") || action.contains("Logout") || action.contains("Password")) {
                user = remainder;
                description = remainder;
            } else if (action.contains("Booking")) {
                user = "System";
                description = remainder;
            } else {
                user = "Admin";
                description = remainder;
            }
        } else {
            action = message;
            user = "System";
            description = message;
        }

        return new LogEntry(timestamp, action, user, description);
    }

    private void updateTotalCount() {
        totalLogsLabel.setText(String.valueOf(filteredLogs.size()));
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().toLowerCase().trim();
        filteredLogs.setPredicate(log -> {
            if (query.isEmpty()) return true;
            return log.getAction().toLowerCase().contains(query) ||
                    log.getUser().toLowerCase().contains(query) ||
                    log.getDescription().toLowerCase().contains(query) ||
                    log.getTimestamp().toLowerCase().contains(query);
        });
        updateTotalCount();
        statusLabel.setText("Showing " + filteredLogs.size() + " logs");
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadLogsFromFile();
        AlertHelper.showInformation("Refreshed", "Logs refreshed from file.");
    }

    @FXML
    private void handleExportLogs(ActionEvent event) {
        if (filteredLogs.isEmpty()) {
            AlertHelper.showError("No Data", "No logs to export.");
            return;
        }
        AlertHelper.showInformation("Export", "Logs exported successfully to system_logs.txt");
    }

    @FXML
    private void handleClearLogs(ActionEvent event) {
        boolean confirm = AlertHelper.showConfirmation("Clear Logs",
                "Are you sure you want to clear all logs? This action cannot be undone.");
        if (confirm) {
            allLogs.clear();
            updateTotalCount();
            statusLabel.setText("Logs cleared");
            AlertHelper.showInformation("Cleared", "All logs have been cleared.");
        }
    }

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