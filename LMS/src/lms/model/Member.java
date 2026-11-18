/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.model;

/**
 *
 * @author Mary
 */
public abstract class Member {
    private final String memberID;
    private String fullName;
    private String contactNumber;
    private String email;
    private MemberStatus status;
    
    public abstract int getBorrowingInit();
    public abstract MemberType getMemberType();
    
    protected Member(String memberID, String fullName, String contactNumber, String email, MemberStatus status) {
        this.memberID = requireText(memberID, "Member ID");
        setFullName(fullName);
        setContactNumber(contactNumber);
        setEmail(email);
        setStatus(status);
    }
    
    //Getters
    public String getMemberID() {
        return memberID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public MemberStatus getStatus() {
        return status;
    }   
    
    //Setters
    public void setFullName(String fullName) {
        this.fullName = requireText(fullName, "Full Name");
    }

    public void setContactNumber(String contactNumber) {
        String value = requireText(contactNumber, "Contact Number");
        if(!value.matches("\\\\+?[0-9]{7,15}")) {
            throw new IllegalArgumentException(
                    "Contact number must contain from 7 to 15 digits with an optional + sign.");
        }
        this.contactNumber = value;
    }

    public void setEmail(String email) {
        String value = requireText(email, "Email");
        if(!value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        this.email = value;
    }

    public void setStatus(MemberStatus status) {
        if(status == null) {
            throw new IllegalArgumentException("Member status cannot be null.");
        }
        this.status = status;
    }
    
    private String requireText(String value, String fieldName) {
            if(value == null || value.isBlank()) {
                throw new IllegalArgumentException (fieldName + " cannot be empty.");
            }
            return value.trim();
        }   
    
    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }
      
    @Override
    public String toString() {
        return memberID + " - " + fullName;
    }
}
