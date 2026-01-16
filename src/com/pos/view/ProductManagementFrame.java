package com.pos.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import com.pos.connection.DBConnection;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;
import java.util.HashMap;

public class ProductManagementFrame extends JFrame {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private HashMap<String, Integer> categoryMap = new HashMap<>();

    public ProductManagementFrame(String role) {
        // --- CONFIGURATION FENÊTRE ---
        setTitle("ADMINISTRATION DES INVENTAIRES - POS TAHA");
        setSize(1300, 850);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(242, 245, 248));
        setLayout(new BorderLayout());

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42)); 
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        JLabel lblTitle = new JLabel("GESTION DU STOCK ET DES RÉFÉRENCES VISUELLES");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblUser = new JLabel("Utilisateur : " + role.toUpperCase());
        lblUser.setForeground(new Color(148, 163, 184));

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblUser, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- CENTRE ---
        JPanel centerContainer = new JPanel(new BorderLayout(0, 15));
        centerContainer.setOpaque(false);
        centerContainer.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(450, 45));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, " 🔍 Rechercher un produit...");
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        // --- TABLE AVEC IMAGE ---
        String[] columns = {"IMAGE", "ID", "CODE-BARRE", "DÉSIGNATION", "CATÉGORIE", "PRIX ACHAT", "PRIX VENTE", "STOCK"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int col) { return (col == 0) ? Icon.class : Object.class; }
        };

        productTable = new JTable(tableModel);
        styleProductTable();

        // LOGIQUE DOUBLE-CLIC
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (role.toUpperCase().contains("ADMIN")) { 
                        int row = productTable.getSelectedRow();
                        if (row != -1) {
                            int modelRow = productTable.convertRowIndexToModel(row);
                            openEditDialog(modelRow);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Accès refusé : Seul l'ADMIN peut modifier.");
                    }
                }
            }
        });

        centerContainer.add(txtSearch, BorderLayout.NORTH);
        centerContainer.add(new JScrollPane(productTable), BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // FOOTER
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        footer.setBackground(Color.WHITE);
        JButton btnRefresh = new JButton("ACTUALISER");
        btnRefresh.addActionListener(ev -> loadProducts());
        footer.add(btnRefresh);
        add(footer, BorderLayout.SOUTH);

        loadProducts();
    }

    private void styleProductTable() {
        productTable.setRowHeight(60);
        sorter = new TableRowSorter<>(tableModel);
        productTable.setRowSorter(sorter);

        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, val, isSel, hasF, row, col);
                int mRow = table.convertRowIndexToModel(row);
                try {
                    int stock = Integer.parseInt(tableModel.getValueAt(mRow, 7).toString());
                    if (stock < 5) {
                        c.setForeground(new Color(220, 38, 38));
                        if (!isSel) c.setBackground(new Color(255, 241, 242));
                    } else {
                        c.setForeground(new Color(30, 41, 59));
                        if (!isSel) c.setBackground(Color.WHITE);
                    }
                } catch (Exception e) {}
                if (isSel) { c.setBackground(new Color(37, 99, 235)); c.setForeground(Color.WHITE); }
                return c;
            }
        });
    }

    private void loadProducts() {
        tableModel.setRowCount(0);
        String sql = "SELECT p.*, c.nom FROM Produit p LEFT JOIN Categorie c ON p.id_cat = c.id_cat ORDER BY p.id_produit ASC";
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                String cb = rs.getString("code_barre");
                ImageIcon icon = null;
                String path = "resources/" + cb + ".pnj";
                File f = new File(path);
                if (f.exists()) {
                    Image img = new ImageIcon(path).getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(img);
                }

                tableModel.addRow(new Object[]{
                    icon, rs.getInt("id_produit"), cb, rs.getString("designation"),
                    rs.getString("nom") == null ? "Sans Catégorie" : rs.getString("nom"),
                    rs.getDouble("prix_achat"), rs.getDouble("prix_vente"), rs.getInt("stock")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openEditDialog(int modelRow) {
        int id = (int) tableModel.getValueAt(modelRow, 1);
        String name = tableModel.getValueAt(modelRow, 3).toString();
        String currentCat = tableModel.getValueAt(modelRow, 4).toString();

        JDialog dialog = new JDialog(this, "MODIFICATION PRODUIT", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        // Utilisation du GridBagLayout pour un alignement parfait
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Espacement entre les composants
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- TITRE / NOM DU PRODUIT ---
        JLabel lblProdNom = new JLabel(name);
        lblProdNom.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblProdNom.setForeground(new Color(37, 99, 235));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        pnl.add(lblProdNom, gbc);

        gbc.gridwidth = 1; // Reset width
        gbc.weightx = 0.3;

        // --- CATÉGORIE ---
        gbc.gridx = 0; gbc.gridy = 1;
        pnl.add(new JLabel("Catégorie :"), gbc);

        JComboBox<String> cbCat = new JComboBox<>();
        loadCategoriesIntoCombo(cbCat);
        cbCat.setSelectedItem(currentCat);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnl.add(cbCat, gbc);

        // --- PRIX DE VENTE ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        pnl.add(new JLabel("Prix Vente (DH) :"), gbc);

        JTextField fPrix = new JTextField(tableModel.getValueAt(modelRow, 6).toString());
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnl.add(fPrix, gbc);

        // --- STOCK ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        pnl.add(new JLabel("Stock actuel :"), gbc);

        JTextField fStock = new JTextField(tableModel.getValueAt(modelRow, 7).toString());
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnl.add(fStock, gbc);

        // --- BOUTON ENREGISTRER ---
        JButton btnSave = new JButton("ENREGISTRER LES MODIFICATIONS");
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 8, 8, 8); // Plus d'espace au-dessus du bouton
        pnl.add(btnSave, gbc);

        btnSave.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                int catId = categoryMap.get(cbCat.getSelectedItem().toString());
                String sql = "UPDATE Produit SET prix_vente=?, stock=?, id_cat=? WHERE id_produit=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setDouble(1, Double.parseDouble(fPrix.getText()));
                ps.setInt(2, Integer.parseInt(fStock.getText()));
                ps.setInt(3, catId);
                ps.setInt(4, id);
                ps.executeUpdate();
                dialog.dispose();
                loadProducts();
                JOptionPane.showMessageDialog(this, "Produit mis à jour avec succès !");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur de saisie. Vérifiez les nombres.");
            }
        });

        dialog.add(pnl);
        dialog.setVisible(true);
    }

    private void loadCategoriesIntoCombo(JComboBox<String> combo) {
        categoryMap.clear();
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT id_cat, nom FROM Categorie")) {
            while (rs.next()) {
                categoryMap.put(rs.getString("nom"), rs.getInt("id_cat"));
                combo.addItem(rs.getString("nom"));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}
