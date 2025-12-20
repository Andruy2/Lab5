package database;

import models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public void saveClient(Client client) throws SQLException {
        String sql = "INSERT INTO clients (passport, name, deposit, client_type, bonus_type, bonus_value) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, client.getPassport());
            pstmt.setString(2, client.getName());
            pstmt.setDouble(3, client.getDeposit());
            pstmt.setString(4, client.getType());

            // Сохраняем информацию о бонусе
            BonusStrategy bonus = client.getBonusStrategy();
            if (bonus instanceof PercentageBonus) {
                pstmt.setString(5, "percentage");
                pstmt.setDouble(6, 0.1); // для VIP
            } else if (bonus instanceof FixedBonus) {
                pstmt.setString(5, "fixed");
                pstmt.setDouble(6, 3000); // для пенсионеров
            } else {
                pstmt.setString(5, "none");
                pstmt.setDouble(6, 0);
            }

            pstmt.executeUpdate();
        }
    }

    public void updateClient(Client client) throws SQLException {
        String sql = "UPDATE clients SET name = ?, deposit = ?, client_type = ?, bonus_type = ?, bonus_value = ? WHERE passport = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, client.getName());
            pstmt.setDouble(2, client.getDeposit());
            pstmt.setString(3, client.getType());

            BonusStrategy bonus = client.getBonusStrategy();
            if (bonus instanceof PercentageBonus) {
                pstmt.setString(4, "percentage");
                pstmt.setDouble(5, 0.1);
            } else if (bonus instanceof FixedBonus) {
                pstmt.setString(4, "fixed");
                pstmt.setDouble(5, 3000);
            } else {
                pstmt.setString(4, "none");
                pstmt.setDouble(5, 0);
            }

            pstmt.setString(6, client.getPassport());
            pstmt.executeUpdate();
        }
    }

    public void deleteClient(String passport) throws SQLException {
        String sql = "DELETE FROM clients WHERE passport = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, passport);
            pstmt.executeUpdate();
        }
    }

    public List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String passport = rs.getString("passport");
                String name = rs.getString("name");
                double deposit = rs.getDouble("deposit");
                String type = rs.getString("client_type");

                Client client;
                switch (type.toLowerCase()) {
                    case "вип":
                        client = new VIPClient(name, passport, deposit);
                        break;
                    case "пенсионер":
                        client = new PensionerClient(name, passport, deposit);
                        break;
                    default:
                        client = new SimpleClient(name, passport, deposit);
                }

                clients.add(client);
            }
        }

        return clients;
    }

    public Client getClientByPassport(String passport) throws SQLException {
        String sql = "SELECT * FROM clients WHERE passport = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, passport);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                double deposit = rs.getDouble("deposit");
                String type = rs.getString("client_type");

                switch (type.toLowerCase()) {
                    case "вип":
                        return new VIPClient(name, passport, deposit);
                    case "пенсионер":
                        return new PensionerClient(name, passport, deposit);
                    default:
                        return new SimpleClient(name, passport, deposit);
                }
            }
        }

        return null;
    }

    public boolean clientExists(String passport) throws SQLException {
        String sql = "SELECT 1 FROM clients WHERE passport = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, passport);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }
}