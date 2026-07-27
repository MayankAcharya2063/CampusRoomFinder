package com.example.roomify.controller;

import com.example.roomify.model.Admin;
import com.example.roomify.model.Staff;
import com.example.roomify.model.Student;
import com.example.roomify.model.User;
import com.example.roomify.service.AuthenticationService;
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
    @FXML private PasswordField passwordField;

    // ==================== SERVICE REFERENCES ====================
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final AlertFactory alertFactory = AlertFactory.getInstance();
    private final AuthenticationService authService = AuthenticationService.getInstance();

    // ==================== DATA ====================
    private ObservableList<UserRow> allUsers = FXCollections.observableArrayList();
    private FilteredList<UserRow> filteredUsers;
    private User currentUser;
    private UserRow lastSelectedUser = null;

    // ==================== INITIALIZATION ====================
    public void initContext(User user) {
        this.currentUser = user;
        setupComboBoxes();
        setupTableColumns();
        loadUsers();
        setupTableSelectionListener();
    }

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
        allUsers.clear();

        // Load from AuthenticationService
        var userMap = authService.getAllUsers();
        for (User user : userMap.values()) {
            String department = "";
            if (user instanceof Student) {
                department = ((Student) user).getDepartment();
            } else if (user instanceof Staff) {
                department = ((Staff) user).getDepartment();
            } else if (user instanceof Admin) {
                department = "Administration";
            }

            allUsers.add(new UserRow(
                    user.getUserId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole().name(),
                    department
            ));
        }

        filteredUsers = new FilteredList<>(allUsers, p -> true);
        userTable.setItems(filteredUsers);
    }

    private void setupTableSelectionListener() {
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lastSelectedUser = newVal;
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
        passwordField.setPromptText("Enter new password to change");
    }

    private void clearForm() {
        userIdField.clear();
        nameField.clear();
        emailField.clear();
        roleComboBox.getSelectionModel().selectFirst();
        departmentField.clear();
        passwordField.clear();
        passwordField.setPromptText("Password (Minimum 6 characters)");
        userTable.getSelectionModel().clearSelection();
        lastSelectedUser = null;
    }

    private String validateForm(boolean isNewUser) {
        String userId = userIdField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();
        String password = passwordField.getText();

        // Validate User ID
        if (userId.isEmpty()) return "User ID is required.";
        if (userId.length() < 3) return "User ID must be at least 3 characters.";
        if (userId.length() > 20) return "User ID must not exceed 20 characters.";
        if (!InputValidator.isAlphanumeric(userId)) return "User ID must contain only letters and numbers.";

        // Validate Name
        if (name.isEmpty()) return "Name is required.";
        if (!InputValidator.isOnlyLettersAndSpaces(name)) {
            return "Name must contain only letters and spaces.";
        }
        if (name.length() < 2) return "Name must be at least 2 characters.";

        // Validate Email
        if (email.isEmpty()) return "Email is required.";
        if (!InputValidator.isValidEmail(email)) {
            return "Email must be a valid Roomify email (e.g., user@roomify.com).";
        }

        // Validate Role
        if (role == null || role.isEmpty()) return "Role is required.";

        // Validate Password (required for new users)
        if (isNewUser) {
            if (password.isEmpty()) return "Password is required.";
            if (!InputValidator.isValidPassword(password)) {
                return "Password must be at least 6 characters, containing one letter and one number.";
            }
        } else if (!password.isEmpty()) {
            if (!InputValidator.isValidPassword(password)) {
                return "Password must be at least 6 characters, containing one letter and one number.";
            }
        }

        return null;
    }

    private boolean isDuplicateEmail(String email, String currentEmail) {
        for (UserRow user : allUsers) {
            if (user.getEmail().equalsIgnoreCase(email) &&
                    !user.getEmail().equalsIgnoreCase(currentEmail)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDuplicateUserId(String userId, String currentUserId) {
        for (UserRow user : allUsers) {
            if (user.getUserId().equalsIgnoreCase(userId) &&
                    !user.getUserId().equalsIgnoreCase(currentUserId)) {
                return true;
            }
        }
        return false;
    }

    private void refreshTable() {
        filteredUsers = new FilteredList<>(allUsers, p -> true);
        userTable.setItems(filteredUsers);
        userTable.refresh();
        userTable.getSelectionModel().clearSelection();
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
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();
        String department = departmentField.getText().trim();
        String password = passwordField.getText();

        // Check duplicate ID
        if (isDuplicateUserId(userId, "")) {
            AlertHelper.showError("Duplicate ID", "User ID '" + userId + "' already exists.");
            return;
        }

        // Check duplicate email
        if (isDuplicateEmail(email, "")) {
            AlertHelper.showError("Duplicate Email", "Email '" + email + "' already registered.");
            return;
        }

        User newUser;
        switch (role) {
            case "STUDENT":
                String studentId = "ST" + userId;
                newUser = new Student(userId, name, email, password, studentId, department);
                break;
            case "STAFF":
                String staffId = "SF" + userId;
                newUser = new Staff(userId, name, email, password, staffId, department);
                break;
            case "ADMIN":
                newUser = new Admin(userId, name, email, password, 1);
                break;
            default:
                AlertHelper.showError("Invalid Role", "Please select a valid role.");
                return;
        }

        if (authService.addUser(newUser)) {
            loadUsers();
            clearForm();
            refreshTable();
            AlertHelper.showInformation("Success", "User added successfully.");
            com.example.roomify.persistence.SystemLogger.logAdminAction("User Created: " + email);
        } else {
            AlertHelper.showError("Error", "Failed to add user.");
        }
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

        String userId = userIdField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();
        String department = departmentField.getText().trim();
        String password = passwordField.getText();

        // Check duplicate ID (excluding current user)
        if (isDuplicateUserId(userId, selected.getUserId())) {
            AlertHelper.showError("Duplicate ID", "User ID '" + userId + "' already exists.");
            return;
        }

        // Check duplicate email (excluding current user)
        if (isDuplicateEmail(email, selected.getEmail())) {
            AlertHelper.showError("Duplicate Email", "Email '" + email + "' already registered.");
            return;
        }

        // Get existing user by email
        User existingUser = authService.getAllUsers().get(selected.getEmail().toLowerCase());
        if (existingUser == null) {
            AlertHelper.showError("Error", "User not found in database.");
            return;
        }

        // Store old email for cleanup
        String oldEmail = existingUser.getEmail().toLowerCase();

        // Update user fields
        existingUser.setUserId(userId);
        existingUser.setName(name);
        existingUser.setEmail(email);
        if (!password.isEmpty()) {
            existingUser.setPassword(password);
        }

        // Update role-specific fields
        if (existingUser instanceof Student && "STUDENT".equals(role)) {
            ((Student) existingUser).setDepartment(department);
        } else if (existingUser instanceof Staff && "STAFF".equals(role)) {
            ((Staff) existingUser).setDepartment(department);
        } else if (existingUser instanceof Admin && "ADMIN".equals(role)) {
            // Admin doesn't have department, but if role changed to admin, we keep admin
        }

        // If role changed, we may need to recreate the user with proper type
        boolean roleChanged = !existingUser.getRole().name().equals(role);
        User updatedUser = existingUser;

        if (roleChanged) {
            // Remove old user
            authService.deleteUser(oldEmail);
            // Create new user with correct role
            if ("STUDENT".equals(role)) {
                updatedUser = new Student(userId, name, email,
                        password.isEmpty() ? existingUser.getPassword() : password,
                        "ST" + userId, department);
            } else if ("STAFF".equals(role)) {
                updatedUser = new Staff(userId, name, email,
                        password.isEmpty() ? existingUser.getPassword() : password,
                        "SF" + userId, department);
            } else if ("ADMIN".equals(role)) {
                updatedUser = new Admin(userId, name, email,
                        password.isEmpty() ? existingUser.getPassword() : password, 1);
            }
            authService.addUser(updatedUser);
        } else {
            // If email changed, remove old entry and add new one
            if (!oldEmail.equals(email.toLowerCase())) {
                authService.deleteUser(oldEmail);
            }
            authService.updateUser(existingUser);
        }

        loadUsers();
        clearForm();
        refreshTable();
        AlertHelper.showInformation("Success", "User updated successfully.");
        com.example.roomify.persistence.SystemLogger.logAdminAction("User Updated: " + email);
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        UserRow selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No Selection", "Please select a user to delete.");
            return;
        }

        // Prevent deleting the last admin
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
            if (authService.deleteUser(selected.getEmail())) {
                loadUsers();
                clearForm();
                refreshTable();
                AlertHelper.showInformation("Success", "User deleted successfully.");
                com.example.roomify.persistence.SystemLogger.logAdminAction("User Deleted: " + selected.getEmail());
            } else {
                AlertHelper.showError("Error", "Failed to delete user.");
            }
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
            User existingUser = authService.getAllUsers().get(selected.getEmail().toLowerCase());
            if (existingUser != null) {
                String tempPassword = "temp1234";
                existingUser.setPassword(tempPassword);
                if (authService.updateUser(existingUser)) {
                    AlertHelper.showInformation("Password Reset",
                            "Password for '" + selected.getName() + "' has been reset.\n" +
                                    "Temporary password: '" + tempPassword + "'");
                    com.example.roomify.persistence.SystemLogger.logAdminAction("Password Reset: " + selected.getEmail());
                } else {
                    AlertHelper.showError("Error", "Failed to reset password.");
                }
            }
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