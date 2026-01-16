package com.pos.view;

import com.pos.connection.DBConnection;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField txtUser = new JTextField(15);
    private JPasswordField txtPass = new JPasswordField(15);

    public LoginFrame() {
        setTitle("Authentification POS");
        setSize(400, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Identifiant");
        txtPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mot de passe");

        JLabel lblTitle = new JLabel("Connexion", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(lblTitle, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;
        mainPanel.add(new JLabel("Utilisateur:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Mot de passe:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(txtPass, gbc);

        JButton btn = new JButton("Se connecter");
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        btn.setBackground(new Color(13, 110, 253));
        btn.setForeground(Color.WHITE);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        mainPanel.add(btn, gbc);

        add(mainPanel);

        btn.addActionListener(e -> performLogin());
        txtPass.addActionListener(e -> performLogin());
    }

    private void performLogin() {
        String typedUser = txtUser.getText().toLowerCase().trim();
        String typedPass = new String(txtPass.getPassword());

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id_u, password, role FROM Utilisateur WHERE username=?");
            ps.setString(1, typedUser);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (dbPassword.equals(typedPass)) {
                    String role = rs.getString("role");
                    int userId = rs.getInt("id_u");
                    this.dispose();

                    // --- REDIRECTION STRICTE CORRIGÉE ---
                    if (typedUser.equals("admin")) {
                        // L'admin va vers MainPOSFrame
                        new MainPOSFrame(userId, role).setVisible(true);
                    } 
                    else if (typedUser.equals("caissier")) {
                        // CORRECTION ICI : On utilise les variables userId et role extraites du rs
                        new MainSalesFrame(userId, role).setVisible(true);
                    } 
                    else if (typedUser.equals("responsable")) {
                        // Le responsable va aux statistiques
                        new ReportsFrame().setVisible(true);
                    } else {
                        // Par défaut, si le nom n'est ni admin ni caissier, on ouvre le menu
                        new MainPOSFrame(userId, role).setVisible(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Mot de passe incorrect !");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Utilisateur '" + typedUser + "' inexistant !");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de base de données : " + ex.getMessage());
        }
    }
}
