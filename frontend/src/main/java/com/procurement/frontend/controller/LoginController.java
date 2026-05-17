package com.procurement.frontend.controller;

import com.procurement.frontend.FrontendApplication;
import com.procurement.frontend.client.APIClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.json.JSONObject;

public class LoginController extends BaseController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final APIClient apiClient = new APIClient();

    @FXML
    public void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty()) {
            lblError.setText("Please enter your email");
            return;
        }

        lblError.setText("Connecting...");

        JSONObject credentials = new JSONObject();
        credentials.put("email", email);
        credentials.put("password", password);

        apiClient.postAsync("/api/auth/login", credentials.toString(), response -> {
            if (response.statusCode() == 200) {
                JSONObject user = new JSONObject(response.body());
                setCurrentUser(user);
                Platform.runLater(() -> {
                    FrontendApplication.setView("/fxml/shell.fxml");
                });
            } else {
                Platform.runLater(() -> {
                    lblError.setText("Invalid credentials or database connection failed");
                });
            }
        });
    }

    @FXML
    public void handleConfigureConnection() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(APIClient.getBaseUrl());
        dialog.setTitle("⚙️ Server Connection Settings");
        dialog.setHeaderText("Configure LAN Server Connection");
        dialog.setContentText("Enter Backend API Base URL:");
        
        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newUrl = result.get().trim();
            if (!newUrl.isEmpty()) {
                APIClient.setBaseUrl(newUrl);
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION,
                    "Client server link successfully saved to:\n" + newUrl
                );
                alert.show();
            }
        }
    }

    @FXML
    public void handleOfflineDemo() {
        java.util.List<String> roles = java.util.Arrays.asList(
            "Super Admin (admin@college.edu)",
            "Faculty (faculty@college.edu)",
            "Store Manager (manager@college.edu)",
            "Chief Accountant (accountant@college.edu)",
            "College Director (director@college.edu)"
        );

        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>("Super Admin (admin@college.edu)", roles);
        dialog.setTitle("🚀 Quick Offline Demo Mode");
        dialog.setHeaderText("Bypass Authentication & Preview UI Roles");
        dialog.setContentText("Select a User Role to Preview:");

        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String selected = result.get();
            JSONObject mockUser = new JSONObject();
            if (selected.contains("Super Admin")) {
                mockUser.put("id", "demo-admin");
                mockUser.put("email", "admin@college.edu");
                mockUser.put("role", "super_admin");
                mockUser.put("name", "Demo Super Admin");
            } else if (selected.contains("Faculty")) {
                mockUser.put("id", "demo-faculty");
                mockUser.put("email", "faculty@college.edu");
                mockUser.put("role", "faculty");
                mockUser.put("name", "Demo Faculty (Prof. Amit Verma)");
            } else if (selected.contains("Store Manager")) {
                mockUser.put("id", "demo-manager");
                mockUser.put("email", "manager@college.edu");
                mockUser.put("role", "store_manager");
                mockUser.put("name", "Demo Store Manager (Mr. Ramesh)");
            } else if (selected.contains("Chief Accountant")) {
                mockUser.put("id", "demo-accountant");
                mockUser.put("email", "accountant@college.edu");
                mockUser.put("role", "accountant");
                mockUser.put("name", "Demo Chief Accountant (Mrs. Priya)");
            } else if (selected.contains("College Director")) {
                mockUser.put("id", "demo-director");
                mockUser.put("email", "director@college.edu");
                mockUser.put("role", "director");
                mockUser.put("name", "Demo Director (Dr. Satish Sharma)");
            }

            setCurrentUser(mockUser);
            FrontendApplication.setView("/fxml/shell.fxml");
        }
    }
}
