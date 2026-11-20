/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.gui;

import lms.service.LibraryService;
import lms.model.Loan;
import lms.model.LoanStatus;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author 
 */
public class ReportsPanel extends JPanel {

    private final LibraryService service;
    private final JTable reportTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> filter;

    public ReportsPanel(LibraryService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Library Loan Reports");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        filter = new JComboBox<>(new String[]{
            "All Loans", "Active", "Returned", "Overdue"
        });

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadReports());
        filter.addActionListener(e -> loadReports());

        JPanel controls = new JPanel();
        controls.add(new JLabel("Filter:"));
        controls.add(filter);
        controls.add(refresh);

        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] columns = {
            "Loan ID", "Book", "Member", "Borrow Date",
            "Due Date", "Return Date", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(28);
        reportTable.setAutoCreateRowSorter(true);
        add(new JScrollPane(reportTable), BorderLayout.CENTER);

        loadReports();
    }

    public void loadReports() {
        String selected = (String) filter.getSelectedItem();

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
                        boolean overdue = loan.getStatus() == LoanStatus.ACTIVE
                                && loan.getDueDate().isBefore(LocalDate.now());

                        boolean include = switch (selected) {
                            case "Active" ->
                                loan.getStatus() == LoanStatus.ACTIVE;
                            case "Returned" ->
                                loan.getStatus() == LoanStatus.RETURNED;
                            case "Overdue" ->
                                overdue;
                            default ->
                                true;
                        };

                        if (!include) {
                            continue;
                        }

                        tableModel.addRow(new Object[]{
                            loan.getLoanID(),
                            loan.getBook().getTitle(),
                            loan.getMember().getFullName(),
                            loan.getBorrowDate(),
                            loan.getDueDate(),
                            loan.getReturnDate(),
                            overdue ? "OVERDUE" : loan.getStatus()
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                            "Unable to load loan reports.",
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
