package services;

import entities.Utilisateur;
import utils.DataBase;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class UtilisateurService implements IUtilisateurService {

    private final Connection connection;

    public UtilisateurService() {
        connection = DataBase.getInstance().getConnection();
    }

    @Override
    public void ajouterUtilisateur(Utilisateur user) {
        String query = "INSERT INTO utilisateur (email, roles, adresse, password, first_name, last_name, date_naissance, image, date_creation, status, is_verified, num_tel,confirmation_token) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, user.getEmail());
            pst.setString(2, user.getRoles());
            pst.setString(3, user.getAdresse());
            pst.setString(4, user.getPassword());
            pst.setString(5, user.getFirstName());
            pst.setString(6, user.getLastName());

            pst.setDate(7, user.getDateNaissance()); // Utilisation directe


            pst.setString(8, user.getImage());

            if (user.getDateCreation() != null) {
                pst.setTimestamp(9, Timestamp.valueOf(user.getDateCreation()));
            } else {
                pst.setNull(9, Types.TIMESTAMP);
            }

            pst.setString(10, "en attente de validation");
            pst.setBoolean(11, user.isVerified());
            pst.setInt(12, user.getNumTel());
            pst.setString(13, user.getConfirmationToken());




            pst.executeUpdate();
            System.out.println("Utilisateur ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
        }
    }
    public void modifierUtilisateur(Utilisateur user) {
        String req = "UPDATE utilisateur SET email=?, first_name=?, last_name=?, adresse=?, num_tel=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getAdresse());
            ps.setInt(5, user.getNumTel());
            ps.setInt(6, user.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur modification: " + e.getMessage());
        }
    }





    @Override
    public void supprimerUtilisateur(int id) {
        String query = "DELETE FROM utilisateur WHERE id=?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Utilisateur supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
        }
    }

    @Override
    public Utilisateur getUtilisateurById(int id) {//datails
        String query = "SELECT * FROM utilisateur WHERE id=?";
        Utilisateur user = null;

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
        }

        return user;
    }

    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification de l'email: " + e.getMessage());
        }

        return false;
    }

    @Override
    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> users = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE email != ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, "wefarmapplication@gmail.com"); // Exclure cet email

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUtilisateur(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des utilisateurs : " + e.getMessage());
        }

        return users;
    }

    @Override
    public Utilisateur getUtilisateurByEmail(String email) {
        String query = "SELECT * FROM utilisateur WHERE email=?";
        Utilisateur user = null;

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de l'utilisateur par email : " + e.getMessage());
        }

        return user;
    }


    public void activerDesactiverUtilisateur(int id, boolean activer) {
        String newStatus = activer ? "actif" : "désactivé";
        String query = "UPDATE utilisateur SET status=? WHERE id=?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, newStatus);
            pst.setInt(2, id);
            pst.executeUpdate();
            System.out.println("Utilisateur " + (activer ? "actif" : "désactivé") + " avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du statut de l'utilisateur : " + e.getMessage());
        }
    }


    public Utilisateur getUtilisateurDetails(int id) {
        return getUtilisateurById(id);
    }

    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur user = new Utilisateur();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setRoles(rs.getString("roles"));
        user.setAdresse(rs.getString("adresse"));
        user.setPassword(rs.getString("password"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setDateNaissance(rs.getDate("date_naissance"));
        user.setImage(rs.getString("image"));
        user.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        user.setStatus(rs.getString("status"));
        user.setVerified(rs.getBoolean("is_verified"));
        user.setNumTel(rs.getInt("num_tel"));
        return user;
    }

    public void updateUserImage(int userId, String imageUrl) {
        String query = "UPDATE utilisateur SET image = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, imageUrl);
            pst.setInt(2, userId);
            pst.executeUpdate();
            System.out.println("Image de l'utilisateur mise à jour avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour de l'image de l'utilisateur : " + e.getMessage());
        }
    }
    //mail confirmation pour valider si mm token envoye et dans bd:
    public Utilisateur findByConfirmationToken(String token) {
        String query = "SELECT * FROM utilisateur WHERE confirmation_token = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, token);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    //modfiier user
    public boolean updateUser(Utilisateur user) {
        String query = "UPDATE utilisateur SET is_verified = ?, status = ?, confirmation_token = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setBoolean(1, user.isVerified());
            pst.setString(2, "actif"); // Statut explicite
            pst.setString(3, user.getConfirmationToken()); // Conserver le token
            pst.setInt(4, user.getId());

            // Debug
            System.out.println("DEBUG - Mise à jour utilisateur:");
            System.out.println("ID: " + user.getId());
            System.out.println("isVerified: " + user.isVerified());
            System.out.println("Status: actif");
            System.out.println("Token conservé: " + user.getConfirmationToken());

            int rowsUpdated = pst.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Erreur SQL dans updateUser: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    //login apres register
    public Utilisateur findByEmail(String email) throws SQLException {
        String query = "SELECT email, is_verified, confirmation_token FROM utilisateur WHERE email = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setEmail(rs.getString("email"));
                user.setVerified(rs.getBoolean("is_verified"));
                user.setConfirmationToken(rs.getString("confirmation_token"));
                return user;
            }
        }
        return null;
    }

    //reactivation compte
    public void updateReactivationDate(int userId, LocalDateTime reactivationDate) {
        String query = "UPDATE utilisateur SET reactivation_date = ?, status = 'désactivé' WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setTimestamp(1, Timestamp.valueOf(reactivationDate));
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour de la date de réactivation : " + e.getMessage());
        }
    }
    public void verifierReactivationAutomatique() {
        String query = "UPDATE utilisateur SET status = 'actif', reactivation_date = NULL " +
                "WHERE status = 'désactivé' AND reactivation_date <= NOW()";
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(query);
        } catch (SQLException e) {
            System.err.println("Erreur vérification réactivation: " + e.getMessage());
        }
    }
//    //stats user cnx
//    public Map<LocalDate, Long> getConnectionStatsByPeriod(LocalDate startDate, LocalDate endDate) {
//        Map<LocalDate, Long> stats = new HashMap<>();
//        String query = "SELECT DATE(date_creation) as connection_date, COUNT(*) as count " +
//                "FROM utilisateur " +
//                "WHERE date_creation BETWEEN ? AND ? " +
//                "GROUP BY DATE(date_creation)";
//
//        try (PreparedStatement pst = connection.prepareStatement(query)) {
//            pst.setDate(1, Date.valueOf(startDate));
//            pst.setDate(2, Date.valueOf(endDate));
//
//            ResultSet rs = pst.executeQuery();
//            while (rs.next()) {
//                stats.put(
//                        rs.getDate("connection_date").toLocalDate(),
//                        rs.getLong("count")
//                );
//            }
//        } catch (SQLException e) {
//            System.out.println("Erreur lors de la récupération des stats: " + e.getMessage());
//        }
//
//        // Remplir les dates manquantes avec 0
//        startDate.datesUntil(endDate.plusDays(1))
//                .forEach(date -> stats.putIfAbsent(date, 0L));
//
//        return stats;
//    }
    //stat cnx
    public long countActiveUsers() {
        String query = "SELECT COUNT(*) FROM utilisateur WHERE status = 'actif'";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage des utilisateurs actifs: " + e.getMessage());
        }
        return 0;
    }

    //stat cnx
    public Map<String, Long> getConnectionStats(LocalDate startDate, LocalDate endDate, String periodType) {
        Map<String, Long> stats = new HashMap<>();
        String groupBy = "DATE(date_creation)";
        String format = "DATE(date_creation)";

        switch (periodType) {
            case "DAY": // Connexions aujourd'hui par heure
                format = "HOUR(date_creation)";
                groupBy = "HOUR(date_creation)";
                break;
            case "WEEK":
                format = "DATE(date_creation)";
                groupBy = "DATE(date_creation)";
                break;
            case "MONTH":
                format = "WEEK(date_creation)";
                groupBy = "WEEK(date_creation)";
                break;
        }

        String query = "SELECT " + format + " as period, COUNT(*) as count " +
                "FROM utilisateur " +
                "WHERE date_creation BETWEEN ? AND ? " +
                "GROUP BY " + groupBy;

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setDate(1, Date.valueOf(startDate));
            pst.setDate(2, Date.valueOf(endDate));

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                stats.put(rs.getString("period"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des stats: " + e.getMessage());
        }

        return stats;
    }
        // Ajoutez ces méthodes à votre UtilisateurService

    public Map<String, Long> getDailyConnections(LocalDate startDate, LocalDate endDate) {
        Map<String, Long> stats = new HashMap<>();
        String query = "SELECT DATE(date_creation) as day, COUNT(*) as count " +
                "FROM utilisateur " +
                "WHERE date_creation BETWEEN ? AND ? " +
                "GROUP BY DATE(date_creation)";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setDate(1, Date.valueOf(startDate));
            pst.setDate(2, Date.valueOf(endDate));

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                stats.put(rs.getDate("day").toLocalDate().toString(), rs.getLong("count"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getDailyConnections: " + e.getMessage());
        }

        // Remplir les dates manquantes avec 0
        startDate.datesUntil(endDate.plusDays(1))
                .forEach(date -> stats.putIfAbsent(date.toString(), 0L));

        return stats;
    }

    public Map<String, Long> getHourlyConnections(LocalDate date) {
        Map<String, Long> stats = new HashMap<>();
        if (date == null) return stats;

        String query = "SELECT HOUR(date_creation) as hour, COUNT(*) as count " +
                "FROM utilisateur " +
                "WHERE DATE(date_creation) = ? " +
                "GROUP BY HOUR(date_creation)";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setDate(1, Date.valueOf(date));

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                stats.put(String.valueOf(rs.getInt("hour")), rs.getLong("count"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getHourlyConnections: " + e.getMessage());
        }

        // Remplir les heures manquantes (0-23) avec 0
        for (int i = 0; i < 24; i++) {
            stats.putIfAbsent(String.valueOf(i), 0L);
        }

        return stats;
    }

    public Map<String, Long> getRegistrationsByType(LocalDate startDate, LocalDate endDate) {
        Map<String, Long> stats = new HashMap<>();
        String query = "SELECT roles, COUNT(*) as count " +
                "FROM utilisateur " +
                "WHERE date_creation BETWEEN ? AND ? " +
                "GROUP BY roles";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setDate(1, Date.valueOf(startDate));
            pst.setDate(2, Date.valueOf(endDate));

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String role = rs.getString("roles");
                // Simplifier les noms de rôles pour l'affichage
                String displayRole = role.replace("ROLE_", "").toLowerCase();
                stats.put(displayRole, rs.getLong("count"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getRegistrationsByType: " + e.getMessage());
        }

        return stats;
    }

    public Map<String, Long> getMonthlyRegistrations(LocalDate startDate, LocalDate endDate) {
        Map<String, Long> stats = new HashMap<>();
        String query = "SELECT DATE_FORMAT(date_creation, '%Y-%m') as month, COUNT(*) as count " +
                "FROM utilisateur " +
                "WHERE date_creation BETWEEN ? AND ? " +
                "GROUP BY DATE_FORMAT(date_creation, '%Y-%m') " +
                "ORDER BY month";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setDate(1, Date.valueOf(startDate));
            pst.setDate(2, Date.valueOf(endDate));

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                stats.put(rs.getString("month"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getMonthlyRegistrations: " + e.getMessage());
        }

        return stats;
    }

    public Map<String, Long> getUserTypesDistribution() {
        Map<String, Long> stats = new HashMap<>();
        String query = "SELECT roles, COUNT(*) as count FROM utilisateur GROUP BY roles";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String role = rs.getString("roles");
                String displayRole = role.replace("ROLE_", "").toLowerCase();
                stats.put(displayRole, rs.getLong("count"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getUserTypesDistribution: " + e.getMessage());
        }

        return stats;
    }

    public Map<String, Long> getUserStatusDistribution() {
        Map<String, Long> stats = new HashMap<>();
        String query = "SELECT status, COUNT(*) as count FROM utilisateur GROUP BY status";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getUserStatusDistribution: " + e.getMessage());
        }

        return stats;
    }


}
