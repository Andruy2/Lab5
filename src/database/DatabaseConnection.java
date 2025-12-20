package database;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:bank_database.db";
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }

    public static void initializeDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS clients (
                passport TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                deposit REAL NOT NULL,
                client_type TEXT NOT NULL,
                bonus_type TEXT,
                bonus_value REAL
            )
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}