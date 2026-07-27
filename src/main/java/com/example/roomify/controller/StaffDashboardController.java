package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.booking.BookingService;
import com.example.roomify.model.Booking;
import com.example.roomify.model.Resource;
import com.example.roomify.model.User;
import com.example.roomify.persistence.ResourceFileHandler;
import com.example.roomify.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Staff Dashboard Controller - Main dashboard for staff.
 */
public class StaffDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Button logoutButton;
    @FXML private Button logoutSidebarBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button searchResourcesBtn;
    @FXML private Button myBookingsBtn;
    @FXML private Button profileBtn;
    @FXML private StackPane contentStack;

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

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final BookingService bookingService = new BookingService();
    private User currentUser;

    private static final String ACTIVE_STYLE = "sidebar-btn-active";
    private static final String INACTIVE_STYLE = "sidebar-btn";

    public void initContext(User user) {
        this.currentUser = user;
        System.out.println("StaffDashboardController.initContext() called for: " + user.getName());

        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getName());
        }
        if (roleLabel != null) {
            roleLabel.setText(user.getRole().name());
        }

        if (contentStack != null) {
            contentStack.setStyle("-fx-background-color: #FFF7FA;");
        }

        setupRecentBookingsTable();
        loadDashboardData();
        loadDashboardView();
        setActiveButton(dashboardBtn);
    }

    private void setupRecentBookingsTable() {
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
            System.err.println("Error setting up table: " + e.getMessage());
        }
    }

    private void loadDashboardData() {
        try {
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

            List<Booking> allBookings = bookingService.getBookings();
            long pending = 0, approved = 0, rejected = 0, today = 0;

            if (allBookings != null && currentUser != null) {
                LocalDateTime now = LocalDateTime.now();
                for (Booking booking : allBookings) {
                    if (booking.getRequesterName().equals(currentUser.getName())) {
                        String status = booking.getStatus();
                        if ("PENDING".equalsIgnoreCase(status)) pending++;
                        else if ("APPROVED".equalsIgnoreCase(status)) approved++;
                        else if ("REJECTED".equalsIgnoreCase(status)) rejected++;

                        if (booking.getStartTime().toLocalDate().equals(now.toLocalDate())) {
                            today++;
                        }
                    }
                }
            }

            if (pendingBookingsLabel != null) pendingBookingsLabel.setText(String.valueOf(pending));
            if (approvedBookingsLabel != null) approvedBookingsLabel.setText(String.valueOf(approved));
            if (rejectedBookingsLabel != null) rejectedBookingsLabel.setText(String.valueOf(rejected));
            if (todayBookingsLabel != null) todayBookingsLabel.setText(String.valueOf(today));

            if (recentBookingsTable != null) {
                recentBookingsTable.getItems().clear();
                if (allBookings != null && currentUser != null) {
                    allBookings.stream()
                            .filter(b -> b.getRequesterName().equals(currentUser.getName()))
                            .limit(5)
                            .forEach(recentBookingsTable.getItems()::add);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
        }
    }

    private void loadDashboardView() {
        loadView("dashboard-view.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            System.out.println("Loading view: " + fxmlFile);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/roomify/" + fxmlFile));

            if (loader.getLocation() == null) {
                System.err.println("FXML file not found: " + fxmlFile);
                Label errorLabel = new Label("View not found: " + fxmlFile);
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                contentStack.getChildren().clear();
                contentStack.getChildren().add(errorLabel);
                return;
            }

            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller != null) {
                System.out.println("Controller found: " + controller.getClass().getSimpleName());
                if (controller instanceof DashboardController) {
                    ((DashboardController) controller).initContext(currentUser);
                } else if (controller instanceof SearchResourcesController) {
                    ((SearchResourcesController) controller).initContext(currentUser);
                } else if (controller instanceof BookingController) {
                    ((BookingController) controller).initContext(currentUser, null);
                } else if (controller instanceof MyBookingsController) {
                    ((MyBookingsController) controller).initContext(currentUser);
                } else if (controller instanceof ProfileController) {
                    ((ProfileController) controller).initContext(currentUser);
                }
            } else {
                System.err.println("Controller is null for: " + fxmlFile);
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
        Button[] buttons = {dashboardBtn, searchResourcesBtn, myBookingsBtn, profileBtn};
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
        loadDashboardData();
    }

    @FXML
    private void handleSearchResources() {
        loadView("search-resources.fxml");
        setActiveButton(searchResourcesBtn);
    }

    @FXML
    private void handleMyBookings() {
        loadView("my-bookings.fxml");
        setActiveButton(myBookingsBtn);
    }

    @FXML
    private void handleProfile() {
        loadView("profile.fxml");
        setActiveButton(profileBtn);
    }

    @FXML
    private void handleLogout() {
        sessionManager.logout();
        Stage currentStage = (Stage) logoutButton.getScene().getWindow();
        StageCoordinator.getInstance().showLogin(currentStage);
    }
}
