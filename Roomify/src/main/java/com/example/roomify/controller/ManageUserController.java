package com.example.roomify.controller;

import com.example.roomify.StageCoordinator;
import com.example.roomify.UserRole;
import com.example.roomify.model.User;
import com.example.roomify.service.SessionManager;
import com.example.roomify.util.AlertFactory;
import com.example.roomify.validation.InputValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Manage User View.
 * Allows admin to create, read, update, and delete users.
 */
public class ManageUserController implements Initializable {

    // ==================== FXML INJECTIONS ====================

    // Header
    @FXML private Label loggedInUserLabel;
    @FXML private Button logoutButton;
    @FXML private Button logoutSidebarBtn;

    // Navigation
    @FXML private Button dashboardBtn;
    @FXML private Button resourcesBtn;
    @FXML private Button usersBtn;
    @FXML private Button approvalsBtn;

    // Search
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearButton;

    // Table
    @FXML private TableView<UserRow> userTable;
    @FXML private TableColumn<UserRow, String> userIdColumn;
    @FXML private TableColumn<UserRow, String> nameColumn;
    @FXML private TableColumn<UserRow, String> emailColumn;
    @FXML private TableColumn<UserRow, String> roleColumn;
    @FXML private TableColumn<UserRow, String> departmentColumn;

    // Form fields
    @FXML private TextField userIdField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField departmentField;

    // Buttons
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button resetPasswordButton;
    @FXML private Button clearFieldsButton;

    // Status
    @FXML private Label statusLabel;
    @FXML private Label totalUsersLabel;

    // ==================== SERVICE REFERENCES ====================

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final AlertFactory alertFactory = AlertFactory.getInstance();

    // ==================== DATA ====================

    private ObservableList<UserRow> allUsers = FXCollections.observableArrayList();
    private FilteredList<UserRow> filteredUsers;

    /**
     * UserRow class for table display.
     */
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

    // ==================== INITIALIZATION ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupTableColumns();
        loadUsers();
        setupTableSelectionListener();
        updateUserInfo();
        updateStatus("Ready");
        updateTotalUsersLabel();
    }

    /**
     * Sets up ComboBoxes with options.
     */
    private void setupComboBoxes() {
        roleComboBox.setItems(FXCollections.observableArrayList("STUDENT", "STAFF", "ADMIN"));
        roleComboBox.getSelectionModel().selectFirst();
    }

    /**
     * Sets up table columns.
     */
    private void setupTableColumns() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));

        // Color-code role column
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
                    default:
                        setStyle("");
                }
            }
        });
    }

    /**
     * Loads users from data source.
     */
    private void loadUsers() {
        List<UserRow> userList = generatePlaceholderUsers();
        allUsers.setAll(userList);

        filteredUsers = new FilteredList<>(allUsers, p -> true);
        userTable.setItems(filteredUsers);

        updateTotalUsersLabel();
    }

    /**
     * Generates placeholder users.
     */
    private List<UserRow> generatePlaceholderUsers() {
        List<UserRow> users = new ArrayList<>();
        users.add(new UserRow("U001", "Admin User", "admin@roomify.com", "ADMIN", "Administration"));
        users.add(new UserRow("U002", "John Staff", "john.staff@roomify.com", "STAFF", "Computer Science"));
        users.add(new UserRow("U003", "Jane Student", "jane.student@roomify.com", "STUDENT", "Engineering"));
        users.add(new UserRow("U004", "Michael Lee", "michael.lee@roomify.com", "STAFF", "Mathematics"));
        users.add(new UserRow("U005", "Sarah Chen", "sarah.chen@roomify.com", "STUDENT", "Business"));
        users.add(new UserRow("U006", "David Kumar", "david.kumar@roomify.com", "STUDENT", "Computer Science"));
        users.add(new UserRow("U007", "Emma Wilson", "emma.wilson@roomify.com", "STAFF", "Physics"));
        users.add(new UserRow("U008", "Robert Tan", "robert.tan@roomify.com", "STUDENT", "Engineering"));
        return users;
    }

    /**
     * Sets up table selection listener to populate form.
     */
    private void setupTableSelectionListener() {
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });
    }

    /**
     * Populates form with selected user data.
     */
    private void populateForm(UserRow user) {
        userIdField.setText(user.getUserId());
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        roleComboBox.setValue(user.getRole());
        departmentField.setText(user.getDepartment());
    }

    /**
     * Updates user info label.
     */
    private void updateUserInfo() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            loggedInUserLabel.setText("Welcome, " + currentUser.getName() + " (ADMIN)");
        } else {
            loggedInUserLabel.setText("Welcome, Admin");
        }
    }

    /**
     * Updates status bar message.
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Updates total users label.
     */
    private void updateTotalUsersLabel() {
        int total = filteredUsers.size();
        totalUsersLabel.setText("Total: " + total + " users");
    }

    /**
     * Applies search filter.
     */
    private void applySearch() {
        String searchText = searchField.getText().toLowerCase().trim();

        filteredUsers.setPredicate(user -> {
            if (searchText.isEmpty()) return true;
            return user.getUserId().toLowerCase().contains(searchText) ||
                    user.getName().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText) ||
                    user.getRole().toLowerCase().contains(searchText) ||
                    user.getDepartment().toLowerCase().contains(searchText);
        });

        updateTotalUsersLabel();
        updateStatus("Showing " + filteredUsers.size() + " users");
    }

    /**
     * Clears the form fields.
     */
    private void clearForm() {
        userIdField.clear();
        nameField.clear();
        emailField.clear();
        roleComboBox.getSelectionModel().selectFirst();
        departmentField.clear();
        userTable.getSelectionModel().clearSelection();
    }

    /**
     * Validates the form inputs.
     */
    private String validateForm(boolean isNewUser) {
        String userId = userIdField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();
        String department = departmentField.getText().trim();

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

        // Department is optional for students, but recommended
        if (department.isEmpty() && !"STUDENT".equals(role)) {
            return "Department is recommended for Staff and Admin.";
        }

        return null;
    }

    // ==================== CONTEXT INITIALIZATION ====================

    /**
     * Initializes controller with user context.
     */
    public void initContext(User user) {
        if (user != null) {
            sessionManager.login(user);
            updateUserInfo();
        }
        loadUsers();
        updateStatus("Manage Users ready");
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Handles search action.
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        applySearch();
    }

    /**
     * Handles clear search action.
     */
    @FXML
    private void handleClear(ActionEvent event) {
        searchField.clear();
        applySearch();
    }

    /**
     * Handles add user action.
     */
    @FXML
    private void handleAdd(ActionEvent event) {
        String validationError = validateForm(true);
        if (validationError != null) {
            alertFactory.showErrorAlert("Validation Error", validationError);
            return;
        }

        // Check for duplicate ID
        String userId = userIdField.getText().trim();
        boolean duplicate = allUsers.stream()
                .anyMatch(u -> u.getUserId().equalsIgnoreCase(userId));

        if (duplicate) {
            alertFactory.showErrorAlert("Duplicate ID",
                    "A user with ID '" + userId + "' already exists.");
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
        updateTotalUsersLabel();
        clearForm();
        updateStatus("User added: " + newUser.getName());
        alertFactory.showSuccessAlert("User Added",
                "User '" + newUser.getName() + "' has been added successfully.\n" +
                        "Default password: 'password123' (Please change on first login)");
    }

    /**
     * Handles edit user action.
     */
    @FXML
    private void handleEdit(ActionEvent event) {
        UserRow selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertFactory.showWarningAlert("No Selection", "Please select a user to edit.");
            return;
        }

        String validationError = validateForm(false);
        if (validationError != null) {
            alertFactory.showErrorAlert("Validation Error", validationError);
            return;
        }

        // Remove old and add updated
        allUsers.remove(selected);
        UserRow updatedUser = new UserRow(
                userIdField.getText().trim(),
                nameField.getText().trim(),
                emailField.getText().trim(),
                roleComboBox.getValue(),
                departmentField.getText().trim()
        );
        allUsers.add(updatedUser);

        userTable.refresh();
        updateStatus("User updated: " + updatedUser.getName());
        alertFactory.showSuccessAlert("User Updated",
                "User '" + updatedUser.getName() + "' has been updated successfully.");
    }

    /**
     * Handles delete user action.
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        UserRow selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertFactory.showWarningAlert("No Selection", "Please select a user to delete.");
            return;
        }

        // Prevent deleting the last admin
        boolean isLastAdmin = allUsers.stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .count() <= 1 && "ADMIN".equals(selected.getRole());

        if (isLastAdmin) {
            alertFactory.showErrorAlert("Cannot Delete",
                    "Cannot delete the last admin user. Promote another user to admin first.");
            return;
        }

        boolean confirm = alertFactory.showConfirmationDialog("Delete User",
                "Are you sure you want to delete user '" + selected.getName() + "'?\n" +
                        "This action cannot be undone.");

        if (confirm) {
            allUsers.remove(selected);
            updateTotalUsersLabel();
            clearForm();
            updateStatus("User deleted: " + selected.getName());
            alertFactory.showSuccessAlert("User Deleted",
                    "User '" + selected.getName() + "' has been deleted.");
        }
    }

    /**
     * Handles reset password action.
     */
    @FXML
    private void handleResetPassword(ActionEvent event) {
        UserRow selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertFactory.showWarningAlert("No Selection", "Please select a user to reset password.");
            return;
        }

        boolean confirm = alertFactory.showConfirmationDialog("Reset Password",
                "Are you sure you want to reset the password for '" + selected.getName() + "'?");

        if (confirm) {
            // In production, generate a new password and save to file
            updateStatus("Password reset for: " + selected.getName());
            alertFactory.showInfoAlert("Password Reset",
                    "Password for '" + selected.getName() + "' has been reset.\n" +
                            "New temporary password: 'temp1234'");
        }
    }

    /**
     * Handles clear fields action.
     */
    @FXML
    private void handleClearFields(ActionEvent event) {
        clearForm();
        updateStatus("Form cleared");
    }

    /**
     * Handles dashboard navigation.
     */
    @FXML
    private void handleDashboard(ActionEvent event) {
        Stage currentStage = (Stage) dashboardBtn.getScene().getWindow();
        User currentUser = sessionManager.getCurrentUser();
        StageCoordinator.getInstance().showAdminDashboard(currentUser, currentStage);
    }

    /**
     * Handles resources navigation.
     */
    @FXML
    private void handleResources(ActionEvent event) {
        Stage currentStage = (Stage) resourcesBtn.getScene().getWindow();
        User currentUser = sessionManager.getCurrentUser();
        StageCoordinator.getInstance().showResourceList(currentUser, currentStage);
    }

    /**
     * Handles users navigation (current view).
     */
    @FXML
    private void handleUsers(ActionEvent event) {
        refreshView();
    }

    /**
     * Handles approvals navigation.
     */
    @FXML
    private void handleApprovals(ActionEvent event) {
        alertFactory.showInfoAlert("Approvals", "Booking approvals will be displayed here.");
    }

    /**
     * Handles logout action.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        boolean confirm = alertFactory.showConfirmationDialog("Logout", "Are you sure you want to logout?");
        if (confirm) {
            sessionManager.logout();
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            StageCoordinator.getInstance().showLogin(currentStage);
        }
    }

    /**
     * Refreshes the view.
     */
    private void refreshView() {
        loadUsers();
        clearForm();
        updateStatus("View refreshed");
    }
}