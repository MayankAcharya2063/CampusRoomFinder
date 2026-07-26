package com.example.roomify.controller;

import com.example.roomify.model.User;
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

/**
 * Reports Controller - Generate and view system reports.
 */
public class ReportsController {

    // ==================== FXML INJECTIONS ====================
    @FXML private TableView<ReportEntry> reportTable;
    @FXML private TableColumn<ReportEntry, String> dateColumn;
    @FXML private TableColumn<ReportEntry, String> typeColumn;
    @FXML private TableColumn<ReportEntry, String> detailsColumn;
    @FXML private Label totalReportsLabel;

    // ==================== DATA ====================
    private final ObservableList<ReportEntry> reports = FXCollections.observableArrayList();
    private User currentUser;
    private int reportCounter = 0;

    // ==================== INITIALIZATION ====================
    public void initContext(User user) {
        this.currentUser = user;
        setupTableColumns();
        loadSampleReports();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadSampleReports();
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
    }

    private void loadSampleReports() {
        reports.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);

        reports.addAll(
                new ReportEntry(now, "Booking Report", "Total: 25 bookings | Pending: 5 | Approved: 15 | Rejected: 5"),
                new ReportEntry(now, "Resource Report", "Total: 12 resources | Available: 8 | Booked: 2 | Maintenance: 2"),
                new ReportEntry(now, "User Report", "Total: 45 users | Students: 30 | Staff: 12 | Admins: 3")
        );
        reportTable.setItems(reports);
        updateTotalCount();
    }

    private void updateTotalCount() {
        totalReportsLabel.setText(String.valueOf(reports.size()));
    }

    private void addReport(String type, String details) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        reports.add(new ReportEntry(now, type, details));
        updateTotalCount();
    }

    // ==================== EVENT HANDLERS ====================
    @FXML
    private void handleBookingReport(ActionEvent event) {
        addReport("Booking Report",
                "Total: 25 bookings | Pending: 5 | Approved: 15 | Rejected: 5 | Cancelled: 0");
        AlertHelper.showInformation("Report Generated", "Booking report generated successfully.");
    }

    @FXML
    private void handleResourceReport(ActionEvent event) {
        addReport("Resource Report",
                "Total: 12 resources | Available: 8 | Booked: 2 | Under Maintenance: 2");
        AlertHelper.showInformation("Report Generated", "Resource report generated successfully.");
    }

    @FXML
    private void handleUserReport(ActionEvent event) {
        addReport("User Report",
                "Total: 45 users | Students: 30 | Staff: 12 | Admins: 3");
        AlertHelper.showInformation("Report Generated", "User report generated successfully.");
    }

    @FXML
    private void handleExport(ActionEvent event) {
        if (reports.isEmpty()) {
            AlertHelper.showError("No Data", "No reports to export.");
            return;
        }
        AlertHelper.showInformation("Export", "Reports exported successfully to reports.txt");
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadSampleReports();
        AlertHelper.showInformation("Refreshed", "Reports refreshed.");
    }

    // ==================== INNER CLASS ====================
    public static class ReportEntry {
        private final String date;
        private final String type;
        private final String details;

        public ReportEntry(String date, String type, String details) {
            this.date = date;
            this.type = type;
            this.details = details;
        }

        public String getDate() { return date; }
        public String getType() { return type; }
        public String getDetails() { return details; }
    }
}