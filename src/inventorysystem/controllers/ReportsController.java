package inventorysystem.controllers;

import inventorysystem.dao.ItemDAO;
import inventorysystem.models.Item;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsController {

    @FXML private Button exportAllBtn;
    @FXML private Button exportBorrowedBtn;
    @FXML private Button exportMissingBtn;
    @FXML private Button exportDamagedBtn;
    @FXML private Button exportBorrowersBtn;

    // Use the same formatter you used elsewhere (keeps consistency)
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // DAO instances (you already have ItemDAO in your project)
    private final ItemDAO itemDAO = new ItemDAO();

    /***********************************************************
     * NOTE:
     * - This controller prefers using existing DAOs / model lists.
     * - For borrowed/borrowers report your project may have
     *   BorrowRecordDAO / BorrowerDAO. If so, replace the stub
     *   sections with those DAO calls (see comments below).
     ***********************************************************/

    // ---------- Export helpers ----------
    private File chooseSaveFile(String suggestedName) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName(suggestedName);
        return fc.showSaveDialog(exportAllBtn.getScene().getWindow());
    }

    private void writeCsv(File file, List<String> lines) throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            for (String line : lines) pw.println(line);
        }
    }

    private String safe(String s) {
        if (s == null) return "";
        // replace CR/LF and commas (basic escaping)
        return s.replace("\r", " ").replace("\n", " ").replace(",", " ");
    }

    private String formatDate(LocalDate d) {
        return d == null ? "" : d.format(displayFormatter);
    }

    // ---------- Exports ----------

    @FXML
    private void exportAllItems() {
        File f = chooseSaveFile("all_items.csv");
        if (f == null) return;

        try {
            List<Item> items = itemDAO.getAllItems(); // uses DAO (in-memory / DB depending on your DAO)
            // header
            List<String> lines = new java.util.ArrayList<>();
            lines.add("Item ID,Item Name,Barcode,Category,Unit,Status,Date Acquired,Last Scanned,Storage Location,In-Charge,Added By");

            for (Item it : items) {
                String lastScanned = it.getLastScanned() == null ? "" : it.getLastScanned().toString();
                String dateAcq = it.getDateAcquired() == null ? "" : it.getDateAcquired().toString();
                lines.add(String.join(",",
                        safe(String.valueOf(it.getItemId())),
                        safe(it.getItemName()),
                        safe(it.getBarcode()),
                        safe(it.getCategoryName()),
                        safe(it.getUnit()),
                        safe(it.getStatus()),
                        safe(dateAcq),
                        safe(lastScanned),
                        safe(it.getStorageLocation()),
                        safe(it.getInChargeName()),
                        safe(it.getAddedBy())
                ));
            }

            writeCsv(f, lines);
            showInfo("Export Complete", "All items exported to CSV.");
        } catch (Exception ex) {
            showError("Export Failed", ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void exportBorrowedItems() {
        // Option A chosen: prefer DAO or in-memory source.
        // If you have BorrowRecordDAO in your project, use it here.
        // Below is a fallback if you don't: we attempt to use ItemDAO to filter status 'Borrowed'
        File f = chooseSaveFile("borrowed_items.csv");
        if (f == null) return;

        try {
            // Prefer borrow-records source if exists
            // Example if you have BorrowRecordDAO:
            // BorrowRecordDAO brDao = new BorrowRecordDAO();
            // List<BorrowRecord> records = brDao.getAllBorrowRecordsByStatus("Borrowed");
            //
            // For this generic implementation we'll export items with status 'Borrowed' and include borrower id/notes if available.
            List<Item> items = itemDAO.getAllItems();
            List<String> lines = new java.util.ArrayList<>();
            lines.add("Item ID,Item Name,Barcode,Quantity Borrowed,Borrower ID,Borrower Name,Borrow Date,Return Date,Remarks");

            // If borrow_records DAO exists you should join and produce richer output.
            for (Item it : items) {
                if ("Borrowed".equalsIgnoreCase(it.getStatus())) {
                    // limited: we don't have borrower info from item table only.
                    lines.add(String.join(",",
                            safe(String.valueOf(it.getItemId())),
                            safe(it.getItemName()),
                            safe(it.getBarcode()),
                            "1", // quantity unknown here; use borrow_records dao for accurate qty
                            "", // borrower id (unknown without join)
                            "", // borrower name
                            "", // borrow date
                            "", // return date
                            ""  // remarks
                    ));
                }
            }

            writeCsv(f, lines);
            showInfo("Export Complete", "Borrowed items CSV exported.\n(For full borrower details: implement BorrowRecordDAO join.)");
        } catch (Exception ex) {
            showError("Export Failed", ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void exportMissingItems() {
        File f = chooseSaveFile("missing_items.csv");
        if (f == null) return;

        try {
            List<Item> items = itemDAO.getAllItems();
            List<String> lines = new java.util.ArrayList<>();
            lines.add("Item ID,Item Name,Barcode,Category,Last Scanned,Storage Location,In-Charge");

            for (Item it : items) {
                if ("Missing".equalsIgnoreCase(it.getStatus())) {
                    String lastScanned = it.getLastScanned() == null ? "" : it.getLastScanned().toString();
                    lines.add(String.join(",",
                            safe(String.valueOf(it.getItemId())),
                            safe(it.getItemName()),
                            safe(it.getBarcode()),
                            safe(it.getCategoryName()),
                            safe(lastScanned),
                            safe(it.getStorageLocation()),
                            safe(it.getInChargeName())
                    ));
                }
            }

            writeCsv(f, lines);
            showInfo("Export Complete", "Missing items exported.");
        } catch (Exception ex) {
            showError("Export Failed", ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void exportDamagedItems() {
        File f = chooseSaveFile("damaged_items.csv");
        if (f == null) return;

        try {
            List<Item> items = itemDAO.getAllItems();
            List<String> lines = new java.util.ArrayList<>();
            lines.add("Item ID,Item Name,Barcode,Category,Date Acquired,Storage Location,In-Charge,Remarks");

            for (Item it : items) {
                if ("Damaged".equalsIgnoreCase(it.getStatus())) {
                    String dateAcq = it.getDateAcquired() == null ? "" : it.getDateAcquired().toString();
                    lines.add(String.join(",",
                            safe(String.valueOf(it.getItemId())),
                            safe(it.getItemName()),
                            safe(it.getBarcode()),
                            safe(it.getCategoryName()),
                            safe(dateAcq),
                            safe(it.getStorageLocation()),
                            safe(it.getInChargeName()),
                            "" // remarks column reserved if you store it elsewhere
                    ));
                }
            }

            writeCsv(f, lines);
            showInfo("Export Complete", "Damaged items exported.");
        } catch (Exception ex) {
            showError("Export Failed", ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void exportBorrowers() {
        File f = chooseSaveFile("borrowers_report.csv");
        if (f == null) return;

        try {
            // If you have a BorrowerDAO, use it. For now we assume borrowers table exists and you have a DAO.
            // Example:
            // BorrowerDAO brDao = new BorrowerDAO();
            // List<Borrower> borrowers = brDao.getAllBorrowers();
            //
            // We'll try to use a DAO class name 'BorrowerDAO' if available, otherwise show message.
            try {
                // Attempt reflection to avoid compile error if BorrowerDAO is absent.
                Class<?> cl = Class.forName("inventorysystem.dao.BorrowerDAO");
                Object dao = cl.getDeclaredConstructor().newInstance();
                java.lang.reflect.Method m = cl.getMethod("getAllBorrowers");
                List<?> borrowers = (List<?>) m.invoke(dao);

                List<String> lines = new java.util.ArrayList<>();
                lines.add("Borrower ID,Borrower Name,Position,Borrower Type");

                for (Object b : borrowers) {
                    // expects methods getBorrowerId(), getBorrowerName(), getPosition(), getBorrowerType()
                    java.lang.reflect.Method idM = b.getClass().getMethod("getBorrowerId");
                    java.lang.reflect.Method nameM = b.getClass().getMethod("getBorrowerName");
                    java.lang.reflect.Method posM = b.getClass().getMethod("getPosition");
                    java.lang.reflect.Method typeM = b.getClass().getMethod("getBorrowerType");

                    lines.add(String.join(",",
                            safe(String.valueOf(idM.invoke(b))),
                            safe(String.valueOf(nameM.invoke(b))),
                            safe(String.valueOf(posM.invoke(b))),
                            safe(String.valueOf(typeM.invoke(b)))
                    ));
                }

                writeCsv(f, lines);
                showInfo("Export Complete", "Borrowers exported.");

            } catch (ClassNotFoundException cnf) {
                // BorrowerDAO not present: fallback to simple message
                showInfo("Not Available", "BorrowerDAO not found in your project. Implement BorrowerDAO.getAllBorrowers() for full export.");
            }

        } catch (Exception ex) {
            showError("Export Failed", ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ---------- small UI helpers ----------
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
