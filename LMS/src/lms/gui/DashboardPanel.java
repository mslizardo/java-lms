/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.gui;

import lms.service.LibraryService;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author
 */
public class DashboardPanel extends JPanel {

    private final LibraryService service;
    private final JLabel[] values = new JLabel[6];

    public DashboardPanel(LibraryService service) {
        this.service = service;
        setLayout(new BorderLayout(15, 15));

        JLabel title = new JLabel("Library Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadDashboard());

        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] labels = {
            "Book Titles", "Total Copies", "Available Copies",
            "Active Members", "Active Loans", "Overdue Loans"
        };

        JPanel cards = new JPanel(new GridLayout(2, 3, 15, 15));

        for (int i = 0; i < labels.length; i++) {
            JPanel card = new JPanel(new BorderLayout(5, 10));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(20, 15, 20, 15)
            ));

            JLabel name = new JLabel(labels[i], SwingConstants.CENTER);
            name.setFont(new Font("SansSerif", Font.BOLD, 16));

            values[i] = new JLabel("...", SwingConstants.CENTER);
            values[i].setFont(new Font("SansSerif", Font.BOLD, 32));

            card.add(name, BorderLayout.NORTH);
            card.add(values[i], BorderLayout.CENTER);
            cards.add(card);
        }

        add(cards, BorderLayout.CENTER);
        loadDashboard();
    }

    public void loadDashboard() {
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                return service.getDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    int[] stats = get();
                    for (int i = 0; i < values.length; i++) {
                        values[i].setText(String.valueOf(stats[i]));
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                            "Unable to load dashboard statistics.",
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
