package services;

import entities.Candidature;
import entities.OffreEmploi;
import utils.DataBase;
import utils.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceCandidature implements IService<Candidature> {
    private Connection connection;

    public ServiceCandidature() {
        this.connection = DataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Candidature candidature) throws SQLException {
        SessionManager session = SessionManager.getInstance();
        final int employeId = session.getId();
        String query = "INSERT INTO candidature (offre_id, employe_id, date_candidature, competences, etat) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, candidature.getOffreId());
            pstmt.setInt(2, employeId);
            pstmt.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setString(4, candidature.getCompetences());
            pstmt.setString(5, "en attente");

            pstmt.executeUpdate();
            System.out.println("✅ Candidature ajoutée avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la candidature : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Candidature candidature) throws SQLException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void supprimer(int id) throws SQLException {

    }
    public boolean supprimerCandidature(int id) throws SQLException {
        String query = "DELETE FROM candidature WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    @Override
    public java.util.List<Candidature> afficher() throws SQLException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    //Méthode pour tester l'existence d'une candidature pour une offre
    public boolean existeCandidature(int offreId, int employeId) throws SQLException {
        String query = "SELECT COUNT(*) FROM candidature WHERE offre_id = ? AND employe_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, offreId);
            pstmt.setInt(2, employeId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;  // Si le nombre de résultats est supérieur à 0, la candidature existe déjà.
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de l'existence de la candidature : " + e.getMessage());
        }
        return false;
    }
    //Méthode pour afficher les candidatures d'un employé
    public List<Candidature> getCandidaturesByUser(int userId) throws SQLException {
        List<Candidature> candidatures = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String query = "SELECT c.*, o.titre as offre_titre, o.date_debut, o.date_fin_estimee "
                + "FROM candidature c "
                + "LEFT JOIN offre_emploi o ON c.offre_id = o.id "
                + "WHERE c.employe_id = ? "
                + "ORDER BY c.date_candidature DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Candidature c = new Candidature();
                    c.setId(rs.getInt("id"));
                    c.setOffreId(rs.getInt("offre_id"));
                    c.setCompetences(rs.getString("competences"));
                    c.setEtat(rs.getString("etat"));

                    // Correction du parsing de date
                    String dateStr = rs.getString("date_candidature");
                    if (dateStr != null) {
                        try {
                            c.setDateCandidature(LocalDateTime.parse(dateStr, formatter));
                        } catch (DateTimeParseException e) {
                            // Fallback si le format est différent
                            c.setDateCandidature(LocalDateTime.now());
                            System.err.println("Format de date invalide pour la candidature ID: " + c.getId());
                        }
                    }

                    // Stockage des infos supplémentaires dans un Map
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("offre_titre", rs.getString("offre_titre"));
                    metadata.put("date_debut", rs.getDate("date_debut"));
                    metadata.put("date_fin", rs.getDate("date_fin_estimee"));
                    c.setMetadata(metadata);

                    candidatures.add(c);
                }
            }
        }

        return candidatures;
    }
    //Récupérer une offre d'emploi par son id
    private OffreEmploi getOffreById(int idOffre) {
        try {
            String query = "SELECT titre, date_debut, date_fin_estimee FROM offre_emploi WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, idOffre);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        OffreEmploi offre = new OffreEmploi();
                        offre.setId(idOffre);
                        offre.setTitre(rs.getString("titre"));
                        offre.setDateDebut(rs.getDate("date_debut"));
                        offre.setDateFinEstimee(rs.getDate("date_fin_estimee"));
                        return offre;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    //Méthode pour afficher les candidatures relatives à une offre d'emploi
    public List<Map<String, Object>> getCandidaturesWithDetailsByOffreId(int offreId) throws SQLException {
        List<Map<String, Object>> candidaturesWithDetails = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String query = "SELECT c.id as candidature_id, c.competences as competences, c.etat, c.date_candidature, "
                + "u.id as user_id, u.last_name as user_nom, u.first_name as user_prenom, u.email as user_email, "
                + "o.id as offre_id, o.titre as offre_titre, o.description as offre_description,o.competences_requises as competences_requises, "
                + "o.date_debut as offre_date_debut, o.date_fin_estimee as offre_date_fin "
                + "FROM candidature c "
                + "JOIN utilisateur u ON c.employe_id = u.id "
                + "JOIN offre_emploi o ON c.offre_id = o.id "
                + "WHERE c.offre_id = ? "
                + "ORDER BY c.date_candidature DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, offreId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> candidatureDetails = new HashMap<>();

                    // Infos de base de la candidature
                    candidatureDetails.put("candidature_id", rs.getInt("candidature_id"));
                    candidatureDetails.put("competences", rs.getString("competences"));
                    candidatureDetails.put("etat", rs.getString("etat"));

                    // Date de candidature
                    String dateStr = rs.getString("date_candidature");
                    if (dateStr != null) {
                        try {
                            candidatureDetails.put("date_candidature", LocalDateTime.parse(dateStr, formatter));
                        } catch (DateTimeParseException e) {
                            candidatureDetails.put("date_candidature", LocalDateTime.now());
                        }
                    }

                    // Infos de l'utilisateur
                    Map<String, Object> userDetails = new HashMap<>();
                    userDetails.put("id", rs.getInt("user_id"));
                    userDetails.put("nom", rs.getString("user_nom"));
                    userDetails.put("prenom", rs.getString("user_prenom"));
                    userDetails.put("email", rs.getString("user_email"));
                    candidatureDetails.put("user", userDetails);

                    // Infos de l'offre
                    Map<String, Object> offreDetails = new HashMap<>();
                    offreDetails.put("id", rs.getInt("offre_id"));
                    offreDetails.put("titre", rs.getString("offre_titre"));
                    offreDetails.put("description", rs.getString("offre_description"));
                    offreDetails.put("date_debut", rs.getDate("offre_date_debut"));
                    offreDetails.put("date_fin", rs.getDate("offre_date_fin"));
                    offreDetails.put("competences_requises", rs.getString("competences_requises"));
                    candidatureDetails.put("offre", offreDetails);

                    candidaturesWithDetails.add(candidatureDetails);
                }
            }
        }

        return candidaturesWithDetails;
    }
    //Méthode pour modifier l'état d'une candidature
    public void modifierEtatCandidature(int idCandidature, String nouvelEtat) throws SQLException {
        String query = "UPDATE candidature SET etat = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, nouvelEtat);
            pst.setInt(2, idCandidature);
            pst.executeUpdate();
        }
    }
    //Pour le corps de l'SMS
    public Map<String, Object> getCandidatureDetails(int candidatureId) throws SQLException {
        String query = "SELECT c.*, u.num_tel, u.email, u.last_name, u.first_name,o.id as offre_id, o.titre " +
                "FROM candidature c " +
                "JOIN utilisateur u ON c.employe_id = u.id " +
                "JOIN offre_emploi o ON c.offre_id = o.id " +
                "WHERE c.id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, candidatureId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> details = new HashMap<>();
                details.put("candidature_id", rs.getInt("id"));

                Map<String, Object> user = new HashMap<>();
                user.put("telephone", rs.getString("num_tel"));
                user.put("email", rs.getString("email"));
                user.put("nom", rs.getString("last_name"));
                user.put("prenom", rs.getString("first_name"));

                Map<String, Object> offre = new HashMap<>();
                offre.put("id", rs.getInt("offre_id")); // Assurez-vous que cette colonne est bien sélectionnée
                offre.put("titre", rs.getString("titre"));

                details.put("user", user);
                details.put("offre", offre);

                return details;
            }
        }
        return null;
    }
    //Pour la décrémentation du nombre de postes d'une offres si une candidatures est acceptée
    public void decrementerPostesOffre(int offreId) throws SQLException {
        String query = "UPDATE offre_emploi SET nombre_postes = nombre_postes - 1 WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, offreId);
            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                // Vérifier si le nombre de postes est maintenant à 0
                String checkQuery = "SELECT nombre_postes FROM offre_emploi WHERE id = ?";
                try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, offreId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt("nombre_postes") <= 0) {
                        // Mettre l'offre comme inactive
                        String updateStatusQuery = "UPDATE offre_emploi SET status = 'inactif' WHERE id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateStatusQuery)) {
                            updateStmt.setInt(1, offreId);
                            updateStmt.executeUpdate();
                        }
                    }
                }
            }
        }
    }
    public String getTitreOffreById(int offreId) throws SQLException {
        String titreOffre = null;
        String query = "SELECT titre FROM offre_emploi WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, offreId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                titreOffre = resultSet.getString("titre");
            }
        }
        return titreOffre;
    }
    public List<Candidature> getCandidaturesAccepteesByUser(int userId) throws SQLException {
        List<Candidature> candidatures = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String query = "SELECT c.*, o.titre as offre_titre FROM candidature c " +
                "LEFT JOIN offre_emploi o ON c.offre_id = o.id " +
                "WHERE c.employe_id = ? AND c.etat = 'acceptée'";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Candidature c = new Candidature();
                    c.setId(rs.getInt("id"));
                    c.setOffreId(rs.getInt("offre_id"));
                    c.setCompetences(rs.getString("competences"));
                    c.setEtat(rs.getString("etat"));

                    // Date de candidature
                    String dateStr = rs.getString("date_candidature");
                    if (dateStr != null) {
                        c.setDateCandidature(LocalDateTime.parse(dateStr, formatter));
                    }

                    // Métadonnées
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("offre_titre", rs.getString("offre_titre"));
                    c.setMetadata(metadata);

                    candidatures.add(c);
                }
            }
        }
        return candidatures;
    }
    public List<Candidature> getCandidaturesByUserAndMonth(int userId, int month, int year) throws SQLException {
        String query = "SELECT * FROM candidature WHERE employe_id = ? AND MONTH(date_candidature) = ? AND YEAR(date_candidature) = ?";
        List<Candidature> candidatures = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, month);
            statement.setInt(3, year);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Candidature c = new Candidature();
                c.setEtat(resultSet.getString("etat"));
                candidatures.add(c);
            }
        }
        return candidatures;
    }






}
