/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventorysystem.models;

public class Incharge {
    private int inchargeId;
    private String inchargeName;
    private String position;
    private String contactInfo;
    private int assignedCategoryId;

    public Incharge() {}

    public Incharge(int inchargeId, String inchargeName, String position, String contactInfo, int assignedCategoryId) {
        this.inchargeId = inchargeId;
        this.inchargeName = inchargeName;
        this.position = position;
        this.contactInfo = contactInfo;
        this.assignedCategoryId = assignedCategoryId;
    }

    public int getInchargeId() {
        return inchargeId;
    }

    public void setInchargeId(int inchargeId) {
        this.inchargeId = inchargeId;
    }

    public String getInchargeName() {
        return inchargeName;
    }

    public void setInchargeName(String inchargeName) {
        this.inchargeName = inchargeName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public int getAssignedCategoryId() {
        return assignedCategoryId;
    }

    public void setAssignedCategoryId(int assignedCategoryId) {
        this.assignedCategoryId = assignedCategoryId;
    }

    @Override
    public String toString() {
        return inchargeName + " - " + position;
    }
}
