/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.gui;
import lms.service.LibraryService;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/**
 *
 * @author Adriel Justine Danga
 */
public class MainFrame extends JFrame {
    private static final String DASHBOARD="Dashboard", BOOKS="Books", MEMBERS="Members", BORROWING="Borrowing / Returns", REPORTS="Reports";
    private final CardLayout cards=new CardLayout();
    private final JPanel pages=new JPanel(cards);
    private final JButton[] navButtons=new JButton[5];
    private final DashboardPanel dashboard;
    private final BooksPanel books;
    private final MembersPanel members;
    private final BorrowingPanel borrowing;
    private final ReportsPanel reports;
    private String currentPage=DASHBOARD;

    public MainFrame() {
        super("Library Management System");
        LibraryService service=new LibraryService();
        dashboard=new DashboardPanel(service);
        books=new BooksPanel(service,this::refreshAll);
        members=new MembersPanel(service,this::refreshAll);
        borrowing=new BorrowingPanel(service,this::refreshAll);
        reports=new ReportsPanel(service);
        pages.add(dashboard,DASHBOARD); pages.add(books,BOOKS); pages.add(members,MEMBERS);
        pages.add(borrowing,BORROWING); pages.add(reports,REPORTS);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setMinimumSize(new Dimension(1000,680)); setSize(1280,820); setLocationRelativeTo(null);
        setLayout(new BorderLayout()); getContentPane().setBackground(Ui.PALE);
        add(sidebar(),BorderLayout.WEST);
        JPanel right=new JPanel(new BorderLayout()); right.setBackground(Ui.PALE);
        right.add(topBar(),BorderLayout.NORTH); right.add(pages,BorderLayout.CENTER); add(right,BorderLayout.CENTER);
        showPage(DASHBOARD,0);
    }

    private JPanel sidebar() {
        JPanel side=new JPanel(new BorderLayout(0,24)); side.setBackground(Ui.NAVY); side.setPreferredSize(new Dimension(220,0));
        side.setBorder(BorderFactory.createEmptyBorder(25,15,18,15));
        JPanel brand=new JPanel(new BorderLayout(0,6)); brand.setOpaque(false);
        JLabel mark=new JLabel("LMS",SwingConstants.CENTER); mark.setOpaque(true); mark.setBackground(new Color(55,104,198)); mark.setForeground(Color.WHITE);
        mark.setFont(new Font("SansSerif",Font.BOLD,22)); mark.setPreferredSize(new Dimension(55,52));
        JPanel brandRow=new JPanel(new BorderLayout(12,0)); brandRow.setOpaque(false); brandRow.add(mark,BorderLayout.WEST);
        JPanel brandWords=new JPanel(new GridLayout(2,1)); brandWords.setOpaque(false);
        JLabel title=new JLabel("Library"); title.setForeground(Color.WHITE); title.setFont(new Font("SansSerif",Font.BOLD,18));
        JLabel subtitle=new JLabel("MANAGEMENT SYSTEM"); subtitle.setForeground(new Color(177,190,215)); subtitle.setFont(new Font("SansSerif",Font.BOLD,9));
        brandWords.add(title); brandWords.add(subtitle); brandRow.add(brandWords,BorderLayout.CENTER); brand.add(brandRow,BorderLayout.NORTH);
        side.add(brand,BorderLayout.NORTH);

        JPanel menu=new JPanel(new GridLayout(0,1,0,8)); menu.setOpaque(false);
        String[] labels={DASHBOARD,BOOKS,MEMBERS,BORROWING,REPORTS};
        String[] captions={"◫   Dashboard","▤   Books","♙   Members","↔   Borrowing / Returns","▧   Reports"};
        for(int i=0;i<labels.length;i++) {
            final int index=i; JButton button=new JButton(captions[i]); navButtons[i]=button;
            button.setHorizontalAlignment(SwingConstants.LEFT); button.setFont(new Font("SansSerif",Font.BOLD,13));
            button.setForeground(new Color(220,228,242)); button.setBackground(Ui.NAVY); button.setFocusPainted(false); button.setBorderPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(13,12,13,8)); button.addActionListener(e -> showPage(labels[index],index)); menu.add(button);
        }
        side.add(menu,BorderLayout.CENTER);
        JLabel footer=new JLabel("FINAL PROJECT • JAVA SWING"); footer.setFont(new Font("SansSerif",Font.BOLD,9)); footer.setForeground(new Color(160,176,204)); side.add(footer,BorderLayout.SOUTH);
        return side;
    }

    private JPanel topBar() {
        JPanel bar=new JPanel(new BorderLayout()); bar.setBackground(Color.WHITE); bar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,Ui.LINE));
        bar.setPreferredSize(new Dimension(0,58));
        JLabel appName=new JLabel("  School Library"); appName.setFont(new Font("SansSerif",Font.BOLD,14)); appName.setForeground(Ui.INK); bar.add(appName,BorderLayout.WEST);
        JLabel user=new JLabel("Librarian   "); user.setFont(new Font("SansSerif",Font.PLAIN,12)); user.setForeground(Ui.MUTED); bar.add(user,BorderLayout.EAST);
        return bar;
    }

    private void showPage(String name,int selected) {
        currentPage=name;
        cards.show(pages,name);
        for(int i=0;i<navButtons.length;i++) {
            navButtons[i].setBackground(i==selected?new Color(48,67,106):Ui.NAVY);
            navButtons[i].setForeground(i==selected?Color.WHITE:new Color(220,228,242));
        }
        switch(name) {
            case DASHBOARD -> dashboard.refresh();
            case BOOKS -> books.refresh();
            case MEMBERS -> members.refresh();
            case BORROWING -> borrowing.refresh();
            case REPORTS -> reports.refresh();
        }
    }

    private void refreshAll() {
        dashboard.refresh();
        switch(currentPage) {
            case BOOKS -> books.refresh();
            case MEMBERS -> members.refresh();
            case BORROWING -> borrowing.refresh();
            case REPORTS -> reports.refresh();
        }
    }
}
