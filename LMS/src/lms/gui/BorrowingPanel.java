/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.gui;

import lms.service.LibraryService;
import lms.model.Loan;
import lms.exception.LibraryException;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Mary
 */
public class BorrowingPanel extends JPanel {

    private final LibraryService service;
    private final JTable loanTable;
    private final DefaultTableModel tableModel;

    public BorrowingPanel(LibraryService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Borrowing and Returns");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JButton borrowButton = new JButton("Borrow Book");
        JButton returnButton = new JButton("Return Book");
        JButton refreshButton = new JButton("Refresh");

        JPanel buttons = new JPanel();
        buttons.add(borrowButton);
        buttons.add(returnButton);
        buttons.add(refreshButton);

        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.NORTH);
        header.add(buttons, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        String[] columns = {
            "Loan ID", "Book ID", "Book Title", "Member ID",
            "Member Name", "Borrow Date", "Due Date",
            "Return Date", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loanTable = new JTable(tableModel);
        loanTable.setRowHeight(28);
        loanTable.setAutoCreateRowSorter(true);
        loanTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loanTable.setFillsViewportHeight(true);

        add(new JScrollPane(loanTable), BorderLayout.CENTER);

        borrowButton.addActionListener(e -> showBorrowForm());
        returnButton.addActionListener(e -> returnSelectedLoan());
        refreshButton.addActionListener(e -> loadLoans());

        loadLoans();
    }

    public void loadLoans() {
        new SwingWorker<List<Loan>, Void>() {
            @Override
            protected List<Loan> doInBackground() throws Exception {
                return service.getAllLoans();
            }

            @Override
            protected void done() {
                try {
                    List<Loan> loans = get();
                    tableModel.setRowCount(0);

                    for (Loan loan : loans) {
                        tableModel.addRow(new Object[]{
                            loan.getLoanID(),
                            loan.getBook().getBookID(),
                            loan.getBook().getTitle(),
                            loan.getMember().getMemberID(),
                            loan.getMember().getFullName(),
                            loan.getBorrowDate(),
                            loan.getDueDate(),
                            loan.getReturnDate(),
                            loan.getStatus()
                        });
                    }
                } catch (Exception e) {
                    showError(e);
                }
            }
        }.execute();
    }

    private void showBorrowForm() {
        JTextField memberID = new JTextField();
        JTextField bookID = new JTextField();
        JTextField dueDate = new JTextField(
                LocalDate.now().plusDays(7).toString()
        );

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Member ID:"));
        form.add(memberID);
        form.add(new JLabel("Book ID:"));
        form.add(bookID);
        form.add(new JLabel("Due Date (YYYY-MM-DD):"));
        form.add(dueDate);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Borrow Book", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String member = memberID.getText().trim();
        String book = bookID.getText().trim();

        if (member.isEmpty() || book.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Member ID and Book ID are required.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate due = LocalDate.parse(dueDate.getText().trim());

            runOperation(() -> {
                service.borrowBook(member, book, LocalDate.now(), due);
                return true;
            }, "Book borrowed successfully!");

        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid date using YYYY-MM-DD.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void returnSelectedLoan() {
        int selected = loanTable.getSelectedRow();

        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a loan first.");
            return;
        }

        int row = loanTable.convertRowIndexToModel(selected);
        long loanID = ((Number) tableModel.getValueAt(row, 0)).longValue();
        String status = tableModel.getValueAt(row, 8).toString();

        if (!"ACTIVE".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "This loan is already returned.",
                    "Return Rejected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Return loan #" + loanID + "?",
                "Confirm Return", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        runOperation(() -> service.returnBook(loanID, LocalDate.now()),
                "Book returned successfully!");
    }

    @FunctionalInterface
    private interface LoanOperation {

        boolean execute() throws Exception;
    }

    private void runOperation(LoanOperation operation, String message) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return operation.execute();
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                BorrowingPanel.this, message);
                        loadLoans();
                    } else {
                        JOptionPane.showMessageDialog(
                                BorrowingPanel.this,
                                "Operation was not completed.");
                    }
                } catch (Exception e) {
                    showError(e);
                }
            }
        }.execute();
    }

    private void showError(Exception e) {
        Throwable cause = e;
        if (e instanceof ExecutionException && e.getCause() != null) {
            cause = e.getCause();
        }

        String message = cause instanceof LibraryException
                || cause instanceof IllegalArgumentException
                        ? cause.getMessage()
                        : "Database operation failed. Check the connection or records.";

        JOptionPane.showMessageDialog(this, message,
                "Borrowing Error", JOptionPane.ERROR_MESSAGE);
    }
}
