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

    @FXML
    private TextField itemNameField;
    @FXML
    private TextField barcodeField;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField unitField;
    @FXML
    private DatePicker dateAcquiredPicker;
    @FXML
    private ComboBox<String> serviceabilityComboBox;
    @FXML
    private ComboBox<String> conditionComboBox;
    @FXML
    private ComboBox<String> availabilityComboBox;
    @FXML
    private TextField locationField;
    @FXML
    private ComboBox<String> inChargeComboBox;
    @FXML
    private TextField addedByField;

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private final ObservableList<String> categories = FXCollections.observableArrayList();
    private final ObservableList<String> inCharges = FXCollections.observableArrayList();
    // map displayName -> incharge_id for easy lookup when saving
    private final Map<String, Integer> inChargeMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadCategories();
        loadInChargeList();
        setupDropdownOptions();

        // Set default date
        dateAcquiredPicker.setValue(LocalDate.now());
    }

    /**
     * Load categories from database
     */
    private void loadCategories() {
        String sql = "SELECT category_name FROM categories ORDER BY category_name ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
            categoryComboBox.setItems(categories);

        } catch (SQLException e) {
            showError("Database Error", "Unable to load categories.", e.getMessage());
        }
    }

    /**
     * Load list of people in charge from database (loads id + name)
     */
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

    /**
     * Helper: Get incharge_id by person name using the cached map
     */
    private Integer getInChargeId(String name) {
        if (name == null) {
            return null;
        }
        return inChargeMap.get(name); // may be null if name not in map
    }

    /**
     * Set up dropdown values for predefined status fields
     */
    private void setupDropdownOptions() {
        serviceabilityComboBox.setItems(FXCollections.observableArrayList("Serviceable", "Unserviceable"));
        conditionComboBox.setItems(FXCollections.observableArrayList("OK", "Damaged", "Disposed"));
        availabilityComboBox.setItems(FXCollections.observableArrayList("Available", "Unavailable"));
    }

    /**
     * Called when Save Item button is clicked
     */
    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        String itemName = itemNameField.getText().trim();
        String barcode = barcodeField.getText().trim();
        String category = categoryComboBox.getValue();
        String quantityText = quantityField.getText().trim();
        String unit = unitField.getText().trim();
        LocalDate dateAcquired = dateAcquiredPicker.getValue();
        String serviceability = serviceabilityComboBox.getValue();
        String condition = conditionComboBox.getValue();
        String availability = availabilityComboBox.getValue();
        String location = locationField.getText().trim();
        String inCharge = inChargeComboBox.getValue();
        String addedBy = addedByField.getText().trim();

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Quantity must be a valid number.", null);
            return;
        }

        // Get corresponding category_id and incharge_id
        Integer categoryId = getCategoryId(category);
        Integer inChargeId = getInChargeId(inCharge);

        if (categoryId == null || inChargeId == null) {
            showError("Missing Data", "Category or In-Charge could not be found.", null);
            return;
        }

        String sql = """
            INSERT INTO items (
                item_name, barcode, category_id, quantity, unit, date_acquired, 
                serviceability_status, condition_status, availability_status, 
                storage_location, incharge_id, added_by
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemName);
            ps.setString(2, barcode);
            ps.setInt(3, categoryId);
            ps.setInt(4, quantity);
            ps.setString(5, unit);
            ps.setDate(6, Date.valueOf(dateAcquired));
            ps.setString(7, serviceability);
            ps.setString(8, condition);
            ps.setString(9, availability);
            ps.setString(10, location);
            ps.setInt(11, inChargeId);
            ps.setString(12, addedBy);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                showInfo("Success", "Item successfully added to the inventory!");
                clearForm();
            }

        } catch (SQLException e) {
            showError("Database Error", "Failed to insert item.", e.getMessage());
        }
    }

    /**
     * Cancel and close the Add Item window
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Helper: Get category_id by name
     */
    private Integer getCategoryId(String categoryName) {
        String sql = "SELECT category_id FROM categories WHERE category_name = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("category_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

  
    /**
     * Validate required inputs
     */
    private boolean validateInputs() {
        if (itemNameField.getText().isEmpty()
                || barcodeField.getText().isEmpty()
                || categoryComboBox.getValue() == null
                || quantityField.getText().isEmpty()
                || unitField.getText().isEmpty()
                || dateAcquiredPicker.getValue() == null
                || serviceabilityComboBox.getValue() == null
                || conditionComboBox.getValue() == null
                || availabilityComboBox.getValue() == null
                || inChargeComboBox.getValue() == null
                || locationField.getText().isEmpty()) {

            showError("Missing Fields", "Please fill in all required fields.", null);
            return false;
        }
        return true;
    }

    /**
     * Show error dialog
     */
    private void showError(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        if (message != null) {
            alert.setContentText(message);
        }
        alert.showAndWait();
    }

    /**
     * Show info dialog
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Clear all form inputs
     */
    private void clearForm() {
        itemNameField.clear();
        barcodeField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        quantityField.clear();
        unitField.clear();
        dateAcquiredPicker.setValue(LocalDate.now());
        serviceabilityComboBox.getSelectionModel().clearSelection();
        conditionComboBox.getSelectionModel().clearSelection();
        availabilityComboBox.getSelectionModel().clearSelection();
        locationField.clear();
        inChargeComboBox.getSelectionModel().clearSelection();
        addedByField.clear();
    }
}
