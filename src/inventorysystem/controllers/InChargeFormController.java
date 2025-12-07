package inventorysystem.controllers;

import inventorysystem.dao.CategoryDAO;
import inventorysystem.dao.InchargeDAO;
import inventorysystem.models.Incharge;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class InChargeFormController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextField nameField, positionField, contactField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Button saveButton;

    private final InchargeDAO dao = new InchargeDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private Map<String, Integer> categories = new HashMap<>();
    private Incharge currentIncharge = null;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        categoryDAO.getAllCategories().forEach(c -> {
            categories.put(c.getCategoryName(), c.getCategoryId());
        });

        categoryComboBox.setItems(FXCollections.observableArrayList(categories.keySet()));
    }

    public void setData(Incharge incharge, Runnable callback) {
        this.currentIncharge = incharge;
        this.onSaveCallback = callback;

        if (incharge == null) {
            titleLabel.setText("Add In-Charge");
        } else {
            titleLabel.setText("Edit In-Charge");

            nameField.setText(incharge.getInchargeName());
            positionField.setText(incharge.getPosition());
            contactField.setText(incharge.getContactInfo());

            String catName = categories.entrySet().stream()
                    .filter(e -> e.getValue() == incharge.getAssignedCategoryId())
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);

            categoryComboBox.setValue(catName);
        }
    }

    @FXML
    private void handleSave() {

        if (!validate()) return;

        if (currentIncharge == null) {
            currentIncharge = new Incharge();
        }

        currentIncharge.setInchargeName(nameField.getText());
        currentIncharge.setPosition(positionField.getText());
        currentIncharge.setContactInfo(contactField.getText());
        currentIncharge.setAssignedCategoryId(categories.get(categoryComboBox.getValue()));

        if (currentIncharge.getInchargeId() == 0) {
            dao.addIncharge(currentIncharge);
        } else {
            dao.updateIncharge(currentIncharge);
        }

        if (onSaveCallback != null) onSaveCallback.run();

        close();
    }

    private void close() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }

    private boolean validate() {
        if (nameField.getText().isEmpty() ||
            positionField.getText().isEmpty() ||
            contactField.getText().isEmpty() ||
            categoryComboBox.getValue() == null) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please fill all fields.");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    private void handleCancel() {
        close();
    }
}
