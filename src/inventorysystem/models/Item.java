/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
    private String availabilityStatus;
    private int inchargeId;

    public Item(int itemId, String itemName, String barcode, int categoryId, int quantity,
                String unit, LocalDate dateAcquired, String serviceabilityStatus,
                String availabilityStatus, int inchargeId) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.barcode = barcode;
        this.categoryId = categoryId;
        this.quantity = quantity;
        this.unit = unit;
        this.dateAcquired = dateAcquired;
        this.serviceabilityStatus = serviceabilityStatus;
        this.availabilityStatus = availabilityStatus;
        this.inchargeId = inchargeId;
    }

    // Getters and setters
    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getBarcode() { return barcode; }
    public int getCategoryId() { return categoryId; }
    public int getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public LocalDate getDateAcquired() { return dateAcquired; }
    public String getServiceabilityStatus() { return serviceabilityStatus; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public int getInchargeId() { return inchargeId; }

    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setDateAcquired(LocalDate dateAcquired) { this.dateAcquired = dateAcquired; }
    public void setServiceabilityStatus(String serviceabilityStatus) { this.serviceabilityStatus = serviceabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public void setInchargeId(int inchargeId) { this.inchargeId = inchargeId; }
}
