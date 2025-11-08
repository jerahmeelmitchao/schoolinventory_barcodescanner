package inventorysystem.models;

import java.time.LocalDate;

public class Item {

    private int itemId;
    private String itemName;
    private String barcode;
    private int categoryId;
    private int quantity;
    private String unit;
    private LocalDate dateAcquired;
    private String serviceabilityStatus;
    private String conditionStatus;      // new field
    private String availabilityStatus;
    private String storageLocation;      // new field
    private int inchargeId;
    private String inChargeName;
    private String categoryName;         // not stored in DB, just for display
    private String addedBy;              // new field
    private LocalDate lastScanned;

    // Constructor including new fields
    public Item(int itemId, String itemName, String barcode, int categoryId, int quantity,
                String unit, LocalDate dateAcquired, String serviceabilityStatus,
                String conditionStatus, String availabilityStatus, String storageLocation,
                int inchargeId, String addedBy) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.barcode = barcode;
        this.categoryId = categoryId;
        this.quantity = quantity;
        this.unit = unit;
        this.dateAcquired = dateAcquired;
        this.serviceabilityStatus = serviceabilityStatus;
        this.conditionStatus = conditionStatus;
        this.availabilityStatus = availabilityStatus;
        this.storageLocation = storageLocation;
        this.inchargeId = inchargeId;
        this.addedBy = addedBy;
    }

    // Getters
    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getBarcode() { return barcode; }
    public int getCategoryId() { return categoryId; }
    public int getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public LocalDate getDateAcquired() { return dateAcquired; }
    public String getServiceabilityStatus() { return serviceabilityStatus; }
    public String getConditionStatus() { return conditionStatus; }       // new getter
    public String getAvailabilityStatus() { return availabilityStatus; }
    public String getStorageLocation() { return storageLocation; }       // new getter
    public int getInchargeId() { return inchargeId; }
    public String getInChargeName() { return inChargeName; }
    public String getCategoryName() { return categoryName; }
    public String getAddedBy() { return addedBy; }                       // new getter
    public LocalDate getLastScanned() { return lastScanned; }

    // Setters
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setDateAcquired(LocalDate dateAcquired) { this.dateAcquired = dateAcquired; }
    public void setServiceabilityStatus(String serviceabilityStatus) { this.serviceabilityStatus = serviceabilityStatus; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }   // new setter
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }   // new setter
    public void setInchargeId(int inchargeId) { this.inchargeId = inchargeId; }
    public void setInChargeName(String inChargeName) { this.inChargeName = inChargeName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }                                    // new setter
    public void setLastScanned(LocalDate lastScanned) { this.lastScanned = lastScanned; }
}
