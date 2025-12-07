package inventorysystem.controllers;

import inventorysystem.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;

import java.sql.*;
import java.time.Month;

public class Dashboard2Controller {

    @FXML
    private GridPane chartContainer;
    @FXML
    private Label lblTotalItems, lblLowStock, lblDamaged, lblUnscanned;

    @FXML
    public void initialize() {
        loadDashboard();
    }

    private void loadDashboard() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            loadCounts(conn);
            loadCategoryChart(conn, 0, 0);
            loadMostScannedChart(conn, 0, 1);
            loadMonthlyAddedChart(conn, 1, 0, 2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCategoryChart(Connection conn, int row, int col) throws SQLException {
        String sql = """
            SELECT c.category_name, COUNT(i.item_id) AS total
            FROM items i
            JOIN categories c ON i.category_id = c.category_id
            GROUP BY c.category_name
            """;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("category_name"), rs.getInt("total")));
            }
        }

        BarChart<String, Number> barChart =
                new BarChart<>(new CategoryAxis(), new NumberAxis());
        barChart.setTitle("Items per Category");
        barChart.getData().add(series);
        barChart.setPrefSize(500, 300);

        chartContainer.add(barChart, col, row);
    }

    private void loadMostScannedChart(Connection conn, int row, int col) throws SQLException {
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
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("item_name"), rs.getInt("scans")));
            }
        }

        BarChart<String, Number> barChart =
                new BarChart<>(new CategoryAxis(), new NumberAxis());
        barChart.setTitle("Top 5 Most Scanned Items (This Month)");
        barChart.getData().add(series);
        barChart.setPrefSize(500, 300);

        chartContainer.add(barChart, col, row);
    }

    private void loadMonthlyAddedChart(Connection conn, int row, int col, int colSpan) throws SQLException {
        String sql = """
            SELECT MONTH(date_acquired) AS month_num, COUNT(*) AS total
            FROM items
            WHERE date_acquired IS NOT NULL
            GROUP BY MONTH(date_acquired)
            """;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int m = rs.getInt("month_num");
                series.getData().add(new XYChart.Data<>(Month.of(m).name(), rs.getInt("total")));
            }
        }

        LineChart<String, Number> lineChart =
                new LineChart<>(new CategoryAxis(), new NumberAxis());
        lineChart.setTitle("Items Added per Month");
        lineChart.getData().add(series);
        lineChart.setPrefSize(1020, 300);

        chartContainer.add(lineChart, col, row, colSpan, 1);
    }

    private void loadCounts(Connection conn) throws SQLException {

        // Count all items
        lblTotalItems.setText(String.valueOf(count(conn,
                "SELECT COUNT(*) FROM items")));

        // Low stock no longer applies → You removed quantity.
        // Use Missing or Borrowed as "problem items"
        lblLowStock.setText(String.valueOf(count(conn,
                "SELECT COUNT(*) FROM items WHERE status IN ('Missing','Borrowed')")));

        // Damaged or Disposed → Using new status field
        lblDamaged.setText(String.valueOf(count(conn,
                "SELECT COUNT(*) FROM items WHERE status IN ('Damaged','Disposed')")));

        // Unscanned logic remains the same
        lblUnscanned.setText(String.valueOf(count(conn,
                "SELECT COUNT(*) FROM items WHERE last_scanned IS NULL OR last_scanned < DATE_SUB(CURDATE(), INTERVAL 90 DAY)")));
    }

    private int count(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
