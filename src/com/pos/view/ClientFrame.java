package com.pos.view;

import com.pos.connection.DBConnection;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ClientFrame extends JFrame {
    private DefaultTableModel model;
    private JTextField txtNom = new JTextField(15);
    private JTextField txtTel = new JTextField(15);
    private JComboBox<String> cbType = new JComboBox<>(new String[]{"Standard", "Fidélisé"});

    public ClientFrame() {
        setTitle("Gestion Clients");
        setSize(900, 550);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Nouveau Client"));
        form.add(new JLabel("Nom:")); form.add(txtNom);
        form.add(new JLabel("Tel:")); form.add(txtTel);
        form.add(cbType);
        JButton btnAdd = new JButton("Enregistrer");
        form.add(btnAdd);

        model = new DefaultTableModel(new String[]{"ID", "Nom", "Téléphone", "Points", "Type"}, 0);
        JTable table = new JTable(model);

        btnAdd.addActionListener(e -> saveClient());
        add(form, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadClients();
    }

    private void saveClient() {
        String sql = "INSERT INTO Client (nom_cli, telephone, type_client) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtNom.getText());
            ps.setString(2, txtTel.getText());
            ps.setString(3, cbType.getSelectedItem().toString());
            ps.executeUpdate();
            loadClients();
            txtNom.setText(""); txtTel.setText("");
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void loadClients() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Client")) {
            while(rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5)});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}
