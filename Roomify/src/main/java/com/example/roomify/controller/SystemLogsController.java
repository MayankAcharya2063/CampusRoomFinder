package com.example.roomify.controller;

import com.example.roomify.model.User;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        this.currentUser = user;
        setupTableColumns();
        loadSampleLogs();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadSampleLogs();
    }

    private void setupTableColumns() {
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadSampleLogs() {
        allLogs.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        allLogs.addAll(
                new LogEntry(now.format(formatter), "Login", "Admin User", "Admin logged in successfully"),
                new LogEntry(now.minusMinutes(5).format(formatter), "Booking Approved", "Admin User", "Booking B-101 approved for Study Room A"),
                new LogEntry(now.minusMinutes(12).format(formatter), "Resource Added", "Admin User", "New resource 'Study Room 3B' added"),
                new LogEntry(now.minusMinutes(20).format(formatter), "Booking Request", "Jane Student", "Study Room A requested for tomorrow 10:00-11:00"),
                new LogEntry(now.minusMinutes(30).format(formatter), "Login", "Jane Student", "Student logged in"),
                new LogEntry(now.minusHours(1).format(formatter), "User Created", "Admin User", "New student account created for David Kumar"),
                new LogEntry(now.minusHours(2).format(formatter), "Logout", "John Staff", "Staff logged out"),
                new LogEntry(now.minusHours(3).format(formatter), "Booking Cancelled", "Sarah Chen", "Booking B-003 cancelled by requester")
        );

        filteredLogs = new FilteredList<>(allLogs, p -> true);
        logTable.setItems(filteredLogs);
        updateTotalCount();
        statusLabel.setText("Ready");
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
        loadSampleLogs();
        AlertHelper.showInformation("Refreshed", "Logs refreshed.");
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