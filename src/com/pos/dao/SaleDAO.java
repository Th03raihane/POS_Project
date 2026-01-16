package com.pos.dao;
import com.pos.connection.DBConnection;
import java.sql.*;

public class SaleDAO {
    public int createSale(double totalHT, double tva, double totalTTC, int userId) throws SQLException {
        String sql = "INSERT INTO Vente (total_ht, tva, total_ttc, id_u) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, totalHT);
            ps.setDouble(2, tva);
            ps.setDouble(3, totalTTC);
            ps.setInt(4, userId);
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }
}
