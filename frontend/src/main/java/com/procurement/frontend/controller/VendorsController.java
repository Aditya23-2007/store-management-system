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

public class VendorsController extends BaseController {

    @FXML private TableView<VendorItem> tblVendors;
    @FXML private TableColumn<VendorItem, String> colName;
    @FXML private TableColumn<VendorItem, String> colContact;
    @FXML private TableColumn<VendorItem, String> colEmail;
    @FXML private TableColumn<VendorItem, String> colGST;
    @FXML private TableColumn<VendorItem, Double> colRating;
    @FXML private TableColumn<VendorItem, Void> colAction;

    @FXML private Button btnNewVendor;
    @FXML private TextField txtSearch;

    // Form Pane
    @FXML private VBox formPane;
    @FXML private TextField txtVendorName;
    @FXML private TextField txtContact;
    @FXML private TextField txtEmail;
    @FXML private TextField txtGST;
    @FXML private TextField txtRating;

    private final APIClient apiClient = new APIClient();
    private final ObservableList<VendorItem> vendorList = FXCollections.observableArrayList();
    private FilteredList<VendorItem> filteredList;

    @FXML
    public void initialize() {
        // Map table columns
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colGST.setCellValueFactory(new PropertyValueFactory<>("gstNumber"));

        setupRatingColumn();
        setupActionColumn();
        setupFilters();
        loadVendors();

        // Control add vendor visibility
        String role = getUserRole();
        btnNewVendor.setVisible(role.equals("store_manager") || role.equals("super_admin"));
    }

    private void setupFilters() {
        filteredList = new FilteredList<>(vendorList, p -> true);
        tblVendors.setItems(filteredList);

        // Search trigger
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            String val = newValue.toLowerCase().trim();
            filteredList.setPredicate(item -> {
                if (val.isEmpty()) return true;
                return item.getName().toLowerCase().contains(val)
                        || item.getContact().toLowerCase().contains(val)
                        || item.getEmail().toLowerCase().contains(val)
                        || item.getGstNumber().toLowerCase().contains(val);
            });
        });
    }

    private void loadVendors() {
        apiClient.getAsync("/api/vendors", response -> {
            if (response.statusCode() == 200) {
                JSONArray arr = new JSONArray(response.body());
                List<VendorItem> items = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    items.add(new VendorItem(
                            obj.getString("id"),
                            obj.getString("name"),
                            obj.optString("contact", "N/A"),
                            obj.optString("email", "N/A"),
                            obj.optString("gstNumber", "N/A"),
                            obj.optDouble("rating", 5.0)
                    ));
                }
                Platform.runLater(() -> {
                    vendorList.setAll(items);
                });
            }
        });
    }

    private void setupRatingColumn() {
        colRating.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VendorItem ven = getTableRow().getItem();
                    double rating = ven.getRating();
                    
                    String text;
                    String badgeClass;
                    if (rating >= 4.5) {
                        text = "⭐ " + rating + " Excellent";
                        badgeClass = "badge-green";
                    } else if (rating >= 4.0) {
                        text = "⭐ " + rating + " Good";
                        badgeClass = "badge-blue";
                    } else {
                        text = "⭐ " + rating + " Standard";
                        badgeClass = "badge-orange";
                    }

                    Label badge = new Label(text);
                    badge.getStyleClass().setAll("card-badge", badgeClass);
                    setGraphic(badge);
                }
            }
        });
    }

    private void setupActionColumn() {
        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button("Edit Score");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    VendorItem ven = getTableRow().getItem();
                    String userRole = getUserRole().toLowerCase();

                    // Only Store Managers or Super Admins can adjust score
                    if (userRole.equals("store_manager") || userRole.equals("super_admin")) {
                        btn.getStyleClass().setAll("btn-primary");
                        btn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                        btn.setOnAction(e -> handleUpdateScore(ven));
                        setGraphic(btn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void handleUpdateScore(VendorItem item) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getRating()));
        dialog.setTitle("Supplier Audit");
        dialog.setHeaderText("Adjust dynamic performance score for: " + item.getName());
        dialog.setContentText("Quality Rating (1.0 - 5.0):");

        dialog.showAndWait().ifPresent(ratingStr -> {
            try {
                double rating = Double.parseDouble(ratingStr);
                if (rating < 1.0 || rating > 5.0) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Rating must be between 1.0 and 5.0!");
                    alert.show();
                    return;
                }

                JSONObject body = new JSONObject();
                body.put("name", item.getName());
                body.put("contact", item.getContact());
                body.put("email", item.getEmail());
                body.put("gstNumber", item.getGstNumber());
                body.put("rating", rating);

                apiClient.putAsync("/api/vendors/" + item.getId(), body.toString(), response -> {
                    if (response.statusCode() == 200) {
                        loadVendors();
                    }
                });
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Rating must be a valid number!");
                alert.show();
            }
        });
    }

    // FXML Handlers
    @FXML
    public void handleNewVendor() {
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
        String name = txtVendorName.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();
        String gst = txtGST.getText().trim();
        String ratingStr = txtRating.getText().trim();

        if (name.isEmpty() || contact.isEmpty() || email.isEmpty() || gst.isEmpty() || ratingStr.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "All registry fields are required!");
            alert.show();
            return;
        }

        try {
            double rating = Double.parseDouble(ratingStr);
            if (rating < 1.0 || rating > 5.0) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Rating must be between 1.0 and 5.0!");
                alert.show();
                return;
            }

            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("contact", contact);
            body.put("email", email);
            body.put("gstNumber", gst);
            body.put("rating", rating);

            apiClient.postAsync("/api/vendors", body.toString(), response -> {
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    Platform.runLater(() -> {
                        formPane.setVisible(false);
                        formPane.setManaged(false);
                        loadVendors();
                    });
                }
            });
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Rating must be a valid number!");
            alert.show();
        }
    }

    private void clearForm() {
        txtVendorName.clear();
        txtContact.clear();
        txtEmail.clear();
        txtGST.clear();
        txtRating.clear();
    }

    // Table Model inner class
    public static class VendorItem {
        private final String id;
        private final String name;
        private final String contact;
        private final String email;
        private final String gstNumber;
        private final double rating;

        public VendorItem(String id, String name, String contact, String email, String gstNumber, double rating) {
            this.id = id;
            this.name = name;
            this.contact = contact;
            this.email = email;
            this.gstNumber = gstNumber;
            this.rating = rating;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getContact() { return contact; }
        public String getEmail() { return email; }
        public String getGstNumber() { return gstNumber; }
        public double getRating() { return rating; }
    }
}
