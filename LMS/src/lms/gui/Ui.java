package lms.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

final class Ui {
    static final Color NAVY = new Color(24, 38, 67);
    static final Color BLUE = new Color(51, 102, 204);
    static final Color PALE = new Color(245, 247, 251);
    static final Color INK = new Color(39, 49, 66);
    static final Color MUTED = new Color(112, 123, 141);
    static final Color LINE = new Color(226, 231, 239);

    private Ui() { }

    static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 25));
        label.setForeground(INK);
        return label;
    }

    static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(MUTED);
        return label;
    }

    static JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        button.setBackground(primary ? BLUE : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : INK);
        if (!primary) button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(9, 15, 9, 15)));
        return button;
    }

    static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setForeground(INK);
        table.setBackground(Color.WHITE);
        table.setGridColor(LINE);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(228, 237, 253));
        table.setSelectionForeground(INK);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(247, 249, 252));
        table.getTableHeader().setForeground(MUTED);
        table.setFillsViewportHeight(true);
    }

    static JScrollPane scroll(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(LINE));
        return pane;
    }

    static void error(Component parent, Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null && (cause instanceof java.util.concurrent.ExecutionException
                || cause instanceof java.lang.reflect.InvocationTargetException)) cause = cause.getCause();
        String message = cause.getMessage();
        if (message == null || message.isBlank()) message = "The operation could not be completed.";
        if (cause instanceof java.sql.SQLException && message.toLowerCase().contains("access denied")) {
            message = "MySQL rejected the login. Check LMS_DB_USER and LMS_DB_PASSWORD.";
        } else if (cause instanceof java.sql.SQLException && message.toLowerCase().contains("duplicate entry")) {
            message = "That book ID, ISBN, or member ID is already in use. Choose a unique identifier.";
        } else if (cause instanceof java.sql.SQLException && (message.toLowerCase().contains("communications link")
                || message.toLowerCase().contains("connection refused"))) {
            message = "Cannot connect to MySQL. Make sure the server is running and database settings are correct.";
        } else if (cause instanceof java.sql.SQLException && message.toLowerCase().contains("unknown database")) {
            message = "The library_management_system database was not found. Run database/library_management_system.sql first.";
        }
        JOptionPane.showMessageDialog(parent, message, "Library Management System", JOptionPane.ERROR_MESSAGE);
    }

    static <T> void async(Component parent, Callable<T> work, Consumer<T> success) {
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return work.call(); }
            @Override protected void done() {
                try { success.accept(get()); }
                catch (Exception ex) { error(parent, ex); }
            }
        }.execute();
    }

    static void emptyTable(DefaultTableModel model, String message, int columns) {
        model.setRowCount(0);
        Object[] row = new Object[columns];
        row[0] = message;
        model.addRow(row);
    }
}
