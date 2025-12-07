package inventorysystem.controllers;

import inventorysystem.dao.CategoryDAO;
import inventorysystem.dao.ItemDAO;
import inventorysystem.models.Item;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ItemController {

    @FXML
    private TableView<Item> itemTable;
    @FXML
    private TableColumn<Item, String> colItemName, colBarcode, colCategory, colUnit,
            colDateAcquired, colLastScanned, colStatus, colInCharge;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterCategoryComboBox, filterStatusComboBox;
    @FXML
    private Button addButton, updateButton, deleteButton, clearFilterButton;

    private final ItemDAO itemDAO = new ItemDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private ObservableList<Item> masterData = FXCollections.observableArrayList();
    private FilteredList<Item> filteredData;

    private ObservableList<String> categoriesList;
    private Map<String, Integer> categoryMap;

    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCategories();
        loadItems();
        setupFiltering();
    }

    private void setupTableColumns() {
        colItemName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getItemName()));
        colBarcode.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getBarcode()));
        colUnit.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUnit()));

        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus()));

        colInCharge.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getInChargeName()));

        colCategory.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCategoryName()));

        colDateAcquired.setCellValueFactory(cd -> {
            LocalDate date = cd.getValue().getDateAcquired();
            String formatted = date != null ? date.format(displayFormatter) : "—";
            return new SimpleStringProperty(formatted);
        });

        colLastScanned.setCellValueFactory(cd -> {
            LocalDate date = cd.getValue().getLastScanned();
            String formatted = date != null ? date.format(displayFormatter) : "—";
            return new SimpleStringProperty(formatted);
        });
    }

    private void loadCategories() {
        categoriesList = FXCollections.observableArrayList();
        categoryMap = new HashMap<>();

        categoryDAO.getAllCategories().forEach(cat -> {
            categoriesList.add(cat.getCategoryName());
            categoryMap.put(cat.getCategoryName(), cat.getCategoryId());
        });

        filterCategoryComboBox.setItems(categoriesList);
    }

    private void loadItems() {
        masterData.setAll(itemDAO.getAllItems());
        itemTable.setItems(masterData);
    }

    @FXML
    private void handleAddItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/inventorysystem/views/add_item.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Item");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadItems();
        } catch (IOException e) {
            showAlert("Error", "Failed to open Add Item form.", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Item selected = itemTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Warning", "No item selected", "Select an item to update.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/inventorysystem/views/update_item.fxml"));
            Parent root = loader.load();

            UpdateItemController controller = loader.getController();

            controller.setItemData(
                    selected.getItemId(),
                    selected.getItemName(),
                    selected.getBarcode(),
                    selected.getCategoryName(),
                    selected.getUnit(),
                    selected.getDateAcquired(),
                    selected.getStatus(), // NEW
                    selected.getStorageLocation(),
                    selected.getInChargeName(),
                    selected.getAddedBy()
            );

            Stage stage = new Stage();
            stage.setTitle("Update Item");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadItems();
        } catch (Exception e) {
            showAlert("Error", "Failed to open Update Item form.", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Item selected = itemTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Warning", "No Selection", "Please select an item to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Item");
        confirm.setContentText("Are you sure you want to delete '" + selected.getItemName() + "'?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    itemDAO.deleteItem(selected.getItemId());
                    loadItems();
                    showAlert("Success", "Item deleted successfully", "");
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete item.", e.getMessage());
                }
            }
        });
    }

    private void setupFiltering() {
        filteredData = new FilteredList<>(masterData, p -> true);

        filterStatusComboBox.setItems(FXCollections.observableArrayList(
                "Available", "Damaged", "Borrowed", "Missing", "Disposed"
        ));

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterCategoryComboBox.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterStatusComboBox.valueProperty().addListener((obs, o, n) -> applyFilters());

        itemTable.setItems(filteredData);
    }

    private void applyFilters() {
        String search = safe(searchField.getText()).toLowerCase();
        String category = filterCategoryComboBox.getValue();
        String status = filterStatusComboBox.getValue();

        filteredData.setPredicate(item -> {
            if (item == null) {
                return false;
            }

            String combined = String.join(" ",
                    safe(item.getItemName()),
                    safe(item.getBarcode()),
                    safe(item.getCategoryName()),
                    safe(item.getStatus()),
                    safe(item.getStorageLocation()),
                    safe(item.getInChargeName()),
                    safe(item.getAddedBy())
            ).toLowerCase();

            boolean matchesSearch = search.isEmpty() || combined.contains(search);
            boolean matchesCategory = category == null || category.equalsIgnoreCase(item.getCategoryName());
            boolean matchesStatus = status == null || status.equalsIgnoreCase(item.getStatus());

            return matchesSearch && matchesCategory && matchesStatus;
        });
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterCategoryComboBox.getSelectionModel().clearSelection();
        filterStatusComboBox.getSelectionModel().clearSelection();
        applyFilters();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void showAlert(String title, String header, String content) {
        Alert.AlertType type = switch (title.toLowerCase()) {
            case "error" ->
                Alert.AlertType.ERROR;
            case "warning" ->
                Alert.AlertType.WARNING;
            case "success", "info" ->
                Alert.AlertType.INFORMATION;
            default ->
                Alert.AlertType.NONE;
        };

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private Button exportButton;

    @FXML
    private void handleExport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText("Exporting Items");
        alert.setContentText("Export functionality coming soon!");
        alert.showAndWait();

        // TODO: Add Excel / CSV export code here
    }

}
