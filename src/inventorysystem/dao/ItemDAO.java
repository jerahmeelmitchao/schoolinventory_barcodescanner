package inventorysystem.dao;

import inventorysystem.models.Item;
import inventorysystem.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public boolean addItem(Item item) {
        String sql = """
            INSERT INTO items 
            (item_name, barcode, category_id, quantity, unit, date_acquired, serviceability_status, 
            condition_status, availability_status, storage_location, incharge_id, added_by) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getBarcode());
            stmt.setInt(3, item.getCategoryId());
            stmt.setInt(4, item.getQuantity());
            stmt.setString(5, item.getUnit());
            stmt.setDate(6, Date.valueOf(item.getDateAcquired()));
            stmt.setString(7, item.getServiceabilityStatus());
            stmt.setString(8, item.getConditionStatus());
            stmt.setString(9, item.getAvailabilityStatus());
            stmt.setString(10, item.getStorageLocation());

            if (item.getInchargeId() > 0) {
                stmt.setInt(11, item.getInchargeId());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }

            stmt.setString(12, item.getAddedBy());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setItemId(generatedKeys.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = """
            SELECT 
                i.item_id, i.item_name, i.barcode, i.category_id, c.category_name, i.quantity,
                i.unit, i.date_acquired, i.serviceability_status, i.condition_status, 
                i.availability_status, i.storage_location, i.last_scanned, i.incharge_id,
                ic.incharge_name, i.added_by
            FROM items i
            LEFT JOIN categories c ON i.category_id = c.category_id
            LEFT JOIN incharge ic ON i.incharge_id = ic.incharge_id
        """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

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
                        rs.getString("condition_status"),
                        rs.getString("availability_status"),
                        rs.getString("storage_location"),
                        rs.getInt("incharge_id"),
                        rs.getString("added_by")
                );

                item.setCategoryName(rs.getString("category_name"));
                item.setInChargeName(rs.getString("incharge_name"));
                item.setLastScanned(rs.getDate("last_scanned") != null ? rs.getDate("last_scanned").toLocalDate() : null);

                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    public void updateItem(Item item) {
        String sql = """
            UPDATE items SET 
                item_name=?, barcode=?, category_id=?, quantity=?, unit=?, date_acquired=?,
                serviceability_status=?, condition_status=?, availability_status=?, 
                storage_location=?, incharge_id=?, added_by=?
            WHERE item_id=?
        """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getBarcode());
            stmt.setInt(3, item.getCategoryId());
            stmt.setInt(4, item.getQuantity());
            stmt.setString(5, item.getUnit());
            stmt.setDate(6, Date.valueOf(item.getDateAcquired()));
            stmt.setString(7, item.getServiceabilityStatus());
            stmt.setString(8, item.getConditionStatus());
            stmt.setString(9, item.getAvailabilityStatus());
            stmt.setString(10, item.getStorageLocation());

            if (item.getInchargeId() > 0) {
                stmt.setInt(11, item.getInchargeId());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }

            stmt.setString(12, item.getAddedBy());
            stmt.setInt(13, item.getItemId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
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
