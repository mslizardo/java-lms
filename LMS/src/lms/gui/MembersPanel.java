/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.gui;
import lms.model.Member;
import lms.model.MemberStatus;
import lms.model.MemberType;
import lms.service.LibraryService;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
public class MembersPanel extends JPanel {
    private final LibraryService service;
    private final Runnable afterChange;
    private final JTextField search = new JTextField(22);
    private final JTextField id = new JTextField();
    private final JTextField name = new JTextField();
    private final JTextField phone = new JTextField();
    private final JTextField email = new JTextField();
    private final JComboBox<MemberType> type = new JComboBox<>(MemberType.values());
    private final JComboBox<MemberStatus> status = new JComboBox<>(MemberStatus.values());
    private final DefaultTableModel model = Ui.tableModel("Member ID", "Name", "Type", "Contact", "Email", "Status", "Loan limit");
    private final JTable table = new JTable(model);
    private final List<Member> rows = new ArrayList<>();
    private boolean editing;

    public MembersPanel(LibraryService service, Runnable afterChange) {
        this.service = service; this.afterChange = afterChange;
        setLayout(new BorderLayout(18, 18)); setBackground(Ui.PALE);
        setBorder(BorderFactory.createEmptyBorder(26, 30, 28, 30));
        JPanel top = new JPanel(new BorderLayout(0, 15)); top.setOpaque(false);
        JPanel heading = new JPanel(new BorderLayout()); heading.setOpaque(false);
        heading.add(Ui.title("Member Records"), BorderLayout.NORTH);
        heading.add(Ui.subtitle("Manage student and faculty accounts. Deactivate accounts to preserve loan history."), BorderLayout.SOUTH);
        top.add(heading, BorderLayout.NORTH);
        JPanel searchRow = new JPanel(new BorderLayout(10, 0)); searchRow.setOpaque(false);
        searchRow.add(new JLabel("Search members"), BorderLayout.WEST);
        search.setToolTipText("Search by member ID, name, email, or type"); searchRow.add(search, BorderLayout.CENTER);
        var refresh = Ui.button("Refresh", false); refresh.addActionListener(e -> refresh()); searchRow.add(refresh, BorderLayout.EAST);
        top.add(searchRow, BorderLayout.SOUTH); add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); Ui.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) selectRow(table.getSelectedRow());
        });
        JPanel form = formPanel(); form.setPreferredSize(new Dimension(285, 450)); add(form, BorderLayout.WEST);
        add(Ui.scroll(table), BorderLayout.CENTER);
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(java.awt.Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Ui.LINE), BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        GridBagConstraints c = new GridBagConstraints(); c.gridx=0; c.weightx=1; c.fill=GridBagConstraints.HORIZONTAL; c.insets=new Insets(4,0,4,0);
        int y=0; y=field(panel,c,y,"Member ID",id); y=field(panel,c,y,"Full name",name);
        y=field(panel,c,y,"Member type",type); y=field(panel,c,y,"Contact number",phone); y=field(panel,c,y,"Email address",email);
        y=field(panel,c,y,"Account status",status);
        var save=Ui.button("Save member",true); save.addActionListener(e -> saveMember()); c.gridy=y++; c.insets=new Insets(12,0,5,0); panel.add(save,c);
        var deactivate=Ui.button("Deactivate selected",false); deactivate.addActionListener(e -> deactivate()); c.gridy=y++; c.insets=new Insets(4,0,5,0); panel.add(deactivate,c);
        var clear=Ui.button("Clear form",false); clear.addActionListener(e -> clearForm()); c.gridy=y; panel.add(clear,c);
        return panel;
    }

    private int field(JPanel panel, GridBagConstraints c, int y, String label, java.awt.Component control) {
        JLabel caption=new JLabel(label); caption.setForeground(Ui.MUTED); caption.setFont(new java.awt.Font("SansSerif",java.awt.Font.BOLD,11));
        c.gridy=y++; panel.add(caption,c); c.gridy=y++; panel.add(control,c); return y;
    }

    public void refresh() {
        String keyword=search.getText();
        Ui.async(this, () -> service.getMembers(keyword), data -> {
            rows.clear(); rows.addAll(data); model.setRowCount(0);
            for(Member m:rows) model.addRow(new Object[]{m.getMemberID(),m.getFullName(),m.getMemberType(),m.getContactNumber(),m.getEmail(),m.getStatus(),m.getBorrowingLimit()});
        });
    }

    private void selectRow(int index) {
        if(index>=rows.size()) return; Member m=rows.get(index); editing=true;
        id.setText(m.getMemberID()); id.setEnabled(false); name.setText(m.getFullName()); phone.setText(m.getContactNumber()); email.setText(m.getEmail());
        type.setSelectedItem(m.getMemberType()); status.setSelectedItem(m.getStatus());
    }

    private void saveMember() {
        try {
            boolean creating=!editing;
            String memberID=id.getText().trim(), fullName=name.getText().trim(), contact=phone.getText().trim(), mail=email.getText().trim();
            MemberType memberType=(MemberType)type.getSelectedItem(); MemberStatus memberStatus=(MemberStatus)status.getSelectedItem();
            Ui.async(this, () -> { service.saveMember(memberID,fullName,memberType,contact,mail,memberStatus,creating); return true; }, done -> {
                JOptionPane.showMessageDialog(this,creating?"Member added successfully.":"Member updated successfully.","Saved",JOptionPane.INFORMATION_MESSAGE);
                clearForm(); refresh(); afterChange.run();
            });
        } catch(Exception ex) { Ui.error(this,ex); }
    }

    private void deactivate() {
        if(!editing) { JOptionPane.showMessageDialog(this,"Select a member from the table first."); return; }
        String memberID=id.getText();
        int choice=JOptionPane.showConfirmDialog(this,"Deactivate " + name.getText() + "? Their history will be kept.","Confirm deactivation",JOptionPane.YES_NO_OPTION);
        if(choice!=JOptionPane.YES_OPTION) return;
        Ui.async(this, () -> { service.saveMember(memberID,name.getText(),(MemberType)type.getSelectedItem(),phone.getText(),email.getText(),MemberStatus.INACTIVE,false); return true; }, done -> {
            JOptionPane.showMessageDialog(this,"Member deactivated.","Updated",JOptionPane.INFORMATION_MESSAGE); clearForm(); refresh(); afterChange.run();
        });
    }

    private void clearForm() {
        editing=false; id.setEnabled(true); id.setText(""); name.setText(""); phone.setText(""); email.setText("");
        type.setSelectedItem(MemberType.STUDENT); status.setSelectedItem(MemberStatus.ACTIVE); table.clearSelection();
    }
}

