/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.dao;

import lms.database.DatabaseConnection;
import lms.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

/**
 *
 * @author Mary
 */
public class LoanDAO {
    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    
    public long insert(Connection conn, Book book, Member member, LocalDate borrowDate, LocalDate dueDate) throws SQLException {
        String sql = """
                     INSERT INTO loans(book_id, member_id, borrow_date, due_date, status)
                     VALUES(?, ?, ?, ?, 'ACTIVE')""";
        
        try(PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getBookID());
            stmt.setString(2, member.getMemberID());
            stmt.setDate(3, Date.valueOf(borrowDate));
            stmt.setDate(4, Date.valueOf(dueDate));
            
            int affected = stmt.executeUpdate();
            
            if (affected != 1) {
                throw new SQLException("Loan insertion failed");
            }
            
            try(ResultSet keys = stmt.getGeneratedKeys()) {
                if(keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new SQLException("Loan ID was not generated");
        }
    }
    
    public Optional<Loan> findByID(long loanID)throws SQLException {
        String sql = "SELECT * FROM loans WHERE loan_id = ?";
        
        try(Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, loanID);
            
            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    public ArrayList<Loan> findAll() throws SQLException {
        String sql = "SELECT * FROM loans ORDER BY borrow_date DESC";
        
        return findMany(sql, null);
    }
    
    public ArrayList<Loan> findByStatus(LoanStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Loan status cannot be null");
        }
        
        String sql = """
                     SELECT * FROM loans WHERE status = ?
                     ORDER BY borrow_date DESC""";
        return findMany(sql, status.name());
    }
    
    public ArrayList<Loan> findOverdue() throws SQLException {
        String sql = """
                     SELECT * FROM loans
                     WHERE status = 'ACTIVE' AND due_date < CURRENT_DATE()
                     ORDER BY due_date ASC""";
        
        return findMany(sql, null);
    }
    
    private ArrayList<Loan> findMany(String sql, String parameter) throws SQLException {
         ArrayList<Loan> loans = new ArrayList<>();
         
          try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
              if (parameter != null) {
                  stmt.setString(1, parameter);
              }
              try(ResultSet rs = stmt.executeQuery()) {
                  while (rs.next()) {                      
                      loans.add(mapRow(rs));
                  }
              }
          }
          return loans;
    }
    
     public boolean markReturned(Connection conn, long loanID, LocalDate returnDate) throws SQLException {
         String sql = """
                    UPDATE loans
                    SET return_date = ?, status = 'RETURNED'
                    WHERE loan_id = ? AND status = 'ACTIVE' AND borrow_date <= ? """;
         
         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
             Date sqlDate = Date.valueOf(returnDate);
             
             stmt.setDate(1, sqlDate);
             stmt.setLong(2, loanID);
             stmt.setDate(3, sqlDate);
             
             return stmt.executeUpdate() == 1;
         }
     }
     
     public int countActiveLoans(Connection conn, String memberID) throws SQLException {
         String sql = """
                      SELECT COUNT(*) AS total FROM loans
                      WHERE member_id = ? AND status = 'ACTIVE'""";
         
         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setString(1, memberID);
             
             try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("total");
            }
         }
     }
     
     public int CountActiveBookLoans(Connection conn, String bookID) throws SQLException {
         String sql = """
                SELECT COUNT(*) AS total FROM loans
                WHERE book_id = ? AND status = 'ACTIVE'""";
         
         try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bookID);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("total");
            }
        }
     }
     
     private Loan mapRow(ResultSet rs) throws SQLException {
         long loanID = rs.getLong("loan_id");
         String bookID = rs.getString("book_id");
         String memberID = rs.getString("member_id");
         
         Book book = bookDAO.findByID(bookID).orElseThrow(() -> new SQLException("Book not found: " + bookID));
         Member member = memberDAO.findByID(memberID).orElseThrow(() -> new SQLException("Member not found: " + memberID));
         
         LocalDate borrowDate = rs.getDate("borrow_date").toLocalDate();

         LocalDate dueDate = rs.getDate("due_date").toLocalDate();
         
         Loan loan = new Loan(loanID, book, member, borrowDate, dueDate);
         
         if("RETURNED".equals(rs.getString("status"))) {
             Date returned = rs.getDate("return_date");
             
             if(returned == null) {
                 throw new SQLException("Returned loan has no return date.");
             }
             
             loan.markReturned(returned.toLocalDate());
         }
         return loan;
     }
}
