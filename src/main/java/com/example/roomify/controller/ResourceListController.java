package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.UserRole;
import com.example.roomify.model.Resource;
import com.example.roomify.model.User;
import com.example.roomify.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Controller for ResourceListView.fxml.
 * <p>
 * Landing screen for Students and Staff after login. Lists all campus
 * resources, supports filtering by name, and lets a user select a resource
 * to book. Admins routed here also see a "Manage Resources" shortcut back
 * to the Admin Dashboard (role-based UI visibility).
 */
public class ResourceListController {

    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;

    @FXML private TableView<Resource> resourceTable;
    @FXML private TableColumn<Resource, String> nameColumn;
    @FXML private TableColumn<Resource, String> typeColumn;
    @FXML private TableColumn<Resource, Integer> capacityColumn;
    @FXML private TableColumn<Resource, String> statusColumn;

    @FXML private Button bookButton;
    @FXML private Button adminDashboardButton;
    @FXML private Button logoutButton;

    private User currentUser;
    private final ObservableList<Resource> masterResourceList = FXCollections.observableArrayList();

    /**
     * Called by StageCoordinator right after this controller's FXML is
     * loaded, since FXMLLoader can't pass constructor arguments.
     */
    public void initContext(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getName() + " (" + user.getRole() + ")");

        // Role-based UI visibility: only Admins get the shortcut to the dashboard
        adminDashboardButton.setVisible(user.getRole() == UserRole.ADMIN);
        adminDashboardButton.setManaged(user.getRole() == UserRole.ADMIN);

        loadSampleResources();
        resourceTable.setItems(masterResourceList);
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterResources(newVal));
    }

    /**
     * TEMPORARY: sample data standing in for Member 6's ResourceManager<T>
     * / file-persisted resource list. Replace with a call such as
     * {@code ResourceManager.getInstance().getAllResources()} once available.
     */
    private void loadSampleResources() {
        masterResourceList.setAll(
                new Resource("R-001", "Study Room A", "Study Room", 4, "AVAILABLE"),
                new Resource("R-002", "Study Room B", "Study Room", 6, "AVAILABLE"),
                new Resource("R-003", "Computer Lab 1", "Lab", 30, "AVAILABLE"),
                new Resource("R-004", "Main Auditorium", "Auditorium", 200, "MAINTENANCE")
        );
    }

    private void filterResources(String query) {
        if (query == null || query.isBlank()) {
            resourceTable.setItems(masterResourceList);
            return;
        }
        String lower = query.toLowerCase();
        ObservableList<Resource> filtered = masterResourceList.filtered(
                r -> r.getName().toLowerCase().contains(lower) || r.getType().toLowerCase().contains(lower)
        );
        resourceTable.setItems(filtered);
    }

    @FXML
    void handleBookResource(ActionEvent event) {
        Resource selected = resourceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Resource Selected", "Please select a resource to book first.");
            return;
        }
        if (!"AVAILABLE".equals(selected.getStatus())) {
            AlertHelper.showError("Resource Unavailable", selected.getName() + " is not currently available for booking.");
            return;
        }

        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        StageCoordinator.getInstance().showBookingView(currentUser, selected, currentStage);
    }

    @FXML
    void handleGoToAdminDashboard(ActionEvent event) {
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        StageCoordinator.getInstance().showAdminDashboard(currentUser, currentStage);
    }

    @FXML
    void handleLogout(ActionEvent event) {
        currentUser.logout();
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        StageCoordinator.getInstance().showLogin(currentStage);
    }
}