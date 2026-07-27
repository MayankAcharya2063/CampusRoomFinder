package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.model.User;
import com.example.roomify.service.SessionManager;
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
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Manage User Controller - CRUD operations for system users.
 */
public class ManageUserController {

    // ==================== FXML INJECTIONS ====================
    @FXML private TextField searchField;

    @FXML private TableView<UserRow> userTable;
    @FXML private TableColumn<UserRow, String> userIdColumn;
    @FXML private TableColumn<UserRow, String> nameColumn;
    @FXML private TableColumn<UserRow, String> emailColumn;
    @FXML private TableColumn<UserRow, String> roleColumn;
    @FXML private TableColumn<UserRow, String> departmentColumn;

    @FXML private TextField userIdField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField departmentField;
    @FXML private TextField passwordField;

    // ==================== SERVICE REFERENCES ====================
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final AlertFactory alertFactory = AlertFactory.getInstance();

    // ==================== DATA ====================
    private ObservableList<UserRow> allUsers = FXCollections.observableArrayList();
    private FilteredList<UserRow> filteredUsers;
    private User currentUser;

    // ==================== INITIALIZATION ====================
    public void initContext(User user) {
        this.currentUser = user;
        setupComboBoxes();
        setupTableColumns();
        loadUsers();
        setupTableSelectionListener();
    }

    // Add this to your ManageUserController.java initialize() method
    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTableColumns();
        loadUsers();
        setupTableSelectionListener();

        // Add row selection styling
        userTable.setRowFactory(tv -> new TableRow<UserRow>() {
            @Override
            protected void updateItem(UserRow item, boolean empty) {
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
        roleComboBox.setItems(FXCollections.observableArrayList("STUDENT", "STAFF", "ADMIN"));
        roleComboBox.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));

        roleColumn.setCellFactory(column -> new TableCell<UserRow, String>() {
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
                    case "ADMIN":
                        setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                        break;
                    case "STAFF":
                        setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold;");
                        break;
                    case "STUDENT":
                        setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                        break;
                }
            }
        });
    }

    private void loadUsers() {
        List<UserRow> userList = generatePlaceholderUsers();
        allUsers.setAll(userList);
        filteredUsers = new FilteredList<>(allUsers, p -> true);
        userTable.setItems(filteredUsers);
    }

    private List<UserRow> generatePlaceholderUsers() {
        List<UserRow> users = new ArrayList<>();
        users.add(new UserRow("U001", "Admin User", "admin@roomify.com", "ADMIN", "Administration"));
        users.add(new UserRow("U002", "John Staff", "john.staff@roomify.com", "STAFF", "Computer Science"));
        users.add(new UserRow("U003", "Jane Student", "jane.student@roomify.com", "STUDENT", "Engineering"));
        users.add(new UserRow("U004", "Michael Lee", "michael.lee@roomify.com", "STAFF", "Mathematics"));
        users.add(new UserRow("U005", "Sarah Chen", "sarah.chen@roomify.com", "STUDENT", "Business"));
        users.add(new UserRow("U006", "David Kumar", "david.kumar@roomify.com", "STUDENT", "Computer Science"));
        return users;
    }

    private void setupTableSelectionListener() {
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });
    }

    private void populateForm(UserRow user) {
        userIdField.setText(user.getUserId());
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        roleComboBox.setValue(user.getRole());
        departmentField.setText(user.getDepartment());
        passwordField.clear();
    }

    private void clearForm() {
        userIdField.clear();
        nameField.clear();
        emailField.clear();
        roleComboBox.getSelectionModel().selectFirst();
        departmentField.clear();
        passwordField.clear();
        userTable.getSelectionModel().clearSelection();
    }

    private String validateForm(boolean isNewUser) {
        String userId = userIdField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();

        if (userId.isEmpty()) return "User ID is required.";
        if (userId.length() < 3) return "User ID must be at least 3 characters.";
        if (userId.length() > 20) return "User ID must not exceed 20 characters.";
        if (!InputValidator.isAlphanumeric(userId)) return "User ID must contain only letters and numbers.";

        if (name.isEmpty()) return "Name is required.";
        if (!InputValidator.isOnlyLettersAndSpaces(name)) {
            return "Name must contain only letters and spaces.";
        }
        if (name.length() < 2) return "Name must be at least 2 characters.";

        if (email.isEmpty()) return "Email is required.";
        if (!InputValidator.isValidEmail(email)) {
            return "Email must be a valid Roomify email (e.g., user@roomify.com).";
        }

        if (role == null || role.isEmpty()) return "Role is required.";

        return null;
    }

    // ==================== EVENT HANDLERS ====================
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().toLowerCase().trim();
        filteredUsers.setPredicate(user -> {
            if (query.isEmpty()) return true;
            return user.getUserId().toLowerCase().contains(query) ||
                    user.getName().toLowerCase().contains(query) ||
                    user.getEmail().toLowerCase().contains(query) ||
                    user.getRole().toLowerCase().contains(query) ||
                    user.getDepartment().toLowerCase().contains(query);
        });
    }

    @FXML
    private void handleClearSearch(ActionEvent event) {
        searchField.clear();
        filteredUsers.setPredicate(user -> true);
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        String error = validateForm(true);
        if (error != null) {
            AlertHelper.showError("Validation Error", error);
            return;
        }

        String userId = userIdField.getText().trim();
        boolean exists = allUsers.stream()
                .anyMatch(u -> u.getUserId().equalsIgnoreCase(userId));

        if (exists) {
            AlertHelper.showError("Duplicate ID", "User with ID '" + userId + "' already exists.");
            return;
        }

        UserRow newUser = new UserRow(
                userId,
                nameField.getText().trim(),
                emailField.getText().trim(),
                roleComboBox.getValue(),
                departmentField.getText().trim()
        );

        allUsers.add(newUser);
        clearForm();
        AlertHelper.showInformation("Success", "User added successfully.");
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        UserRow selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a user to edit.");
            return;
        }

        String error = validateForm(false);
        if (error != null) {
            AlertHelper.showError("Validation Error", error);
            return;
        }

        allUsers.remove(selected);
        UserRow updated = new UserRow(
                userIdField.getText().trim(),
                nameField.getText().trim(),
                emailField.getText().trim(),
                roleComboBox.getValue(),
                departmentField.getText().trim()
        );
        allUsers.add(updated);
        userTable.refresh();
        clearForm();
        AlertHelper.showInformation("Success", "User updated successfully.");
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        UserRow selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a user to delete.");
            return;
        }

        boolean isLastAdmin = allUsers.stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .count() <= 1 && "ADMIN".equals(selected.getRole());

        if (isLastAdmin) {
            AlertHelper.showError("Cannot Delete", "Cannot delete the last admin user.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation("Delete User",
                "Are you sure you want to delete '" + selected.getName() + "'?");

        if (confirm) {
            allUsers.remove(selected);
            clearForm();
            AlertHelper.showInformation("Success", "User deleted successfully.");
        }
    }

    @FXML
    private void handleResetPassword(ActionEvent event) {
        UserRow selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a user to reset password.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation("Reset Password",
                "Reset password for '" + selected.getName() + "'?");

        if (confirm) {
            AlertHelper.showInformation("Password Reset",
                    "Password for '" + selected.getName() + "' has been reset.\n" +
                            "Temporary password: 'temp1234'");
        }
    }

    @FXML
    private void handleClearFields(ActionEvent event) {
        clearForm();
    }

    // ==================== INNER CLASS ====================
    public static class UserRow {
        private final String userId;
        private final String name;
        private final String email;
        private final String role;
        private final String department;

        public UserRow(String userId, String name, String email, String role, String department) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.role = role;
            this.department = department;
        }

        public String getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getDepartment() { return department; }
    }
}