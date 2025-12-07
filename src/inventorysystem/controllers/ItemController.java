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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

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
    private static String loggedUsername;  // set from LoginController when user logs in

    private static String loggedFirstName = "";
    private static String loggedLastName = "";

// Call these from LoginController after login
    public static void setLoggedUserNames(String first, String last) {
        loggedFirstName = first;
        loggedLastName = last;
    }

    public static String getLoggedFullName() {
        return loggedFirstName + " " + loggedLastName;
    }

    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCategories();
        loadItems();
        setupFiltering();
//        loadLoggedUserInfo();

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
        dialog.initStyle(javafx.stage.StageStyle.UNDECORATED);
        dialog.setTitle(null);
        dialog.setHeaderText(null);

        VBox box = new VBox(14);
        box.setStyle(
                "-fx-background-color: white;"
                + "-fx-padding: 20;"
                + "-fx-border-radius: 12;"
                + "-fx-background-radius: 12;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 4);"
        );

        Label title = new Label("📄 Export Reports");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Choose what type of report you want to download:");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        box.getChildren().addAll(title, subtitle);

        VBox options = new VBox(10);
        options.getChildren().addAll(
                makeOption("📦  All Items", () -> chooseFormat("all")),
                makeOption("🔍  Filtered Items", () -> chooseFormat("filtered")),
                makeOption("📤  Borrowed Items", () -> chooseFormat("borrowed")),
                makeOption("❓  Missing Items", () -> chooseFormat("missing")),
                makeOption("💢  Damaged Items", () -> chooseFormat("damaged")),
                makeOption("🧑‍🤝‍🧑 Borrowers", () -> chooseFormat("borrowers"))
        );

        box.getChildren().add(options);

        Button closeBtn = new Button("Close");
        closeBtn.setStyle(
                "-fx-background-color: #3498db;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 6;"
                + "-fx-padding: 10 20;"
                + "-fx-font-size: 14px;"
        );

        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());

        HBox closeRow = new HBox(closeBtn);
        closeRow.setStyle("-fx-alignment: center; -fx-padding: 10 0 0 0;");
        box.getChildren().add(closeRow);

        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().setStyle("-fx-background-color: transparent;");

        dialog.show();
    }

    private void chooseFormat(String reportType) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dialog.setHeaderText(null);

        VBox card = new VBox(14);
        card.setStyle("""
        -fx-background-color: white;
        -fx-padding: 25;
        -fx-background-radius: 14;
        -fx-border-radius: 14;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 20, 0, 0, 6);
    """);

        Label title = new Label("📂 Select Export Format");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill:#2c3e50;");

        Label sub = new Label("Choose how you want to download your report:");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill:#666;");

        VBox options = new VBox(12);
        options.getChildren().addAll(
                makeOption("🧾 CSV Format", () -> exportFormat(reportType, "csv")),
                makeOption("📄 PDF Format", () -> exportFormat(reportType, "pdf"))
        );

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("""
        -fx-background-color:#3498db;
        -fx-text-fill:white;
        -fx-padding:8 25;
        -fx-background-radius:8;
        -fx-font-size:14px;
        -fx-font-weight:bold;
    """);
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());

        card.getChildren().addAll(title, sub, options, closeBtn);
        dialog.getDialogPane().setContent(card);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().setStyle("-fx-background-color: transparent;");

        // Animation
        card.setScaleX(0.8);
        card.setScaleY(0.8);
        card.setOpacity(0);

        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(180), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(180), card);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);

        fade.play();
        scale.play();

        dialog.show();
    }

    private String currentSignatureName = "Inventory Officer";

    private void exportFormat(String reportType, String format) {

        // Always load latest user info
//        loadLoggedUserInfo();
        switch (reportType) {

            case "all" -> {
                if (format.equals("csv")) {
                    exportAllItems();
                } else {
                    exportAllItemsPDF();
                }
            }

            case "filtered" -> {
                if (format.equals("csv")) {
                    exportFilteredItems();
                } else {
                    exportFilteredItemsPDF();
                }
            }

            case "borrowed" -> {
                if (format.equals("csv")) {
                    exportBorrowedItems();
                } else {
                    exportBorrowedItemsPDF();
                }
            }

            case "missing" -> {
                if (format.equals("csv")) {
                    exportMissingItems();
                } else {
                    exportMissingItemsPDF();
                }
            }

            case "damaged" -> {
                if (format.equals("csv")) {
                    exportDamagedItems();
                } else {
                    exportDamagedItemsPDF();
                }
            }

            case "borrowers" -> {
                if (format.equals("csv")) {
                    exportBorrowers();
                } else {
                    exportBorrowersPDF();
                }
            }
        }
    }

    private void generatePDF(String filename, String titleText, String[][] tableData) {
        try {
            // File chooser w/ Downloads default
            FileChooser fc = new FileChooser();
            fc.setInitialFileName(filename);
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF File", "*.pdf"));
            File downloads = getDefaultDownloadsFolder();
            if (downloads != null && downloads.exists()) {
                fc.setInitialDirectory(downloads);
            }

            File file = fc.showSaveDialog(exportButton != null && exportButton.getScene() != null
                    ? exportButton.getScene().getWindow()
                    : null);
            if (file == null) {
                return;
            }

            // Document + writer
            Document doc = new Document(PageSize.A4, 40, 40, 50, 70); // extra bottom margin for signature
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));

            // Page numbers in footer
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter w, Document d) {
                    ColumnText.showTextAligned(
                            w.getDirectContent(),
                            Element.ALIGN_RIGHT,
                            new Phrase("Page " + w.getPageNumber(), new Font(Font.HELVETICA, 9)),
                            d.right() - 10,
                            d.bottom() - 20,
                            0
                    );
                }
            });

            doc.open();

            // --- Header ---
            Paragraph school = new Paragraph(
                    "DAVAO ORIENTAL STATE UNIVERSITY\nINVENTORY REPORT",
                    new Font(Font.TIMES_ROMAN, 14, Font.BOLD)
            );
            school.setAlignment(Element.ALIGN_CENTER);
            doc.add(school);

            // Logo (optional)
            try {
                Image logo = Image.getInstance(getClass().getResource("/inventorysystem/assets/logo.png"));
                logo.scaleToFit(90, 90);
                logo.setAlignment(Image.ALIGN_CENTER);
                doc.add(logo);
            } catch (Exception ignored) {
            }

            // Title + meta
            Paragraph title = new Paragraph(titleText, new Font(Font.TIMES_ROMAN, 12, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("Date Generated: " + LocalDate.now()));
            doc.add(new Paragraph("Generated By:   " + getLoggedFullName()));
            doc.add(new Paragraph("Department:     IT Department"));
            doc.add(new Paragraph("\n"));

            // Calculate usable page area for content (width & height)
            float pageWidth = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
            float pageHeight = doc.getPageSize().getHeight() - doc.topMargin() - doc.bottomMargin();

            // Signature block height we reserve at bottom (approx)
            final float signatureBlockHeight = 120f;

            // If no data -> show "NO DATA AVAILABLE"
            boolean hasRows = tableData != null && tableData.length > 1;
            if (!hasRows) {
                Paragraph empty = new Paragraph("NO DATA AVAILABLE", new Font(Font.TIMES_ROMAN, 10, Font.BOLD));
                empty.setAlignment(Element.ALIGN_CENTER);
                empty.setSpacingBefore(20);
                doc.add(empty);

                // Fill vertical space so signature will be at the bottom
                PdfPTable filler = new PdfPTable(1);
                filler.setTotalWidth(pageWidth);
                filler.setLockedWidth(true);
                PdfPCell gap = new PdfPCell(new Phrase(""));
                gap.setFixedHeight(pageHeight - 200); // leave some top space for header
                gap.setBorder(Rectangle.NO_BORDER);
                filler.addCell(gap);
                doc.add(filler);

                // Add signature area (pinned to bottom because filler took the space)
                addSignatureToDocument(doc);
                doc.close();
                showAlert("Success", "PDF Report Generated", file.getAbsolutePath());
                return;
            }

            // Build the content table (headers + rows)
            PdfPTable table = new PdfPTable(tableData[0].length);
            table.setWidthPercentage(100);
            table.setTotalWidth(pageWidth);
            table.setLockedWidth(true);

            // Header cells
            for (String h : tableData[0]) {
                PdfPCell head = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 12, Font.BOLD)));
                head.setHorizontalAlignment(Element.ALIGN_CENTER);
                head.setPadding(6);
                table.addCell(head);
            }

            // Add data rows
            for (int i = 1; i < tableData.length; i++) {
                for (String cellData : tableData[i]) {
                    PdfPCell c = new PdfPCell(new Phrase(cellData == null ? "" : cellData, new Font(Font.HELVETICA, 11)));
                    c.setPadding(5);
                    table.addCell(c);
                }
            }

            /*
         * Attempt to place the table in remaining space while reserving room for signature.
         * If the table fits in a single page, we will show it and then pin signature to bottom.
         * If the table is larger, the table will flow naturally to following pages and the signature
         * will be added after the table (may appear on its own new page).
             */
            // Try to estimate needed height by rendering the table to a PdfPTable and checking totalHeight.
            // This is an approximation — good enough for typical tables.
            table.completeRow();
            float tableHeight = table.getTotalHeight();
            if (tableHeight == 0f) {
                // force layout measurement by calling writeSelectedRows on a dummy canvas
                PdfContentByte cb = writer.getDirectContent();
                try {
                    tableHeight = table.getTotalHeight(); // still may be 0; fall back
                } catch (Exception ignored) {
                }
            }

            // If table height <= available space minus signature -> draw table then signature at bottom.
            if (tableHeight > 0 && tableHeight < (pageHeight - signatureBlockHeight - 20)) {
                doc.add(table);

                // Fill remaining space so signature is pushed to absolute bottom
                float used = tableHeight + 120; // add some extra for metadata already added
                float remaining = pageHeight - used - signatureBlockHeight;
                if (remaining > 0) {
                    PdfPTable spacer = new PdfPTable(1);
                    spacer.setTotalWidth(pageWidth);
                    spacer.setLockedWidth(true);
                    PdfPCell emptyCell = new PdfPCell(new Phrase(""));
                    emptyCell.setFixedHeight(remaining);
                    emptyCell.setBorder(Rectangle.NO_BORDER);
                    spacer.addCell(emptyCell);
                    doc.add(spacer);
                }

                addSignatureToDocument(doc);
                doc.close();
                showAlert("Success", "PDF Report Generated", file.getAbsolutePath());
                return;
            }

            // TABLE is large (or height unknown) -> just add table normally (it will span pages)
            doc.add(table);

            // After large tables, put signature in its own block at the bottom of the final page
            // We insert some spacing, then signature. For very long outputs this will be on a new page,
            // which is acceptable as a last-page signature.
            doc.add(new Paragraph("\n\n"));
            addSignatureToDocument(doc);

            doc.close();
            showAlert("Success", "PDF Report Generated", file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "PDF Generation Failed", e.getMessage());
        }
    }

    /**
     * Helper: actually adds signature block aligned to right (keeps code tidy)
     */
    private void addSignatureToDocument(Document doc) {
        try {
//            doc.add(new Paragraph("\n\nPrepared By:\n\n"));

            try {
                Image sign = Image.getInstance(getClass().getResource("/inventorysystem/assets/signature.png"));
                sign.scaleToFit(120, 60);
                sign.setAlignment(Image.ALIGN_RIGHT);
                doc.add(sign);
            } catch (Exception ignored) {
            }

//            Paragraph sig = new Paragraph(
//                    "_____________________________\n"
//                    + getLoggedFullName() + "\nInventory Officer",
//                    new Font(Font.HELVETICA, 11)
//            );
//            sig.setAlignment(Element.ALIGN_RIGHT);
//            doc.add(sig);
        } catch (Exception e) {
            // swallow — signature is non-critical
        }
    }

    /**
     * Helper to return Downloads folder (used elsewhere as recommended)
     */
    private File getDefaultDownloadsFolder() {
        String home = System.getProperty("user.home");
        File downloads = new File(home, "Downloads");
        if (downloads.exists() && downloads.isDirectory()) {
            return downloads;
        }
        return new File(home); // fallback
    }

    private void exportAllItemsPDF() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Item ID", "Item Name", "Category", "Status", "In-Charge"});

        for (Item it : masterData) {
            rows.add(new String[]{
                "" + it.getItemId(),
                it.getItemName(),
                it.getCategoryName(),
                it.getStatus(),
                it.getInChargeName()
            });
        }

        generatePDF("all_items.pdf", "📦 ALL ITEMS INVENTORY REPORT", rows.toArray(new String[0][]));
    }

    private void exportFilteredItemsPDF() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Item ID", "Item Name", "Category", "Status", "In-Charge"});

        for (Item it : filteredData) {
            rows.add(new String[]{
                "" + it.getItemId(),
                it.getItemName(),
                it.getCategoryName(),
                it.getStatus(),
                it.getInChargeName()
            });
        }

        generatePDF("filtered_items.pdf", "🔍 FILTERED ITEMS REPORT", rows.toArray(new String[0][]));
    }

    private void exportBorrowedItemsPDF() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"ID", "Name", "Category", "In-Charge"});

        for (Item it : masterData) {
            if ("Borrowed".equalsIgnoreCase(it.getStatus())) {
                rows.add(new String[]{
                    "" + it.getItemId(),
                    it.getItemName(),
                    it.getCategoryName(),
                    it.getInChargeName()
                });
            }
        }

        generatePDF("borrowed_items.pdf", "📤 BORROWED ITEMS REPORT", rows.toArray(new String[0][]));
    }

    private void exportMissingItemsPDF() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"ID", "Name", "Category", "Last Scanned", "In-Charge"});

        for (Item it : masterData) {
            if ("Missing".equalsIgnoreCase(it.getStatus())) {
                rows.add(new String[]{
                    "" + it.getItemId(),
                    it.getItemName(),
                    it.getCategoryName(),
                    it.getLastScanned() != null ? it.getLastScanned().toString() : "",
                    it.getInChargeName()
                });
            }
        }

        generatePDF("missing_items.pdf", "❓ MISSING ITEMS REPORT", rows.toArray(new String[0][]));
    }

    private void exportDamagedItemsPDF() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"ID", "Name", "Category", "Date Acquired", "In-Charge"});

        for (Item it : masterData) {
            if ("Damaged".equalsIgnoreCase(it.getStatus())) {
                rows.add(new String[]{
                    "" + it.getItemId(),
                    it.getItemName(),
                    it.getCategoryName(),
                    it.getDateAcquired() != null ? it.getDateAcquired().toString() : "",
                    it.getInChargeName()
                });
            }
        }

        generatePDF("damaged_items.pdf", "💢 DAMAGED ITEMS REPORT", rows.toArray(new String[0][]));
    }

    private void exportBorrowersPDF() {
        try {
            Class<?> daoClass = Class.forName("inventorysystem.dao.BorrowerDAO");
            Object dao = daoClass.getDeclaredConstructor().newInstance();
            List<?> list = (List<?>) daoClass.getMethod("getAllBorrowers").invoke(dao);

            List<String[]> rows = new ArrayList<>();
            rows.add(new String[]{"ID", "Name", "Position", "Type"});

            for (Object b : list) {
                rows.add(new String[]{
                    "" + daoClass.getMethod("getBorrowerId").invoke(b),
                    "" + daoClass.getMethod("getBorrowerName").invoke(b),
                    "" + daoClass.getMethod("getPosition").invoke(b),
                    "" + daoClass.getMethod("getBorrowerType").invoke(b)
                });
            }

            generatePDF("borrowers.pdf", "🧑‍🤝‍🧑 BORROWERS REPORT", rows.toArray(new String[0][]));

        } catch (Exception e) {
            showAlert("Error", "Borrower PDF failed", e.getMessage());
        }
    }

    private HBox makeOption(String text, Runnable action) {
        HBox row = new HBox();
        row.setStyle("""
        -fx-background-color: #f6f8fa;
        -fx-padding: 12;
        -fx-background-radius: 8;
    """);

        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #2c3e50;");

        row.getChildren().add(lbl);

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle("""
        -fx-background-color: #e8f0fe;
        -fx-padding: 12;
        -fx-background-radius: 8;
    """));

        row.setOnMouseExited(e -> row.setStyle("""
        -fx-background-color: #f6f8fa;
        -fx-padding: 12;
        -fx-background-radius: 8;
    """));

        // Click action
        row.setOnMouseClicked(e -> {
            ((Stage) row.getScene().getWindow()).close();
            action.run();
        });

        return row;
    }
// -----------------------
// CSV helpers (paste inside ItemController)
// -----------------------

    private File chooseSaveFile(String suggestedName) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName(suggestedName);
        // exportButton must be non-null and part of scene; if null, open without owner:
        if (exportButton != null && exportButton.getScene() != null) {
            return fc.showSaveDialog(exportButton.getScene().getWindow());
        } else {
            return fc.showSaveDialog(null);
        }
    }

    private void writeCsv(File file, List<String> lines) throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            for (String line : lines) {
                pw.println(line);
            }
        }
    }

    private String csvSafe(Object o) {
        if (o == null) {
            return "";
        }
        String s = String.valueOf(o);
        // replace commas/newlines so CSV columns stay intact (simple approach)
        s = s.replace(",", " ").replace("\r", " ").replace("\n", " ");
        return s;
    }

    private void exportAllItems() {
        File file = chooseSaveFile("all_items.csv");
        if (file == null) {
            return;
        }

        try {
            List<String> lines = new ArrayList<>();
            lines.add("Item ID,Item Name,Barcode,Category,Unit,Status,Date Acquired,Last Scanned,Storage Location,In-Charge");

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
                        csvSafe(it.getInChargeName())
                ));
            }

            writeCsv(file, lines);
            showAlert("Success", "Export Complete", "All items exported.");
        } catch (Exception e) {
            showAlert("Error", "Export Failed", e.getMessage());
        }
    }

    private void exportFilteredItems() {
        File file = chooseSaveFile("filtered_items.csv");
        if (file == null) {
            return;
        }

        try {
            List<String> lines = new ArrayList<>();
            lines.add("Item ID,Item Name,Barcode,Category,Unit,Status,Location,In-Charge");

            for (Item it : filteredData) {
                lines.add(String.join(",",
                        csvSafe("" + it.getItemId()),
                        csvSafe(it.getItemName()),
                        csvSafe(it.getBarcode()),
                        csvSafe(it.getCategoryName()),
                        csvSafe(it.getUnit()),
                        csvSafe(it.getStatus()),
                        csvSafe(it.getStorageLocation()),
                        csvSafe(it.getInChargeName())
                ));
            }

            writeCsv(file, lines);
            showAlert("Success", "Export Complete", "Filtered items exported.");
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
            showAlert("Success", "Export Complete", "Borrowed items exported.");
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
            lines.add("Item ID,Item Name,Barcode,Category,Last Scanned,In-Charge");

            for (Item it : masterData) {
                if ("Missing".equalsIgnoreCase(it.getStatus())) {
                    lines.add(String.join(",",
                            csvSafe("" + it.getItemId()),
                            csvSafe(it.getItemName()),
                            csvSafe(it.getBarcode()),
                            csvSafe(it.getCategoryName()),
                            csvSafe(it.getLastScanned() != null ? it.getLastScanned().toString() : ""),
                            csvSafe(it.getInChargeName())
                    ));
                }
            }

            writeCsv(file, lines);
            showAlert("Success", "Export Complete", "Missing items exported.");
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
            lines.add("Item ID,Item Name,Barcode,Category,Date Acquired,In-Charge");

            for (Item it : masterData) {
                if ("Damaged".equalsIgnoreCase(it.getStatus())) {
                    lines.add(String.join(",",
                            csvSafe("" + it.getItemId()),
                            csvSafe(it.getItemName()),
                            csvSafe(it.getBarcode()),
                            csvSafe(it.getCategoryName()),
                            csvSafe(it.getDateAcquired() != null ? it.getDateAcquired().toString() : ""),
                            csvSafe(it.getInChargeName())
                    ));
                }
            }

            writeCsv(file, lines);
            showAlert("Success", "Export Complete", "Damaged items exported.");
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

        } catch (Exception e) {
            showAlert("Error", "Borrower export failed", e.getMessage());
        }
    }

    private String[] askForSignatureInfo() {

        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Export Verification");

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );

        Label nameLbl = new Label("Full Name:");
        Label passLbl = new Label("Password:");

        TextField nameField = new TextField();
        PasswordField passField = new PasswordField();

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);
        gp.add(nameLbl, 0, 0);
        gp.add(nameField, 1, 0);
        gp.add(passLbl, 0, 1);
        gp.add(passField, 1, 1);

        dialog.getDialogPane().setContent(gp);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new String[]{nameField.getText(), passField.getText()};
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private boolean verifyPassword(String username, String password) {
        try {
            Class<?> daoClass = Class.forName("inventorysystem.dao.UserDAO");
            Object dao = daoClass.getDeclaredConstructor().newInstance();
            Object user = daoClass.getMethod("getUser", String.class, String.class).invoke(dao, username, password);

            return user != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static void setLoggedUsername(String username) {
        loggedUsername = username;
    }

//    private void loadLoggedUserInfo() {
//        try {
//            Class<?> daoClass = Class.forName("inventorysystem.dao.UserDAO");
//            Object dao = daoClass.getDeclaredConstructor().newInstance();
//
//            // Your DB lookup: getUserByUsername(username)
//            Object user = daoClass.getMethod("getUserByUsername", String.class)
//                    .invoke(dao, loggedUsername);
//
//            if (user != null) {
//                loggedFirstName = (String) user.getClass().getMethod("getFirstName").invoke(user);
//                loggedLastName = (String) user.getClass().getMethod("getLastName").invoke(user);
//            }
//
//        } catch (Exception e) {
//            System.out.println("⚠ Could not load user info, using defaults.");
//        }
//    }
}
