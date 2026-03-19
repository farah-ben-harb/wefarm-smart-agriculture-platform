package services;

import entities.Evenement;
import entities.Reservation;
import utils.DataBase;
import utils.SessionManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceReservation {
    private Connection connection;

    public ServiceReservation() {
        this.connection = DataBase.getInstance().getConnection();
    }



    public boolean supprimerReservation(int id) throws SQLException {
        String query = "DELETE FROM reservation WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean existeReservation(int evenementId, int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reservation WHERE evenement_id = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, evenementId);
            pstmt.setInt(2, userId);

            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public List<ReservationWithDetails> getReservationsByUser(int userId) throws SQLException {
        List<ReservationWithDetails> reservations = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String query = "SELECT r.*, e.titre as event_titre, e.date_reservation, e.localisation "
                + "FROM reservation r "
                + "LEFT JOIN evenement e ON r.evenement_id = e.id "
                + "WHERE r.user_id = ? "
                + "ORDER BY r.date_reservation DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReservationWithDetails r = new ReservationWithDetails();
                    r.setId(rs.getInt("id"));
                    r.setEvenementId(rs.getInt("evenement_id"));
                    r.setDateReservation(LocalDateTime.parse(rs.getString("date_reservation"), formatter));
                    r.setEtat(rs.getString("etat"));

                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("event_titre", rs.getString("event_titre"));
                    metadata.put("date", rs.getDate("date"));
                    metadata.put("lieu", rs.getString("lieu"));
                    r.setMetadata(metadata);

                    reservations.add(r);
                }
            }
        }

        return reservations;
    }

    public List<ReservationWithDetails> getReservationsForEvent(int eventId) throws SQLException {
        List<ReservationWithDetails> reservations = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String query = "SELECT r.id, r.evenement_id, r.date_reservation, r.etat, "
                + "u.id as user_id, u.last_name as user_nom, u.first_name as user_prenom, "
                + "e.id as event_id, e.titre as event_titre, e.description as event_description, "
                + "e.date_debut as event_date, e.localisation as event_lieu "
                + "FROM reservation r "
                + "JOIN utilisateur u ON r.client_id = u.id "
                + "JOIN evenement e ON r.evenement_id = e.id "
                + "WHERE r.evenement_id = ? "
                + "ORDER BY r.date_reservation DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, eventId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReservationWithDetails reservation = new ReservationWithDetails();
                    reservation.setId(rs.getInt("id"));
                    reservation.setEvenementId(rs.getInt("evenement_id")); // Utilisation correcte

                    // Gestion de la date
                    String dateStr = rs.getString("date_reservation");
                    reservation.setDateReservation(LocalDate.parse(dateStr, dateFormatter).atStartOfDay());

                    reservation.setEtat(rs.getString("etat"));

                    // Métadonnées (si nécessaire)
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("some_key", "some_value"); // Exemple
                    reservation.setMetadata(metadata); // Utilisation correcte

                    // Infos client
                    Map<String, Object> client = new HashMap<>();
                    client.put("id", rs.getInt("user_id"));
                    client.put("nom", rs.getString("user_nom"));
                    client.put("prenom", rs.getString("user_prenom"));
                    reservation.setClientDetails(client);

                    // Infos événement
                    Map<String, Object> event = new HashMap<>();
                    event.put("id", rs.getInt("event_id"));
                    event.put("titre", rs.getString("event_titre"));
                    event.put("description", rs.getString("event_description"));
                    event.put("date", rs.getDate("event_date"));
                    event.put("lieu", rs.getString("event_lieu"));
                    reservation.setEventDetails(event);

                    reservations.add(reservation);
                }
            }
        }

        return reservations;
    }

    public void modifierEtatReservation(int idReservation, String nouvelEtat) throws SQLException {
        String query = "UPDATE reservation SET etat = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, nouvelEtat);
            pst.setInt(2, idReservation);
            pst.executeUpdate();
        }
    }

    public void decrementerCapaciteEvenement(int evenementId) throws SQLException {
        String query = "UPDATE evenement SET capacite = capacite - 1 WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, evenementId);
            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                // Vérifier si la capacité est maintenant à 0
                String checkQuery = "SELECT capacite FROM evenement WHERE id = ?";
                try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, evenementId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt("capacite") <= 0) {
                        // Mettre l'événement comme complet
                        String updateStatusQuery = "UPDATE evenement SET status = 'complet' WHERE id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateStatusQuery)) {
                            updateStmt.setInt(1, evenementId);
                            updateStmt.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    public boolean deleteReservation(int reservationId) throws SQLException {
        // Vérification de l'ID
        if (reservationId <= 0) {
            throw new IllegalArgumentException("ID de réservation invalide");
        }

        String query = "DELETE FROM reservation WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reservationId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Réservation #" + reservationId + " supprimée avec succès");
                return true;
            } else {
                System.out.println("⚠️ Aucune réservation trouvée avec l'ID: " + reservationId);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de la réservation #" + reservationId);
            System.err.println("Code d'erreur SQL: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            throw e; // Propagation de l'exception pour gestion par l'appelant
        }
    }

    public static class ReservationWithDetails {
        private int id;
        private int evenementId; // Ajouté
        private LocalDateTime dateReservation;
        private String etat;
        private Map<String, Object> metadata; // Ajouté
        private Map<String, Object> clientDetails;
        private Map<String, Object> eventDetails;

        // Getters et Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getEvenementId() { return evenementId; } // Ajouté
        public void setEvenementId(int evenementId) { this.evenementId = evenementId; } // Ajouté

        public LocalDateTime getDateReservation() { return dateReservation; }
        public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }

        public String getEtat() { return etat; }
        public void setEtat(String etat) { this.etat = etat; }

        public Map<String, Object> getMetadata() { return metadata; } // Ajouté
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; } // Ajouté

        public Map<String, Object> getClientDetails() { return clientDetails; }
        public void setClientDetails(Map<String, Object> clientDetails) { this.clientDetails = clientDetails; }

        public Map<String, Object> getEventDetails() { return eventDetails; }
        public void setEventDetails(Map<String, Object> eventDetails) { this.eventDetails = eventDetails; }
    }
}