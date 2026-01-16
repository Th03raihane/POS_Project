package com.pos.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import com.pos.connection.DBConnection;
import com.formdev.flatlaf.FlatClientProperties;

// --- IMPORTS POUR LA GÉNÉRATION PDF ---
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;

/**
 * Dashboard Professionnel - POS Taha
 * Statistiques en temps réel et Exportation de Rapports
 */
public class ReportsFrame extends JFrame {
    private JLabel lblVentesValeur, lblClientsValeur, lblStockValeur;
    private JTable recentSalesTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> cbPeriode;

    public ReportsFrame() {
        // --- INITIALISATION DU SYSTÈME DE FICHIERS ---
        File exportDir = new File("exports");
        if (!exportDir.exists()) exportDir.mkdir();

        // --- CONFIGURATION DE LA FENÊTRE ---
        setTitle("Business Intelligence - POS TAHA (Dashboard Responsable)");
        setSize(1200, 850);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(248, 250, 252));
        setLayout(new BorderLayout());

        // --- 1. HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42)); // Slate 900
        header.setPreferredSize(new Dimension(0, 75));
        header.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel title = new JLabel("TABLEAU DE BORD ANALYTIQUE");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // --- 2. ZONE DE CONTENU PRINCIPAL ---
        JPanel mainContent = new JPanel(new BorderLayout(0, 30));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Grille des Statistiques
        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 25, 0));
        statsGrid.setOpaque(false);
        lblVentesValeur = new JLabel("0.00 DH");
        lblClientsValeur = new JLabel("0");
        lblStockValeur = new JLabel("0 articles");
        statsGrid.add(createStatCard("Chiffre d'Affaires", lblVentesValeur, new Color(34, 197, 94), "📈"));
        statsGrid.add(createStatCard("Clients Servis", lblClientsValeur, new Color(59, 130, 246), "👥"));
        statsGrid.add(createStatCard("Alertes Stock", lblStockValeur, new Color(239, 68, 68), "⚠️"));
        mainContent.add(statsGrid, BorderLayout.NORTH);

        // Barre d'Actions (Filtres et Exports)
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftActions.setOpaque(false);
        cbPeriode = new JComboBox<>(new String[]{"AUJOURD'HUI", "CE MOIS", "CETTE ANNÉE"});
        cbPeriode.setPreferredSize(new Dimension(180, 35));
        cbPeriode.addActionListener(e -> refreshAllData());
        leftActions.add(new JLabel("Période du Rapport :"));
        leftActions.add(cbPeriode);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightActions.setOpaque(false);
        JButton btnExcel = new JButton("Exporter EXCEL");
        btnExcel.putClientProperty(FlatClientProperties.STYLE, "background: #16a34a; foreground: #FFFFFF; arc: 10;");
        JButton btnPdf = new JButton("Exporter PDF");
        btnPdf.putClientProperty(FlatClientProperties.STYLE, "background: #dc2626; foreground: #FFFFFF; arc: 10;");

        btnExcel.addActionListener(e -> exportToCSV());
        btnPdf.addActionListener(e -> exportToPDF());

        rightActions.add(btnExcel);
        rightActions.add(btnPdf);

        actionBar.add(leftActions, BorderLayout.WEST);
        actionBar.add(rightActions, BorderLayout.EAST);

        // Tableau des Transactions
        JPanel tableContainer = new JPanel(new BorderLayout(0, 15));
        tableContainer.setOpaque(false);
        tableContainer.add(actionBar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID Vente", "Date & Heure", "Paiement", "Total TTC"}, 0);
        recentSalesTable = new JTable(tableModel);
        styleTable(recentSalesTable);
        JScrollPane scrollPane = new JScrollPane(recentSalesTable);
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");

        tableContainer.add(scrollPane, BorderLayout.CENTER);
        mainContent.add(tableContainer, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // --- 3. FOOTER ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        footer.setOpaque(false);
        JButton btnRefresh = new JButton("Actualiser");
        btnRefresh.addActionListener(e -> refreshAllData());
        JButton btnLogout = new JButton("Déconnexion");
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "foreground: #EF4444;");
        btnLogout.addActionListener(e -> { this.dispose(); new LoginFrame().setVisible(true); });

        footer.add(btnRefresh);
        footer.add(btnLogout);
        add(footer, BorderLayout.SOUTH);

        refreshAllData();
    }

    // --- LOGIQUE D'EXPORTATION PDF ---
    private void exportToPDF() {
        String period = cbPeriode.getSelectedItem().toString();
        String path = "exports/Rapport_" + period + "_" + System.currentTimeMillis() + ".pdf";
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            document.add(new Paragraph("RAPPORT DE VENTES - POS TAHA", titleFont));
            document.add(new Paragraph("Période : " + period));
            document.add(new Paragraph("Généré le : " + new java.util.Date().toString()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("ID Vente"); table.addCell("Date/Heure"); table.addCell("Paiement"); table.addCell("Total");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < 4; j++) {
                    table.addCell(tableModel.getValueAt(i, j).toString());
                }
            }
            document.add(table);
            document.close();
            JOptionPane.showMessageDialog(this, "PDF généré avec succès !");
            Desktop.getDesktop().open(new File("exports"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur PDF (Vérifiez la lib iText) : " + e.getMessage());
        }
    }

    // --- LOGIQUE D'EXPORTATION EXCEL (CSV) ---
    private void exportToCSV() {
        String path = "exports/Rapport_" + cbPeriode.getSelectedItem().toString() + "_" + System.currentTimeMillis() + ".csv";
        try (PrintWriter writer = new PrintWriter(new File(path))) {
            writer.println("ID Vente;Date;Paiement;Total");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.println(tableModel.getValueAt(i, 0) + ";" + tableModel.getValueAt(i, 1) + ";" +
                               tableModel.getValueAt(i, 2) + ";" + tableModel.getValueAt(i, 3));
            }
            JOptionPane.showMessageDialog(this, "Fichier Excel créé !");
            Desktop.getDesktop().open(new File("exports"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur Excel : " + e.getMessage());
        }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent, String icon) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(Color.WHITE);
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16;");
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        JPanel bar = new JPanel(); bar.setBackground(accent); bar.setPreferredSize(new Dimension(5, 0));
        JPanel txt = new JPanel(new GridLayout(2, 1, 0, 5)); txt.setOpaque(false);
        JLabel lblT = new JLabel(title.toUpperCase()); lblT.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblT.setForeground(new Color(100, 116, 139));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        txt.add(lblT); txt.add(valueLabel);
        card.add(bar, BorderLayout.WEST); card.add(txt, BorderLayout.CENTER);
        card.add(new JLabel(icon), BorderLayout.EAST);
        return card;
    }

    private void refreshAllData() {
        tableModel.setRowCount(0);
        String selection = cbPeriode.getSelectedItem().toString();
        String dateFilter = "DATE(date_vente) = CURDATE()";
        if (selection.equals("CE MOIS")) dateFilter = "MONTH(date_vente) = MONTH(CURDATE()) AND YEAR(date_vente) = YEAR(CURDATE())";
        else if (selection.equals("CETTE ANNÉE")) dateFilter = "YEAR(date_vente) = YEAR(CURDATE())";

        try (Connection conn = DBConnection.getConnection()) {
            updateCard(conn, "SELECT SUM(total_ttc) FROM Vente WHERE " + dateFilter, lblVentesValeur, true);
            updateCard(conn, "SELECT COUNT(id_vente) FROM Vente WHERE " + dateFilter, lblClientsValeur, false);
            updateCard(conn, "SELECT COUNT(*) FROM Produit WHERE stock < 5", lblStockValeur, false);

            String sql = "SELECT id_vente, date_vente, mode_paiement, total_ttc FROM Vente WHERE " + dateFilter + " ORDER BY date_vente DESC";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        "#" + rs.getInt("id_vente"), rs.getTimestamp("date_vente"),
                        rs.getString("mode_paiement") == null ? "N/A" : rs.getString("mode_paiement").toUpperCase(),
                        String.format("%.2f DH", rs.getDouble("total_ttc"))
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateCard(Connection conn, String sql, JLabel label, boolean isCurrency) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                if (isCurrency) label.setText(String.format("%.2f DH", rs.getDouble(1)));
                else label.setText(rs.getString(1) != null ? rs.getString(1) : "0");
            }
        }
    }
}
