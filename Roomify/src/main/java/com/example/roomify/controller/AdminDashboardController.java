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
import com.example.roomify.persistence.SystemLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private Button logoutSidebarBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button resourcesBtn;
    @FXML private Button usersBtn;
    @FXML private Button approvalsBtn;
    @FXML private Button reportsBtn;
    @FXML private Button logsBtn;
    @FXML private StackPane contentStack;

    // Dashboard stats
    @FXML private Label availableResourcesLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label pendingBookingsLabel;
    @FXML private Label approvedBookingsLabel;
    @FXML private Label rejectedBookingsLabel;
    @FXML private Label todayBookingsLabel;
    @FXML private Label pendingCountLabel;
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> resourceColumn;
    @FXML private TableColumn<Booking, String> requesterColumn;
    @FXML private TableColumn<Booking, String> startColumn;
    @FXML private TableColumn<Booking, String> endColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private Label statusLabel;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final BookingService bookingService = new BookingService();
    private final AuthenticationService authService = AuthenticationService.getInstance();
    private User currentUser;

    private static final String ACTIVE_STYLE = "sidebar-btn-active";
    private static final String INACTIVE_STYLE = "sidebar-btn";

    public void initContext(User user) {
        this.currentUser = user;
        System.out.println("AdminDashboardController.initContext() called for: " + user.getName());

        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getName());
        }

        contentStack.setStyle("-fx-background-color: #FFF5F8;");

        setupBookingTable();
        loadAdminDashboardData();
        loadDashboardView();
        setActiveButton(dashboardBtn);
    }

    private void setupBookingTable() {
        resourceColumn.setCellValueFactory(new PropertyValueFactory<>("resourceName"));
        requesterColumn.setCellValueFactory(new PropertyValueFactory<>("requesterName"));
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
    }

    private void loadAdminDashboardData() {
        try {
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

            // Load users count
            var users = authService.getAllUsers();
            if (totalUsersLabel != null) {
                totalUsersLabel.setText(String.valueOf(users.size()));
            }

            // Load bookings
            List<Booking> allBookings = bookingService.getBookings();
            System.out.println("Admin - Total bookings loaded: " + (allBookings != null ? allBookings.size() : 0));

            long pending = 0, approved = 0, rejected = 0, today = 0;

            if (allBookings != null) {
                LocalDateTime now = LocalDateTime.now();
                for (Booking booking : allBookings) {
                    String status = booking.getStatus();
                    if ("PENDING".equalsIgnoreCase(status)) pending++;
                    else if ("APPROVED".equalsIgnoreCase(status)) approved++;
                    else if ("REJECTED".equalsIgnoreCase(status)) rejected++;

                    if (booking.getStartTime().toLocalDate().equals(now.toLocalDate())) {
                        today++;
                    }
                }
            }

            if (pendingBookingsLabel != null) pendingBookingsLabel.setText(String.valueOf(pending));
            if (approvedBookingsLabel != null) approvedBookingsLabel.setText(String.valueOf(approved));
            if (rejectedBookingsLabel != null) rejectedBookingsLabel.setText(String.valueOf(rejected));
            if (todayBookingsLabel != null) todayBookingsLabel.setText(String.valueOf(today));

            // Load pending bookings into table
            if (bookingTable != null) {
                bookingTable.getItems().clear();
                if (allBookings != null) {
                    // Show only pending bookings in the admin table
                    allBookings.stream()
                            .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()))
                            .forEach(bookingTable.getItems()::add);
                    System.out.println("Admin - Added " + bookingTable.getItems().size() + " pending bookings to table");
                }
            }

            // Update pending count
            if (pendingCountLabel != null) {
                pendingCountLabel.setText("Pending: " + pending);
            }

            if (statusLabel != null) {
                statusLabel.setText("Last updated: " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        } catch (Exception e) {
            System.err.println("Error loading admin dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDashboardView() {
        loadView("dashboard-view.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            System.out.println("Loading view: " + fxmlFile);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/roomify/" + fxmlFile));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller != null) {
                System.out.println("Controller found: " + controller.getClass().getSimpleName());
                if (controller instanceof DashboardController) {
                    ((DashboardController) controller).initContext(currentUser);
                } else if (controller instanceof ManageResourceController) {
                    ((ManageResourceController) controller).initContext(currentUser);
                } else if (controller instanceof ManageUserController) {
                    ((ManageUserController) controller).initContext(currentUser);
                } else if (controller instanceof BookingApprovalController) {
                    ((BookingApprovalController) controller).initContext(currentUser);
                } else if (controller instanceof ReportsController) {
                    ((ReportsController) controller).initContext(currentUser);
                } else if (controller instanceof SystemLogsController) {
                    ((SystemLogsController) controller).initContext(currentUser);
                }
            }

            contentStack.getChildren().clear();
            contentStack.getChildren().add(view);
            System.out.println("View loaded successfully: " + fxmlFile);
        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlFile);
            e.printStackTrace();

            Label errorLabel = new Label("Failed to load: " + fxmlFile + "\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            contentStack.getChildren().clear();
            contentStack.getChildren().add(errorLabel);
        }
    }

    private void setActiveButton(Button activeButton) {
        Button[] buttons = {dashboardBtn, resourcesBtn, usersBtn, approvalsBtn, reportsBtn, logsBtn};
        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().removeAll(ACTIVE_STYLE, INACTIVE_STYLE);
                btn.getStyleClass().add(INACTIVE_STYLE);
            }
        }

        if (activeButton != null) {
            activeButton.getStyleClass().removeAll(INACTIVE_STYLE);
            activeButton.getStyleClass().add(ACTIVE_STYLE);
        }
    }

    @FXML
    private void handleDashboard() {
        loadDashboardView();
        setActiveButton(dashboardBtn);
        loadAdminDashboardData();
    }

    @FXML
    private void handleResources() {
        loadView("manage-resources.fxml");
        setActiveButton(resourcesBtn);
    }

    @FXML
    private void handleUsers() {
        loadView("manage-users-view.fxml");
        setActiveButton(usersBtn);
    }

    @FXML
    private void handleApprovals() {
        loadView("booking-approvals.fxml");
        setActiveButton(approvalsBtn);
    }

    @FXML
    private void handleReports() {
        loadView("reports-view.fxml");
        setActiveButton(reportsBtn);
    }

    @FXML
    private void handleLogs() {
        loadView("system-logs-view.fxml");
        setActiveButton(logsBtn);
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Admin - Refresh clicked");
        loadAdminDashboardData();
        AlertHelper.showInformation("Refreshed", "Admin dashboard data refreshed.");
    }

    @FXML
    private void handleApprove() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a booking to approve.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(selected.getStatus())) {
            AlertHelper.showError("Invalid Status", "Only pending bookings can be approved.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation("Approve Booking",
                "Approve booking '" + selected.getBookingId() + "' for " + selected.getResourceName() + "?");

        if (confirm) {
            boolean success = bookingService.approveBooking(selected.getBookingId());
            if (success) {
                loadAdminDashboardData();
                AlertHelper.showInformation("Success", "Booking '" + selected.getBookingId() + "' approved.");
            } else {
                AlertHelper.showError("Error", "Failed to approve booking.");
            }
        }
    }

    @FXML
    private void handleReject() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a booking to reject.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(selected.getStatus())) {
            AlertHelper.showError("Invalid Status", "Only pending bookings can be rejected.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Booking");
        dialog.setHeaderText("Reject: " + selected.getBookingId());
        dialog.setContentText("Reason for rejection:");

        dialog.showAndWait().ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                AlertHelper.showError("Reason Required", "Please provide a reason.");
                return;
            }

            boolean success = bookingService.rejectBooking(selected.getBookingId());
            if (success) {
                loadAdminDashboardData();
                AlertHelper.showInformation("Success", "Booking '" + selected.getBookingId() + "' rejected.");
            } else {
                AlertHelper.showError("Error", "Failed to reject booking.");
            }
        });
    }

    @FXML
    private void handleLogout() {
        sessionManager.logout();
        Stage currentStage = (Stage) logoutButton.getScene().getWindow();
        StageCoordinator.getInstance().showLogin(currentStage);
    }
}