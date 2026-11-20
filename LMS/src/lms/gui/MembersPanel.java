/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.gui;

import lms.service.LibraryService;
import lms.model.*;
import lms.exception.LibraryException;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author
 */
public class MembersPanel extends JPanel {

    private final LibraryService service;
    private final JTable memberTable;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;
    private List<Member> members = new java.util.ArrayList<>();

    public MembersPanel(LibraryService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Member Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JButton register = new JButton("Register Member");
        JButton edit = new JButton("Edit Member");
        JButton status = new JButton("Change Status");
        JButton refresh = new JButton("Refresh");
        searchField = new JTextField(15);

        JPanel buttons = new JPanel();
        buttons.add(register);
        buttons.add(edit);
        buttons.add(status);
        buttons.add(refresh);
        buttons.add(new JLabel("Search:"));
        buttons.add(searchField);

        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.NORTH);
        header.add(buttons, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        String[] columns = {
            "Member ID", "Full Name", "Type", "Contact Number",
            "Email", "Status", "Borrowing Limit"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        memberTable = new JTable(tableModel);
        memberTable.setRowHeight(28);
        memberTable.setFillsViewportHeight(true);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableModel);
        memberTable.setRowSorter(sorter);
        add(new JScrollPane(memberTable), BorderLayout.CENTER);

        register.addActionListener(e -> showMemberForm(null));
        edit.addActionListener(e -> {
            Member selected = selectedMember();
            if (selected != null) {
                showMemberForm(selected);
            }
        });
        status.addActionListener(e -> changeStatus());
        refresh.addActionListener(e -> {
            searchField.setText("");
            loadMembers();
        });

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchMembers();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchMembers();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchMembers();
            }
        });

        loadMembers();
    }

    public void loadMembers() {
        new SwingWorker<List<Member>, Void>() {
            @Override
            protected List<Member> doInBackground() throws Exception {
                return service.getAllMembers();
            }

            @Override
            protected void done() {
                try {
                    members = get();
                    tableModel.setRowCount(0);

                    for (Member m : members) {
                        tableModel.addRow(new Object[]{
                            m.getMemberID(), m.getFullName(),
                            m.getMemberType(), m.getContactNumber(),
                            m.getEmail(), m.getStatus(),
                            m.getBorrowingLimit()
                        });
                    }
                } catch (Exception e) {
                    showError(e);
                }
            }
        }.execute();
    }

    private void searchMembers() {
        String keyword = searchField.getText().trim();
        sorter.setRowFilter(keyword.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), 0, 1, 2, 3, 4, 5));
    }

    private Member selectedMember() {
        int row = memberTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a member first.");
            return null;
        }

        int modelRow = memberTable.convertRowIndexToModel(row);
        String memberID = tableModel.getValueAt(modelRow, 0).toString();

        return members.stream()
                .filter(m -> m.getMemberID().equals(memberID))
                .findFirst().orElse(null);
    }

    private void showMemberForm(Member existing) {
        boolean creating = existing == null;

        JTextField id = new JTextField(creating ? "" : existing.getMemberID());
        JTextField name = new JTextField(creating ? "" : existing.getFullName());
        JTextField phone = new JTextField(creating ? "" : existing.getContactNumber());
        JTextField email = new JTextField(creating ? "" : existing.getEmail());

        JComboBox<MemberType> type = new JComboBox<>(MemberType.values());
        JComboBox<MemberStatus> status = new JComboBox<>(MemberStatus.values());

        if (!creating) {
            type.setSelectedItem(existing.getMemberType());
            status.setSelectedItem(existing.getStatus());
        }

        id.setEditable(creating);
        type.setEnabled(creating);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Member ID:"));
        form.add(id);
        form.add(new JLabel("Full Name:"));
        form.add(name);
        form.add(new JLabel("Contact Number:"));
        form.add(phone);
        form.add(new JLabel("Email:"));
        form.add(email);
        form.add(new JLabel("Member Type:"));
        form.add(type);
        form.add(new JLabel("Status:"));
        form.add(status);

        int result = JOptionPane.showConfirmDialog(this, form,
                creating ? "Register Member" : "Edit Member",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String memberID = id.getText().trim();
            String fullName = name.getText().trim();
            String contact = phone.getText().trim();
            String mail = email.getText().trim();

            MemberType memberType = (MemberType) type.getSelectedItem();
            MemberStatus memberStatus = (MemberStatus) status.getSelectedItem();

            Member member = switch (memberType) {
                case STUDENT ->
                    new StudentMember(
                    memberID, fullName, contact, mail, memberStatus);
                case FACULTY ->
                    new FacultyMember(
                    memberID, fullName, contact, mail, memberStatus);
            };

            runMemberOperation(() -> creating
                    ? service.registerMember(member)
                    : service.updateMember(member),
                    creating ? "Member registered!" : "Member updated!");

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void changeStatus() {
        Member selected = selectedMember();
        if (selected == null) {
            return;
        }

        JComboBox<MemberStatus> options = new JComboBox<>(MemberStatus.values());
        options.setSelectedItem(selected.getStatus());

        int result = JOptionPane.showConfirmDialog(this, options,
                "Change Member Status", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        MemberStatus newStatus = (MemberStatus) options.getSelectedItem();

        runMemberOperation(
                () -> service.changeMemberStatus(selected.getMemberID(), newStatus),
                "Member status updated!"
        );
    }

    @FunctionalInterface
    private interface MemberOperation {
        boolean execute() throws SQLException, LibraryException;
    }

    private void runMemberOperation(MemberOperation operation, String message) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return operation.execute();
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(MembersPanel.this, message);
                        loadMembers();
                    } else {
                        JOptionPane.showMessageDialog(MembersPanel.this,
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
                        : "Unable to complete the operation. Check the database connection or constraints.";

        JOptionPane.showMessageDialog(this, message,
                "Member Management Error", JOptionPane.ERROR_MESSAGE);
    }
}
