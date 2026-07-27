package com.example.roomify;

import com.example.roomify.controller.AdminDashboardController;
import com.example.roomify.controller.BookingController;
import com.example.roomify.controller.ResourceListController;
import com.example.roomify.controller.StaffDashboardController;
import com.example.roomify.controller.StudentDashboardController;
import com.example.roomify.model.Resource;
import com.example.roomify.model.User;
import com.example.roomify.util.AlertHelper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Central Stage Routing Manager.
 */
public class StageCoordinator {

    private static final String FXML_BASE = "/com/example/roomify/";

    private static StageCoordinator instance;
    private User currentUser;

    private StageCoordinator() {
        // Singleton pattern
    }

    public static synchronized StageCoordinator getInstance() {
        if (instance == null) {
            instance = new StageCoordinator();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Navigate to the Admin Dashboard (single-window approach).
     */
    public void showAdminDashboard(User user, Stage stage) {
        this.currentUser = user;
        try {
            FXMLLoader loader = createLoader("admin-dashboard.fxml");
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            if (controller != null) {
                controller.initContext(user);
            }

            switchScene(stage, root, "Roomify - Admin Dashboard", 1100, 700);
        } catch (Exception e) {
            handleLoadFailure("Admin Dashboard", e);
        }
    }

    /**
     * Navigate to the Staff Dashboard.
     */
    public void showStaffDashboard(User user, Stage stage) {
        this.currentUser = user;
        try {
            FXMLLoader loader = createLoader("staff-dashboard-view.fxml");
            Parent root = loader.load();

            StaffDashboardController controller = loader.getController();
            if (controller != null) {
                controller.initContext(user);
            }

            switchScene(stage, root, "Roomify - Staff Dashboard", 1100, 700);
        } catch (Exception e) {
            handleLoadFailure("Staff Dashboard", e);
        }
    }

    /**
     * Navigate to the Student Dashboard.
     */
    public void showStudentDashboard(User user, Stage stage) {
        this.currentUser = user;
        try {
            FXMLLoader loader = createLoader("student-dashboard-view.fxml");
            Parent root = loader.load();

            StudentDashboardController controller = loader.getController();
            if (controller != null) {
                controller.initContext(user);
            }

            switchScene(stage, root, "Roomify - Student Dashboard", 1100, 700);
        } catch (Exception e) {
            handleLoadFailure("Student Dashboard", e);
        }
    }

    /**
     * Route user to their role-specific dashboard.
     */
    public void showDashboardForUser(User user, Stage stage) {
        if (user == null) {
            showLogin(stage);
            return;
        }
        if (user.getRole() == null) {
            showLogin(stage);
            return;
        }
        switch (user.getRole()) {
            case ADMIN:
                showAdminDashboard(user, stage);
                break;
            case STAFF:
                showStaffDashboard(user, stage);
                break;
            case STUDENT:
                showStudentDashboard(user, stage);
                break;
            default:
                showLogin(stage);
        }
    }
    /**
     * Navigate to the Resource List screen.
     */
    public void showResourceList(User user, Stage stage) {
        this.currentUser = user;
        try {
            FXMLLoader loader = createLoader("resource-list-view.fxml");
            Parent root = loader.load();

            ResourceListController controller = loader.getController();
            if (controller != null) {
                controller.initContext(user);
            }

            switchScene(stage, root, "Roomify - Available Resources", 900, 600);
        } catch (Exception e) {
            handleLoadFailure("Resource List", e);
        }
    }

    /**
     * Navigate to the Booking screen.
     */
    public void showBookingView(User user, Resource resource, Stage stage) {
        this.currentUser = user;
        try {
            FXMLLoader loader = createLoader("booking-view.fxml");
            Parent root = loader.load();

            BookingController controller = loader.getController();
            if (controller != null) {
                controller.initContext(user, resource);
            }

            switchScene(stage, root, "Roomify - New Booking", 640, 520);
        } catch (Exception e) {
            handleLoadFailure("Booking View", e);
        }
    }


    /**
     * Navigate to the Booking Approval screen.
     */
    public void showApprovalDashboard(Stage stage) {
        try {
            FXMLLoader loader = createLoader("approval-view.fxml");
            Parent root = loader.load();

            switchScene(stage, root, "Roomify - Booking Approval", 1000, 650);
        } catch (Exception e) {
            handleLoadFailure("Booking Approval", e);
        }
    }

    /**
     * Navigate to the Reports screen.
     */
    public void showReports(Stage stage) {
        try {
            FXMLLoader loader = createLoader("report-view.fxml");
            Parent root = loader.load();

            switchScene(stage, root, "Roomify - Reports", 950, 650);
        } catch (Exception e) {
            handleLoadFailure("Reports", e);
        }
    }
    /**
     * Navigate back to the Login screen.
     */
    public void showLogin(Stage stage) {
        this.currentUser = null;
        try {
            FXMLLoader loader = createLoader("login-view.fxml");
            Parent root = loader.load();

            switchScene(stage, root, "Roomify - Login", 700, 500);
        } catch (Exception e) {
            handleLoadFailure("Login View", e);
        }
    }

    /**
     * Creates an FXMLLoader with fallback logic if the standard path isn't found.
     */
    private FXMLLoader createLoader(String fxmlFileName) throws IOException {
        String primaryPath = FXML_BASE + fxmlFileName;
        URL resourceUrl = getClass().getResource(primaryPath);

        // Fallback search at root classpath
        if (resourceUrl == null) {
            resourceUrl = getClass().getResource("/" + fxmlFileName);
        }

        if (resourceUrl == null) {
            throw new IOException("Could not locate FXML file: " + fxmlFileName);
        }

        return new FXMLLoader(resourceUrl);
    }

    /**
     * Displays a uniform error dialog when a view fails to load.
     */
    private void handleLoadFailure(String viewName, Exception e) {
        System.err.println("Failed to load " + viewName + ": " + e.getMessage());
        e.printStackTrace();
        AlertHelper.showError(
                "Navigation Error",
                "Failed to load " + viewName + ".\nDetails: " + e.getMessage()
        );
    }

    private void switchScene(Stage stage, Parent root, String title, double width, double height) {
        Scene scene = new Scene(root, width, height);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}