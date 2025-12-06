package inventorysystem.dao;

import inventorysystem.models.BorrowRecord;
import inventorysystem.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAO {

    // Add a new borrow record
    public void addBorrowRecord(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records (item_id, borrower_id, borrow_date, return_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, record.getItemId());
            stmt.setInt(2, record.getBorrowerId());
            stmt.setDate(3, Date.valueOf(record.getBorrowDate()));

            if (record.getReturnDate() != null) {
                stmt.setDate(4, Date.valueOf(record.getReturnDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, record.getStatus());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all borrow records
    public List<BorrowRecord> getAllBorrowRecords() {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM borrow_records";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BorrowRecord r = new BorrowRecord(
                        rs.getInt("record_id"),
                        rs.getInt("item_id"),
                        rs.getInt("borrower_id"),
                        rs.getDate("borrow_date").toLocalDate(),
                        rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null,
                        rs.getString("status")
                );
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Get a borrow record by ID
    public BorrowRecord getBorrowRecordById(int id) {
        String sql = "SELECT * FROM borrow_records WHERE record_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new BorrowRecord(
                            rs.getInt("record_id"),
                            rs.getInt("item_id"),
                            rs.getInt("borrower_id"),
                            rs.getDate("borrow_date").toLocalDate(),
                            rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null,
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update a borrow record
    public void updateBorrowRecord(BorrowRecord record) {
        String sql = "UPDATE borrow_records SET item_id=?, borrower_id=?, borrow_date=?, return_date=?, status=? WHERE record_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, record.getItemId());
            stmt.setInt(2, record.getBorrowerId());
            stmt.setDate(3, Date.valueOf(record.getBorrowDate()));

            if (record.getReturnDate() != null) {
                stmt.setDate(4, Date.valueOf(record.getReturnDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, record.getStatus());
            stmt.setInt(6, record.getRecordId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete a borrow record
    public void deleteBorrowRecord(int id) {
        String sql = "DELETE FROM borrow_records WHERE record_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get borrow records by borrower
    public List<BorrowRecord> getBorrowRecordsByBorrower(int borrowerId) {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM borrow_records WHERE borrower_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, borrowerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BorrowRecord r = new BorrowRecord(
                            rs.getInt("record_id"),
                            rs.getInt("item_id"),
                            rs.getInt("borrower_id"),
                            rs.getDate("borrow_date").toLocalDate(),
                            rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null,
                            rs.getString("status")
                    );
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Mark record as returned
    public void returnBorrowRecord(int recordId, LocalDate returnDate, String remarks) {
        String sql = "UPDATE borrow_records SET return_date=?, status=?, remarks=? WHERE record_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(returnDate));
            stmt.setString(2, "Returned");
            stmt.setString(3, remarks);
            stmt.setInt(4, recordId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
