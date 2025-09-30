package inventorysystem.models;

import javafx.beans.property.*;

public class Borrower {
    private final IntegerProperty borrowerId = new SimpleIntegerProperty();
    private final StringProperty borrowerName = new SimpleStringProperty();
    private final StringProperty position = new SimpleStringProperty();
    private final StringProperty borrowerType = new SimpleStringProperty(); // "Student", "Teacher", "Staff"

    public Borrower() {}

    public Borrower(int borrowerId, String borrowerName, String position, String borrowerType) {
        this.borrowerId.set(borrowerId);
        this.borrowerName.set(borrowerName);
        this.position.set(position);
        this.borrowerType.set(borrowerType);
    }

    // --- Getters and setters
    public int getBorrowerId() { return borrowerId.get(); }
    public void setBorrowerId(int borrowerId) { this.borrowerId.set(borrowerId); }
    public IntegerProperty borrowerIdProperty() { return borrowerId; }

    public String getBorrowerName() { return borrowerName.get(); }
    public void setBorrowerName(String borrowerName) { this.borrowerName.set(borrowerName); }
    public StringProperty borrowerNameProperty() { return borrowerName; }

    public String getPosition() { return position.get(); }
    public void setPosition(String position) { this.position.set(position); }
    public StringProperty positionProperty() { return position; }

    public String getBorrowerType() { return borrowerType.get(); }
    public void setBorrowerType(String borrowerType) { this.borrowerType.set(borrowerType); }
    public StringProperty borrowerTypeProperty() { return borrowerType; }

    @Override
    public String toString() {
        return getBorrowerName() + " (" + getBorrowerType() + ")";
    }
}
