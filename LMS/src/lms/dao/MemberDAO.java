/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.dao;

import lms.database.DatabaseConnection;
import lms.model.Member;
import lms.model.StudentMember;
import lms.model.FacultyMember;
import lms.model.MemberType;
import lms.model.MemberStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Optional;

/**
 *
 * @author Mary
 */
public class MemberDAO {
    public boolean insert(Member member) throws SQLException {
        String sql = """
                     INSERT INTO members(member_id, full_name, member_type, contact_number, email, status) 
                     VALUES (?, ?, ?, ?, ?, ?)""";
            
        try(Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getMemberID());
            stmt.setString(2, member.getFullName());
            stmt.setString(3, member.getMemberType().name());
            stmt.setString(4, member.getContactNumber());
            stmt.setString(5, member.getEmail());
            stmt.setString(6, member.getStatus().name());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public Optional<Member> findById(String memberID) throws SQLException {
        String sql = "SELECT * FROM members WHERE member_id = ?";
        
        try(Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberID);
            
            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    public ArrayList<Member> findAll() throws SQLException {
        ArrayList<Member> members = new ArrayList<>();
        
        String sql = "SELECT * FROM member ORDER BY full_name";
        
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                members.add(mapRow(rs));
            }
        }
        return members;
    }
    
     public boolean update(Member member) throws SQLException {
        String sql = """
                     UPDATE members 
                     SET full_name = ?, member_type = ?, contact_number = ?, email = ?, status = ?
                     WHERE member_id = ?""";
    
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {           
            stmt.setString(1, member.getFullName());
            stmt.setString(2, member.getMemberType().name());
            stmt.setString(3, member.getContactNumber());
            stmt.setString(4, member.getEmail());
            stmt.setString(5, member.getStatus().name());
            stmt.setString(6, member.getMemberID());
            
            return stmt.executeUpdate() > 0;
        }
    }
     
    public boolean updateStatus(String memberID, MemberStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Member status cannot be null.");
        }
        String sql = "UPDATE members SET status = ? WHERE member_id = ?";
        
        try(Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setString(2, memberID);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(String memberID) throws SQLException {
        String sql = "DELETE FROM members WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberID);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public ArrayList<Member> search(String keyword) throws SQLException {
        ArrayList<Member> members = new ArrayList<>();
        
        String sql = """
                     SELECT * FROM members 
                     WHERE member_id LIKE ? OR full_name LIKE ? OR email LIKE ? OR member_type LIKE ? 
                     ORDER BY full_name""";
        
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            
            for (int i = 1; i <= 4; i++) {
                stmt.setString(i, pattern);
            }
        
            try(ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapRow(rs));  
                }
            }
        }
        return members;
    }
    
    private Member mapRow(ResultSet rs) throws SQLException {
        String memberID = rs.getString("member_id");
        String fullName = rs.getString("full_name");
        String contactNumber = rs.getString("contact_number");
        String email = rs.getString("email");
        
        MemberStatus status = MemberStatus.valueOf(rs.getString("status"));
        MemberType type = MemberType.valueOf(rs.getString("member_type"));
        
        switch(type) {
            case STUDENT -> {
                return new StudentMember(memberID, fullName, contactNumber, email, status);
            }
            case FACULTY -> {
                return new FacultyMember(memberID, fullName, contactNumber, email, status);
            }
            default -> throw new SQLException("This is an unsupported member type: " + type);
        }
    }
}
