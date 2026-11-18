/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.model;

/**
 *
 * @author Mary
 */
public class StudentMember extends Member {
    private static final int BORROWING_LIMIT = 3;

    public StudentMember(String memberID, String fullName, String contactNumber, String email, MemberStatus status) {
        super(memberID, fullName, contactNumber, email, status);
    }
   
    @Override
    public int getBorrowingLimit() {
        return BORROWING_LIMIT;
    }
    
    @Override
    public MemberType getMemberType() {
        return MemberType.STUDENT;
    }
}
