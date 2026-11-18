/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author Mary
 */
public class Loan {
    private final long loanID;
    private final Book book;
    private final Member member;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    
    private LocalDate returnDate;
    private LoanStatus status;

    public Loan(long loanID, Book book, Member member, LocalDate borrowDate, LocalDate dueDate) {
        if(loanID <= 0) {
            throw new IllegalArgumentException("Loan ID must be positive.");
        }
        
        this.loanID = loanID;
        this.book = Objects.requireNonNull(book, "Book cannot be null.");
        this.member = Objects.requireNonNull(member, "Member cannot be null.");
        
        if(borrowDate == null || dueDate == null) {
            throw new IllegalArgumentException("Borrow and due dates cannot be null.");
        }
        
        if(dueDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Due date cannot be before borrow date.");
        }
        
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.status = LoanStatus.ACTIVE;
    }
    
    //Getters
    public long getLoanID() {
        return loanID;
    }

    public Book getBook() {
        return book;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }
    
    public boolean isActive() {
        return status == LoanStatus.ACTIVE;
    }
    
    public boolean isOverdue() {
        return isActive()&& LocalDate.now().isAfter(dueDate);
    }
    
    public void markReturned(LocalDate returnDate) {
        if(!isActive()) {
            throw new IllegalArgumentException("This loan was already returned.");
        }
        if(returnDate == null) {
            throw new IllegalArgumentException("Return date cannot be null.");
        }
        if(returnDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Return date cannot be before borrow date.");
        }
        this.returnDate = returnDate;
        this.status = LoanStatus.RETURNED;
    }
    
    @Override
    public String toString() {
        return "Loan #" + loanID + " - " + book.getTitle() + " - " + member.getFullName();
    }
}
