package com.example.roomify.controller;

import com.example.roomify.model.Resource;
import com.example.roomify.model.User;
import com.example.roomify.persistence.ResourceFileHandler;
import com.example.roomify.util.AlertFactory;
import com.example.roomify.util.AlertHelper;
import com.example.roomify.validation.InputValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage Resource Controller - CRUD operations for campus resources.
 */
public class ManageResourceController {

    // ==================== FXML INJECTIONS ====================
    @FXML private TextField searchField;
    @FXML private TableView<Resource> resourceTable;
    @FXML private TableColumn<Resource, String> resourceIdColumn;
    @FXML private TableColumn<Resource, String> nameColumn;
    @FXML private TableColumn<Resource, String> typeColumn;
    @FXML private TableColumn<Resource, String> locationColumn;
    @FXML private TableColumn<Resource, Integer> capacityColumn;
    @FXML private TableColumn<Resource, String> statusColumn;

    @FXML private TextField resourceIdField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField locationField;
    @FXML private TextField capacityField;
    @FXML private ComboBox<String> statusComboBox;

    // ==================== DATA ====================
    private final ObservableList<Resource> allResources = FXCollections.observableArrayList();
    private FilteredList<Resource> filteredResources;
    private User currentUser;

    // ==================== INITIALIZATION ====================
    public void initContext(User user) {
        this.currentUser = user;
        setupComboBoxes();
        setupTableColumns();
        loadResources();
        setupTableSelectionListener();
    }

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTableColumns();
        loadResources();
        setupTableSelectionListener();

        // Add row selection styling
        resourceTable.setRowFactory(tv -> new TableRow<Resource>() {
            @Override
            protected void updateItem(Resource item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if (isSelected()) {
                        setStyle("-fx-background-color: #DBEAFE;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void setupComboBoxes() {
        typeComboBox.setItems(FXCollections.observableArrayList(
                "Study Room", "Computer Lab", "Lecture Hall", "Auditorium",
                "Conference Room", "Discussion Room", "Library", "Other"
        ));
        statusComboBox.setItems(FXCollections.observableArrayList(
                "AVAILABLE", "BOOKED", "UNDER_MAINTENANCE", "DISABLED"
        ));
        typeComboBox.getSelectionModel().selectFirst();
        statusComboBox.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
        resourceIdColumn.setCellValueFactory(new PropertyValueFactory<>("resourceId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Color-code status column
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
                switch (item) {
                    case "AVAILABLE":
                        setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                        break;
                    case "BOOKED":
                        setStyle("-fx-text-fill: #3B82F6; -fx-font-weight: bold;");
                        break;
                    case "UNDER_MAINTENANCE":
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                        break;
                    case "DISABLED":
                        setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                        break;
                    default:
                        setStyle("");
                }
            }
        });
    }

    private void loadResources() {
        List<Resource> loaded = ResourceFileHandler.loadResources();
        if (loaded != null && !loaded.isEmpty()) {
            allResources.setAll(loaded);
        } else {
            // Default seed resources if resources.dat does not exist yet
            allResources.setAll(
                    new Resource("RES-001", "Study Room 3A", "Study Room", 4, "AVAILABLE", "Library Building"),
                    new Resource("RES-002", "Computer Lab C", "Computer Lab", 30, "AVAILABLE", "BCS Block"),
                    new Resource("RES-003", "Main Auditorium", "Auditorium", 200, "UNDER_MAINTENANCE", "Main Hall"),
                    new Resource("RES-004", "Discussion Room 2B", "Discussion Room", 8, "AVAILABLE", "Block A"),
                    new Resource("RES-005", "Conference Room", "Conference Room", 15, "BOOKED", "Admin Building")
            );
            ResourceFileHandler.saveResources(new ArrayList<>(allResources));
        }

        filteredResources = new FilteredList<>(allResources, p -> true);
        resourceTable.setItems(filteredResources);
    }

    private void setupTableSelectionListener() {
        resourceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });
    }

    private void populateForm(Resource resource) {
        resourceIdField.setText(resource.getResourceId());
        nameField.setText(resource.getName());
        typeComboBox.setValue(resource.getType());
        locationField.setText(resource.getLocation());
        capacityField.setText(String.valueOf(resource.getCapacity()));
        statusComboBox.setValue(resource.getStatus());
    }

    private void clearForm() {
        resourceIdField.clear();
        nameField.clear();
        typeComboBox.getSelectionModel().selectFirst();
        locationField.clear();
        capacityField.clear();
        statusComboBox.getSelectionModel().selectFirst();
        resourceTable.getSelectionModel().clearSelection();
    }

    private String validateForm() {
        String id = resourceIdField.getText().trim();
        String name = nameField.getText().trim();
        String type = typeComboBox.getValue();
        String location = locationField.getText().trim();
        String capacity = capacityField.getText().trim();

        if (id.isEmpty()) return "Resource ID is required.";
        if (!InputValidator.isValidResourceId(id)) {
            return "Resource ID must be in format RES-XXX (e.g., RES-001).";
        }
        if (name.isEmpty()) return "Resource name is required.";
        if (name.length() < 2) return "Name must be at least 2 characters.";
        if (type == null || type.isEmpty()) return "Type is required.";
        if (location.isEmpty()) return "Location is required.";
        try {
            int cap = Integer.parseInt(capacity);
            if (cap <= 0) return "Capacity must be a positive number.";
            if (cap > 1000) return "Capacity cannot exceed 1000.";
        } catch (NumberFormatException e) {
            return "Capacity must be a valid number.";
        }
        return null;
    }

    // ==================== EVENT HANDLERS ====================
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().toLowerCase().trim();
        filteredResources.setPredicate(resource -> {
            if (query.isEmpty()) return true;
            return resource.getResourceId().toLowerCase().contains(query) ||
                    resource.getName().toLowerCase().contains(query) ||
                    resource.getType().toLowerCase().contains(query) ||
                    resource.getLocation().toLowerCase().contains(query);
        });
    }

    @FXML
    private void handleClearSearch(ActionEvent event) {
        searchField.clear();
        filteredResources.setPredicate(resource -> true);
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        String error = validateForm();
        if (error != null) {
            AlertHelper.showError("Validation Error", error);
            return;
        }

        String id = resourceIdField.getText().trim();
        boolean exists = allResources.stream()
                .anyMatch(r -> r.getResourceId().equalsIgnoreCase(id));

        if (exists) {
            AlertHelper.showError("Duplicate ID", "Resource with ID '" + id + "' already exists.");
            return;
        }

        Resource resource = new Resource(
                id,
                nameField.getText().trim(),
                typeComboBox.getValue(),
                Integer.parseInt(capacityField.getText().trim()),
                statusComboBox.getValue(),
                locationField.getText().trim()
        );

        allResources.add(resource);
        ResourceFileHandler.saveResources(new ArrayList<>(allResources));
        clearForm();
        AlertHelper.showInformation("Success", "Resource added successfully.");
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        Resource selected = resourceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a resource to edit.");
            return;
        }

        String error = validateForm();
        if (error != null) {
            AlertHelper.showError("Validation Error", error);
            return;
        }

        // Remove old and add updated
        allResources.remove(selected);
        Resource updated = new Resource(
                resourceIdField.getText().trim(),
                nameField.getText().trim(),
                typeComboBox.getValue(),
                Integer.parseInt(capacityField.getText().trim()),
                statusComboBox.getValue(),
                locationField.getText().trim()
        );
        allResources.add(updated);
        ResourceFileHandler.saveResources(new ArrayList<>(allResources));
        resourceTable.refresh();
        clearForm();
        AlertHelper.showInformation("Success", "Resource updated successfully.");
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Resource selected = resourceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a resource to delete.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation("Delete Resource",
                "Are you sure you want to delete '" + selected.getName() + "'?");

        if (confirm) {
            allResources.remove(selected);
            ResourceFileHandler.saveResources(new ArrayList<>(allResources));
            clearForm();
            AlertHelper.showInformation("Success", "Resource deleted successfully.");
        }
    }

    @FXML
    private void handleClearFields(ActionEvent event) {
        clearForm();
    }

    // ==================== INNER CLASS - Extended Resource ====================
    // Note: The Resource model class needs to be extended with 'location' field
    // If not, we'll use a wrapper or modify the existing Resource class
}