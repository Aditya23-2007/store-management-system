package com.procurement.frontend.controller;

import com.procurement.frontend.client.APIClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InventoryController extends BaseController {

    @FXML private TableView<InventoryItem> tblInventory;
    @FXML private TableColumn<InventoryItem, String> colSKU;
    @FXML private TableColumn<InventoryItem, String> colName;
    @FXML private TableColumn<InventoryItem, String> colCategory;
    @FXML private TableColumn<InventoryItem, Integer> colQuantity;
    @FXML private TableColumn<InventoryItem, String> colUnit;
    @FXML private TableColumn<InventoryItem, Integer> colThreshold;
    @FXML private TableColumn<InventoryItem, String> colStatus;
    @FXML private TableColumn<InventoryItem, Void> colAction;

    @FXML private Button btnNewItem;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbCategoryFilter;
    @FXML private ComboBox<String> cmbStockFilter;

    // Form Pane
    @FXML private VBox formPane;
    @FXML private TextField txtItemName;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtSKU;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtUnit;
    @FXML private TextField txtThreshold;

    private final APIClient apiClient = new APIClient();
    private final ObservableList<InventoryItem> inventoryList = FXCollections.observableArrayList();
    private FilteredList<InventoryItem> filteredList;

    @FXML
    public void initialize() {
        // Table cell mapping
        colSKU.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colThreshold.setCellValueFactory(new PropertyValueFactory<>("lowStockThreshold"));

        setupStatusColumn();
        setupActionColumn();
        setupFilters();
        setupFormOptions();
        loadInventory();

        // Control button visibility by role
        String role = getUserRole();
        btnNewItem.setVisible(role.equals("store_manager") || role.equals("super_admin"));
    }

    private void setupFilters() {
        cmbCategoryFilter.setItems(FXCollections.observableArrayList("All Categories", "Stationery", "Electronics", "Office Supplies", "Lab Equipment", "Furniture", "Others"));
        cmbCategoryFilter.setValue("All Categories");

        cmbStockFilter.setItems(FXCollections.observableArrayList("All Levels", "Optimal", "Low Stock"));
        cmbStockFilter.setValue("All Levels");

        filteredList = new FilteredList<>(inventoryList, p -> true);
        tblInventory.setItems(filteredList);

        // Filter triggers
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cmbCategoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cmbStockFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String search = txtSearch.getText().toLowerCase().trim();
        String category = cmbCategoryFilter.getValue();
        String stockLevel = cmbStockFilter.getValue();

        filteredList.setPredicate(item -> {
            // Search field
            if (!search.isEmpty()) {
                boolean matches = item.getName().toLowerCase().contains(search)
                        || item.getSku().toLowerCase().contains(search)
                        || item.getCategory().toLowerCase().contains(search);
                if (!matches) return false;
            }

            // Category filter
            if (category != null && !category.equals("All Categories")) {
                if (!item.getCategory().equalsIgnoreCase(category)) return false;
            }

            // Stock level filter
            if (stockLevel != null && !stockLevel.equals("All Levels")) {
                boolean isLow = item.getQuantity() < item.getLowStockThreshold();
                if (stockLevel.equals("Optimal") && isLow) return false;
                if (stockLevel.equals("Low Stock") && !isLow) return false;
            }

            return true;
        });
    }

    private void setupFormOptions() {
        cmbCategory.setItems(FXCollections.observableArrayList("Stationery", "Electronics", "Office Supplies", "Lab Equipment", "Furniture", "Others"));
    }

    private void loadInventory() {
        apiClient.getAsync("/api/inventory", response -> {
            if (response.statusCode() == 200) {
                JSONArray arr = new JSONArray(response.body());
                List<InventoryItem> items = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    items.add(new InventoryItem(
                            obj.getString("id"),
                            obj.getString("name"),
                            obj.getString("category"),
                            obj.getInt("quantity"),
                            obj.getString("unit"),
                            obj.getInt("lowStockThreshold"),
                            obj.getString("sku")
                    ));
                }
                Platform.runLater(() -> {
                    inventoryList.setAll(items);
                    applyFilters();
                });
            }
        });
    }

    private void setupStatusColumn() {
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    InventoryItem inv = getTableRow().getItem();
                    boolean isLow = inv.getQuantity() < inv.getLowStockThreshold();
                    
                    Label badge = new Label(isLow ? "Low Stock" : "Optimal");
                    badge.getStyleClass().setAll("card-badge", isLow ? "badge-red" : "badge-green");
                    setGraphic(badge);
                }
            }
        });
    }

    private void setupActionColumn() {
        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button("Reconcile");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    InventoryItem inv = getTableRow().getItem();
                    String userRole = getUserRole().toLowerCase();

                    // Only Store Manager or Super Admin can edit inventory stock directly
                    if (userRole.equals("store_manager") || userRole.equals("super_admin")) {
                        btn.getStyleClass().setAll("btn-primary");
                        btn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                        btn.setOnAction(e -> handleReconcileStock(inv));
                        setGraphic(btn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void handleReconcileStock(InventoryItem item) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getQuantity()));
        dialog.setTitle("Stock Reconciliation");
        dialog.setHeaderText("Adjust current stock level for: " + item.getName());
        dialog.setContentText("Physical Stock Count:");

        dialog.showAndWait().ifPresent(qtyStr -> {
            try {
                int quantity = Integer.parseInt(qtyStr);
                apiClient.patchAsync("/api/inventory/" + item.getId() + "/stock?quantity=" + quantity, "", response -> {
                    if (response.statusCode() == 200) {
                        loadInventory();
                    }
                });
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Quantity must be an integer!");
                alert.show();
            }
        });
    }

    // FXML Handlers
    @FXML
    public void handleNewItem() {
        formPane.setVisible(true);
        formPane.setManaged(true);
        clearForm();

        // High-end smooth scale-in and fade-in transition
        formPane.setOpacity(0.0);
        formPane.setScaleX(0.95);
        formPane.setScaleY(0.95);

        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(250), formPane);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(250), formPane);
        scale.setFromX(0.95);
        scale.setFromY(0.95);
        scale.setToX(1.0);
        scale.setToY(1.0);

        javafx.animation.ParallelTransition parallel = new javafx.animation.ParallelTransition(fade, scale);
        parallel.play();
    }

    @FXML
    public void handleCancelForm() {
        formPane.setVisible(false);
        formPane.setManaged(false);
    }

    @FXML
    public void handleSubmitForm() {
        String name = txtItemName.getText().trim();
        String category = cmbCategory.getValue();
        String sku = txtSKU.getText().trim();
        String qtyStr = txtQuantity.getText().trim();
        String unit = txtUnit.getText().trim();
        String thresholdStr = txtThreshold.getText().trim();

        if (name.isEmpty() || category == null || sku.isEmpty() || qtyStr.isEmpty() || unit.isEmpty() || thresholdStr.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "All form fields are required!");
            alert.show();
            return;
        }

        try {
            int quantity = Integer.parseInt(qtyStr);
            int threshold = Integer.parseInt(thresholdStr);

            JSONObject item = new JSONObject();
            item.put("name", name);
            item.put("category", category);
            item.put("sku", sku);
            item.put("quantity", quantity);
            item.put("unit", unit);
            item.put("lowStockThreshold", threshold);

            apiClient.postAsync("/api/inventory", item.toString(), response -> {
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    Platform.runLater(() -> {
                        formPane.setVisible(false);
                        formPane.setManaged(false);
                        loadInventory();
                    });
                }
            });
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Quantity and Threshold must be integers!");
            alert.show();
        }
    }

    private void clearForm() {
        txtItemName.clear();
        cmbCategory.setValue(null);
        txtSKU.clear();
        txtQuantity.clear();
        txtUnit.clear();
        txtThreshold.clear();
    }

    // Table Model inner class
    public static class InventoryItem {
        private final String id;
        private final String name;
        private final String category;
        private final int quantity;
        private final String unit;
        private final int lowStockThreshold;
        private final String sku;

        public InventoryItem(String id, String name, String category, int quantity, String unit, int lowStockThreshold, String sku) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.unit = unit;
            this.lowStockThreshold = lowStockThreshold;
            this.sku = sku;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
        public String getUnit() { return unit; }
        public int getLowStockThreshold() { return lowStockThreshold; }
        public String getSku() { return sku; }
    }
}
