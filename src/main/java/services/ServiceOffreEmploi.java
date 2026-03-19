package services;

import entities.OffreEmploi;
import utils.DataBase;
import utils.SessionManager;

import java.sql.*;
import java.util.*;
import java.sql.Date;

public class ServiceOffreEmploi implements IService<OffreEmploi> {
    private Connection connection;

    public ServiceOffreEmploi() {
        connection = DataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(OffreEmploi offre) throws SQLException {
        String query = "INSERT INTO offre_emploi (titre, description, nombre_postes, date_debut, date_fin_estimee, localisation, competences_requises, salaire, status, date_publication, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, offre.getTitre());
        pst.setString(2, offre.getDescription());
        pst.setInt(3, offre.getNombrePostes());
        pst.setDate(4, new java.sql.Date(offre.getDateDebut().getTime()));
        pst.setDate(5, new java.sql.Date(offre.getDateFinEstimee().getTime()));
        pst.setString(6, offre.getLocalisation());
        pst.setString(7, offre.getCompetencesRequises());
        pst.setDouble(8, offre.getSalaire());
        pst.setString(9, offre.getStatus());
        pst.setDate(10, new java.sql.Date(offre.getDatePublication().getTime()));
        SessionManager session = SessionManager.getInstance();
        int userId = session.getId();
        pst.setInt(11,userId);

        pst.executeUpdate();
        System.out.println("Offre d'emploi ajoutée !");
    }

    @Override
    public void modifier(OffreEmploi offre) throws SQLException {
        String sql = "UPDATE offre_emploi SET titre = ?, description = ?, nombre_postes = ?,  localisation = ?, competences_requises = ?, salaire = ?, status = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, offre.getTitre());
            stmt.setString(2, offre.getDescription());
            stmt.setInt(3, offre.getNombrePostes());
            stmt.setString(4, offre.getLocalisation());
            stmt.setString(5, offre.getCompetencesRequises());
            stmt.setDouble(6, offre.getSalaire());
            stmt.setString(7, offre.getStatus());
            stmt.setInt(8, offre.getId());
            stmt.executeUpdate();
        }
    }


    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM offre_emploi WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("L'offre d'emploi avec l'ID " + id + " a été supprimée.");
            } else {
                System.out.println("Aucune offre d'emploi trouvée avec l'ID " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Erreur lors de la suppression de l'offre d'emploi", e);
        }
    }

    @Override
    public List<OffreEmploi> afficher() throws SQLException {
        List<OffreEmploi> offres = new ArrayList<>();
        String query = "SELECT * FROM offre_emploi ORDER BY date_publication DESC";
        try (Statement stmt = DataBase.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                OffreEmploi offre = new OffreEmploi(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getInt("nombre_postes"),
                        rs.getDate("date_debut"),
                        rs.getDate("date_fin_estimee"),
                        rs.getString("status"),
                        rs.getString("localisation"),
                        rs.getDate("date_publication"),
                        rs.getString("competences_requises"),
                        rs.getDouble("salaire")

                );
                offres.add(offre);
            }
        }
        return offres;
    }
    //Méthode pour récupérer une offre par Id
    public OffreEmploi getById(int id) throws SQLException {
        String query = "SELECT * FROM offre_emploi WHERE id = ?";
        OffreEmploi offre = null;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    offre = new OffreEmploi();
                    offre.setId(rs.getInt("id"));
                    offre.setTitre(rs.getString("titre"));
                    offre.setDescription(rs.getString("description"));
                    offre.setCompetencesRequises(rs.getString("competences_requises"));
                    offre.setSalaire(rs.getDouble("salaire"));
                    offre.setDatePublication(rs.getDate("date_publication"));
                    offre.setDateDebut(rs.getDate("date_debut"));
                    offre.setDateFinEstimee(rs.getDate("date_fin_estimee"));
                    offre.setLocalisation(rs.getString("localisation"));
                    offre.setStatus(rs.getString("status"));
                    offre.setNombrePostes(rs.getInt("nombre_postes"));
                }
            }
        }

        if (offre == null) {
            throw new SQLException("Aucune offre trouvée avec l'ID: " + id);
        }

        return offre;
    }
    //Statistiques
    public Map<String, Double> getOffresParSaison() throws SQLException {
        Map<String, Double> stats = new LinkedHashMap<>(); // LinkedHashMap pour garder l'ordre
        int totalOffres = 0;

        // D'abord récupérer le total
        String countQuery = "SELECT COUNT(*) as total FROM offre_emploi";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next()) {
                totalOffres = rs.getInt("total");
            }
        }

        if (totalOffres == 0) {
            // Retourner des 0% si aucune offre
            stats.put("Printemps", 0.0);
            stats.put("Été", 0.0);
            stats.put("Automne", 0.0);
            stats.put("Hiver", 0.0);
            return stats;
        }

        String query = "SELECT " +
                "CASE " +
                "  WHEN MONTH(date_debut) BETWEEN 3 AND 5 THEN 'Printemps' " +
                "  WHEN MONTH(date_debut) BETWEEN 6 AND 8 THEN 'Été' " +
                "  WHEN MONTH(date_debut) BETWEEN 9 AND 11 THEN 'Automne' " +
                "  ELSE 'Hiver' " +
                "END as saison, " +
                "COUNT(*) as nombre " +
                "FROM offre_emploi " +
                "GROUP BY saison";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int count = rs.getInt("nombre");
                double percentage = (count * 100.0) / totalOffres;
                stats.put(rs.getString("saison"), Math.round(percentage * 100.0) / 100.0); // Arrondi à 2 décimales
            }
        }

        // S'assurer que toutes les saisons sont présentes
        String[] toutesSaisons = {"Printemps", "Été", "Automne", "Hiver"};
        for (String saison : toutesSaisons) {
            stats.putIfAbsent(saison, 0.0);
        }

        return stats;
    }
    public Map<String, Double> getOffresParGouvernorat() throws SQLException {
        Map<String, Double> stats = new LinkedHashMap<>();
        int totalOffres = 0;

        // Récupérer le total
        String countQuery = "SELECT COUNT(*) as total FROM offre_emploi";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next()) {
                totalOffres = rs.getInt("total");
            }
        }

        if (totalOffres == 0) {
            // Initialiser tous les gouvernorats à 0%
            String[] tousGouvernorats = {"Ariana", "Béja", "Ben Arous", "Bizerte",
                    "Gabès", "Gafsa", "Jendouba", "Kairouan",
                    "Kasserine", "Kébili", "Le Kef", "Mahdia",
                    "La Manouba", "Médenine", "Monastir", "Nabeul",
                    "Sfax", "Sidi Bouzid", "Siliana", "Sousse",
                    "Tataouine", "Tozeur", "Tunis", "Zaghouan"};

            for (String gouv : tousGouvernorats) {
                stats.put(gouv, 0.0);
            }
            return stats;
        }

        String query = "SELECT localisation, COUNT(*) as nombre FROM offre_emploi GROUP BY localisation";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int count = rs.getInt("nombre");
                double percentage = (count * 100.0) / totalOffres;
                stats.put(rs.getString("localisation"), Math.round(percentage * 100.0) / 100.0);
            }
        }

        // Compléter avec les gouvernorats manquants (0%)
        String[] tousGouvernorats = {"Ariana", "Béja", "Ben Arous", "Bizerte",
                "Gabès", "Gafsa", "Jendouba", "Kairouan",
                "Kasserine", "Kébili", "Le Kef", "Mahdia",
                "La Manouba", "Médenine", "Monastir", "Nabeul",
                "Sfax", "Sidi Bouzid", "Siliana", "Sousse",
                "Tataouine", "Tozeur", "Tunis", "Zaghouan"};

        for (String gouv : tousGouvernorats) {
            stats.putIfAbsent(gouv, 0.0);
        }

        return stats;
    }
    //Unicité d'ajout
    public boolean offreExisteDeja(OffreEmploi offre) throws SQLException {
        String query = "SELECT COUNT(*) FROM offre_emploi WHERE " +
                "titre = ? ";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, offre.getTitre());

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}
