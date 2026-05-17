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

public class RequestsController extends BaseController {

    @FXML private TableView<RequestItem> tblRequests;
    @FXML private TableColumn<RequestItem, String> colItemName;
    @FXML private TableColumn<RequestItem, String> colCategory;
    @FXML private TableColumn<RequestItem, String> colRequester;
    @FXML private TableColumn<RequestItem, Integer> colQuantity;
    @FXML private TableColumn<RequestItem, Double> colEstCost;
    @FXML private TableColumn<RequestItem, String> colUrgency;
    @FXML private TableColumn<RequestItem, String> colStatus;
    @FXML private TableColumn<RequestItem, Void> colAction;

    @FXML private Button btnNewRequest;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbStatusFilter;
    @FXML private ComboBox<String> cmbUrgencyFilter;

    // Form Pane
    @FXML private VBox formPane;
    @FXML private Label lblFormTitle;
    @FXML private TextField txtItemName;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtEstCost;
    @FXML private ComboBox<String> cmbUrgency;
    @FXML private TextArea txtPurpose;

    private final APIClient apiClient = new APIClient();
    private final ObservableList<RequestItem> requestList = FXCollections.observableArrayList();
    private FilteredList<RequestItem> filteredList;

    @FXML
    public void initialize() {
        // Table cell factories
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colRequester.setCellValueFactory(new PropertyValueFactory<>("requesterName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colEstCost.setCellValueFactory(new PropertyValueFactory<>("estimatedCost"));
        colUrgency.setCellValueFactory(new PropertyValueFactory<>("urgency"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupActionColumn();
        setupFilters();
        setupFormOptions();
        loadRequests();

        // Control visibility of "New Request" based on roles (usually Faculty, HOD, Super Admin)
        String role = getUserRole();
        btnNewRequest.setVisible(role.equals("faculty") || role.equals("super_admin"));
    }

    private void setupFilters() {
        cmbStatusFilter.setItems(FXCollections.observableArrayList("All Statuses", "Pending", "Director Approved", "Accountant Approved", "Purchased", "Bill Verified", "Delivered"));
        cmbStatusFilter.setValue("All Statuses");

        cmbUrgencyFilter.setItems(FXCollections.observableArrayList("All Urgencies", "Low", "Medium", "High", "Critical"));
        cmbUrgencyFilter.setValue("All Urgencies");

        filteredList = new FilteredList<>(requestList, p -> true);
        tblRequests.setItems(filteredList);

        // Filter listeners
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cmbStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cmbUrgencyFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String search = txtSearch.getText().toLowerCase().trim();
        String status = cmbStatusFilter.getValue();
        String urgency = cmbUrgencyFilter.getValue();

        filteredList.setPredicate(item -> {
            // Search
            if (!search.isEmpty()) {
                boolean matchesSearch = item.getItemName().toLowerCase().contains(search)
                        || item.getRequesterName().toLowerCase().contains(search)
                        || item.getCategory().toLowerCase().contains(search);
                if (!matchesSearch) return false;
            }

            // Status filter
            if (status != null && !status.equals("All Statuses")) {
                String normalizedStatus = status.toLowerCase().replace(" ", "_");
                if (!item.getStatus().toLowerCase().equals(normalizedStatus)) return false;
            }

            // Urgency filter
            if (urgency != null && !urgency.equals("All Urgencies")) {
                if (!item.getUrgency().toLowerCase().equals(urgency.toLowerCase())) return false;
            }

            return true;
        });
    }

    private void setupFormOptions() {
        cmbCategory.setItems(FXCollections.observableArrayList("Stationery", "Electronics", "Office Supplies", "Lab Equipment", "Furniture", "Others"));
        cmbUrgency.setItems(FXCollections.observableArrayList("Low", "Medium", "High", "Critical"));
    }

    private void loadRequests() {
        apiClient.getAsync("/api/requests", response -> {
            if (response.statusCode() == 200) {
                JSONArray arr = new JSONArray(response.body());
                List<RequestItem> items = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    items.add(new RequestItem(
                            obj.getString("id"),
                            obj.getString("itemName"),
                            obj.getString("category"),
                            obj.getString("requesterName"),
                            obj.getInt("quantity"),
                            obj.getDouble("estimatedCost"),
                            obj.optDouble("actualCost", 0.0),
                            obj.getString("urgency"),
                            obj.getString("status")
                    ));
                }
                Platform.runLater(() -> {
                    requestList.setAll(items);
                    applyFilters();
                });
            }
        });
    }

    private void setupActionColumn() {
        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    RequestItem req = getTableRow().getItem();
                    String status = req.getStatus().toLowerCase();
                    String userRole = getUserRole().toLowerCase();

                    btn.getStyleClass().setAll("btn-primary");
                    btn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");

                    boolean visible = false;

                    if (status.equals("pending") && (userRole.equals("director") || userRole.equals("super_admin"))) {
                        btn.setText("Approve (Dir)");
                        btn.setOnAction(e -> handleStatusChange(req.getId(), "director_approved"));
                        visible = true;
                    } else if (status.equals("director_approved") && (userRole.equals("accountant") || userRole.equals("super_admin"))) {
                        btn.setText("Approve (Fin)");
                        btn.setOnAction(e -> handleStatusChange(req.getId(), "accountant_approved"));
                        visible = true;
                    } else if (status.equals("accountant_approved") && (userRole.equals("store_manager") || userRole.equals("super_admin"))) {
                        btn.setText("Mark Purchased");
                        btn.setOnAction(e -> handleMarkPurchased(req));
                        visible = true;
                    } else if (status.equals("purchased") && (userRole.equals("accountant") || userRole.equals("super_admin"))) {
                        btn.setText("Verify Bill");
                        btn.setOnAction(e -> handleStatusChange(req.getId(), "bill_verified"));
                        visible = true;
                    } else if (status.equals("bill_verified") && (userRole.equals("store_manager") || userRole.equals("super_admin"))) {
                        btn.setText("Deliver");
                        btn.setOnAction(e -> handleStatusChange(req.getId(), "delivered"));
                        visible = true;
                    }

                    if (visible) {
                        setGraphic(btn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void handleStatusChange(String id, String newStatus) {
        apiClient.patchAsync("/api/requests/" + id + "/status?status=" + newStatus, "", response -> {
            if (response.statusCode() == 200) {
                loadRequests();
            }
        });
    }

    private void handleMarkPurchased(RequestItem item) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getEstimatedCost()));
        dialog.setTitle("Mark Purchased");
        dialog.setHeaderText("Enter Actual Invoice Cost for: " + item.getItemName());
        dialog.setContentText("Actual Cost ($):");

        dialog.showAndWait().ifPresent(costStr -> {
            try {
                double actualCost = Double.parseDouble(costStr);
                JSONObject body = new JSONObject();
                body.put("itemName", item.getItemName());
                body.put("category", item.getCategory());
                body.put("quantity", item.getQuantity());
                body.put("estimatedCost", item.getEstimatedCost());
                body.put("actualCost", actualCost);
                body.put("urgency", item.getUrgency());
                body.put("status", "purchased");
                body.put("purpose", "Auto-updated during store purchasing.");

                apiClient.putAsync("/api/requests/" + item.getId(), body.toString(), response -> {
                    if (response.statusCode() == 200) {
                        loadRequests();
                    }
                });
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid cost value!");
                alert.show();
            }
        });
    }

    // FXML Handlers
    @FXML
    public void handleNewRequest() {
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
        String itemName = txtItemName.getText().trim();
        String category = cmbCategory.getValue();
        String qtyStr = txtQuantity.getText().trim();
        String costStr = txtEstCost.getText().trim();
        String urgency = cmbUrgency.getValue();
        String purpose = txtPurpose.getText().trim();

        if (itemName.isEmpty() || category == null || qtyStr.isEmpty() || costStr.isEmpty() || urgency == null || purpose.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill in all request fields!");
            alert.show();
            return;
        }

        try {
            int quantity = Integer.parseInt(qtyStr);
            double estCost = Double.parseDouble(costStr);

            JSONObject req = new JSONObject();
            req.put("requesterId", getUserId());
            req.put("requesterName", getUserName());
            req.put("category", category);
            req.put("itemName", itemName);
            req.put("quantity", quantity);
            req.put("estimatedCost", estCost);
            req.put("purpose", purpose);
            req.put("urgency", urgency);
            req.put("status", "pending");

            apiClient.postAsync("/api/requests", req.toString(), response -> {
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    Platform.runLater(() -> {
                        formPane.setVisible(false);
                        formPane.setManaged(false);
                        loadRequests();
                    });
                }
            });

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Quantity and Cost must be numeric values!");
            alert.show();
        }
    }

    private void clearForm() {
        txtItemName.clear();
        cmbCategory.setValue(null);
        txtQuantity.clear();
        txtEstCost.clear();
        cmbUrgency.setValue(null);
        txtPurpose.clear();
    }

    // Table Model inner class
    public static class RequestItem {
        private final String id;
        private final String itemName;
        private final String category;
        private final String requesterName;
        private final int quantity;
        private final double estimatedCost;
        private final double actualCost;
        private final String urgency;
        private final String status;

        public RequestItem(String id, String itemName, String category, String requesterName, int quantity, double estimatedCost, double actualCost, String urgency, String status) {
            this.id = id;
            this.itemName = itemName;
            this.category = category;
            this.requesterName = requesterName;
            this.quantity = quantity;
            this.estimatedCost = estimatedCost;
            this.actualCost = actualCost;
            this.urgency = urgency;
            this.status = status;
        }

        public String getId() { return id; }
        public String getItemName() { return itemName; }
        public String getCategory() { return category; }
        public String getRequesterName() { return requesterName; }
        public int getQuantity() { return quantity; }
        public double getEstimatedCost() { return estimatedCost; }
        public double getActualCost() { return actualCost; }
        public String getUrgency() { return urgency; }
        public String getStatus() { return status; }
    }
}
