package services;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import entities.Reservation;
import utils.DataBase;
import utils.Mail;
import utils.SessionManager;
import javax.mail.MessagingException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReservationService implements IService<Reservation> {
    private static Connection connection;

    public ReservationService() {
        connection = DataBase.getInstance().getConnection();
    }

    SessionManager session = SessionManager.getInstance();
    int clientId = session.getId();

    @Override
    public void ajouter(Reservation reservation) throws SQLException {
        String query = "INSERT INTO reservation (evenement_id, client_id, date_reservation, etat) VALUES (?, ?, NOW(), 'en attente')";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, reservation.getEvenementId());
            preparedStatement.setInt(2, clientId);
            preparedStatement.executeUpdate();
            System.out.println("Réservation ajoutée avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la réservation : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void modifier(Reservation reservation) throws SQLException {
        // Implémentation si nécessaire
    }

    @Override
    public void supprimer(int idReservation) throws SQLException {
        String query = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, idReservation);
            preparedStatement.executeUpdate();
            System.out.println("Réservation annulée avec succès.");
        }
    }

    @Override
    public List<Reservation> afficher() throws SQLException {
        return new ArrayList<>();
    }

    public boolean existeDeja(int evenementId, int clientId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reservation WHERE evenement_id = ? AND client_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, evenementId);
            preparedStatement.setInt(2, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<Map<String, Object>> getReservationsWithEventTitleByClientId(int clientId) throws SQLException {
        String query = """
            SELECT r.id AS reservation_id, r.date_reservation, r.etat, e.titre AS evenement_titre
            FROM reservation r
            JOIN evenement e ON r.evenement_id = e.id
            WHERE r.client_id = ?""";

        List<Map<String, Object>> reservations = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> reservationData = new HashMap<>();
                reservationData.put("reservation_id", resultSet.getInt("reservation_id"));
                reservationData.put("date_reservation", resultSet.getDate("date_reservation").toLocalDate());
                reservationData.put("etat", resultSet.getString("etat"));
                reservationData.put("evenement_titre", resultSet.getString("evenement_titre"));
                reservations.add(reservationData);
            }
        }
        return reservations;
    }

    public List<Map<String, Object>> getReservationsByEvenementId(int evenementId) throws SQLException {
        String query = """
            SELECT r.id AS reservation_id, r.date_reservation, r.etat, c.last_name AS client_nom
            FROM reservation r
            JOIN utilisateur c ON r.client_id = c.id
            WHERE r.evenement_id = ?""";

        List<Map<String, Object>> reservations = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, evenementId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> reservationData = new HashMap<>();
                reservationData.put("reservation_id", resultSet.getInt("reservation_id"));
                reservationData.put("date_reservation", resultSet.getDate("date_reservation").toLocalDate());
                reservationData.put("etat", resultSet.getString("etat"));
                reservationData.put("client_nom", resultSet.getString("client_nom"));
                reservations.add(reservationData);
            }
        }
        return reservations;
    }

    public void modifierEtatReservation(int reservationId, String nouvelEtat) throws SQLException {
        String updateQuery = "UPDATE reservation SET etat = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setString(1, nouvelEtat);
            preparedStatement.setInt(2, reservationId);
            preparedStatement.executeUpdate();

            if ("confirmée".equalsIgnoreCase(nouvelEtat)) {
                String selectQuery = """
                SELECT u.email, u.first_name, e.titre, 
                       e.date_debut, e.localisation, e.prix
                FROM reservation r
                JOIN utilisateur u ON r.client_id = u.id
                JOIN evenement e ON r.evenement_id = e.id
                WHERE r.id = ?""";

                try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
                    ps.setInt(1, reservationId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        // Récupérer toutes les infos nécessaires pour le QR code
                        String email = rs.getString("email");
                        String prenom = rs.getString("first_name");
                        String titre = rs.getString("titre");
                        String date = rs.getTimestamp("date_debut").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                        String lieu = rs.getString("localisation");
                        double prix = rs.getDouble("prix");

                        // Construire le contenu détaillé pour le QR code
                        String qrContent = String.format(
                                "Réservation #%d\nÉvénement: %s\nDate: %s\nLieu: %s\nPrix: %.2f DT",
                                reservationId, titre, date, lieu, prix
                        );

                        // Envoyer l'email avec QR code
                        new Mail().sendReservationConfirmation(
                                email,
                                prenom,
                                titre,
                                reservationId,
                                qrContent
                        );
                    }
                } catch (MessagingException e) {
                    System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
                }
            }
        }
    }

    public Map<String, Integer> getReservationsParEvenement() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String query = """
            SELECT e.titre, COUNT(r.id) AS count 
            FROM evenement e 
            LEFT JOIN reservation r ON e.id = r.evenement_id 
            GROUP BY e.titre""";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stats.put(rs.getString("titre"), rs.getInt("count"));
            }
        }
        return stats;
    }

    public void exportReservationsToPdf(int evenementId, String filePath) throws Exception {
        List<Map<String, Object>> reservations = getReservationsByEvenementId(evenementId);

        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            // LOGO
            Image logo = new Image(ImageDataFactory.create(getClass().getResource("/img/logo.png")))
                    .setHeight(50)
                    .setAutoScale(true);
            document.add(logo);

            // DATE ET HEURE DE GÉNÉRATION
            String dateTimeNow = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            Paragraph info = new Paragraph("Généré le : " + dateTimeNow)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(info);

            // TITRE
            Paragraph title = new Paragraph("📋 Liste des Réservations")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10)
                    .setMarginBottom(20);
            document.add(title);

            // TABLEAU
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 3, 2}));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setBorder(new SolidBorder(ColorConstants.GRAY, 1));

            // En-têtes
            table.addHeaderCell(new Cell().add(new Paragraph("Client").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Date de réservation").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("État").setBold()));

            // Contenu
            for (Map<String, Object> res : reservations) {
                table.addCell(res.get("client_nom").toString());
                table.addCell(res.get("date_reservation").toString());
                table.addCell(res.get("etat").toString());
            }

            document.add(table);
        }
    }
    public List<Map<String, Object>> getEvenementClassementParReservation() throws SQLException {
        String query = """
        SELECT e.id AS evenement_id, e.titre, COUNT(r.id) AS nombre_reservations
        FROM evenement e
        LEFT JOIN reservation r ON e.id = r.evenement_id
        GROUP BY e.id, e.titre
        ORDER BY nombre_reservations DESC
        LIMIT 3""";

        List<Map<String, Object>> classement = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("evenement_id", rs.getInt("evenement_id"));
                data.put("titre", rs.getString("titre"));
                data.put("nombre_reservations", rs.getInt("nombre_reservations"));
                classement.add(data);
            }
        }
        return classement;
    }
    public Map<String, Integer> getNombreReservationsParEvenementPourAgriculteur(int agriculteurId) throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String query = """
        SELECT e.titre, COUNT(r.id) AS nombre
        FROM reservation r
        JOIN evenement e ON r.evenement_id = e.id
        WHERE e.agriculteur_id = ?
        GROUP BY e.titre
        ORDER BY nombre DESC
    """;

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, agriculteurId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            stats.put(rs.getString("titre"), rs.getInt("nombre"));
        }

        return stats;
    }


}