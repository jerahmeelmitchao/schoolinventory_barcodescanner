package inventorysystem.controllers;

import inventorysystem.dao.BorrowerDAO;
import inventorysystem.dao.BorrowRecordDAO;
import inventorysystem.dao.ItemDAO;
import inventorysystem.models.BorrowRecord;
import inventorysystem.models.Borrower;
import inventorysystem.models.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BorrowerController {

    @FXML
    private TableView<Borrower> borrowerTable;
    @FXML
    private TableColumn<Borrower, Integer> colId;
    @FXML
    private TableColumn<Borrower, String> colName;
    @FXML
    private TableColumn<Borrower, String> colPosition;
    @FXML
    private TableColumn<Borrower, String> colType;
    @FXML
    private TableColumn<Borrower, Void> colActions;

    @FXML
    private TextField nameField;
    @FXML
    private TextField positionField;
    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button cancelButton;

    private final BorrowerDAO borrowerDAO = new BorrowerDAO();
    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
    private final ItemDAO itemDAO = new ItemDAO();

    private ObservableList<Borrower> borrowerList;
    private Borrower selectedBorrower;

    @FXML
    public void initialize() {
        // Setup table columns
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getBorrowerId()).asObject());
        colName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBorrowerName()));
        colPosition.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPosition()));
        colType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBorrowerType()));

        // Setup type options
        typeComboBox.setItems(FXCollections.observableArrayList("Student", "Teacher", "Staff"));

        // Action buttons column
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button borrowBtn = new Button("Borrow");
            private final Button returnBtn = new Button("Return");
            private final HBox box = new HBox(10, borrowBtn, returnBtn);

            {
                borrowBtn.setOnAction(e -> {
                    Borrower borrower = getTableView().getItems().get(getIndex());
                    openBorrowPanel(borrower);
                });

                returnBtn.setOnAction(e -> {
                    Borrower borrower = getTableView().getItems().get(getIndex());
                    openReturnPanel(borrower);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Borrower borrower = getTableView().getItems().get(getIndex());
                    boolean hasBorrowed = !borrowRecordDAO.getBorrowRecordsByBorrower(borrower.getBorrowerId())
                            .stream()
                            .filter(r -> r.getStatus().equals("Borrowed"))
                            .toList()
                            .isEmpty();

                    returnBtn.setDisable(!hasBorrowed);
                    setGraphic(box);
                }
            }
        });

        // Table row double-click
        borrowerTable.setRowFactory(tv -> {
            TableRow<Borrower> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    showBorrowerDetails(row.getItem());
                }
            });
            return row;
        });

        // Load data
        loadBorrowers();

        // Selection listener
        borrowerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedBorrower = newSel;
                nameField.setText(newSel.getBorrowerName());
                positionField.setText(newSel.getPosition());
                typeComboBox.setValue(newSel.getBorrowerType());

                addButton.setDisable(true);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
                cancelButton.setVisible(true);
            }
        });

        handleCancel(); // reset buttons
    }

    private void loadBorrowers() {
        borrowerList = FXCollections.observableArrayList(borrowerDAO.getAllBorrowers());
        borrowerTable.setItems(borrowerList);
    }

    @FXML
    private void handleAdd() {
        String name = nameField.getText();
        String position = positionField.getText();
        String type = typeComboBox.getValue();

        if (name.isEmpty() || position.isEmpty() || type == null) {
            showAlert("Validation Error", "All fields are required!");
            return;
        }

        Borrower borrower = new Borrower(0, name, position, type);
        if (borrowerDAO.insertBorrower(borrower)) {
            borrowerList.add(borrower);
            handleCancel();
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedBorrower == null) {
            return;
        }

        selectedBorrower.setBorrowerName(nameField.getText());
        selectedBorrower.setPosition(positionField.getText());
        selectedBorrower.setBorrowerType(typeComboBox.getValue());

        if (borrowerDAO.updateBorrower(selectedBorrower)) {
            borrowerTable.refresh();
            handleCancel();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedBorrower == null) {
            return;
        }

        if (borrowerDAO.deleteBorrower(selectedBorrower.getBorrowerId())) {
            borrowerList.remove(selectedBorrower);
            handleCancel();
        }
    }

    @FXML
    private void handleCancel() {
        nameField.clear();
        positionField.clear();
        typeComboBox.setValue(null);
        borrowerTable.getSelectionModel().clearSelection();
        selectedBorrower = null;

        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        cancelButton.setVisible(false);
    }

    // Borrow panel
    private void openBorrowPanel(Borrower borrower) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Borrow Item");

        TextField searchField = new TextField();
        searchField.setPromptText("Search item...");

        ListView<Item> itemListView = new ListView<>();
        ObservableList<Item> allItems = FXCollections.observableArrayList(itemDAO.getAllItems());
        itemListView.setItems(allItems);

        searchField.textProperty().addListener((obs, old, nw) -> {
            List<Item> filtered = allItems.stream()
                    .filter(i -> i.getItemName().toLowerCase().contains(nw.toLowerCase()))
                    .collect(Collectors.toList());
            itemListView.setItems(FXCollections.observableArrayList(filtered));
        });

        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, 1);

        VBox content = new VBox(10, searchField, itemListView, new Label("Quantity:"), qtySpinner);
        dialog.getDialogPane().setContent(content);

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == saveBtn) {
                Item selected = itemListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int qty = qtySpinner.getValue();
                    if (itemDAO.decreaseQuantity(selected.getItemId(), qty)) {
                        BorrowRecord record = new BorrowRecord(
                                0,
                                selected.getItemId(),
                                borrower.getBorrowerId(),
                                LocalDate.now(),
                                null,
                                qty,
                                "Borrowed"
                        );
                        borrowRecordDAO.addBorrowRecord(record);
                        new Alert(Alert.AlertType.INFORMATION, "Item borrowed successfully!").showAndWait();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Not enough stock!").showAndWait();
                    }
                }
            }
            return null;
        });

        dialog.showAndWait();
        borrowerTable.refresh();
    }

    // Return panel
    private void openReturnPanel(Borrower borrower) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Return Item");

        List<BorrowRecord> active = borrowRecordDAO.getBorrowRecordsByBorrower(borrower.getBorrowerId())
                .stream().filter(r -> r.getStatus().equals("Borrowed")).toList();

        ListView<BorrowRecord> recordListView = new ListView<>(FXCollections.observableArrayList(active));
        TextArea remarksArea = new TextArea();
        remarksArea.setPromptText("Remarks...");

        VBox content = new VBox(10, recordListView, new Label("Remarks:"), remarksArea);
        dialog.getDialogPane().setContent(content);

        ButtonType saveBtn = new ButtonType("Return", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == saveBtn) {
                BorrowRecord selected = recordListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    borrowRecordDAO.returnBorrowRecord(selected.getRecordId(), LocalDate.now(), remarksArea.getText());
                    itemDAO.increaseQuantity(selected.getItemId(), selected.getQuantityBorrowed());
                    new Alert(Alert.AlertType.INFORMATION, "Item returned successfully!").showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
        borrowerTable.refresh();
    }

    // Borrower details
    private void showBorrowerDetails(Borrower borrower) {
        List<BorrowRecord> records = borrowRecordDAO.getBorrowRecordsByBorrower(borrower.getBorrowerId());

        StringBuilder sb = new StringBuilder();
        sb.append("Borrower: ").append(borrower.getBorrowerName()).append("\n")
                .append("Position: ").append(borrower.getPosition()).append("\n")
                .append("Type: ").append(borrower.getBorrowerType()).append("\n\n");

        for (BorrowRecord r : records) {
            sb.append("Item ID: ").append(r.getItemId())
                    .append(", Qty: ").append(r.getQuantityBorrowed())
                    .append(", Status: ").append(r.getStatus())
                    .append(", Borrowed: ").append(r.getBorrowDate())
                    .append(", Returned: ").append(r.getReturnDate() != null ? r.getReturnDate() : "-")
                    .append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
        alert.setTitle("Borrower Details");
        alert.setHeaderText("Borrower Information");
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
