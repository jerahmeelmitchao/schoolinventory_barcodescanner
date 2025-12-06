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

public class AddItemController {

    @FXML private TextField itemNameField;
    @FXML private TextField barcodeField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField unitField;
    @FXML private DatePicker dateAcquiredPicker;
    @FXML private ComboBox<String> statusComboBox;  // NEW
    @FXML private TextField locationField;
    @FXML private ComboBox<String> inChargeComboBox;
    @FXML private TextField addedByField;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final ObservableList<String> categories = FXCollections.observableArrayList();
    private final ObservableList<String> inCharges = FXCollections.observableArrayList();
    private final Map<String, Integer> inChargeMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadCategories();
        loadInChargeList();
        setupDropdownOptions();

        dateAcquiredPicker.setValue(LocalDate.now());
    }

    /** Load category names */
    private void loadCategories() {
        String sql = "SELECT category_name FROM categories ORDER BY category_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
            categoryComboBox.setItems(categories);

        } catch (SQLException e) {
            showError("Database Error", "Unable to load categories.", e.getMessage());
        }
    }

    /** Load In-Charge list */
    private void loadInChargeList() {
        String sql = "SELECT incharge_id, incharge_name FROM incharge ORDER BY incharge_name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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

    /** Lookup */
    private Integer getInChargeId(String name) {
        return name == null ? null : inChargeMap.get(name);
    }

    /** NEW enum dropdown */
    private void setupDropdownOptions() {
        statusComboBox.setItems(FXCollections.observableArrayList(
                "Available", "Damaged", "Borrowed", "Missing", "Disposed"
        ));
    }

    /** Save item */
    @FXML
    private void handleSave() {
        if (!validateInputs()) return;

        String itemName = itemNameField.getText().trim();
        String barcode = barcodeField.getText().trim();
        String category = categoryComboBox.getValue();
        String unit = unitField.getText().trim();
        LocalDate dateAcquired = dateAcquiredPicker.getValue();
        String status = statusComboBox.getValue();
        String location = locationField.getText().trim();
        String inCharge = inChargeComboBox.getValue();
        String addedBy = addedByField.getText().trim();

        Integer categoryId = getCategoryId(category);
        Integer inChargeId = getInChargeId(inCharge);

        if (categoryId == null || inChargeId == null) {
            showError("Missing Data", "Category or In-Charge not found.", null);
            return;
        }

        String sql = """
            INSERT INTO items (
                item_name, barcode, category_id, unit, date_acquired,
                status, storage_location, incharge_id, added_by
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemName);
            ps.setString(2, barcode);
            ps.setInt(3, categoryId);
            ps.setString(4, unit);
            ps.setDate(5, Date.valueOf(dateAcquired));
            ps.setString(6, status);
            ps.setString(7, location);
            ps.setInt(8, inChargeId);
            ps.setString(9, addedBy);

            if (ps.executeUpdate() > 0) {
                showInfo("Success", "Item successfully added!");
                clearForm();
            }

        } catch (SQLException e) {
            showError("Database Error", "Failed to insert item.", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /** Lookup */
    private Integer getCategoryId(String categoryName) {
        String sql = "SELECT category_id FROM categories WHERE category_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt("category_id");

        } catch (SQLException ignored) {}
        return null;
    }

    /** Validation */
    private boolean validateInputs() {
        if (itemNameField.getText().isEmpty()
                || barcodeField.getText().isEmpty()
                || categoryComboBox.getValue() == null
                || unitField.getText().isEmpty()
                || dateAcquiredPicker.getValue() == null
                || statusComboBox.getValue() == null
                || inChargeComboBox.getValue() == null
                || locationField.getText().isEmpty()) {

            showError("Missing Fields", "Please complete all required fields.", null);
            return false;
        }
        return true;
    }

    private void showError(String title, String header, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void clearForm() {
        itemNameField.clear();
        barcodeField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        unitField.clear();
        statusComboBox.getSelectionModel().clearSelection();
        dateAcquiredPicker.setValue(LocalDate.now());
        locationField.clear();
        inChargeComboBox.getSelectionModel().clearSelection();
        addedByField.clear();
    }
}
