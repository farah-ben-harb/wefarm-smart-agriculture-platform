package services;

import entities.Evenement;
import entities.OffreEmploi;
import utils.DataBase;
import utils.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.Date;
import java.util.Map;

public class EvenementService implements IService<Evenement> {
    private Connection connection;

    public EvenementService() {
        connection = DataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Evenement evenement) throws SQLException {
        String query = "INSERT INTO evenement (titre, description, date_debut, type_visite, localisation, prix,agriculteur_id) " +
                "VALUES (?, ?, ?, ?, ?, ?,  ?)";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, evenement.getTitre());
        pst.setString(2, evenement.getDescription());
        pst.setTimestamp(3, java.sql.Timestamp.valueOf(evenement.getDateDebut()));
        pst.setString(4, evenement.getTypeVisite());
        pst.setString(5, evenement.getLocalisation());
        pst.setDouble(6, evenement.getPrix());
        SessionManager session = SessionManager.getInstance();
        int agriculteurId = session.getId();
        pst.setInt(7,agriculteurId);

        pst.executeUpdate();
        System.out.println("Offre d'emploi ajoutée !");
    }

    @Override
    public void modifier(Evenement evenement) throws SQLException {
        String sql = "UPDATE evenement SET titre = ?, description = ?, date_debut = ?,  type_visite = ?, localisation = ?, prix = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, evenement.getTitre());
            stmt.setString(2, evenement.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(evenement.getDateDebut()));
            stmt.setString(4, evenement.getTypeVisite());
            stmt.setString(5, evenement.getLocalisation());
            stmt.setDouble(6, evenement.getPrix());
            stmt.setInt(7, evenement.getId());
            stmt.executeUpdate();
        }

    }


    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM evenement WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("L'événement avec l'ID " + id + " a été supprimé.");
            } else {
                System.out.println("Aucun événement trouvé avec l'ID " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Erreur lors de la suppression de l'événement", e);
        }

    }

    @Override
    public List<Evenement> afficher() throws SQLException {
        List<Evenement> events = new ArrayList<>();
        String query = "SELECT * FROM evenement";

        try (Statement stmt = DataBase.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                // Conversion correcte de la date
                java.sql.Timestamp timestamp = rs.getTimestamp("date_debut");
                LocalDateTime dateDebut = timestamp != null ? timestamp.toLocalDateTime() : null;

                Evenement evenement = new Evenement(
                        rs.getInt("id"),
                        rs.getInt("agriculteur_id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        dateDebut,  // Utilisation de la LocalDateTime convertie
                        rs.getString("type_visite"),
                        rs.getString("localisation"),
                        rs.getDouble("prix")
                );
                events.add(evenement);
            }
        }
        return events;
    }
    public List<Evenement> getByCriteria(String titre, LocalDateTime dateDebut, String typeVisite) throws SQLException {
        String query = "SELECT * FROM evenement WHERE titre = ? AND date_debut = ? AND type_visite = ?";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, titre);
        pst.setTimestamp(2, Timestamp.valueOf(dateDebut));
        pst.setString(3, typeVisite);

        ResultSet rs = pst.executeQuery();
        List<Evenement> result = new ArrayList<>();
        while (rs.next()) {
            result.add(createEvenementFromResultSet(rs));
        }
        return result;
    }
    private Evenement createEvenementFromResultSet(ResultSet rs) throws SQLException {
        Evenement evenement = new Evenement();
        evenement.setId(rs.getInt("id"));
        evenement.setAgriculteurId(rs.getInt("agriculteur_id"));
        evenement.setTitre(rs.getString("titre"));
        evenement.setDescription(rs.getString("description"));

        // Conversion du Timestamp SQL en LocalDateTime
        Timestamp timestamp = rs.getTimestamp("date_debut");
        evenement.setDateDebut(timestamp != null ? timestamp.toLocalDateTime() : null);

        evenement.setTypeVisite(rs.getString("type_visite"));
        evenement.setLocalisation(rs.getString("localisation"));
        evenement.setPrix(rs.getDouble("prix"));

        return evenement;
    }

}
