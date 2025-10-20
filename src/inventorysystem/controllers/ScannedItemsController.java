package inventorysystem.controllers;

import inventorysystem.utils.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.LocalDateTime;

import javafx.scene.layout.StackPane;

public class ScannedItemsController {

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TableView<ScannedItem> tableScannedItems;
    @FXML
    private TableColumn<ScannedItem, String> colItemName;
    @FXML
    private TableColumn<ScannedItem, String> colScanDate;

    // Item details labels
    @FXML
    private Label lblName;
    @FXML
    private Label lblCategory;
    @FXML
    private Label lblQuantity;
    @FXML
    private Label lblCondition;
    @FXML
    private Label lblLocation;
    @FXML
    private Label lblLastScan;

    // Chart placeholder label (from FXML)
    @FXML
    private Label chartPlaceholder;
    // Parent VBox where we’ll insert the chart dynamically
    @FXML
    private VBox mainVBox;

    private final ObservableList<ScannedItem> scannedList = FXCollections.observableArrayList();
    @FXML

    private StackPane chartContainer;
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public void initialize() {
        colItemName.setCellValueFactory(data -> data.getValue().itemNameProperty());
        colScanDate.setCellValueFactory(data -> {
            String raw = data.getValue().getScanDate();
            String formatted = "—";
            try {
                if (raw != null) {
                    LocalDate date = LocalDate.parse(raw.split(" ")[0]); // handle timestamps
                    formatted = date.format(displayFormatter);
                }
            } catch (Exception e) {
                formatted = raw; // fallback to raw if parsing fails
            }
            return new SimpleStringProperty(formatted);
        });

        tableScannedItems.setItems(scannedList);

        // When user selects a row, show details
        tableScannedItems.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> {
                    if (newSel != null) {
                        showItemDetails(newSel.getItemName());
                    }
                }
        );

        loadScannedVsUnscannedChart(); // Load chart once view is ready
    }

    @FXML
    private void onFilterClicked() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null) {
            new Alert(Alert.AlertType.WARNING, "Please select both start and end dates.").showAndWait();
            return;
        }

        loadScannedItems(start, end);
    }

    private void loadScannedItems(LocalDate start, LocalDate end) {
        scannedList.clear();

        String sql = """
            SELECT i.item_name, s.scan_date
            FROM scan_log s
            JOIN items i ON s.item_id = i.item_id
            WHERE DATE(s.scan_date) BETWEEN ? AND ?
            ORDER BY s.scan_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                scannedList.add(new ScannedItem(
                        rs.getString("item_name"),
                        rs.getString("scan_date")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showItemDetails(String itemName) {
        String sql = """
            SELECT i.item_name, c.category_name, i.quantity,
                   i.condition_status, i.storage_location, i.last_scanned
            FROM items i
            LEFT JOIN categories c ON i.category_id = c.category_id
            WHERE i.item_name = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                lblName.setText("Item Name: " + rs.getString("item_name"));
                lblCategory.setText("Category: " + rs.getString("category_name"));
                lblQuantity.setText("Quantity: " + rs.getInt("quantity"));
                lblCondition.setText("Condition: " + rs.getString("condition_status"));
                lblLocation.setText("Storage Location: " + rs.getString("storage_location"));
                String lastScannedRaw = rs.getString("last_scanned");
                String formattedLastScanned = "—";
                if (lastScannedRaw != null) {
                    try {
                        LocalDate date = LocalDate.parse(lastScannedRaw.split(" ")[0]);
                        formattedLastScanned = date.format(displayFormatter);
                    } catch (Exception e) {
                        formattedLastScanned = lastScannedRaw;
                    }
                }
                lblLastScan.setText(formattedLastScanned);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadScannedVsUnscannedChart() {
        if (mainVBox == null) {
            System.err.println("❌ mainVBox is null — make sure fx:id is set in FXML.");
            return;
        }

        mainVBox.getChildren().removeIf(node -> node instanceof BarChart);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        xAxis.setTickLabelRotation(45);
        xAxis.setAutoRanging(true);
        // 🖤 Make axis labels and tick labels black
        xAxis.setStyle("-fx-text-fill: black; -fx-tick-label-fill: black;");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Items");
        yAxis.setForceZeroInRange(true);
        yAxis.setAutoRanging(true);
        // 🖤 Make axis labels and tick labels black
        yAxis.setStyle("-fx-text-fill: black; -fx-tick-label-fill: black;");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setPrefHeight(400);
        barChart.setLegendVisible(true);
        barChart.setCategoryGap(20);
        barChart.setBarGap(5);

        XYChart.Series<String, Number> scannedSeries = new XYChart.Series<>();
        scannedSeries.setName("✅ Scanned Items");

        XYChart.Series<String, Number> unscannedSeries = new XYChart.Series<>();
        unscannedSeries.setName("⚠️ Unscanned Items");

        ObservableList<String> months = FXCollections.observableArrayList();

        String sql = """
                     SELECT 
                         DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL n.n MONTH), '%M') AS month_name,
                         COALESCE((
                             SELECT COUNT(DISTINCT item_id)
                             FROM scan_log
                             WHERE MONTH(scan_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL n.n MONTH))
                               AND YEAR(scan_date) = YEAR(DATE_SUB(CURDATE(), INTERVAL n.n MONTH))
                         ), 0) AS scanned,
                         COALESCE((
                             (SELECT COUNT(*) FROM items) -
                             (SELECT COUNT(DISTINCT item_id)
                              FROM scan_log
                              WHERE MONTH(scan_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL n.n MONTH))
                                AND YEAR(scan_date) = YEAR(DATE_SUB(CURDATE(), INTERVAL n.n MONTH)))
                         ), 0) AS unscanned
                     FROM (
                         SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 
                         UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
                     ) AS n
                     ORDER BY n.n DESC;
        """;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String month = rs.getString("month_name");
                int scanned = rs.getInt("scanned");
                int unscanned = rs.getInt("unscanned");

                months.add(month);
                scannedSeries.getData().add(new XYChart.Data<>(month, scanned));
                unscannedSeries.getData().add(new XYChart.Data<>(month, unscanned));
            }

            xAxis.setCategories(months);
            barChart.getData().addAll(scannedSeries, unscannedSeries);

            for (XYChart.Series<String, Number> series : barChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            if (series.getName().contains("Scanned")) {
                                newNode.setStyle("-fx-bar-fill: #27ae60;");
                            } else {
                                newNode.setStyle("-fx-bar-fill: #e74c3c;");
                            }
                        }
                    });
                }
            }

            chartContainer.getChildren().clear();
            chartContainer.getChildren().add(barChart);
            chartPlaceholder.setVisible(false);

        } catch (SQLException e) {
            e.printStackTrace();
            chartPlaceholder.setText("⚠️ Failed to load chart data.");
            chartPlaceholder.setVisible(true);
        }
    }

    // Inner class: data model for the table
    public static class ScannedItem {

        private final SimpleStringProperty itemName;
        private final SimpleStringProperty scanDate;

        public ScannedItem(String itemName, String scanDate) {
            this.itemName = new SimpleStringProperty(itemName);
            this.scanDate = new SimpleStringProperty(scanDate);
        }

        public String getItemName() {
            return itemName.get();
        }

        public SimpleStringProperty itemNameProperty() {
            return itemName;
        }

        public String getScanDate() {
            return scanDate.get();
        }

        public SimpleStringProperty scanDateProperty() {
            return scanDate;
        }
    }
}
