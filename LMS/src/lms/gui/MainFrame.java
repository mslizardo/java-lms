/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.gui;

import lms.service.LibraryService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author
 */
public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    LibraryService service = new LibraryService();
    
    public MainFrame() {
        setTitle("Library Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        contentPanel.add(new DashboardPanel(service), "Dashboard");
        contentPanel.add(new BooksPanel(service), "Books");
        contentPanel.add(new MembersPanel(service), "Members");
        contentPanel.add(new BorrowingPanel(service), "Borrowing");
        contentPanel.add(new ReportsPanel(service), "Reports");
        
        JPanel navigation = new JPanel(new GridLayout(5,1,5,5));
        String[] screens = {
            "Dashboard", "Books", "Members", "Borrowing", "Reports"
        };
        
        for(String screen : screens) {
            JButton button = new JButton(screen);
            button.addActionListener(e -> cardLayout.show(contentPanel, screen));
            
            navigation.add(button);
        }
        setLayout(new BorderLayout());
        add(navigation, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "Dashboard");
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {new MainFrame().setVisible(true);});
    }
}
