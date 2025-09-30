/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventorysystem.models;

import java.time.LocalDate;

public class BorrowRecord {
    private int recordId;
    private int itemId;
    private int borrowerId;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private int quantityBorrowed;
    private String status; // e.g. "Borrowed", "Returned"

    public BorrowRecord() {}

    public BorrowRecord(int recordId, int itemId, int borrowerId, LocalDate borrowDate,
                        LocalDate returnDate, int quantityBorrowed, String status) {
        this.recordId = recordId;
        this.itemId = itemId;
        this.borrowerId = borrowerId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.quantityBorrowed = quantityBorrowed;
        this.status = status;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(int borrowerId) {
        this.borrowerId = borrowerId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public int getQuantityBorrowed() {
        return quantityBorrowed;
    }

    public void setQuantityBorrowed(int quantityBorrowed) {
        this.quantityBorrowed = quantityBorrowed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Record #" + recordId + " - ItemID: " + itemId + " BorrowerID: " + borrowerId;
    }
}
