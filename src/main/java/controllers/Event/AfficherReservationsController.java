package controllers.Event;

import entities.Reservation;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.ReservationService;
import utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AfficherReservationsController {

    @FXML
    private TilePane reservationTilePane;

    private final ReservationService reservationService = new ReservationService();
    private SessionManager session = SessionManager.getInstance();
    private final int clientId = session.getId();

    @FXML
    public void initialize() {
        chargerReservations();
    }

    private void chargerReservations() {
        reservationTilePane.getChildren().clear();
        try {
            List<Map<String, Object>> reservations = reservationService.getReservationsWithEventTitleByClientId(clientId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Map<String, Object> reservation : reservations) {
                VBox card = new VBox(10);
                card.setPadding(new Insets(10));
                card.setAlignment(Pos.CENTER_LEFT);
                card.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5px; -fx-background-radius: 5px;");
                card.setPrefWidth(300);

                int reservationId = (int) reservation.get("reservation_id");
                String titre = (String) reservation.get("evenement_titre");
                String etat = (String) reservation.get("etat");
                LocalDate dateReservation = (LocalDate) reservation.get("date_reservation");

                Label evenementLabel = new Label("Événement : " + titre);
                Label dateLabel = new Label("Date de réservation : " + formatter.format(dateReservation));
                Label etatLabel = new Label("État : " + etat);

                // 🎯 Bouton Supprimer
                Button supprimerBtn = new Button("Annuler la réservation");
                supprimerBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5;");

                supprimerBtn.setOnAction(e -> {
                    try {
                        reservationService.supprimer(reservationId);
                        showSuccessAlert("Réservation annulée avec succès !");
                        chargerReservations(); // Refresh après suppression
                    } catch (SQLException ex) {
                        showErrorAlert("Erreur lors de l'annulation : " + ex.getMessage());
                    }
                });

                card.getChildren().addAll(evenementLabel, dateLabel, etatLabel, supprimerBtn);
                reservationTilePane.getChildren().add(card);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Méthodes d'alerte améliorées
    // Nouvelle alerte de succès (toast notification)
    private void showSuccessAlert(String message) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/success.png")));
        icon.setFitWidth(30);
        icon.setFitHeight(30);

        Notifications.create()
                .title("Succès")
                .text(message)
                .graphic(icon)
                .hideAfter(Duration.seconds(3)) // Se ferme automatiquement après 3 secondes
                .position(Pos.TOP_RIGHT) // Position en haut à droite
                .show();
    }
    // Nouvelle alerte d'erreur (toast notification)
    private void showErrorAlert(String message) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/error.png")));
        icon.setFitWidth(30);
        icon.setFitHeight(30);

        Notifications.create()
                .title("Erreur")
                .text(message)
                .graphic(icon)
                .hideAfter(Duration.seconds(4)) // Se ferme automatiquement après 5 secondes
                .position(Pos.TOP_RIGHT)
                .show();
    }



}
