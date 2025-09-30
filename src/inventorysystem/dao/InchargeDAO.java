/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventorysystem.dao;

import inventorysystem.models.Incharge;
import inventorysystem.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InchargeDAO {

    public void addIncharge(Incharge incharge) {
        String sql = "INSERT INTO incharge (incharge_name, position, contact_info, assigned_category_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, incharge.getInchargeName());
            stmt.setString(2, incharge.getPosition());
            stmt.setString(3, incharge.getContactInfo());
            stmt.setInt(4, incharge.getAssignedCategoryId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Incharge> getAllIncharges() {
        List<Incharge> list = new ArrayList<>();
        String sql = "SELECT * FROM incharge";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Incharge i = new Incharge(
                        rs.getInt("incharge_id"),
                        rs.getString("incharge_name"),
                        rs.getString("position"),
                        rs.getString("contact_info"),
                        rs.getInt("assigned_category_id")
                );
                list.add(i);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Incharge getInchargeById(int id) {
        String sql = "SELECT * FROM incharge WHERE incharge_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Incharge(
                            rs.getInt("incharge_id"),
                            rs.getString("incharge_name"),
                            rs.getString("position"),
                            rs.getString("contact_info"),
                            rs.getInt("assigned_category_id")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateIncharge(Incharge incharge) {
        String sql = "UPDATE incharge SET incharge_name=?, position=?, contact_info=?, assigned_category_id=? WHERE incharge_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, incharge.getInchargeName());
            stmt.setString(2, incharge.getPosition());
            stmt.setString(3, incharge.getContactInfo());
            stmt.setInt(4, incharge.getAssignedCategoryId());
            stmt.setInt(5, incharge.getInchargeId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteIncharge(int id) {
        String sql = "DELETE FROM incharge WHERE incharge_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
