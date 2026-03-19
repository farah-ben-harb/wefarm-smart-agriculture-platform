package controllers.Event;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.ReservationService;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AfficherReservationsParEvenementController {

    @FXML
    private TilePane reservationTilePane;
    @FXML
    private Button exportPdfBtn; // ce bouton doit avoir fx:id dans ton fichier FXML

    private final ReservationService reservationService = new ReservationService();
    private int evenementId;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setEvenementId(int evenementId) {
        this.evenementId = evenementId;
    }

    public void chargerReservations() {
        reservationTilePane.getChildren().clear();
        try {
            List<Map<String, Object>> reservations = reservationService.getReservationsByEvenementId(evenementId);
            if (reservations.isEmpty()) {
                Label noData = new Label("Aucune réservation pour cet événement.");
                reservationTilePane.getChildren().add(noData);
                return;
            }

            for (Map<String, Object> reservation : reservations) {
                VBox card = new VBox(10);
                card.setPadding(new Insets(10));
                card.setAlignment(Pos.CENTER_LEFT);
                card.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5px; -fx-background-radius: 5px;");
                card.setPrefWidth(300);

                int reservationId = (int) reservation.get("reservation_id");
                String clientNom = (String) reservation.get("client_nom");
                String etat = (String) reservation.get("etat");
                String dateReservation = formatter.format((java.time.LocalDate) reservation.get("date_reservation"));

                Label clientLabel = new Label("Client : " + clientNom);
                Label dateLabel = new Label("Date de réservation : " + dateReservation);
                Label etatLabel = new Label("État actuel : " + etat);

                // ComboBox pour changer l'état
                ComboBox<String> etatComboBox = new ComboBox<>();
                etatComboBox.getItems().addAll("en attente", "confirmée", "refusée");
                etatComboBox.setValue(etat);

                // Bouton Modifier
                Button modifierBtn = new Button("Modifier l'état");
                modifierBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 5px;");
                modifierBtn.setOnAction(e -> {
                    String nouvelEtat = etatComboBox.getValue();
                    try {
                        reservationService.modifierEtatReservation(reservationId, nouvelEtat);
                        showSuccessAlert("État de la réservation mis à jour avec succès !");
                        chargerReservations();
                    } catch (SQLException ex) {
                        showErrorAlert("Erreur lors de la modification : " + ex.getMessage());
                    }
                });

                card.getChildren().addAll(clientLabel, dateLabel, etatLabel, etatComboBox, modifierBtn);
                reservationTilePane.getChildren().add(card);
            }
        } catch (SQLException e) {
            showErrorAlert("Erreur lors du chargement des réservations : " + e.getMessage());
        }
    }

    @FXML
    private void handleExportPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.setInitialFileName("reservations_evenement_" + evenementId + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                reservationService.exportReservationsToPdf(evenementId, file.getAbsolutePath());
                showSuccessAlert("PDF généré avec succès !");
            } catch (Exception e) {
                showErrorAlert("Erreur lors de la génération du PDF : " + e.getMessage());
            }
        }
    }

    private void showSuccessAlert(String message) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/success.png")));
        icon.setFitWidth(30);
        icon.setFitHeight(30);
        Notifications.create()
                .title("Succès")
                .text(message)
                .graphic(icon)
                .hideAfter(Duration.seconds(3))
                .position(Pos.TOP_RIGHT)
                .show();
    }

    private void showErrorAlert(String message) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/error.png")));
        icon.setFitWidth(30);
        icon.setFitHeight(30);
        Notifications.create()
                .title("Erreur")
                .text(message)
                .graphic(icon)
                .hideAfter(Duration.seconds(4))
                .position(Pos.TOP_RIGHT)
                .show();
    }
}
