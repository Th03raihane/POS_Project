package com.pos.view;

import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatClientProperties;

/**
 * Menu Principal du Système POS - Taha
 */
public class MainPOSFrame extends JFrame {
    private int userId;
    private String userRole;

    public MainPOSFrame(int id, String role) {
        this.userId = id;
        this.userRole = (role != null) ? role.toUpperCase() : ""; // Sécurité anti-null

        // Configuration de la fenêtre
        setTitle("Système POS - Taha");
        setSize(1100, 750); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- DÉFINITION DES PERMISSIONS ---
        // isAdmin est vrai si le rôle contient ADMIN ou RESPONSABLE
        boolean isAdmin = userRole.contains("ADMIN") || userRole.contains("RESPONSABLE");

        // --- HEADER (Barre supérieure) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(44, 62, 80));
        header.setPreferredSize(new Dimension(0, 80));
        
        JLabel lblWelcome = new JLabel("  SESSION : " + userRole);
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(lblWelcome, BorderLayout.WEST);
        
        add(header, BorderLayout.NORTH);

        // --- CENTRE : GRILLE DE MENU ---
        JPanel menuPanel = new JPanel(new GridLayout(2, 3, 25, 25));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // 1. GÉRER STOCK (Restriction Admin/Responsable)
        JButton btnStock = createMenuButton("GÉRER STOCK", "📦", new Color(52, 152, 219));
        // CORRECTION : Passage de userRole au constructeur
        btnStock.addActionListener(e -> new ProductManagementFrame(userRole).setVisible(true));
        btnStock.setEnabled(isAdmin);

        // 2. TERMINAL DE VENTE (Accessible à tous)
        JButton btnSales = createMenuButton("TERMINAL VENTE", "🛒", new Color(46, 204, 113));
        btnSales.addActionListener(e -> new MainSalesFrame(userId, userRole).setVisible(true));

        // 3. GESTION CLIENTS (Accessible à tous)
        JButton btnClients = createMenuButton("CLIENTS", "👥", new Color(241, 196, 15));
        btnClients.addActionListener(e -> new ClientFrame().setVisible(true));

        // 4. HISTORIQUE DES ACHATS (Restriction Admin/Responsable)
        JButton btnHistory = createMenuButton("HISTORIQUE", "📊", new Color(231, 76, 60));
        btnHistory.addActionListener(e -> new SalesHistoryFrame().setVisible(true));
        btnHistory.setEnabled(isAdmin);

        // 5. UTILISATEURS (Strictement Admin)
        JButton btnUsers = createMenuButton("UTILISATEURS", "🔑", new Color(155, 89, 182));
        btnUsers.addActionListener(e -> new UserManagementFrame().setVisible(true));
        btnUsers.setEnabled(userRole.contains("ADMIN"));

        // 6. PARAMÈTRES (Strictement Admin)
        JButton btnSettings = createMenuButton("PARAMÈTRES", "⚙️", new Color(149, 165, 166));
        btnSettings.addActionListener(e -> new SettingsFrame().setVisible(true));
        btnSettings.setEnabled(userRole.contains("ADMIN"));

        // Ajout des boutons au panneau central
        menuPanel.add(btnStock);
        menuPanel.add(btnSales);
        menuPanel.add(btnClients);
        menuPanel.add(btnHistory);
        menuPanel.add(btnUsers);
        menuPanel.add(btnSettings);

        add(menuPanel, BorderLayout.CENTER);

        // --- FOOTER (Barre inférieure) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        
        JButton btnLogout = new JButton("Déconnexion");
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
        footer.add(btnLogout);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Helper pour créer des boutons de menu stylisés uniformément
     */
    private JButton createMenuButton(String text, String icon, Color color) {
        JButton btn = new JButton("<html><center><font size='7'>" + icon + "</font><br><br>" + text + "</center></html>");
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 35"); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Effet visuel si désactivé
        if (!btn.isEnabled()) {
            btn.setToolTipText("Accès non autorisé pour votre profil");
        }
        
        return btn;
    }
}
