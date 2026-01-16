package com.pos.view;

import com.pos.connection.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SalesHistoryFrame extends JFrame {
    private JTable historyTable;
    private DefaultTableModel model;

    public SalesHistoryFrame() {
        setTitle("Historique des Ventes - Mode Administrateur");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(new Color(231, 76, 60));
        JLabel lblTitle = new JLabel("HISTORIQUE GLOBAL DES TRANSACTIONS");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Mise à jour des colonnes pour inclure HT et TTC si tu veux
        String[] columns = {"ID Vente", "Date & Heure", "Total HT", "Total TTC", "Caissier"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(model);
        historyTable.setRowHeight(30);
        
        loadHistory();

        add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Actualiser");
        btnRefresh.addActionListener(e -> loadHistory());
        JPanel pnlFooter = new JPanel();
        pnlFooter.add(btnRefresh);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void loadHistory() {
        model.setRowCount(0);
        // CORRECTION ICI : On utilise total_ht et total_ttc comme dans ton DESCRIBE
        String sql = "SELECT v.id_vente, v.date_vente, v.total_ht, v.total_ttc, u.username " +
                     "FROM Vente v " +
                     "INNER JOIN Utilisateur u ON v.id_u = u.id_u " +
                     "ORDER BY v.date_vente DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_vente"),
                    rs.getTimestamp("date_vente"),
                    String.format("%.2f DH", rs.getDouble("total_ht")),
                    String.format("%.2f DH", rs.getDouble("total_ttc")),
                    rs.getString("username")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // On affiche le message d'erreur précis du système
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage());
        }
    }
}
