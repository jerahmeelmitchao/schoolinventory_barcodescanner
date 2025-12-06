package inventorysystem.models;

import java.time.LocalDate;

public class Item {

    private int itemId;
    private String itemName;
    private String barcode;
    private int categoryId;
    private String unit;
    private LocalDate dateAcquired;
    private String status;               // NEW merged field
    private String storageLocation;
    private int inchargeId;
    private String inChargeName;
    private String categoryName;
    private String addedBy;
    private LocalDate lastScanned;

    public Item(int itemId, String itemName, String barcode, int categoryId,
                String unit, LocalDate dateAcquired, String status,
                String storageLocation, int inchargeId, String addedBy) {

        this.itemId = itemId;
        this.itemName = itemName;
        this.barcode = barcode;
        this.categoryId = categoryId;
        this.unit = unit;
        this.dateAcquired = dateAcquired;
        this.status = status;
        this.storageLocation = storageLocation;
        this.inchargeId = inchargeId;
        this.addedBy = addedBy;
    }

    // Getters
    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getBarcode() { return barcode; }
    public int getCategoryId() { return categoryId; }
    public String getUnit() { return unit; }
    public LocalDate getDateAcquired() { return dateAcquired; }
    public String getStatus() { return status; }
    public String getStorageLocation() { return storageLocation; }
    public int getInchargeId() { return inchargeId; }
    public String getInChargeName() { return inChargeName; }
    public String getCategoryName() { return categoryName; }
    public String getAddedBy() { return addedBy; }
    public LocalDate getLastScanned() { return lastScanned; }

    // Setters
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setDateAcquired(LocalDate dateAcquired) { this.dateAcquired = dateAcquired; }
    public void setStatus(String status) { this.status = status; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public void setInchargeId(int inchargeId) { this.inchargeId = inchargeId; }
    public void setInChargeName(String name) { this.inChargeName = name; }
    public void setCategoryName(String name) { this.categoryName = name; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }
    public void setLastScanned(LocalDate lastScanned) { this.lastScanned = lastScanned; }
}
