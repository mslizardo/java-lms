/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.gui;
import lms.model.Loan;
import lms.service.LibraryService;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
/**
 *
 * @author Adriel Justine Danga
 */
public class ReportsPanel extends JPanel {
    private final LibraryService service;
    private final JTextField search=new JTextField(24);
    private final JComboBox<String> status=new JComboBox<>(new String[]{"All records","Active","Returned","Overdue"});
    private final DefaultTableModel model=Ui.tableModel("Loan ID","Member","Book","Borrow date","Due date","Return date","Status");
    private final JTable table=new JTable(model);
    private List<Loan> loans=List.of();

    public ReportsPanel(LibraryService service) {
        this.service=service; setLayout(new BorderLayout(0,18)); setBackground(Ui.PALE);
        setBorder(BorderFactory.createEmptyBorder(26,30,28,30));
        JPanel top=new JPanel(new BorderLayout(0,15)); top.setOpaque(false);
        JPanel heading=new JPanel(new BorderLayout()); heading.setOpaque(false);
        heading.add(Ui.title("Reports"),BorderLayout.NORTH);
        heading.add(Ui.subtitle("Review active, returned, and overdue transactions."),BorderLayout.SOUTH); top.add(heading,BorderLayout.NORTH);
        JPanel filters=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); filters.setOpaque(false);
        filters.add(new JLabel("Status")); filters.add(status); filters.add(new JLabel("Search"));
        search.setToolTipText("Filter by member name, book title, or transaction ID"); filters.add(search);
        var refresh=Ui.button("Refresh",false); refresh.addActionListener(e -> refresh()); filters.add(refresh);
        top.add(filters,BorderLayout.SOUTH); add(top,BorderLayout.NORTH);
        table.setAutoCreateRowSorter(true); Ui.styleTable(table); add(Ui.scroll(table),BorderLayout.CENTER);
        status.addActionListener(e -> render());
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { render(); }
            public void removeUpdate(DocumentEvent e) { render(); }
            public void changedUpdate(DocumentEvent e) { render(); }
        });
    }

    public void refresh() {
        Ui.async(this,service::getLoans,data -> { loans=data; render(); });
    }

    private void render() {
        if(model==null) return;
        String selected=(String)status.getSelectedItem();
        String query=search.getText().trim().toLowerCase();
        model.setRowCount(0);
        for(Loan loan:loans) {
            boolean overdue=loan.isOverdue();
            String state=overdue?"OVERDUE":loan.getStatus().name();
            if("Active".equals(selected)&&!loan.isActive()) continue;
            if("Returned".equals(selected)&&loan.isActive()) continue;
            if("Overdue".equals(selected)&&!overdue) continue;
            String haystack=(loan.getLoanID()+" "+loan.getMember().getFullName()+" "+loan.getBook().getTitle()).toLowerCase();
            if(!haystack.contains(query)) continue;
            model.addRow(new Object[]{loan.getLoanID(),loan.getMember().getFullName(),loan.getBook().getTitle(),loan.getBorrowDate(),
                    loan.getDueDate(),loan.getReturnDate()==null?"—":loan.getReturnDate(),state});
        }
    }
}
