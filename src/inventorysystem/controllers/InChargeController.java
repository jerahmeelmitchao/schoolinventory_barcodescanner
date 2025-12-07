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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InChargeController {

    @FXML
    private TableView<Incharge> inchargeTable;

    @FXML
    private TableColumn<Incharge, Integer> colId;
    @FXML
    private TableColumn<Incharge, String> colName, colPosition, colContact, colAssignedCategory;

    @FXML
    private Button addButton, editButton, deleteButton;

    private final InchargeDAO dao = new InchargeDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Incharge> inchargeList;

    private Map<Integer, String> categoryMap;  // id -> name

    @FXML
    public void initialize() {

        // Load categories
        categoryMap = new HashMap<>();
        categoryDAO.getAllCategories().forEach(cat ->
                categoryMap.put(cat.getCategoryId(), cat.getCategoryName()));

        // Bind column data
        colId.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getInchargeId()).asObject());
        colName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getInchargeName()));
        colPosition.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getPosition()));
        colContact.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getContactInfo()));
        colAssignedCategory.setCellValueFactory(cd -> 
                new SimpleStringProperty(categoryMap.getOrDefault(cd.getValue().getAssignedCategoryId(), "None")));

        loadIncharges();

        // Enable/disable buttons when selecting rows
        inchargeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            editButton.setDisable(!selected);
            deleteButton.setDisable(!selected);
        });
    }

    private void loadIncharges() {
        inchargeList = FXCollections.observableArrayList(dao.getAllIncharges());
        inchargeTable.setItems(inchargeList);
    }

    @FXML
    private void openAddPopup() {
        openFormPopup(null); // passing null → add mode
    }

    @FXML
    private void openEditPopup() {
        Incharge selected = inchargeTable.getSelectionModel().getSelectedItem();
        if (selected != null) openFormPopup(selected);
    }

    private void openFormPopup(Incharge incharge) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/inventorysystem/views/incharge_form.fxml"));
            Parent form = loader.load();

            InChargeFormController controller = loader.getController();
            controller.setData(incharge, this::loadIncharges);  // callback after save

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(incharge == null ? "Add In-Charge" : "Edit In-Charge");
            stage.setScene(new Scene(form));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Incharge selected = inchargeTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selected.getInchargeName() + "?",
                ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                dao.deleteIncharge(selected.getInchargeId());
                loadIncharges();
            }
        });
    }
}
