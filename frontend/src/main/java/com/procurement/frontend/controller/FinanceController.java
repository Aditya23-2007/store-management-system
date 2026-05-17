package com.procurement.frontend.controller;

import com.procurement.frontend.client.APIClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class FinanceController extends BaseController {

    @FXML private Label lblTotalBudget;
    @FXML private Label lblTotalSpent;
    @FXML private Label lblTotalRemaining;

    @FXML private TableView<DepartmentBudget> tblBudgets;
    @FXML private TableColumn<DepartmentBudget, String> colDeptName;
    @FXML private TableColumn<DepartmentBudget, Double> colAllocated;
    @FXML private TableColumn<DepartmentBudget, Double> colSpent;
    @FXML private TableColumn<DepartmentBudget, Double> colRemaining;
    @FXML private TableColumn<DepartmentBudget, String> colStatus;
    @FXML private TableColumn<DepartmentBudget, Void> colAction;

    @FXML private PieChart budgetPieChart;

    private final APIClient apiClient = new APIClient();
    private final ObservableList<DepartmentBudget> budgetList = FXCollections.observableArrayList();

    private static final java.util.Map<String, Double> customAllocations = new java.util.HashMap<>();
    static {
        customAllocations.put("Electronics", 200000.0);
        customAllocations.put("Stationery", 50000.0);
        customAllocations.put("Office Supplies", 60000.0);
        customAllocations.put("Lab Equipment", 150000.0);
        customAllocations.put("Furniture", 100000.0);
        customAllocations.put("Others", 40000.0);
    }

    @FXML
    public void initialize() {
        colDeptName.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colAllocated.setCellValueFactory(new PropertyValueFactory<>("allocated"));
        colSpent.setCellValueFactory(new PropertyValueFactory<>("spent"));
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remaining"));

        setupStatusColumn();
        setupActionColumn();
        loadRealFinanceData();
        tblBudgets.setItems(budgetList);
    }

    private void loadRealFinanceData() {
        apiClient.getAsync("/api/requests", response -> {
            if (response.statusCode() == 200) {
                JSONArray arr = new JSONArray(response.body());
                
                // Group spending by category
                java.util.Map<String, Double> spentByCategory = new java.util.HashMap<>();
                for (String cat : customAllocations.keySet()) {
                    spentByCategory.put(cat, 0.0);
                }

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String category = obj.optString("category", "Others");
                    String status = obj.optString("status", "pending").toLowerCase();
                    
                    // Normalize category name to match our keys
                    String matchedCat = "Others";
                    for (String key : customAllocations.keySet()) {
                        if (key.equalsIgnoreCase(category)) {
                            matchedCat = key;
                            break;
                        }
                    }

                    // Count approved/purchased/delivered requests toward expenditures
                    if (!status.equals("pending")) {
                        double cost = obj.optDouble("actualCost", 0.0);
                        if (cost <= 0) {
                            cost = obj.optDouble("estimatedCost", 0.0);
                        }
                        spentByCategory.put(matchedCat, spentByCategory.getOrDefault(matchedCat, 0.0) + cost);
                    }
                }

                List<DepartmentBudget> items = new java.util.ArrayList<>();
                for (String cat : customAllocations.keySet()) {
                    double allocated = customAllocations.get(cat);
                    double spent = spentByCategory.getOrDefault(cat, 0.0);
                    items.add(new DepartmentBudget(cat, allocated, spent));
                }

                Platform.runLater(() -> {
                    budgetList.setAll(items);
                    updateSummary();
                });
            }
        });
    }

    private void updateSummary() {
        double totalAllocation = 0;
        double totalSpent = 0;

        for (DepartmentBudget db : budgetList) {
            totalAllocation += db.getAllocated();
            totalSpent += db.getSpent();
        }

        double totalRemaining = totalAllocation - totalSpent;

        lblTotalBudget.setText(String.format("$%,.2f", totalAllocation));
        lblTotalSpent.setText(String.format("$%,.2f", totalSpent));
        lblTotalRemaining.setText(String.format("$%,.2f", totalRemaining));

        // Update PieChart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (DepartmentBudget db : budgetList) {
            pieData.add(new PieChart.Data(db.getDepartmentName(), db.getAllocated()));
        }
        budgetPieChart.setData(pieData);
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
                    DepartmentBudget db = getTableRow().getItem();
                    double pct = (db.getSpent() / db.getAllocated()) * 100.0;
                    
                    boolean critical = pct >= 70.0;
                    Label badge = new Label(critical ? "Alert: Near Limit" : "Stable");
                    badge.getStyleClass().setAll("card-badge", critical ? "badge-red" : "badge-green");
                    setGraphic(badge);
                }
            }
        });
    }

    private void setupActionColumn() {
        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button("Top-Up");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    DepartmentBudget db = getTableRow().getItem();
                    String userRole = getUserRole().toLowerCase();

                    // Only Accountants or Super Admins can top-up budgets
                    if (userRole.equals("accountant") || userRole.equals("super_admin")) {
                        btn.getStyleClass().setAll("btn-primary");
                        btn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                        btn.setOnAction(e -> handleBudgetTopup(db));
                        setGraphic(btn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void handleBudgetTopup(DepartmentBudget db) {
        TextInputDialog dialog = new TextInputDialog("20000");
        dialog.setTitle("Budget Allocation Top-Up");
        dialog.setHeaderText("Increase funding limit for: " + db.getDepartmentName());
        dialog.setContentText("Top-Up Amount ($):");

        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double topUp = Double.parseDouble(amountStr);
                double newAllocated = db.getAllocated() + topUp;
                customAllocations.put(db.getDepartmentName(), newAllocated);
                
                loadRealFinanceData();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Budget allocation topped up successfully!");
                alert.show();
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Amount must be a number!");
                alert.show();
            }
        });
    }

    // Model class for departmental budgets
    public static class DepartmentBudget {
        private final String departmentName;
        private double allocated;
        private final double spent;

        public DepartmentBudget(String departmentName, double allocated, double spent) {
            this.departmentName = departmentName;
            this.allocated = allocated;
            this.spent = spent;
        }

        public String getDepartmentName() { return departmentName; }
        public double getAllocated() { return allocated; }
        public void setAllocated(double allocated) { this.allocated = allocated; }
        public double getSpent() { return spent; }
        public double getRemaining() { return allocated - spent; }
    }
}
