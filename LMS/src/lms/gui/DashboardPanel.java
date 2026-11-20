/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.gui;
import lms.service.LibraryService;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/**
 *
 * @author Adriel Justine Danga
 */
public class DashboardPanel extends JPanel {
    private final LibraryService service;
    private final JLabel titles = new JLabel("—");
    private final JLabel available = new JLabel("—");
    private final JLabel members = new JLabel("—");
    private final JLabel loans = new JLabel("—");
    private final JLabel overdue = new JLabel("—");

    public DashboardPanel(LibraryService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 22));
        setBackground(Ui.PALE);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JPanel words = new JPanel(new BorderLayout(0, 5));
        words.setOpaque(false);
        words.add(Ui.title("Dashboard"), BorderLayout.NORTH);
        words.add(Ui.subtitle("A quick overview of your library today."), BorderLayout.SOUTH);
        heading.add(words, BorderLayout.WEST);
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        date.setForeground(Ui.MUTED);
        heading.add(date, BorderLayout.EAST);
        add(heading, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 3, 16, 16));
        cards.setOpaque(false);
        cards.add(card("BOOK TITLES", titles, new Color(51, 102, 204)));
        cards.add(card("AVAILABLE COPIES", available, new Color(22, 143, 112)));
        cards.add(card("ACTIVE MEMBERS", members, new Color(124, 92, 207)));
        cards.add(card("ACTIVE LOANS", loans, new Color(221, 139, 35)));
        cards.add(card("OVERDUE LOANS", overdue, new Color(204, 75, 75)));
        JPanel note = new JPanel(new BorderLayout());
        note.setBackground(new Color(235, 241, 252));
        note.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        JLabel noteText = new JLabel("Tip: use the sidebar to manage records, check returns, and review reports.");
        noteText.setForeground(Ui.NAVY);
        noteText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        note.add(noteText);
        cards.add(note);
        add(cards, BorderLayout.CENTER);
    }

    private JPanel card(String name, JLabel value, Color accent) {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Ui.LINE),
                        BorderFactory.createEmptyBorder(20, 18, 20, 18))));
        JLabel label = new JLabel(name);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(Ui.MUTED);
        value.setFont(new Font("SansSerif", Font.BOLD, 34));
        value.setForeground(accent);
        panel.add(label, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    public void refresh() {
        Ui.async(this, service::getDashboardStats, stats -> {
            titles.setText(String.valueOf(stats.titles()));
            available.setText(String.valueOf(stats.availableCopies()));
            members.setText(String.valueOf(stats.activeMembers()));
            loans.setText(String.valueOf(stats.activeLoans()));
            overdue.setText(String.valueOf(stats.overdueLoans()));
        });
    }
}
