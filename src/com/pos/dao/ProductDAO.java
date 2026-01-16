package com.pos.dao;

import com.pos.model.Product;
import com.pos.connection.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    /**
     * Recherche un produit par son code-barre (Utile pour le scanner en caisse)
     */
    public Product getProductByBarcode(String barcode) {
        String sql = "SELECT id_produit, code_barre, designation, prix_vente, prix_achat, stock, id_categorie FROM Produit WHERE code_barre = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, barcode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                        rs.getInt("id_produit"),
                        rs.getString("code_barre"),
                        rs.getString("designation"),
                        rs.getDouble("prix_vente"),
                        rs.getDouble("prix_achat"),
                        rs.getInt("stock"),
                        rs.getInt("id_categorie")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère tous les produits (Pour l'affichage du tableau de stock)
     */
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT id_produit, code_barre, designation, prix_vente, prix_achat, stock, id_categorie FROM Produit";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(new Product(
                    rs.getInt("id_produit"),
                    rs.getString("code_barre"),
                    rs.getString("designation"),
                    rs.getDouble("prix_vente"),
                    rs.getDouble("prix_achat"),
                    rs.getInt("stock"),
                    rs.getInt("id_categorie")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Met à jour le prix et le stock (Utilisé par le bouton "Modifier" du Stock)
     */
    public void updateProduct(int id, double newPrice, int newStock) {
        String sql = "UPDATE Produit SET prix_vente = ?, stock = ? WHERE id_produit = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, newStock);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Décrémente le stock lors d'une vente
     */
    public void updateStock(int productId, int quantitySold) throws SQLException {
        String sql = "UPDATE Produit SET stock = stock - ? WHERE id_produit = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantitySold);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
        }
    }
}
