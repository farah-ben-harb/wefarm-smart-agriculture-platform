package services;

import entities.Transaction;
import utils.DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTransaction {
    private Connection conn = DataBase.getInstance().getConnection();

    // Ajouter une transaction avec l'agriculteur connecté
    public void ajouter(Transaction transaction) throws SQLException {
        String query = "INSERT INTO transaction (terrain_id, agriculteur_id, client_id, type, date_transaction, montant) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, transaction.getTerrainId());
            pst.setInt(2, transaction.getAgriculteurId());
            pst.setInt(3, transaction.getClientId());
            pst.setString(4, transaction.getType());
            pst.setDate(5, new Date(transaction.getDateTransaction().getTime()));
            pst.setDouble(6, transaction.getMontant());


            pst.executeUpdate();
        }
    }

    // Récupérer toutes les transactions
    public List<Transaction> afficher() {
        System.out.println("Tentative de connexion à la base...");
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT id, terrain_id, client_id, type, date_transaction, montant FROM transaction";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            System.out.println("Connexion réussie. Exécution de la requête...");

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                System.out.println("ID trouvé: " + t.getId());

                t.setTerrainId(rs.getInt("terrain_id"));
                t.setClientId(rs.getInt("client_id"));
                t.setType(rs.getString("type"));

                Date sqlDate = rs.getDate("date_transaction");
                if (sqlDate != null) {
                    t.setDateTransaction(sqlDate);
                }

                t.setMontant(rs.getDouble("montant"));
                transactions.add(t);
            }
            System.out.println("Nombre de transactions récupérées: " + transactions.size());
        } catch (SQLException e) {
            System.err.println("ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }
    // Récupérer une transaction par son ID
    public Transaction getById(int id) {
        String query = "SELECT * FROM transaction WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setTerrainId(rs.getInt("terrain_id"));
                t.setAgriculteurId(rs.getInt("agriculteur_id"));
                t.setClientId(rs.getInt("client_id"));
                t.setType(rs.getString("type"));
                t.setDateTransaction(rs.getDate("date_transaction"));
                t.setMontant(rs.getDouble("montant"));

                return t;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la transaction: " + e.getMessage());
        }
        return null;
    }

    // Mettre à jour une transaction
    public void modifier(Transaction transaction) {
        String query = "UPDATE transaction SET terrain_id=?, agriculteur_id=?, client_id=?, " +
                "type=?, date_transaction=?, montant=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, transaction.getTerrainId());
            pst.setInt(2, transaction.getAgriculteurId());
            pst.setInt(3, transaction.getClientId());
            pst.setString(4, transaction.getType());
            pst.setDate(5, new Date(transaction.getDateTransaction().getTime()));
            pst.setDouble(6, transaction.getMontant());

            pst.setInt(8, transaction.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la transaction: " + e.getMessage());
        }
    }

    // Supprimer une transaction
    public void supprimer(int id) {
        String query = "DELETE FROM transaction WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la transaction: " + e.getMessage());
        }
    }

    // Récupérer les transactions par terrain
    public List<Transaction> getByTerrainId(int terrainId) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM transaction WHERE terrain_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, terrainId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setTerrainId(rs.getInt("terrain_id"));
                t.setAgriculteurId(rs.getInt("agriculteur_id"));
                t.setClientId(rs.getInt("client_id"));
                t.setType(rs.getString("type"));
                t.setDateTransaction(rs.getDate("date_transaction"));
                t.setMontant(rs.getDouble("montant"));


                transactions.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des transactions par terrain: " + e.getMessage());
        }
        return transactions;
    }

    // Récupérer les transactions par agriculteur
    public List<Transaction> getByAgriculteurId(int agriculteurId) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM transaction WHERE agriculteur_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, agriculteurId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setTerrainId(rs.getInt("terrain_id"));
                t.setAgriculteurId(rs.getInt("agriculteur_id"));
                t.setClientId(rs.getInt("client_id"));
                t.setType(rs.getString("type"));
                t.setDateTransaction(rs.getDate("date_transaction"));
                t.setMontant(rs.getDouble("montant"));


                transactions.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des transactions par agriculteur: " + e.getMessage());
        }
        return transactions;
    }

    // Calculer le montant total par terrain
    public double getTotalMontantByTerrain(int terrainId) {
        String query = "SELECT SUM(montant) as total FROM transaction WHERE terrain_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, terrainId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du montant total: " + e.getMessage());
        }
        return 0;
    }
}