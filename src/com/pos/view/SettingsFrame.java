package com.pos.view;

import com.pos.connection.DBConnection;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SettingsFrame extends JFrame {
    private JTabbedPane tabs = new JTabbedPane();
    
    // Éléments pour la boutique
    private JTextField txtShopName = new JTextField();
    private JTextField txtAddress = new JTextField();
    private JTextField txtPhone = new JTextField();

    public SettingsFrame() {
        setTitle("Paramètres Système - Boutique & Utilisateurs");
        setSize(850, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        tabs.putClientProperty(FlatClientProperties.STYLE, "tabType: card");

        // Ajout des 3 onglets
        tabs.addTab("📁 Catégories", createCategoryPanel());
        tabs.addTab("👥 Utilisateurs", createUserPanel());
        tabs.addTab("🏪 Boutique", createShopPanel());

        add(tabs);
        loadShopSettings();
    }

    // --- ONGLET 1 : CATÉGORIES ---
    private JPanel createCategoryPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtCat = new JTextField(20);
        JButton btnAdd = new JButton("Ajouter");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Nouvelle Catégorie :")); 
        top.add(txtCat); 
        top.add(btnAdd);

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Nom de Catégorie"}, 0);
        JTable table = new JTable(model);

        btnAdd.addActionListener(e -> {
            if(txtCat.getText().isEmpty()) return;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO Categorie (nom_cat) VALUES (?)")) {
                ps.setString(1, txtCat.getText());
                ps.executeUpdate();
                loadData("SELECT * FROM Categorie", model);
                txtCat.setText("");
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erreur ou doublon !"); }
        });

        loadData("SELECT * FROM Categorie", model);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // --- ONGLET 2 : UTILISATEURS ---
    private JPanel createUserPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Utilisateur", "Rôle"}, 0);
        JTable table = new JTable(model);
        
        loadData("SELECT id_u, username, role FROM Utilisateur", model);
        
        p.add(new JLabel("Liste des utilisateurs inscrits :"), BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // --- ONGLET 3 : BOUTIQUE ---
    private JPanel createShopPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);

        gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel("Nom Boutique :"), gbc);
        gbc.gridx = 1; txtShopName.setPreferredSize(new Dimension(250, 30)); p.add(txtShopName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; p.add(new JLabel("Adresse :"), gbc);
        gbc.gridx = 1; p.add(txtAddress, gbc);

        gbc.gridx = 0; gbc.gridy = 2; p.add(new JLabel("Téléphone :"), gbc);
        gbc.gridx = 1; p.add(txtPhone, gbc);

        JButton btnSave = new JButton("Enregistrer les infos");
        btnSave.setBackground(new Color(13, 110, 253));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveShopSettings());
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(30, 20, 10, 20);
        p.add(btnSave, gbc);

        return p;
    }

    // --- MÉTHODES DE CHARGEMENT ---
    private void loadData(String sql, DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            int cols = rs.getMetaData().getColumnCount();
            while(rs.next()) {
                Object[] row = new Object[cols];
                for(int i=0; i<cols; i++) row[i] = rs.getObject(i+1);
                model.addRow(row);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void loadShopSettings() {
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Settings WHERE id=1")) {
            if (rs.next()) {
                txtShopName.setText(rs.getString("shop_name"));
                txtAddress.setText(rs.getString("address"));
                txtPhone.setText(rs.getString("phone"));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void saveShopSettings() {
        String sql = "UPDATE Settings SET shop_name=?, address=?, phone=? WHERE id=1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtShopName.getText());
            ps.setString(2, txtAddress.getText());
            ps.setString(3, txtPhone.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Paramètres enregistrés !");
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}
