package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.model.Resource;
import com.example.roomify.model.User;
import com.example.roomify.persistence.ResourceFileHandler;
import com.example.roomify.service.SessionManager;
import com.example.roomify.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

/**
 * Search Resources Controller - Allows users to search and view resources.
 */
public class SearchResourcesController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> locationFilter;
    @FXML private CheckBox availableOnlyCheck;

    @FXML private TableView<Resource> resourceTable;
    @FXML private TableColumn<Resource, String> resourceIdColumn;
    @FXML private TableColumn<Resource, String> nameColumn;
    @FXML private TableColumn<Resource, String> typeColumn;
    @FXML private TableColumn<Resource, String> locationColumn;
    @FXML private TableColumn<Resource, Integer> capacityColumn;
    @FXML private TableColumn<Resource, String> statusColumn;

    @FXML private Button bookButton;
    @FXML private Button refreshButton;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final ObservableList<Resource> allResources = FXCollections.observableArrayList();
    private FilteredList<Resource> filteredResources;
    private User currentUser;

    public void initContext(User user) {
        this.currentUser = user;
        setupComboBoxes();
        setupTableColumns();
        loadResources();
    }

    private void setupComboBoxes() {
        typeFilter.setItems(FXCollections.observableArrayList(
                "All Types", "Study Room", "Computer Lab", "Lecture Hall",
                "Auditorium", "Conference Room", "Discussion Room", "Library", "Other"
        ));
        typeFilter.getSelectionModel().selectFirst();

        locationFilter.setItems(FXCollections.observableArrayList(
                "All Locations", "Library Building", "BCS Block", "Main Hall",
                "Block A", "Admin Building"
        ));
        locationFilter.getSelectionModel().selectFirst();

        availableOnlyCheck.setSelected(false);
    }

    private void setupTableColumns() {
        resourceIdColumn.setCellValueFactory(new PropertyValueFactory<>("resourceId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusColumn.setCellFactory(column -> new TableCell<Resource, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                if ("AVAILABLE".equals(item)) {
                    setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                } else if ("BOOKED".equals(item)) {
                    setStyle("-fx-text-fill: #3B82F6; -fx-font-weight: bold;");
                } else if ("UNDER_MAINTENANCE".equals(item)) {
                    setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                } else if ("DISABLED".equals(item)) {
                    setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void loadResources() {
        allResources.clear();
        List<Resource> savedResources = ResourceFileHandler.loadResources();
        if (savedResources != null && !savedResources.isEmpty()) {
            allResources.addAll(savedResources);
        }

        filteredResources = new FilteredList<>(allResources, p -> true);
        resourceTable.setItems(filteredResources);
        applyFilters();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String type = typeFilter.getValue();
        String location = locationFilter.getValue();
        boolean availableOnly = availableOnlyCheck.isSelected();

        filteredResources.setPredicate(resource -> {
            // Search filter
            if (!searchText.isEmpty()) {
                boolean matches = resource.getResourceId().toLowerCase().contains(searchText) ||
                        resource.getName().toLowerCase().contains(searchText) ||
                        resource.getType().toLowerCase().contains(searchText) ||
                        resource.getLocation().toLowerCase().contains(searchText);
                if (!matches) return false;
            }

            // Type filter
            if (type != null && !type.equals("All Types") && !resource.getType().equals(type)) {
                return false;
            }

            // Location filter
            if (location != null && !location.equals("All Locations") && !resource.getLocation().equals(location)) {
                return false;
            }

            // Available only filter
            if (availableOnly && !"AVAILABLE".equals(resource.getStatus())) {
                return false;
            }

            return true;
        });

        // Enable/disable book button based on selection
        resourceTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && "AVAILABLE".equals(newVal.getStatus())) {
                bookButton.setDisable(false);
            } else {
                bookButton.setDisable(true);
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFilters();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadResources();
        AlertHelper.showInformation("Refreshed", "Resources refreshed.");
    }

    @FXML
    private void handleBookResource(ActionEvent event) {
        Resource selected = resourceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a resource to book.");
            return;
        }

        if (!"AVAILABLE".equals(selected.getStatus())) {
            AlertHelper.showError("Unavailable", "This resource is not available for booking.");
            return;
        }

        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        StageCoordinator.getInstance().showBookingView(currentUser, selected, currentStage);
    }

    @FXML
    private void handleClearFilters(ActionEvent event) {
        searchField.clear();
        typeFilter.getSelectionModel().selectFirst();
        locationFilter.getSelectionModel().selectFirst();
        availableOnlyCheck.setSelected(false);
        applyFilters();
    }
}