package inventorysystem.controllers;

import inventorysystem.dao.CategoryDAO;
import inventorysystem.dao.ItemDAO;
import inventorysystem.models.Item;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javafx.event.ActionEvent;


public class ItemController {

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
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button cancelButton;

    @FXML
    private TableView<Item> itemTable;
    @FXML
    private TableColumn<Item, String> colItemName;
    @FXML
    private TableColumn<Item, String> colBarcode;
    @FXML
    private TableColumn<Item, Integer> colQuantity;
    @FXML
    private TableColumn<Item, String> colUnit;
    @FXML
    private TableColumn<Item, LocalDate> colDateAcquired;
    @FXML
    private TableColumn<Item, String> colServiceability;
    @FXML
    private TableColumn<Item, String> colAvailability;
    

    private final ItemDAO itemDAO = new ItemDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Item> itemList;
    private ObservableList<String> categoriesList;
    private Map<String, Integer> categoryMap;

    @FXML
    public void initialize() {
        // Bind table columns
        colItemName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getItemName()));
        colBarcode.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getBarcode()));
       // colBarcode.setCellFactory(column -> new BarcodeTableCell());
        colQuantity.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getQuantity()).asObject());
        colUnit.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUnit()));
        colDateAcquired.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getDateAcquired()));
        colServiceability.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getServiceabilityStatus()));
        colAvailability.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAvailabilityStatus()));
     
        // Load categories and items
        loadCategories();
        loadItems();

        // Initially disable update/delete buttons
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        // Numeric-only input for quantity
        quantityField.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (!e.getCharacter().matches("\\d")) {
                e.consume();
            }
        });

        // Populate fields when selecting a row
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateFields(newSel);
                // Disable Add when editing
                addButton.setDisable(true);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
                cancelButton.setVisible(true); 
            } else {
                clearFields();
            }
        });
    }

    private void loadCategories() {
        categoriesList = FXCollections.observableArrayList();
        categoryMap = new HashMap<>();

        categoryDAO.getAllCategories().forEach(cat -> {
            categoriesList.add(cat.getCategoryName());
            categoryMap.put(cat.getCategoryName(), cat.getCategoryId());
        });

        categoryComboBox.setItems(categoriesList);
        if (!categoriesList.isEmpty()) {
            categoryComboBox.getSelectionModel().selectFirst();
        }
    }

    private void loadItems() {
        itemList = FXCollections.observableArrayList(itemDAO.getAllItems());
        itemTable.setItems(itemList);
    }

    private void populateFields(Item item) {
        itemNameField.setText(item.getItemName());
        barcodeField.setText(item.getBarcode());
        quantityField.setText(String.valueOf(item.getQuantity()));
        unitField.setText(item.getUnit());
        categoryComboBox.getSelectionModel().select(
                categoriesList.stream()
                        .filter(name -> categoryMap.get(name) == item.getCategoryId())
                        .findFirst()
                        .orElse(null)
        );
    }

    @FXML
    private void handleCancel() {
        clearFields(); // reset everything
//        showConfirmation("Action cancelled. Ready to add a new item.");
        cancelButton.setVisible(false);
    }

    @FXML
    private void handleAddItem() {
        String itemName = itemNameField.getText();
        String barcode = barcodeField.getText();
        String unit = unitField.getText();
        String categoryName = categoryComboBox.getSelectionModel().getSelectedItem();

        // Validation
        if (itemName.isEmpty() || barcode.isEmpty() || unit.isEmpty()
                || categoryName == null || quantityField.getText().isEmpty()) {
            showAlert("Validation Error", "Please fill all fields.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Quantity must be a number.");
            return;
        }

        int categoryId = categoryMap.get(categoryName);

        // Debug log
        System.out.println("👉 Adding Item: " + itemName
                + ", CategoryID=" + categoryId
                + ", Quantity=" + quantity
                + ", InchargeID=1");

        Item newItem = new Item(
                0,
                sanitizeInput(itemName),
                sanitizeInput(barcode),
                categoryId,
                quantity,
                sanitizeInput(unit),
                LocalDate.now(),
                "Serviceable",
                "Available",
                1 // TODO: Replace with selected incharge later
        );

        try {
            boolean success = itemDAO.addItem(newItem);
            if (success) {
                showConfirmation("Item Added Successfully!");
                loadItems();
                clearFields();
            } else {
                showAlert("Database Error", "Failed to add item. Check logs for details.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an item to update.");
            return;
        }
        if (!allFieldsValid()) {
            return;
        }

        try {
            int categoryId = categoryMap.get(categoryComboBox.getSelectionModel().getSelectedItem());

            selected.setItemName(sanitizeInput(itemNameField.getText()));
            selected.setBarcode(sanitizeInput(barcodeField.getText()));
            selected.setCategoryId(categoryId);
            selected.setQuantity(Integer.parseInt(quantityField.getText()));
            selected.setUnit(sanitizeInput(unitField.getText()));

            itemDAO.updateItem(selected);
            loadItems();
            clearFields();
            showConfirmation("Item updated successfully!");
            cancelButton.setVisible(false);
        } catch (Exception e) {
            showAlert("Error", "Failed to update item: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an item to delete.");
            return;
        }

        try {
            itemDAO.deleteItem(selected.getItemId());
            loadItems();
            clearFields();
            showConfirmation("Item deleted successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to delete item: " + e.getMessage());
        }
    }

    private void clearFields() {
        itemNameField.clear();
        barcodeField.clear();
        quantityField.clear();
        unitField.clear();

        if (!categoriesList.isEmpty()) {
            categoryComboBox.getSelectionModel().selectFirst();
        }

        itemTable.getSelectionModel().clearSelection();

        // Reset button states
        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private boolean allFieldsValid() {
        if (itemNameField.getText().isEmpty()
                || barcodeField.getText().isEmpty()
                || quantityField.getText().isEmpty()
                || unitField.getText().isEmpty()
                || categoryComboBox.getSelectionModel().getSelectedItem() == null) {
            showAlert("Validation Error", "Please fill all fields.");
            return false;
        }
        return true;
    }

    private String sanitizeInput(String input) {
        return input.replaceAll("[<>\"'%;()&+]", "");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
