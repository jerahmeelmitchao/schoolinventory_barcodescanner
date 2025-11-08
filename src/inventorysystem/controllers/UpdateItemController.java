package inventorysystem.controllers;

import inventorysystem.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class UpdateItemController {

    @FXML private TextField itemNameField;
    @FXML private TextField barcodeField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField quantityField;
    @FXML private TextField unitField;
    @FXML private DatePicker dateAcquiredPicker;
    @FXML private ComboBox<String> serviceabilityComboBox;
    @FXML private ComboBox<String> conditionComboBox;
    @FXML private ComboBox<String> availabilityComboBox;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> inChargeComboBox;
    @FXML private TextField addedByField;
    @FXML private Button updateButton;
    @FXML private Button cancelButton;

    private final ObservableList<String> categories = FXCollections.observableArrayList();
    private final ObservableList<String> inCharges = FXCollections.observableArrayList();
    private final Map<String, Integer> inChargeMap = new HashMap<>();
    private int itemId;

    @FXML
    public void initialize() {
        loadCategories();
        loadInChargeList();
        setupDropdownOptions();
    }

    public void setItemData(int itemId, String itemName, String barcode, String category, int quantity, String unit,
                            LocalDate dateAcquired, String serviceability, String condition, String availability,
                            String location, String inCharge, String addedBy) {

        this.itemId = itemId;
        itemNameField.setText(itemName);
        barcodeField.setText(barcode);
        categoryComboBox.setValue(category);
        quantityField.setText(String.valueOf(quantity));
        unitField.setText(unit);
        dateAcquiredPicker.setValue(dateAcquired);
        serviceabilityComboBox.setValue(serviceability);
        conditionComboBox.setValue(condition);
        availabilityComboBox.setValue(availability);
        locationField.setText(location);
        inChargeComboBox.setValue(inCharge);
        addedByField.setText(addedBy);
    }

    private void loadCategories() {
        String sql = "SELECT category_name FROM categories ORDER BY category_name ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) categories.add(rs.getString("category_name"));
            categoryComboBox.setItems(categories);
        } catch (SQLException e) {
            showError("Database Error", "Unable to load categories.", e.getMessage());
        }
    }

    private void loadInChargeList() {
        String sql = "SELECT incharge_id, incharge_name FROM incharge ORDER BY incharge_name ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            inCharges.clear();
            inChargeMap.clear();
            while (rs.next()) {
                int id = rs.getInt("incharge_id");
                String name = rs.getString("incharge_name");
                inCharges.add(name);
                inChargeMap.put(name, id);
            }
            inChargeComboBox.setItems(inCharges);
        } catch (SQLException e) {
            showError("Database Error", "Unable to load In-Charge list.", e.getMessage());
        }
    }

    private void setupDropdownOptions() {
        serviceabilityComboBox.setItems(FXCollections.observableArrayList("Serviceable", "Unserviceable"));
        conditionComboBox.setItems(FXCollections.observableArrayList("OK", "Damaged", "Disposed"));
        availabilityComboBox.setItems(FXCollections.observableArrayList("Available", "Unavailable"));
    }

    @FXML
    private void handleUpdate() {
        if (!validateInputs()) return;

        String sql = """
            UPDATE items SET
                item_name = ?, barcode = ?, category_id = ?, quantity = ?, unit = ?, date_acquired = ?,
                serviceability_status = ?, condition_status = ?, availability_status = ?, storage_location = ?,
                incharge_id = ?, added_by = ?
            WHERE item_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemNameField.getText().trim());
            ps.setString(2, barcodeField.getText().trim());
            ps.setInt(3, getCategoryId(categoryComboBox.getValue()));
            ps.setInt(4, Integer.parseInt(quantityField.getText().trim()));
            ps.setString(5, unitField.getText().trim());
            ps.setDate(6, Date.valueOf(dateAcquiredPicker.getValue()));
            ps.setString(7, serviceabilityComboBox.getValue());
            ps.setString(8, conditionComboBox.getValue());
            ps.setString(9, availabilityComboBox.getValue());
            ps.setString(10, locationField.getText().trim());
            ps.setInt(11, inChargeMap.get(inChargeComboBox.getValue()));
            ps.setString(12, addedByField.getText().trim());
            ps.setInt(13, itemId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                showInfo("Success", "Item successfully updated!");
                closeWindow();
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to update item.", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private Integer getCategoryId(String categoryName) {
        String sql = "SELECT category_id FROM categories WHERE category_name = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("category_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private boolean validateInputs() {
        if (itemNameField.getText().isEmpty() || barcodeField.getText().isEmpty() || categoryComboBox.getValue() == null
            || quantityField.getText().isEmpty() || unitField.getText().isEmpty() || dateAcquiredPicker.getValue() == null
            || serviceabilityComboBox.getValue() == null || conditionComboBox.getValue() == null
            || availabilityComboBox.getValue() == null || inChargeComboBox.getValue() == null || locationField.getText().isEmpty()) {
            showError("Missing Fields", "Please fill in all required fields.", null);
            return false;
        }
        return true;
    }

    private void showError(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(header);
        if (message != null) alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
