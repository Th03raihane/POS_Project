package com.pos.view;

import com.pos.connection.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UserManagementFrame extends JFrame {
    private JTable userTable;
    private DefaultTableModel model;

    public UserManagementFrame() {
        setTitle("Gestion des Utilisateurs - POS Taha");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Tableau des utilisateurs
        String[] columns = {"ID", "Nom d'utilisateur", "Rôle"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(model);
        
        loadUsers();

        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Bouton Fermer
        JButton btnClose = new JButton("Fermer");
        btnClose.addActionListener(e -> dispose());
        JPanel pnl = new JPanel();
        pnl.add(btnClose);
        add(pnl, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        model.setRowCount(0);
        // Utilisation de id_u comme vu dans ton DESCRIBE
        String sql = "SELECT id_u, username, role FROM Utilisateur ORDER BY id_u ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id_u"), rs.getString("username"), rs.getString("role")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
