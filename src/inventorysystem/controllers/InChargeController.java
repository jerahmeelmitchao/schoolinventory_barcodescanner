package inventorysystem.controllers;

import inventorysystem.dao.CategoryDAO;
import inventorysystem.dao.InchargeDAO;
import inventorysystem.models.Category;
import inventorysystem.models.Incharge;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.Map;

public class InChargeController {

    @FXML
    private TextField nameField, positionField, contactField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Button addButton, updateButton, deleteButton, cancelButton;

    @FXML
    private TableView<Incharge> inchargeTable;

    @FXML
    private TableColumn<Incharge, Integer> colId;
    @FXML
    private TableColumn<Incharge, String> colName, colPosition, colContact, colAssignedCategory;

    private final InchargeDAO dao = new InchargeDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Incharge> inchargeList;
    private Map<Integer, String> categoryMap;  // id -> name
    private Map<String, Integer> reverseCategoryMap; // name -> id

    @FXML
    public void initialize() {
        // Load categories into maps
        categoryMap = new HashMap<>();
        reverseCategoryMap = new HashMap<>();
        categoryDAO.getAllCategories().forEach(cat -> {
            categoryMap.put(cat.getCategoryId(), cat.getCategoryName());
            reverseCategoryMap.put(cat.getCategoryName(), cat.getCategoryId());
        });

        // Populate ComboBox
        categoryComboBox.setItems(FXCollections.observableArrayList(reverseCategoryMap.keySet()));

        // Bind columns
        colId.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getInchargeId()).asObject());
        colName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getInchargeName()));
        colPosition.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getPosition()));
        colContact.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getContactInfo()));
        colAssignedCategory.setCellValueFactory(cd -> {
            int catId = cd.getValue().getAssignedCategoryId();
            String catName = categoryMap.getOrDefault(catId, "Unknown");
            return new SimpleStringProperty(catName);
        });

        // Load table data
        loadIncharges();

        // Populate fields when a row is selected
        inchargeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateFields(newSel);
                addButton.setDisable(true);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
                cancelButton.setVisible(true);
            } else {
                clearFields();
            }
        });
    }

    private void loadIncharges() {
        inchargeList = FXCollections.observableArrayList(dao.getAllIncharges());
        inchargeTable.setItems(inchargeList);
    }

    private void populateFields(Incharge i) {
        nameField.setText(i.getInchargeName());
        positionField.setText(i.getPosition());
        contactField.setText(i.getContactInfo());
        String categoryName = categoryMap.get(i.getAssignedCategoryId());
        categoryComboBox.setValue(categoryName);
    }

    private void clearFields() {
        nameField.clear();
        positionField.clear();
        contactField.clear();
        categoryComboBox.getSelectionModel().clearSelection();

        inchargeTable.getSelectionModel().clearSelection();
        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        cancelButton.setVisible(false);
    }

    @FXML
    private void handleAdd() {
        if (!allFieldsValid()) return;

        Incharge i = new Incharge();
        i.setInchargeName(nameField.getText());
        i.setPosition(positionField.getText());
        i.setContactInfo(contactField.getText());
        i.setAssignedCategoryId(reverseCategoryMap.get(categoryComboBox.getValue()));

        dao.addIncharge(i);
        loadIncharges();
        clearFields();
    }

    @FXML
    private void handleUpdate() {
        Incharge selected = inchargeTable.getSelectionModel().getSelectedItem();
        if (selected == null || !allFieldsValid()) return;

        selected.setInchargeName(nameField.getText());
        selected.setPosition(positionField.getText());
        selected.setContactInfo(contactField.getText());
        selected.setAssignedCategoryId(reverseCategoryMap.get(categoryComboBox.getValue()));

        dao.updateIncharge(selected);
        loadIncharges();
        clearFields();
    }

    @FXML
    private void handleDelete() {
        Incharge selected = inchargeTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        dao.deleteIncharge(selected.getInchargeId());
        loadIncharges();
        clearFields();
    }

    @FXML
    private void handleCancel() {
        clearFields();
    }

    private boolean allFieldsValid() {
        if (nameField.getText().isEmpty() || positionField.getText().isEmpty()
                || contactField.getText().isEmpty() || categoryComboBox.getValue() == null) {
            showAlert("Validation Error", "Please fill all fields.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
