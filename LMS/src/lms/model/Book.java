/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.model;

import java.time.Year;

/**
 *
 * @author Mary
 */
public class Book {
    private final String bookID;
    private String isbn;    
    private String title;    
    private String author;    
    private String category;    
    private String publisher;    
    private int publicationYear;
    private int totalCopies;
    
    
    public Book(String bookID, String isbn, String title, String author ,String category,
    String publisher, int publicationYear, int totalCopies) {
        this.bookID = requireText(bookID, "Book ID");
        setIsbn(isbn);
        setTitle(title);
        setAuthor(author);
        setCategory(category);
        setPublisher(publisher);
        setPublicationYear(publicationYear);
        setTotalCopies(totalCopies);
    }   
    
    //Getters
    public String getBookID() {
        return bookID;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public String getPublisher() {
        return publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    //Setters
    public void setIsbn(String isbn) {
        this.isbn = (isbn == null || isbn.isBlank()) ? null : isbn.trim();
    }

    public void setTitle(String title) {
        this.title = requireText(title, "Title");
    }

    public void setAuthor(String author) {
        this.author = requireText(author, "Author");
    }

    public void setCategory(String category) {
        this.category = requireText(category, "Category");
    }

    public void setPublisher(String publisher) {
        this.publisher = requireText(publisher, "Publisher");
    }

    public void setPublicationYear(int publicationYear) {
        if(publicationYear < 1 || publicationYear > Year.now().getValue()) {
            throw new IllegalArgumentException ("Invalid Publication Year");
        }
        this.publicationYear = publicationYear;
    }

    public void setTotalCopies(int totalCopies) {
        if(totalCopies < 0) {
            throw new IllegalArgumentException ("Total copies cannot be negative.");
        }
        this.totalCopies = totalCopies;
    }

    private String requireText(String value, String fieldName) {
        if(value == null || value.isBlank()) {
            throw new IllegalArgumentException (fieldName + " cannot be empty.");
        }
        return value.trim();
    }   
    
    @Override
    public String toString() {
        return bookID + " - " + title;
    }
}
