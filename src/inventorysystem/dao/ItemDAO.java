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
            (item_name, barcode, category_id, unit, date_acquired, status, 
             storage_location, incharge_id, added_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getBarcode());
            stmt.setInt(3, item.getCategoryId());
            stmt.setString(4, item.getUnit());
            stmt.setDate(5, Date.valueOf(item.getDateAcquired()));
            stmt.setString(6, item.getStatus());
            stmt.setString(7, item.getStorageLocation());

            if (item.getInchargeId() > 0) {
                stmt.setInt(8, item.getInchargeId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.setString(9, item.getAddedBy());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                return false;
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setItemId(keys.getInt(1));
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("❌ Add Item Error: " + e.getMessage());
            return false;
        }
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();

        String sql = """
            SELECT 
                i.item_id, i.item_name, i.barcode, i.category_id, c.category_name,
                i.unit, i.date_acquired, i.status,
                i.storage_location, i.last_scanned, i.incharge_id, 
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
                        rs.getString("unit"),
                        rs.getDate("date_acquired").toLocalDate(),
                        rs.getString("status"),
                        rs.getString("storage_location"),
                        rs.getInt("incharge_id"),
                        rs.getString("added_by")
                );

                item.setCategoryName(rs.getString("category_name"));
                item.setInChargeName(rs.getString("incharge_name"));

                if (rs.getDate("last_scanned") != null) {
                    item.setLastScanned(rs.getDate("last_scanned").toLocalDate());
                }

                items.add(item);
            }

        } catch (SQLException e) {
            System.err.println("❌ Load Items Error: " + e.getMessage());
        }

        return items;
    }

    public void updateItem(Item item) {

        String sql = """
            UPDATE items SET 
                item_name=?, barcode=?, category_id=?, unit=?, date_acquired=?,
                status=?, storage_location=?, incharge_id=?, added_by=?
            WHERE item_id=?
        """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getBarcode());
            stmt.setInt(3, item.getCategoryId());
            stmt.setString(4, item.getUnit());
            stmt.setDate(5, Date.valueOf(item.getDateAcquired()));
            stmt.setString(6, item.getStatus());
            stmt.setString(7, item.getStorageLocation());

            if (item.getInchargeId() > 0) {
                stmt.setInt(8, item.getInchargeId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.setString(9, item.getAddedBy());
            stmt.setInt(10, item.getItemId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Update Item Error: " + e.getMessage());
        }
    }

    public void deleteItem(int itemId) {
        String sql = "DELETE FROM items WHERE item_id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Delete Item Error: " + e.getMessage());
        }
    }

    public boolean updateItemStatus(int itemId, String status) {
        String sql = "UPDATE items SET status=? WHERE item_id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Update Item Status Error: " + e.getMessage());
            return false;
        }
    }

}
