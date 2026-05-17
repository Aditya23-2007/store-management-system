package com.procurement.frontend.controller;

import com.procurement.frontend.client.APIClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

public class SettingsController extends BaseController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtRole;

    @FXML private TextField txtAPIEndpoint;
    @FXML private ComboBox<String> cmbSyncInterval;
    @FXML private CheckBox chkNotifications;

    // Super Admin User Access Controls
    @FXML private VBox adminUserCard;
    @FXML private TableView<UserItem> tblUsers;
    @FXML private TableColumn<UserItem, String> colUserName;
    @FXML private TableColumn<UserItem, String> colUserEmail;
    @FXML private TableColumn<UserItem, String> colUserRole;
    @FXML private TableColumn<UserItem, String> colUserDept;

    @FXML private TextField txtNewName;
    @FXML private TextField txtNewEmail;
    @FXML private ComboBox<String> cmbNewRole;
    @FXML private TextField txtNewDept;

    private final APIClient apiClient = new APIClient();
    private final ObservableList<UserItem> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Load Current Profile
        txtFullName.setText(getUserName());
        txtEmail.setText(getUserEmail());
        
        String role = getUserRole();
        txtRole.setText(role.replace("_", " ").toUpperCase());

        // Setup General Options
        cmbSyncInterval.setItems(FXCollections.observableArrayList("5 seconds", "15 seconds", "30 seconds", "60 seconds"));
        cmbSyncInterval.setValue("15 seconds");

        // Load dynamic API URL from config properties file
        txtAPIEndpoint.setText(APIClient.getBaseUrl());

        // Role-Based User Access Panel Control
        boolean isAdmin = role.equalsIgnoreCase("super_admin");
        adminUserCard.setVisible(isAdmin);
        adminUserCard.setManaged(isAdmin);

        if (isAdmin) {
            setupUserTable();
            loadUsers();
        }
    }

    private void setupUserTable() {
        colUserName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserDept.setCellValueFactory(new PropertyValueFactory<>("departmentId"));

        cmbNewRole.setItems(FXCollections.observableArrayList("faculty", "director", "accountant", "store_manager", "super_admin"));

        tblUsers.setItems(userList);

        // Row Selection Listener
        tblUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtNewName.setText(newSel.getName());
                txtNewEmail.setText(newSel.getEmail());
                cmbNewRole.setValue(newSel.getRole());
                txtNewDept.setText(newSel.getDepartmentId());
            }
        });
    }

    private void loadUsers() {
        apiClient.getAsync("/api/auth/users", response -> {
            if (response.statusCode() == 200) {
                JSONArray arr = new JSONArray(response.body());
                java.util.List<UserItem> items = new java.util.ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    items.add(new UserItem(
                        obj.getString("id"),
                        obj.getString("name"),
                        obj.getString("email"),
                        obj.getString("role"),
                        obj.optString("departmentId", "")
                    ));
                }
                Platform.runLater(() -> {
                    userList.setAll(items);
                });
            }
        });
    }

    @FXML
    public void handleSaveUser() {
        String name = txtNewName.getText().trim();
        String email = txtNewEmail.getText().trim();
        String role = cmbNewRole.getValue();
        String dept = txtNewDept.getText().trim();

        if (name.isEmpty() || email.isEmpty() || role == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Name, Email, and Role are required fields!");
            alert.show();
            return;
        }

        UserItem selected = tblUsers.getSelectionModel().getSelectedItem();
        String userId = (selected != null) ? selected.getId() : "";

        JSONObject payload = new JSONObject();
        payload.put("id", userId);
        payload.put("name", name);
        payload.put("email", email);
        payload.put("role", role);
        payload.put("departmentId", dept);

        apiClient.postAsync("/api/auth/users", payload.toString(), response -> {
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "User profile and role permissions successfully saved!");
                    alert.show();
                    handleClearUserForm();
                    loadUsers();
                });
            }
        });
    }

    @FXML
    public void handleDeleteUser() {
        UserItem selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a user from the table to delete!");
            alert.show();
            return;
        }

        if (selected.getEmail().equalsIgnoreCase(getUserEmail())) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You cannot delete your own active administrator profile!");
            alert.show();
            return;
        }

        apiClient.deleteAsync("/api/auth/users/" + selected.getId(), response -> {
            if (response.statusCode() == 200) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "User successfully deleted!");
                    alert.show();
                    handleClearUserForm();
                    loadUsers();
                });
            }
        });
    }

    @FXML
    public void handleClearUserForm() {
        txtNewName.clear();
        txtNewEmail.clear();
        cmbNewRole.setValue(null);
        txtNewDept.clear();
        tblUsers.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleSaveSettings() {
        String endpoint = txtAPIEndpoint.getText().trim();
        if (endpoint.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "API Endpoint cannot be empty!");
            alert.show();
            return;
        }

        APIClient.setBaseUrl(endpoint);

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "System preferences and REST connection links successfully saved!");
        alert.show();
    }

    // Model class for User items
    public static class UserItem {
        private final String id;
        private final String name;
        private final String email;
        private final String role;
        private final String departmentId;

        public UserItem(String id, String name, String email, String role, String departmentId) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
            this.departmentId = departmentId;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getDepartmentId() { return departmentId; }
    }
}
