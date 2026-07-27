package com.example.roomify;

import com.example.roomify.controller.*;
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
     * Navigate to the Admin Dashboard.
     */
    public void showAdminDashboard(User user, Stage stage) {
        this.currentUser = user;
        try {
            FXMLLoader loader = createLoader("admin-dashboard-view.fxml");
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
     * Navigate to the Student Dashboard.
     */
    public void showStudentDashboard(User user, Stage stage) {
        this.currentUser = user;
        try {
            System.out.println("Loading Student Dashboard...");
            FXMLLoader loader = createLoader("student-dashboard-view.fxml");
            Parent root = loader.load();
            StudentDashboardController controller = loader.getController();
            if (controller != null) {
                controller.initContext(user);
            }
            switchScene(stage, root, "Roomify - Student Dashboard", 1100, 700);
        } catch (Exception e) {
            System.err.println("Error loading Student Dashboard: " + e.getMessage());
            e.printStackTrace();
            handleLoadFailure("Student Dashboard", e);
        }
    }

    /**
     * Navigate to the Staff Dashboard.
     */
    public void showStaffDashboard(User user, Stage stage) {
        this.currentUser = user;
        try {
            System.out.println("Loading Staff Dashboard...");
            FXMLLoader loader = createLoader("staff-dashboard-view.fxml");
            Parent root = loader.load();
            StaffDashboardController controller = loader.getController();
            if (controller != null) {
                controller.initContext(user);
            }
            switchScene(stage, root, "Roomify - Staff Dashboard", 1100, 700);
        } catch (Exception e) {
            System.err.println("Error loading Staff Dashboard: " + e.getMessage());
            e.printStackTrace();
            handleLoadFailure("Staff Dashboard", e);
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
            switchScene(stage, root, "Roomify - New Booking", 640, 620);
        } catch (Exception e) {
            handleLoadFailure("Booking View", e);
        }
    }

    /**
     * Navigate to the Login screen.
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

        System.out.println("Looking for: " + primaryPath);

        // Fallback search at root classpath
        if (resourceUrl == null) {
            resourceUrl = getClass().getResource("/" + fxmlFileName);
            System.out.println("Fallback looking for: /" + fxmlFileName);
        }

        if (resourceUrl == null) {
            // Try without leading slash
            resourceUrl = getClass().getResource(fxmlFileName);
            System.out.println("Final fallback looking for: " + fxmlFileName);
        }

        if (resourceUrl == null) {
            throw new IOException("Could not locate FXML file: " + fxmlFileName);
        }

        System.out.println("Found FXML at: " + resourceUrl);
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