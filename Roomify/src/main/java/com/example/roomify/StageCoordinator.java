package com.example.roomify;

import com.example.roomify.controller.AdminDashboardController;
import com.example.roomify.controller.BookingController;
import com.example.roomify.controller.ResourceListController;
import com.example.roomify.model.Resource;
import com.example.roomify.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Central Stage Routing Manager.
 * <p>
 * Singleton responsible for all JavaFX scene switching in the application.
 * It keeps track of the currently logged-in user for the duration of the
 * session and hands that user (plus any other needed context, e.g. a
 * selected Resource) to each controller after its FXML is loaded, since
 * FXMLLoader instantiates controllers with a no-arg constructor and can't
 * pass constructor arguments itself.
 * <p>
 * Usage from LoginController (Member 1's module) once a user authenticates:
 * <pre>
 *     StageCoordinator.getInstance().showResourceList(authenticatedUser, currentStage); // Student/Staff
 *     StageCoordinator.getInstance().showAdminDashboard(authenticatedUser, currentStage); // Admin
 * </pre>
 */
public class StageCoordinator {

    private static final String FXML_BASE = "/com/example/roomify/";

    private static StageCoordinator instance;

    private User currentUser;

    private StageCoordinator() {
        // Private constructor: singleton
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
     * Navigate to the Resource List screen (default landing page for
     * Students and Staff after login).
     */
    public void showResourceList(User user, Stage stage) {
        this.currentUser = user;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_BASE + "resource-list-view.fxml"));
            Parent root = loader.load();

            ResourceListController controller = loader.getController();
            controller.initContext(user);

            switchScene(stage, root, "Roomify - Available Resources", 900, 600);
        } catch (IOException e) {
            System.err.println("Failed to load Resource List view: " + e.getMessage());
        }
    }

    /**
     * Navigate to the Booking screen for a specific resource the user has
     * chosen from the Resource List.
     */
    public void showBookingView(User user, Resource resource, Stage stage) {
        this.currentUser = user;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_BASE + "booking-view.fxml"));
            Parent root = loader.load();

            BookingController controller = loader.getController();
            controller.initContext(user, resource);

            switchScene(stage, root, "Roomify - New Booking", 640, 520);
        } catch (IOException e) {
            System.err.println("Failed to load Booking view: " + e.getMessage());
        }
    }

    /**
     * Navigate to the Admin Dashboard (default landing page for Admins
     * after login).
     */
    public void showAdminDashboard(User user, Stage stage) {
        this.currentUser = user;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_BASE + "admin-dashboard-view.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.initContext(user);

            switchScene(stage, root, "Roomify - Admin Dashboard", 950, 650);
        } catch (IOException e) {
            System.err.println("Failed to load Admin Dashboard view: " + e.getMessage());
        }
    }

    /**
     * Navigate back to the Login screen (e.g. on logout). Clears the
     * current session's user reference.
     */
    public void showLogin(Stage stage) {
        this.currentUser = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_BASE + "login-view.fxml"));
            Parent root = loader.load();
            switchScene(stage, root, "Roomify - Login", 700, 500);
        } catch (IOException e) {
            System.err.println("Failed to load Login view: " + e.getMessage());
        }
    }

    private void switchScene(Stage stage, Parent root, String title, double width, double height) {
        Scene scene = new Scene(root, width, height);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}
