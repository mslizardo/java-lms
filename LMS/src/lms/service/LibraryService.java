/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.service;

import lms.dao.BookDAO;
import lms.dao.MemberDAO;
import lms.dao.LoanDAO;
import lms.database.DatabaseConnection;
import lms.exception.LibraryException;
import lms.model.Book;
import lms.model.Member;
import lms.model.Loan;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

/**
 *
 * @author Mary
 */
public class LibraryService {
    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;
    private final LoanDAO loanDAO;

    public LibraryService() {
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
        this.loanDAO = new LoanDAO();
    }
    
    public Loan borrowBook(String memberID, String bookID, LocalDate borrowDate, LocalDate dueDate) throws SQLException, LibraryException {
        if(borrowDate == null || dueDate == null) {
            throw new LibraryException("Borrow and due dates are required.");
        }
        if(dueDate.isBefore(LocalDate.now())) {
            throw new LibraryException("Due date cannot be before borrowing date.");
        }
        if(borrowDate.isAfter(LocalDate.now())) {
            throw new LibraryException("Borrowed date cannot be in the future.");
        }
        
        try(Connection conn = DatabaseConnection.getConnection()) {
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            
            try {
                Member member = memberDAO.findByID(conn, memberID).orElseThrow(() -> new LibraryException("Member not found."));
                if(!member.isActive()) {
                    throw new LibraryException("Member account is inactive.");
                }
                
                Book book = bookDAO.findByID(conn, bookID).orElseThrow(() -> new LibraryException("Member not found."));
                
                int activeLoans = loanDAO.countActiveLoans(conn, memberID);
                
                if(activeLoans >= member.getBorrowingLimit()) {
                    throw new LibraryException("Member has reached the borrowing limit.");
                }
                
                int borrowedCopies = loanDAO.countActiveBookLoans(conn, bookID);
                int availableCopies = book.getTotalCopies() - borrowedCopies;
                if(availableCopies <= 0) {
                    throw new LibraryException("No available copies of this book");
                }
                
                if (loanDAO.hasActiveLoan(conn, memberID, bookID)) {
                    throw new LibraryException(
                            "Member already has an active loan "
                            + "for this book."
                    );
                }

                long loanID = loanDAO.insert(conn, book, member, borrowDate, dueDate);
                conn.commit();
                
                return new Loan(loanID, book, member, borrowDate, dueDate);
            } catch(SQLException | LibraryException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                } throw e;
            }
        }
    }
    
    public boolean returnBook(long loanID, LocalDate returnDate) throws SQLException, LibraryException {
        if(loanID <= 0) {
            throw new LibraryException("Invalid loan ID.");
        }
        if(returnDate == null) {
            throw new LibraryException("Return date is required.");
        }
        if(returnDate.isAfter(LocalDate.now())) {
            throw new LibraryException("Return date cannot be in the future.");
        }
        
        try(Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                
                LocalDate borrowDate = loanDAO.findActiveBorrowDate(conn, loanID);

                if (borrowDate == null) {
                    throw new LibraryException("Loan not found or already returned.");
                }

                if(returnDate.isBefore(borrowDate)) {
                    throw new LibraryException("Return date cannot be before borrow date.");
                }

                boolean returned = loanDAO.markReturned(conn, loanID, returnDate);

                if(!returned) {
                    throw new LibraryException("Unable to complete the return.");
                }

                conn.commit();

                return true;
            } catch(SQLException | LibraryException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch(SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                } throw e;
            }
        }
    }
    
    public boolean updateBook(Book book) throws SQLException, LibraryException {
        if(book == null) {
            throw new LibraryException("Book information is required.");
        }
        try(Connection conn = DatabaseConnection.getConnection()) {
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            
            try {
                  Book existing = bookDAO.findByID(conn, book.getBookID()).orElseThrow(() 
                        -> new LibraryException("Book record not found."));
                  
                  int activeLoans = loanDAO.countActiveBookLoans(conn, existing.getBookID());
                    System.out.println("=== UPDATE BOOK DEBUG ===");
                    System.out.println("Book ID: " + existing.getBookID());
                    System.out.println("Current DB copies: " + existing.getTotalCopies());
                    System.out.println("Requested copies: " + book.getTotalCopies());
                    System.out.println("Active loans: " + activeLoans);
                    
                  if (book.getTotalCopies() < activeLoans) {
                      throw new LibraryException(
                              "Cannot reduce total copies below "
                              + activeLoans + " active loans."
                      );
                  }
                  boolean updated = bookDAO.update(conn, book);
                  if(!updated) {
                      throw new LibraryException("Book update failed.");
                  }
                  conn.commit();
                  return true;
                
            } catch (SQLException | LibraryException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch(SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                } throw e;
            }
        }
    }
    
    public boolean deleteBook(String bookID) throws SQLException, LibraryException {
        if(bookID == null || bookID.isBlank()) {
            throw new LibraryException("Book ID is required.");
        }
        try(Connection conn = DatabaseConnection.getConnection()) {
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            
            try {
                  bookDAO.findByID(conn, bookID).orElseThrow(() 
                          -> new LibraryException("Book record not found."));
                  
                  int loanHistory = loanDAO.countActiveBookLoans(conn, bookID);
                  if(loanHistory > 0) {
                      throw new LibraryException("Cannot delete this book because it has borrowing history.");
                  }
                  
                  boolean deleted = bookDAO.delete(conn, bookID);
                  if(!deleted) {
                      throw new LibraryException("Book deletion failed.");
                  }
                  conn.commit();
                  return true;
            } catch (SQLException | LibraryException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch(SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                } throw e;
            }
        }
    }
}
