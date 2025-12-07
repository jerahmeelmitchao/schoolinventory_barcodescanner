package inventorysystem.controllers;

import inventorysystem.dao.CategoryDAO;
import inventorysystem.dao.ItemDAO;
import inventorysystem.models.Item;
import java.io.File;
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
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.stage.FileChooser;

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

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Export Reports");
        dialog.setHeaderText("Choose what to export:");

        ButtonType allBtn = new ButtonType("All Items");
        ButtonType borrowedBtn = new ButtonType("Borrowed Items");
        ButtonType missingBtn = new ButtonType("Missing Items");
        ButtonType damagedBtn = new ButtonType("Damaged Items");
        ButtonType borrowersBtn = new ButtonType("Borrowers");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(
                allBtn, borrowedBtn, missingBtn, damagedBtn, borrowersBtn, cancelBtn
        );

        dialog.showAndWait().ifPresent(type -> {
            if (type == allBtn) {
                exportAllItems();
            } else if (type == borrowedBtn) {
                exportBorrowedItems();
            } else if (type == missingBtn) {
                exportMissingItems();
            } else if (type == damagedBtn) {
                exportDamagedItems();
            } else if (type == borrowersBtn) {
                exportBorrowers();
            }
        });
    }

    // ============================
// CSV Helper Methods
// ============================
    private File chooseSaveFile(String suggestedName) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName(suggestedName);
        return fc.showSaveDialog(exportButton.getScene().getWindow());
    }

    private void writeCsv(File file, List<String> lines) throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            for (String line : lines) {
                pw.println(line);
            }
        }
    }

    private String csvSafe(String s) {
        return s == null ? "" : s.replace(",", " ").replace("\n", " ");
    }

    private void exportAllItems() {
        File file = chooseSaveFile("all_items.csv");
        if (file == null) {
            return;
        }

        try {
            List<String> lines = new ArrayList<>();
            lines.add("Item ID,Item Name,Barcode,Category,Unit,Status,Date Acquired,Last Scanned,Storage Location,In-Charge,Added By");

            for (Item it : masterData) {
                lines.add(String.join(",",
                        csvSafe("" + it.getItemId()),
                        csvSafe(it.getItemName()),
                        csvSafe(it.getBarcode()),
                        csvSafe(it.getCategoryName()),
                        csvSafe(it.getUnit()),
                        csvSafe(it.getStatus()),
                        csvSafe(it.getDateAcquired() != null ? it.getDateAcquired().toString() : ""),
                        csvSafe(it.getLastScanned() != null ? it.getLastScanned().toString() : ""),
                        csvSafe(it.getStorageLocation()),
                        csvSafe(it.getInChargeName()),
                        csvSafe(it.getAddedBy())
                ));
            }

            writeCsv(file, lines);
            showAlert("Success", "Export Complete", "All Items exported.");

        } catch (Exception e) {
            showAlert("Error", "Export Failed", e.getMessage());
        }
    }

    private void exportMissingItems() {
        File file = chooseSaveFile("missing_items.csv");
        if (file == null) {
            return;
        }

        try {
            List<String> lines = new ArrayList<>();
            lines.add("Item ID,Item Name,Barcode,Category,Last Scanned,Location,In-Charge");

            for (Item it : masterData) {
                if ("Missing".equalsIgnoreCase(it.getStatus())) {
                    lines.add(String.join(",",
                            csvSafe("" + it.getItemId()),
                            csvSafe(it.getItemName()),
                            csvSafe(it.getBarcode()),
                            csvSafe(it.getCategoryName()),
                            csvSafe(it.getLastScanned() != null ? it.getLastScanned().toString() : ""),
                            csvSafe(it.getStorageLocation()),
                            csvSafe(it.getInChargeName())
                    ));
                }
            }

            writeCsv(file, lines);
            showAlert("Success", "Export Complete", "Missing Items exported.");

        } catch (Exception e) {
            showAlert("Error", "Export Failed", e.getMessage());
        }
    }

    private void exportDamagedItems() {
        File file = chooseSaveFile("damaged_items.csv");
        if (file == null) {
            return;
        }

        try {
            List<String> lines = new ArrayList<>();
            lines.add("Item ID,Item Name,Barcode,Category,Date Acquired,Location,In-Charge");

            for (Item it : masterData) {
                if ("Damaged".equalsIgnoreCase(it.getStatus())) {
                    lines.add(String.join(",",
                            csvSafe("" + it.getItemId()),
                            csvSafe(it.getItemName()),
                            csvSafe(it.getBarcode()),
                            csvSafe(it.getCategoryName()),
                            csvSafe(it.getDateAcquired() != null ? it.getDateAcquired().toString() : ""),
                            csvSafe(it.getStorageLocation()),
                            csvSafe(it.getInChargeName())
                    ));
                }
            }

            writeCsv(file, lines);
            showAlert("Success", "Export Complete", "Damaged Items exported.");

        } catch (Exception e) {
            showAlert("Error", "Export Failed", e.getMessage());
        }
    }

    private void exportBorrowedItems() {
        File file = chooseSaveFile("borrowed_items.csv");
        if (file == null) {
            return;
        }

        try {
            List<String> lines = new ArrayList<>();
            lines.add("Item ID,Item Name,Barcode,Category,Location,In-Charge");

            for (Item it : masterData) {
                if ("Borrowed".equalsIgnoreCase(it.getStatus())) {
                    lines.add(String.join(",",
                            csvSafe("" + it.getItemId()),
                            csvSafe(it.getItemName()),
                            csvSafe(it.getBarcode()),
                            csvSafe(it.getCategoryName()),
                            csvSafe(it.getStorageLocation()),
                            csvSafe(it.getInChargeName())
                    ));
                }
            }

            writeCsv(file, lines);
            showAlert("Success", "Export Complete", "Borrowed Items exported.");

        } catch (Exception e) {
            showAlert("Error", "Export Failed", e.getMessage());
        }
    }

    private void exportBorrowers() {
        File file = chooseSaveFile("borrowers_report.csv");
        if (file == null) {
            return;
        }

        try {
            Class<?> daoClass = Class.forName("inventorysystem.dao.BorrowerDAO");
            Object dao = daoClass.getDeclaredConstructor().newInstance();
            List<?> list = (List<?>) daoClass.getMethod("getAllBorrowers").invoke(dao);

            List<String> lines = new ArrayList<>();
            lines.add("Borrower ID,Borrower Name,Position,Borrower Type");

            for (Object b : list) {
                lines.add(String.join(",",
                        csvSafe("" + b.getClass().getMethod("getBorrowerId").invoke(b)),
                        csvSafe("" + b.getClass().getMethod("getBorrowerName").invoke(b)),
                        csvSafe("" + b.getClass().getMethod("getPosition").invoke(b)),
                        csvSafe("" + b.getClass().getMethod("getBorrowerType").invoke(b))
                ));
            }

            writeCsv(file, lines);
            showAlert("Success", "Export Complete", "Borrowers exported.");

        } catch (ClassNotFoundException e) {
            showAlert("Unavailable", "BorrowerDAO missing", "Cannot export borrower data.");
        } catch (Exception e) {
            showAlert("Error", "Export Failed", e.getMessage());
        }
    }

}
