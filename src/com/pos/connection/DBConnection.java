package com.pos.connection;
import java.sql.*;

public class DBConnection {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/pos_db";
        return DriverManager.getConnection(url, "taha", "pos123");
    }
}
