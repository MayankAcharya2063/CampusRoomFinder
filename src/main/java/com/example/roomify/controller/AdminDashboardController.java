package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.model.User;
import com.example.roomify.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

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

    private final SessionManager sessionManager = SessionManager.getInstance();
    private User currentUser;

    private static final String ACTIVE_STYLE = "sidebar-btn-active";
    private static final String INACTIVE_STYLE = "sidebar-btn";

    public void initContext(User user) {
        this.currentUser = user;

        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getName());
        }

        // Updated background color to soft pinkish white (matches CSS content-area)
        contentStack.setStyle("-fx-background-color: #FFF5F8;");

        loadDashboardView();
        setActiveButton(dashboardBtn);
    }

    private void loadDashboardView() {
        loadView("dashboard-view.fxml");
    }

    private void loadResourcesView() {
        loadView("manage-resources.fxml");
    }

    private void loadUsersView() {
        loadView("manage-users.fxml");
    }

    private void loadApprovalsView() {
        loadView("booking-approvals.fxml");
    }

    private void loadReportsView() {
        loadView("reports-view.fxml");
    }

    private void loadLogsView() {
        loadView("system-logs-view.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/roomify/" + fxmlFile));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller != null) {
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
        } catch (IOException e) {
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
    }

    @FXML
    private void handleResources() {
        loadResourcesView();
        setActiveButton(resourcesBtn);
    }

    @FXML
    private void handleUsers() {
        loadUsersView();
        setActiveButton(usersBtn);
    }

    @FXML
    private void handleApprovals() {
        loadApprovalsView();
        setActiveButton(approvalsBtn);
    }

    @FXML
    private void handleReports() {
        loadReportsView();
        setActiveButton(reportsBtn);
    }

    @FXML
    private void handleLogs() {
        loadLogsView();
        setActiveButton(logsBtn);
    }

    @FXML
    private void handleLogout() {
        sessionManager.logout();
        Stage currentStage = (Stage) logoutButton.getScene().getWindow();
        StageCoordinator.getInstance().showLogin(currentStage);
    }
}