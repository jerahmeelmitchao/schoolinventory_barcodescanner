package inventorysystem.controllers;

import inventorysystem.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.Month;

public class Dashboard2Controller {

    @FXML
    private VBox chartContainer;
    @FXML
    private Label lblTotalItems, lblLowStock, lblDamaged, lblUnscanned;

    @FXML
    public void initialize() {
        loadDashboard();
    }

    private void loadDashboard() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            loadCounts(conn);
            loadCategoryChart(conn);
            loadMostScannedChart(conn);
            loadMonthlyAddedChart(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // === ANTIGA: Total per Category ===
    private void loadCategoryChart(Connection conn) throws SQLException {
        String sql = """
            SELECT c.category_name, COUNT(i.item_id) AS total
            FROM items i
            JOIN categories c ON i.category_id = c.category_id
            GROUP BY c.category_name
            """;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("category_name"), rs.getInt("total")));
            }
        }
        BarChart<String, Number> barChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        barChart.setTitle("Items per Category");
        barChart.getData().add(series);
        chartContainer.getChildren().add(barChart);
    }

    // === ANTIGA (2): Most Scanned this Month ===
    private void loadMostScannedChart(Connection conn) throws SQLException {
        String sql = """
            SELECT i.item_name, COUNT(s.scan_id) AS scans
            FROM items i
            JOIN scan_log s ON i.item_id = s.item_id
            WHERE MONTH(s.scan_date) = MONTH(CURDATE())
            GROUP BY i.item_name
            ORDER BY scans DESC
            LIMIT 5
            """;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("item_name"), rs.getInt("scans")));
            }
        }
        BarChart<String, Number> barChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        barChart.setTitle("Top 5 Most Scanned Items (This Month)");
        barChart.getData().add(series);
        chartContainer.getChildren().add(barChart);
    }

    // === AMANTE: Items Added per Month ===
    private void loadMonthlyAddedChart(Connection conn) throws SQLException {
        String sql = """
            SELECT MONTH(date_acquired) AS month_num, COUNT(*) AS total
            FROM items
            WHERE date_acquired IS NOT NULL
            GROUP BY MONTH(date_acquired)
            """;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int m = rs.getInt("month_num");
                series.getData().add(new XYChart.Data<>(Month.of(m).name(), rs.getInt("total")));
            }
        }
        LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        chart.setTitle("Items Added per Month");
        chart.getData().add(series);
        chartContainer.getChildren().add(chart);
    }

    // === EBRADO / LAGMAY / ROSETE: Summary Counts ===
    private void loadCounts(Connection conn) throws SQLException {
        lblTotalItems.setText(String.valueOf(count(conn, "SELECT COUNT(*) FROM items")));
        lblLowStock.setText(String.valueOf(count(conn, "SELECT COUNT(*) FROM items WHERE quantity < 5")));
        lblDamaged.setText(String.valueOf(count(conn, "SELECT COUNT(*) FROM items WHERE condition_status IN ('Damaged','Disposed')")));
        lblUnscanned.setText(String.valueOf(count(conn, "SELECT COUNT(*) FROM items WHERE last_scanned IS NULL OR last_scanned < DATE_SUB(CURDATE(), INTERVAL 90 DAY)")));
    }

    private int count(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
