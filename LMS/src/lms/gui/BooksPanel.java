/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import lms.service.LibraryService;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import lms.model.Book;
import lms.exception.LibraryException;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import java.util.regex.Pattern;

/**
 *
 * @author Mary
 */
public class BooksPanel extends JPanel { 
    private final JTable bookTable;
    private final DefaultTableModel tableModel;
    private final LibraryService service;
    private final JTextField searchField;
    private final TableRowSorter<DefaultTableModel> sorter;
    
    public BooksPanel(LibraryService service) {
        this.service = service;
        
        setLayout(new BorderLayout(10, 10));
        JLabel title = new JLabel("Book Catalog Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        JButton registerButton = new JButton("Register Book");
        registerButton.addActionListener(e -> showRegistrationForm());
        
        JButton editButton = new JButton("Edit Book");
        editButton.addActionListener(e -> showEditForm());

        JButton deleteButton = new JButton("Delete Book");
        deleteButton.addActionListener(e -> deleteSelectedBook());
        
        JButton refreshButton = new JButton("Refresh");
        
        
        JPanel buttons = new JPanel();
        buttons.add(registerButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        buttons.add(refreshButton);

        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.WEST);
        header.add(buttons, BorderLayout.EAST);
        
        searchField = new JTextField(18);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchBooks());

        searchField.addActionListener(e -> searchBooks());

        buttons.add(new JLabel("Search:"));
        buttons.add(searchField);
        buttons.add(searchButton);
        
        add(header, BorderLayout.NORTH);
        
        String[] columns = {"Book ID", "ISBN", "Title", "Author", "Category", "Total", "Borrowed", "Available"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column){
            return false;
            }
        };
        bookTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        bookTable.setRowSorter(sorter);
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            sorter.setRowFilter(null);
            loadBooks();
        });
        
        bookTable.setRowHeight(28);
        bookTable.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, BorderLayout.CENTER);
        
        loadBooks();
        }
                
    private void loadBooks() {
        SwingWorker<ArrayList<Object[]>, Void> worker = new SwingWorker<>() {

            @Override
            protected ArrayList<Object[]> doInBackground()
                throws Exception {
                    return service.getBookInventory();
                }

            @Override
            protected void done() {
                 try {
                    ArrayList<Object[]> books = get();
                    tableModel.setRowCount(0);

                    for (Object[] book : books) {
                            tableModel.addRow(book);
                    }
                 } catch (Exception e) {

                    JOptionPane.showMessageDialog(BooksPanel.this, "Unable to load books: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                  }
             }
        };
        worker.execute();
    }
    
    private void showRegistrationForm() {
        JTextField bookID = new JTextField();
        JTextField isbn = new JTextField();
        JTextField title = new JTextField();
        JTextField author = new JTextField();
        JTextField category = new JTextField();
        JTextField publisher = new JTextField();
        JTextField year = new JTextField();
        JTextField copies = new JTextField();
        
        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
        
        form.add(new JLabel("Book ID:"));
        form.add(bookID);

        form.add(new JLabel("ISBN (Optional):"));
        form.add(isbn);

        form.add(new JLabel("Title:"));
        form.add(title);

        form.add(new JLabel("Author:"));
        form.add(author);

        form.add(new JLabel("Category:"));
        form.add(category);

        form.add(new JLabel("Publisher:"));
        form.add(publisher);

        form.add(new JLabel("Publication Year:"));
        form.add(year);

        form.add(new JLabel("Total Copies:"));
        form.add(copies);
        
        int result = JOptionPane.showConfirmDialog(this, form, "Register New Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        try {
            String isbnValue = isbn.getText().trim();
            if (isbnValue.isEmpty()) {
                isbnValue = null;
            }

            Book book = new Book(
                    bookID.getText().trim(), isbnValue, title.getText().trim(),
                    author.getText().trim(),category.getText().trim(),
                    publisher.getText().trim(),
                    Integer.parseInt(year.getText().trim()), Integer.parseInt(copies.getText().trim()));

            registerBookAsync(book);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Publication year and total copies must be valid numbers.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid Book Information", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void registerBookAsync(Book book) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                return service.registerBook(book);
            }

            @Override
            protected void done() {
                try {
                    boolean inserted = get();

                    if(inserted) {
                        JOptionPane.showMessageDialog(BooksPanel.this, "Book registered successfully!");
                        loadBooks();
                    } else {
                        JOptionPane.showMessageDialog(BooksPanel.this, "Book registration was not completed.", "Registration Failed", JOptionPane.WARNING_MESSAGE);
                    }

                } catch(Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(
                            BooksPanel.this,
                            cause instanceof LibraryException || cause instanceof IllegalArgumentException ? cause.getMessage()
                                : "Unable to register book. Check the database constraints or connection.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private String getSelectedBookID() {
        int selectedRow = bookTable.getSelectedRow();
        
        if (selectedRow == -1) {JOptionPane.showMessageDialog(this, "Please select a book first.", "No Book Selected", JOptionPane.WARNING_MESSAGE);

            return null;
        }
        
        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        return tableModel.getValueAt(modelRow, 0).toString();
    }
    
    private void showEditForm() {
        String selectedID = getSelectedBookID();

        if (selectedID == null) {
            return;
        }

        SwingWorker<Book, Void> worker = new SwingWorker<>() {
            @Override
            protected Book doInBackground() throws Exception {
                return service.getBookByID(selectedID);
            }
            @Override
            protected void done() {
                try {
                    Book existing = get();

                    JTextField isbn = new JTextField(existing.getIsbn() == null ? "" : existing.getIsbn());                    
                    JTextField title = new JTextField(existing.getTitle());                       
                    JTextField author = new JTextField(existing.getAuthor());
                    JTextField category = new JTextField(existing.getCategory());
                    JTextField publisher = new JTextField(existing.getPublisher());
                    JTextField year = new JTextField(String.valueOf(existing.getPublicationYear()));
                    JTextField copies = new JTextField(String.valueOf(existing.getTotalCopies()));

                    JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));

                    form.add(new JLabel("ISBN (Optional):"));
                    form.add(isbn);

                    form.add(new JLabel("Title:"));
                    form.add(title);

                    form.add(new JLabel("Author:"));
                    form.add(author);

                    form.add(new JLabel("Category:"));
                    form.add(category);

                    form.add(new JLabel("Publisher:"));
                    form.add(publisher);

                    form.add(new JLabel("Publication Year:"));
                    form.add(year);

                    form.add(new JLabel("Total Copies:"));
                    form.add(copies);

                    int result = JOptionPane.showConfirmDialog(BooksPanel.this, form, "Edit Book: " + selectedID,
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE );
                            
                    if (result != JOptionPane.OK_OPTION) {
                        return;
                    }

                    try {
                        String isbnValue = isbn.getText().trim();

                        if (isbnValue.isEmpty()) {
                            isbnValue = null;
                        }

                        Book updated = new Book(
                                selectedID,
                                isbnValue,
                                title.getText().trim(),
                                author.getText().trim(),
                                category.getText().trim(),
                                publisher.getText().trim(),
                                Integer.parseInt(year.getText().trim()),
                                Integer.parseInt(copies.getText().trim())
                        );

                        updateBookAsync(updated);

                    } catch (NumberFormatException ex) {

                        JOptionPane.showMessageDialog(BooksPanel.this,
                                "Year and copies must be valid numbers.", "Invalid Input", JOptionPane.WARNING_MESSAGE);

                    } catch (IllegalArgumentException ex) {

                        JOptionPane.showMessageDialog(
                                BooksPanel.this, ex.getMessage(), "Invalid Book", JOptionPane.WARNING_MESSAGE);
                    }

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            BooksPanel.this,
                            "Unable to load book: "
                            + rootMessage(ex),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }
    
    private void updateBookAsync(Book book) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                return service.updateBook(book);
            }
            @Override
            protected void done() {

                try {
                    boolean updated = get();

                    if (updated) {
                        JOptionPane.showMessageDialog(BooksPanel.this, "Book updated successfully!");
                        loadBooks();
                    } else {
                        JOptionPane.showMessageDialog(BooksPanel.this, "Book update was not completed.");
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            BooksPanel.this,
                            rootMessage(ex),
                            "Update Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }
    
    private void deleteSelectedBook() {
        String bookID = getSelectedBookID();
        
        if (bookID == null) {
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Delete book " + bookID + "?\n"
                + "Books with borrowing history cannot be deleted.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                return service.deleteBook(bookID);
            }

            @Override
            protected void done() {
                try {
                    boolean deleted = get();
                    if (deleted) {
                        JOptionPane.showMessageDialog(BooksPanel.this, "Book deleted successfully!");
                        loadBooks();
                    } else {
                        JOptionPane.showMessageDialog(BooksPanel.this, "Book was not deleted.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            BooksPanel.this,
                            rootMessage(ex),
                            "Deletion Rejected",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }
    
    private void searchBooks() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        String safeKeyword = Pattern.quote(keyword);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + safeKeyword, 0, 1, 2, 3, 4));
    }
    
    private String rootMessage(Exception exception) {
        Throwable cause = exception;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? "An unexpected error occurred." : cause.getMessage();
    }
}
