/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.gui;

import lms.model.Book;
import lms.model.BookInventory;
import lms.service.LibraryService;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Adriel Justine Danga
 */
public class BooksPanel extends JPanel {
    private final LibraryService service;
    private final Runnable afterChange;
    private final JTextField search = new JTextField(22);
    private final JTextField id = new JTextField();
    private final JTextField isbn = new JTextField();
    private final JTextField title = new JTextField();
    private final JTextField author = new JTextField();
    private final JTextField category = new JTextField();
    private final JTextField publisher = new JTextField();
    private final JTextField year = new JTextField();
    private final JTextField copies = new JTextField();
    private final DefaultTableModel model = Ui.tableModel("Book ID", "ISBN", "Title", "Author", "Category", "Total", "On loan", "Available");
    private final JTable table = new JTable(model);
    private final List<BookInventory> rows = new ArrayList<>();
    private boolean editing;

    public BooksPanel(LibraryService service, Runnable afterChange) {
        this.service = service;
        this.afterChange = afterChange;
        setLayout(new BorderLayout(18, 18));
        setBackground(Ui.PALE);
        setBorder(BorderFactory.createEmptyBorder(26, 30, 28, 30));

        JPanel top = new JPanel(new BorderLayout(0, 15));
        top.setOpaque(false);
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(Ui.title("Book Records"), BorderLayout.NORTH);
        heading.add(Ui.subtitle("Add, update, search, and track available copies."), BorderLayout.SOUTH);
        top.add(heading, BorderLayout.NORTH);
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.add(new JLabel("Search books"), BorderLayout.WEST);
        search.setToolTipText("Search by ID, ISBN, title, author, or category");
        searchRow.add(search, BorderLayout.CENTER);
        JButton refresh = Ui.button("Refresh", false);
        refresh.addActionListener(e -> refresh());
        searchRow.add(refresh, BorderLayout.EAST);
        top.add(searchRow, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Ui.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) selectRow(table.getSelectedRow());
        });
        JScrollPane tableScroll = Ui.scroll(table);

        JPanel form = formPanel();
        form.setPreferredSize(new Dimension(285, 500));
        add(form, BorderLayout.WEST);
        add(tableScroll, BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(java.awt.Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Ui.LINE),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 0, 3, 0);
        int y = 0;
        y = field(panel, c, y, "Book ID", id);
        y = field(panel, c, y, "ISBN (optional)", isbn);
        y = field(panel, c, y, "Title", title);
        y = field(panel, c, y, "Author", author);
        y = field(panel, c, y, "Category", category);
        y = field(panel, c, y, "Publisher", publisher);
        y = field(panel, c, y, "Publication year", year);
        y = field(panel, c, y, "Total copies", copies);

        JButton save = Ui.button("Save book", true);
        save.addActionListener(e -> saveBook());
        c.gridy = y++; c.insets = new Insets(12, 0, 5, 0);
        panel.add(save, c);
        JButton delete = Ui.button("Delete selected", false);
        delete.addActionListener(e -> deleteBook());
        c.gridy = y++; c.insets = new Insets(4, 0, 5, 0);
        panel.add(delete, c);
        JButton clear = Ui.button("Clear form", false);
        clear.addActionListener(e -> clearForm());
        c.gridy = y; panel.add(clear, c);
        return panel;
    }

    private int field(JPanel panel, GridBagConstraints c, int y, String label, JTextField input) {
        JLabel caption = new JLabel(label);
        caption.setForeground(Ui.MUTED);
        caption.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 11));
        c.gridy = y++; panel.add(caption, c);
        c.gridy = y++; panel.add(input, c);
        return y;
    }

    public void refresh() {
        String keyword = search.getText();
        Ui.async(this, () -> service.getBooks(keyword), data -> {
            rows.clear(); rows.addAll(data); model.setRowCount(0);
            for (BookInventory item : rows) {
                Book b = item.book();
                model.addRow(new Object[]{b.getBookID(), b.getIsbn() == null ? "—" : b.getIsbn(), b.getTitle(),
                        b.getAuthor(), b.getCategory(), b.getTotalCopies(), item.borrowedCopies(), item.availableCopies()});
            }
        });
    }

    private void selectRow(int index) {
        if (index >= rows.size()) return;
        Book book = rows.get(index).book();
        editing = true;
        id.setText(book.getBookID()); id.setEnabled(false);
        isbn.setText(book.getIsbn() == null ? "" : book.getIsbn());
        title.setText(book.getTitle()); author.setText(book.getAuthor()); category.setText(book.getCategory());
        publisher.setText(book.getPublisher()); year.setText(String.valueOf(book.getPublicationYear()));
        copies.setText(String.valueOf(book.getTotalCopies()));
    }

    private void saveBook() {
        try {
            String bookID = id.getText().trim();
            if (bookID.isEmpty()) throw new IllegalArgumentException("Book ID cannot be empty.");
            Book book = new Book(bookID, isbn.getText(), title.getText(), author.getText(), category.getText(),
                    publisher.getText(), Integer.parseInt(year.getText().trim()), Integer.parseInt(copies.getText().trim()));
            boolean creating = !editing;
            Ui.async(this, () -> { service.saveBook(book, creating); return true; }, done -> {
                JOptionPane.showMessageDialog(this, creating ? "Book added successfully." : "Book updated successfully.",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
                clearForm(); refresh(); afterChange.run();
            });
        } catch (NumberFormatException ex) {
            Ui.error(this, new IllegalArgumentException("Publication year and total copies must be whole numbers."));
        } catch (Exception ex) { Ui.error(this, ex); }
    }

    private void deleteBook() {
        if (!editing) { JOptionPane.showMessageDialog(this, "Select a book from the table first."); return; }
        int choice = JOptionPane.showConfirmDialog(this, "Delete book “" + id.getText() + "”?", "Confirm deletion", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;
        String bookID = id.getText();
        Ui.async(this, () -> { service.deleteBook(bookID); return true; }, done -> {
            JOptionPane.showMessageDialog(this, "Book deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); refresh(); afterChange.run();
        });
    }

    private void clearForm() {
        editing = false; id.setEnabled(true);
        id.setText(""); isbn.setText(""); title.setText(""); author.setText(""); category.setText("");
        publisher.setText(""); year.setText(""); copies.setText(""); table.clearSelection();
    }
}
