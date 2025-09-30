package inventorysystem.dao;

import inventorysystem.models.Item;
import inventorysystem.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public boolean addItem(Item item) {
        String sql = "INSERT INTO items (item_name, barcode, category_id, quantity, unit, date_acquired, serviceability_status, availability_status, incharge_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getBarcode());
            stmt.setInt(3, item.getCategoryId());
            stmt.setInt(4, item.getQuantity());
            stmt.setString(5, item.getUnit());
            stmt.setDate(6, Date.valueOf(item.getDateAcquired()));
            stmt.setString(7, item.getServiceabilityStatus());
            stmt.setString(8, item.getAvailabilityStatus());

            // Handle incharge_id (nullable if not provided)
            if (item.getInchargeId() > 0) {
                stmt.setInt(9, item.getInchargeId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("⚠ No rows inserted.");
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setItemId(generatedKeys.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error inserting item:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            return false;
        }
    }

    public List<Item> getAllItems() {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Item item = new Item(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("barcode"),
                        rs.getInt("category_id"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getDate("date_acquired").toLocalDate(),
                        rs.getString("serviceability_status"),
                        rs.getString("availability_status"),
                        rs.getInt("incharge_id")
                );
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Item getItemById(int id) {
        String sql = "SELECT * FROM items WHERE item_id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Item(
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getString("barcode"),
                            rs.getInt("category_id"),
                            rs.getInt("quantity"),
                            rs.getString("unit"),
                            rs.getDate("date_acquired").toLocalDate(),
                            rs.getString("serviceability_status"),
                            rs.getString("availability_status"),
                            rs.getInt("incharge_id")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateItem(Item item) {
        String sql = "UPDATE items SET item_name=?, barcode=?, category_id=?, quantity=?, unit=?, date_acquired=?, serviceability_status=?, availability_status=?, incharge_id=? WHERE item_id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getBarcode());
            stmt.setInt(3, item.getCategoryId());
            stmt.setInt(4, item.getQuantity());
            stmt.setString(5, item.getUnit());
            stmt.setDate(6, Date.valueOf(item.getDateAcquired()));
            stmt.setString(7, item.getServiceabilityStatus());
            stmt.setString(8, item.getAvailabilityStatus());

            if (item.getInchargeId() > 0) {
                stmt.setInt(9, item.getInchargeId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }

            stmt.setInt(10, item.getItemId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Error updating item: " + e.getMessage());
        }
    }

    public void deleteItem(int id) {
        String sql = "DELETE FROM items WHERE item_id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error deleting item: " + e.getMessage());
        }
    }

    public boolean decreaseQuantity(int itemId, int quantity) {
        String sql = "UPDATE items SET quantity = quantity - ? WHERE item_id=? AND quantity >= ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, itemId);
            stmt.setInt(3, quantity);

            int rows = stmt.executeUpdate();
            return rows > 0; // true if updated
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void increaseQuantity(int itemId, int quantity) {
        String sql = "UPDATE items SET quantity = quantity + ? WHERE item_id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
