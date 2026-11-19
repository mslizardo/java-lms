/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.dao;

import lms.database.DatabaseConnection;
import lms.model.Book;

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
public class BookDAO {
    public boolean insert(Book book) throws SQLException {
        String sql = """
                     INSERT INTO books(book_id, isbn, title, author, category, publisher, publication_year, total_copies) 
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?)""";
            
        try(Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getBookID());
            stmt.setString(2, book.getIsbn());
            stmt.setString(3, book.getTitle());
            stmt.setString(4, book.getAuthor());
            stmt.setString(5, book.getCategory());
            stmt.setString(6, book.getPublisher());
            stmt.setInt(7, book.getPublicationYear());
            stmt.setInt(8, book.getTotalCopies());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public Optional<Book> findById(String bookID) throws SQLException {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        
        try(Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookID);
            
            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    public ArrayList<Book> findAll() throws SQLException {
        ArrayList<Book> books = new ArrayList<>();
        
        String sql = "SELECT * FROM books ORDER BY title";
        
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        }
        return books;
    }
    
    public boolean update(Book book) throws SQLException {
        String sql = """
                     UPDATE books 
                     SET isbn = ?, title = ?, author = ?, category = ?, publisher = ?, publication_year = ?, total_copies = ? 
                     WHERE book_id = ?""";
    
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {           
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getCategory());
            stmt.setString(5, book.getPublisher());
            stmt.setInt(6, book.getPublicationYear());
            stmt.setInt(7, book.getTotalCopies());
            stmt.setString(8, book.getBookID());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(String bookID) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookID);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public ArrayList<Book> search(String keyword) throws SQLException {
        ArrayList<Book> books = new ArrayList<>();
        
        String sql = """
                     SELECT * FROM books 
                     WHERE book_id LIKE ? OR isbn LIKE ? OR title LIKE ? OR author LIKE ? OR category LIKE ? 
                     ORDER BY title""";
        
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            
            for (int i = 1; i <= 5; i++) {
                stmt.setString(i, pattern);
            }
        
            try(ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));  
                }
            }
        }
        return books;
    }
            
     private Book mapRow(ResultSet rs)throws SQLException {

        return new Book(
                rs.getString("book_id"),
                rs.getString("isbn"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("category"),
                rs.getString("publisher"),
                rs.getInt("publication_year"),
                rs.getInt("total_copies")
        );
    }
}
