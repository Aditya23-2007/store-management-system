package com.procurement.frontend.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ShellController extends BaseController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUsername;
    @FXML private Label lblUserRole;
    @FXML private Button btnDashboard;
    @FXML private Button btnRequests;
    @FXML private Button btnInventory;
    @FXML private Button btnVendors;
    @FXML private Button btnFinance;
    @FXML private Button btnSettings;
    @FXML private Button btnSignOut;

    @FXML
    public void initialize() {
        lblUsername.setText(getUserName());
        
        // Format role name nicely
        String role = getUserRole();
        String formattedRole = role.replace("_", " ").toUpperCase();
        lblUserRole.setText(formattedRole);
        
        // Dynamic Role-Based Sidebar Navigation Filtering
        String normRole = role.toLowerCase();
        
        // Hide all conditional tabs by default
        btnRequests.setVisible(false);
        btnRequests.setManaged(false);
        btnInventory.setVisible(false);
        btnInventory.setManaged(false);
        btnVendors.setVisible(false);
        btnVendors.setManaged(false);
        btnFinance.setVisible(false);
        btnFinance.setManaged(false);
        btnSettings.setVisible(false);
        btnSettings.setManaged(false);

        if (normRole.equals("super_admin")) {
            btnRequests.setVisible(true);
            btnRequests.setManaged(true);
            btnInventory.setVisible(true);
            btnInventory.setManaged(true);
            btnVendors.setVisible(true);
            btnVendors.setManaged(true);
            btnFinance.setVisible(true);
            btnFinance.setManaged(true);
            btnSettings.setVisible(true);
            btnSettings.setManaged(true);
        } else if (normRole.equals("faculty")) {
            btnRequests.setVisible(true);
            btnRequests.setManaged(true);
        } else if (normRole.equals("store_manager")) {
            btnRequests.setVisible(true);
            btnRequests.setManaged(true);
            btnInventory.setVisible(true);
            btnInventory.setManaged(true);
            btnVendors.setVisible(true);
            btnVendors.setManaged(true);
        } else if (normRole.equals("accountant")) {
            btnRequests.setVisible(true);
            btnRequests.setManaged(true);
            btnVendors.setVisible(true);
            btnVendors.setManaged(true);
            btnFinance.setVisible(true);
            btnFinance.setManaged(true);
        } else if (normRole.equals("director")) {
            btnRequests.setVisible(true);
            btnRequests.setManaged(true);
            btnFinance.setVisible(true);
            btnFinance.setManaged(true);
        }

        // Load default view
        showDashboard();
    }

    private void setActiveButton(Button activeBtn) {
        btnDashboard.getStyleClass().remove("active-nav-btn");
        btnRequests.getStyleClass().remove("active-nav-btn");
        btnInventory.getStyleClass().remove("active-nav-btn");
        btnVendors.getStyleClass().remove("active-nav-btn");
        btnFinance.getStyleClass().remove("active-nav-btn");
        btnSettings.getStyleClass().remove("active-nav-btn");
        if (btnSignOut != null) {
            btnSignOut.getStyleClass().remove("active-nav-btn");
        }
        
        if (activeBtn != null && !activeBtn.getStyleClass().contains("active-nav-btn")) {
            activeBtn.getStyleClass().add("active-nav-btn");
        }
    }

    @FXML
    public void showDashboard() {
        setActiveButton(btnDashboard);
        loadSubView("/fxml/dashboard.fxml");
    }

    @FXML
    public void showRequests() {
        setActiveButton(btnRequests);
        loadSubView("/fxml/requests.fxml");
    }

    @FXML
    public void showInventory() {
        setActiveButton(btnInventory);
        loadSubView("/fxml/inventory.fxml");
    }

    @FXML
    public void showVendors() {
        setActiveButton(btnVendors);
        loadSubView("/fxml/vendors.fxml");
    }

    @FXML
    public void showFinance() {
        setActiveButton(btnFinance);
        loadSubView("/fxml/finance.fxml");
    }

    @FXML
    public void showSettings() {
        setActiveButton(btnSettings);
        loadSubView("/fxml/settings.fxml");
    }

    @FXML
    public void handleSignOut() {
        BaseController.setCurrentUser(null);
        com.procurement.frontend.FrontendApplication.setView("/fxml/login.fxml");
    }

    private void loadSubView(String fxmlPath) {
        try {
            contentArea.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent subView = loader.load();
            
            subView.setOpacity(0.0);
            contentArea.getChildren().add(subView);
            
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(350), subView);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
