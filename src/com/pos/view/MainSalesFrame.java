package com.pos.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import com.pos.dao.ProductDAO;
import com.pos.model.Product;
import com.pos.connection.DBConnection;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainSalesFrame extends JFrame {
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JTextField txtBarcode, txtCashAmount, txtCardAmount;
    private JComboBox<String> comboPaymentMode;
    private JLabel lblTotal, lblChange;
    private double totalAmount = 0.0;
    private ProductDAO productDAO = new ProductDAO();
    private int userId;
    private String userRole;

    public MainSalesFrame(int userId, String role) {
        this.userId = userId;
        this.userRole = role;

        setTitle("TERMINAL DE VENTE PRO - Taha");
        setSize(1350, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 246, 250));

        // --- TOP BAR ---
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(44, 62, 80));
        navBar.setPreferredSize(new Dimension(0, 60));
        JLabel logo = new JLabel("  POS SYSTEM | CAISSIER : " + role.toUpperCase());
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        navBar.add(logo, BorderLayout.WEST);
        add(navBar, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(25, 0));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainContent.setOpaque(false);

        // --- LEFT: SCAN & TABLE ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);

        txtBarcode = new JTextField();
        txtBarcode.setPreferredSize(new Dimension(0, 55));
        txtBarcode.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtBarcode.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "🔍 Scannez un code-barre...");
        txtBarcode.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");

        // --- CONFIGURATION TABLEAU ---
        tableModel = new DefaultTableModel(new String[]{"ID", "PRODUIT", "PRIX UNIT.", "QTÉ", "TOTAL"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        salesTable = new JTable(tableModel);
        styleTable();

        // --- APPLICATION SÉCURITÉ CAISSIER (BLOQUAGE) ---
        applyTableSecurity();

        leftPanel.add(txtBarcode, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);

        // --- RIGHT: PAYMENT PANEL ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setPreferredSize(new Dimension(450, 0)); 
        rightPanel.setBackground(Color.WHITE);
        rightPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 25;");
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.weightx = 1.0;

        lblTotal = new JLabel("0.00 DH", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 50));
        lblTotal.setForeground(new Color(13, 110, 253));

        comboPaymentMode = new JComboBox<>(new String[]{"ESPÈCES", "CARTE BANCAIRE", "MIXTE"});
        comboPaymentMode.setFont(new Font("Segoe UI", Font.BOLD, 16));

        txtCashAmount = new JTextField();
        txtCashAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtCardAmount = new JTextField();
        txtCardAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtCardAmount.setEnabled(false);

        lblChange = new JLabel("RENDU : 0.00 DH", SwingConstants.RIGHT);
        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton btnPay = new JButton("VALIDER LA VENTE");
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btnPay.setBackground(new Color(40, 167, 69));
        btnPay.setForeground(Color.WHITE);
        btnPay.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPay.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");

        gbc.gridy = 0; rightPanel.add(new JLabel("MONTANT TOTAL"), gbc);
        gbc.gridy = 1; rightPanel.add(lblTotal, gbc);
        gbc.gridy = 2; rightPanel.add(new JSeparator(), gbc);
        gbc.gridy = 3; rightPanel.add(new JLabel("MODE DE PAIEMENT"), gbc);
        gbc.gridy = 4; rightPanel.add(comboPaymentMode, gbc);
        gbc.gridy = 5; rightPanel.add(new JLabel("MONTANT ESPÈCES"), gbc);
        gbc.gridy = 6; rightPanel.add(txtCashAmount, gbc);
        gbc.gridy = 7; rightPanel.add(new JLabel("MONTANT CARTE"), gbc);
        gbc.gridy = 8; rightPanel.add(txtCardAmount, gbc);
        gbc.gridy = 9; rightPanel.add(lblChange, gbc);
        gbc.weighty = 1.0; gbc.gridy = 10; rightPanel.add(new JLabel(""), gbc);
        gbc.weighty = 0; gbc.gridy = 11; gbc.ipady = 30; rightPanel.add(btnPay, gbc);

        mainContent.add(leftPanel, BorderLayout.CENTER);
        mainContent.add(rightPanel, BorderLayout.EAST);
        add(mainContent, BorderLayout.CENTER);

        // --- ACTIONS ---
        txtBarcode.addActionListener(e -> { addProductToSale(txtBarcode.getText()); txtBarcode.setText(""); });
        comboPaymentMode.addActionListener(e -> handleModeChange());
        
        KeyAdapter calcKey = new KeyAdapter() { public void keyReleased(KeyEvent e) { calculateChange(); } };
        txtCashAmount.addKeyListener(calcKey);
        txtCardAmount.addKeyListener(calcKey);

        btnPay.addActionListener(e -> processPayment());
    }

    /**
     * Méthode pour bloquer la sélection si l'utilisateur est un caissier
     */
    private void applyTableSecurity() {
        if (userRole != null && !userRole.equalsIgnoreCase("ADMIN") && !userRole.equalsIgnoreCase("ADMINISTRATEUR")) {
            // VERROUILLAGE POUR CAISSIER
            salesTable.setEnabled(false); // Empêche le clic, la sélection et la modification
            salesTable.setRowSelectionAllowed(false);
            salesTable.setCellSelectionEnabled(false);
            salesTable.setFocusable(false);
            salesTable.getTableHeader().setReorderingAllowed(false);
            System.out.println("Mode Caissier : Tableau verrouillé.");
        } else {
            // ADMIN : Tout est permis
            salesTable.setEnabled(true);
            salesTable.setRowSelectionAllowed(true);
            salesTable.setCellSelectionEnabled(true);
            salesTable.setFocusable(true);
        }
    }

    private void handleModeChange() {
        String mode = (String) comboPaymentMode.getSelectedItem();
        txtCashAmount.setEnabled(mode.equals("ESPÈCES") || mode.equals("MIXTE"));
        txtCardAmount.setEnabled(mode.equals("CARTE BANCAIRE") || mode.equals("MIXTE"));
        if (mode.equals("CARTE BANCAIRE")) {
            txtCardAmount.setText(String.valueOf(totalAmount));
            txtCashAmount.setText("0");
        }
        calculateChange();
    }

    private void calculateChange() {
        try {
            double cash = txtCashAmount.getText().isEmpty() ? 0 : Double.parseDouble(txtCashAmount.getText());
            double card = txtCardAmount.getText().isEmpty() ? 0 : Double.parseDouble(txtCardAmount.getText());
            double diff = (cash + card) - totalAmount;
            lblChange.setText(String.format(diff >= 0 ? "RENDU : %.2f DH" : "RESTE : %.2f DH", Math.abs(diff)));
            lblChange.setForeground(diff >= 0 ? new Color(40, 167, 69) : Color.RED);
        } catch (Exception e) { lblChange.setText("FORMAT ERREUR"); }
    }

    private void processPayment() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Veuillez scanner des produits avant de valider.");
            return;
        }

        String mode = (String) comboPaymentMode.getSelectedItem();
        if (mode.contains("CARTE") || mode.equals("MIXTE")) {
            if (!showCardDialog()) return;
        }

        try {
            double cash = txtCashAmount.getText().isEmpty() ? 0 : Double.parseDouble(txtCashAmount.getText());
            double card = txtCardAmount.getText().isEmpty() ? 0 : Double.parseDouble(txtCardAmount.getText());

            if ((cash + card) < totalAmount) {
                JOptionPane.showMessageDialog(this, "Le montant payé est insuffisant !");
                return;
            }
            saveToDB(mode, cash, card);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer des montants valides.");
        }
    }

    private boolean showCardDialog() {
        JPanel p = new JPanel(new GridLayout(3, 2, 5, 5));
        p.add(new JLabel("N° Carte:")); p.add(new JTextField());
        p.add(new JLabel("Expiration:")); p.add(new JTextField("MM/AA"));
        p.add(new JLabel("CVV:")); p.add(new JPasswordField());
        return JOptionPane.showConfirmDialog(this, p, "Terminal Bancaire", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }

    private void saveToDB(String mode, double cash, double card) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            String sqlV = "INSERT INTO Vente (total_ttc, id_u, mode_paiement) VALUES (?, ?, ?)";
            PreparedStatement psV = conn.prepareStatement(sqlV, Statement.RETURN_GENERATED_KEYS);
            psV.setDouble(1, totalAmount);
            psV.setInt(2, userId);
            psV.setString(3, mode);
            psV.executeUpdate();
            
            ResultSet rs = psV.getGeneratedKeys();
            if (rs.next()) {
                int idV = rs.getInt(1);
                conn.commit();
                JOptionPane.showMessageDialog(this, "✅ Vente enregistrée !");
                generateTicketFile(idV, mode);
                clearAll();
            }
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Erreur BDD: " + e.getMessage());
        }
    }

    private void generateTicketFile(int id, String mode) {
        String filename = "ticket_" + id + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("      MAGASIN TAHA POS      ");
            pw.println("Ticket N: " + id);
            pw.println("Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            pw.println("Mode: " + mode);
            pw.println("----------------------------");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                pw.println(tableModel.getValueAt(i, 1) + " x" + tableModel.getValueAt(i, 3) + " : " + tableModel.getValueAt(i, 4) + " DH");
            }
            pw.println("----------------------------");
            pw.println("TOTAL: " + totalAmount + " DH");
            pw.println("   MERCI DE VOTRE VISITE    ");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addProductToSale(String barcode) {
        Product p = productDAO.getProductByBarcode(barcode);
        if (p != null && p.stock > 0) {
            tableModel.addRow(new Object[]{p.id, p.name, p.price, 1, p.price});
            totalAmount += p.price;
            lblTotal.setText(String.format("%.2f DH", totalAmount));
            calculateChange();
        } else {
            JOptionPane.showMessageDialog(this, "Produit non trouvé ou stock épuisé !");
        }
    }

    private void styleTable() {
        salesTable.setRowHeight(40);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        salesTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
    }

    private void clearAll() {
        tableModel.setRowCount(0); totalAmount = 0.0;
        lblTotal.setText("0.00 DH"); txtCashAmount.setText("");
        txtCardAmount.setText(""); lblChange.setText("RENDU : 0.00 DH");
        txtBarcode.requestFocus();
    }
}
