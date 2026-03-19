package services;



import entities.Activites;
import utils.DataBase;
import utils.DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivitiesService {

    private final Connection conn;
    public ActivitiesService() {
        conn = DataBase.getInstance().getConnection();
    }

    public List<Activites> getAllActivites() {
        List<Activites> activites = new ArrayList<>();
        String query = "SELECT * FROM activites ORDER BY date DESC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Activites a = new Activites(
                        rs.getInt("id"),
                        rs.getInt("utilisateur_id"),
                        rs.getString("action"),
                        rs.getString("meta_data"),

                        new Date(rs.getTimestamp("date").getTime())
                );
                activites.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités: " + e.getMessage());
        }
        return activites;
    }

    public boolean supprimerActivite(int id) {
        String query = "DELETE FROM activites WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    public boolean ajouterActivite(Activites activite) {
        String query = "INSERT INTO activites (utilisateur_id, action, meta_data, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, activite.getUtilisateurId());
            pst.setString(2, activite.getAction());
            pst.setString(3, activite.getMetaData());
            pst.setTimestamp(4, new Timestamp(activite.getDate().getTime()));

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout d'une activité: " + e.getMessage());
            return false;
        }
    }
    public List<Activites> getActivitesByPeriod(String periodType) {
        List<Activites> activites = new ArrayList<>();
        String query = "";

        if ("semaine".equals(periodType)) {
            query = "SELECT * FROM activites WHERE date >= NOW() - INTERVAL 7 DAY ORDER BY date DESC";
        } else if ("mois".equals(periodType)) {
            query = "SELECT * FROM activites WHERE date >= NOW() - INTERVAL 1 MONTH ORDER BY date DESC";
        }


        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Activites a = new Activites(
                        rs.getInt("id"),
                        rs.getInt("utilisateur_id"),
                        rs.getString("action"),
                        rs.getString("meta_data"),
                        new Date(rs.getTimestamp("date").getTime())
                );
                activites.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités: " + e.getMessage());
        }

        return activites;
    }



}