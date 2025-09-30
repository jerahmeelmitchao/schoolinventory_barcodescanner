/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventorysystem.dao;

import inventorysystem.models.Borrower;
import inventorysystem.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowerDAO {

    // Get all borrowers
    public List<Borrower> getAllBorrowers() {
        List<Borrower> list = new ArrayList<>();
        String sql = "SELECT borrower_id, borrower_name, position, borrower_type FROM borrowers ORDER BY borrower_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Borrower b = new Borrower(
                        rs.getInt("borrower_id"),
                        rs.getString("borrower_name"),
                        rs.getString("position"),
                        rs.getString("borrower_type")
                );
                list.add(b);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Get borrower by ID
    public Borrower getBorrowerById(int id) {
        String sql = "SELECT borrower_id, borrower_name, position, borrower_type FROM borrowers WHERE borrower_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Borrower(
                            rs.getInt("borrower_id"),
                            rs.getString("borrower_name"),
                            rs.getString("position"),
                            rs.getString("borrower_type")
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Insert a borrower
    public boolean insertBorrower(Borrower borrower) {
        String sql = "INSERT INTO borrowers (borrower_name, position, borrower_type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, borrower.getBorrowerName());
            ps.setString(2, borrower.getPosition());
            ps.setString(3, borrower.getBorrowerType());
            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        borrower.setBorrowerId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Update a borrower
    public boolean updateBorrower(Borrower borrower) {
        String sql = "UPDATE borrowers SET borrower_name = ?, position = ?, borrower_type = ? WHERE borrower_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, borrower.getBorrowerName());
            ps.setString(2, borrower.getPosition());
            ps.setString(3, borrower.getBorrowerType());
            ps.setInt(4, borrower.getBorrowerId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Delete a borrower
    public boolean deleteBorrower(int borrowerId) {
        String sql = "DELETE FROM borrowers WHERE borrower_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowerId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            // you may want to catch FK constraint violation here and present a friendly message
            ex.printStackTrace();
        }
        return false;
    }

    // Find borrowers by type (Student/Teacher/Staff)
    public List<Borrower> findByType(String type) {
        List<Borrower> list = new ArrayList<>();
        String sql = "SELECT borrower_id, borrower_name, position, borrower_type FROM borrowers WHERE borrower_type = ? ORDER BY borrower_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Borrower b = new Borrower(
                            rs.getInt("borrower_id"),
                            rs.getString("borrower_name"),
                            rs.getString("position"),
                            rs.getString("borrower_type")
                    );
                    list.add(b);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Search borrowers by name (partial match)
    public List<Borrower> searchByName(String term) {
        List<Borrower> list = new ArrayList<>();
        String sql = "SELECT borrower_id, borrower_name, position, borrower_type FROM borrowers WHERE borrower_name LIKE ? ORDER BY borrower_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + term + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Borrower b = new Borrower(
                            rs.getInt("borrower_id"),
                            rs.getString("borrower_name"),
                            rs.getString("position"),
                            rs.getString("borrower_type")
                    );
                    list.add(b);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Quick test (run this main to test DAO)
    public static void main(String[] args) {
        BorrowerDAO dao = new BorrowerDAO();

        System.out.println("All borrowers:");
        dao.getAllBorrowers().forEach(System.out::println);

        // Insert example
        Borrower b = new Borrower();
        b.setBorrowerName("Test Student");
        b.setPosition("Student");
        b.setBorrowerType("Student");
        if (dao.insertBorrower(b)) {
            System.out.println("Inserted, new id = " + b.getBorrowerId());
        }

        // Search
        System.out.println("Search 'Test':");
        dao.searchByName("Test").forEach(System.out::println);

        // Cleanup for the test
        // dao.deleteBorrower(b.getBorrowerId());
    }
}
