package controllers.Event;

import entities.Evenement;
import entities.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.EvenementService;
import services.ReservationService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfficherEvenementsClientController {

    @FXML private ScrollPane mainScroll;
    @FXML private TilePane evenementTilePane;
    @FXML private TextField searchField;

    private final EvenementService evenementService = new EvenementService();
    private final ReservationService reservationService = new ReservationService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final SessionManager session = SessionManager.getInstance();
    private final int currentUserId = session.getId();

    @FXML
    public void initialize() {
        calculerClassementEtoiles();
        chargerEvenements();
        searchField.setOnKeyReleased(e -> handleSearch());
    }
    private Map<Integer, Integer> classementEtoiles = new HashMap<>();

    private void calculerClassementEtoiles() {
        try {
            List<Map<String, Object>> classement = reservationService.getEvenementClassementParReservation();
            int count = 0;
            for (Map<String, Object> row : classement) {
                int evenementId = (int) row.get("evenement_id");
                int etoiles = 0;
                if (count == 0) {
                    etoiles = 3; // Le plus réservé → 3 étoiles
                } else if (count == 1) {
                    etoiles = 2; // Deuxième → 2 étoiles
                } else if (count == 2) {
                    etoiles = 1; // Troisième → 1 étoile
                }
                classementEtoiles.put(evenementId, etoiles);
                count++;
                if (count >= 3) break; // Limiter au top 3
            }
        } catch (SQLException e) {
            showErrorAlert("Erreur lors du calcul des recommandations : " + e.getMessage());
        }
    }

    private void chargerEvenements() {
        evenementTilePane.getChildren().clear();
        try {
            List<Evenement> evenements = evenementService.afficher();
            for (Evenement event : evenements) {
                evenementTilePane.getChildren().add(createEventCard(event));
            }
        } catch (SQLException e) {
            showErrorAlert("Erreur lors du chargement des événements : " + e.getMessage());
        }
    }

    private VBox createEventCard(Evenement event) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");
        card.setPadding(new Insets(15));
        card.setPrefWidth(350);

        // ----- HEADER -----
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label dateLabel = new Label(dateFormatter.format(event.getDateDebut()));
        dateLabel.getStyleClass().add("event-date");
        Label typeLabel = new Label(event.getTypeVisite());
        typeLabel.getStyleClass().add("event-type");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(dateLabel, spacer, typeLabel);

        // ----- TITLE -----
        Label titleLabel = new Label(event.getTitre());
        titleLabel.getStyleClass().add("event-title");
        titleLabel.setWrapText(true);

        // ----- DESCRIPTION -----
        TextArea descriptionArea = new TextArea(event.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(3);
        descriptionArea.getStyleClass().add("event-description");

        // ----- LOCATION -----
        HBox locationBox = new HBox(5);
        Label locationLabel = new Label("Localisation :");
        Hyperlink locationLink = new Hyperlink(event.getLocalisation());
        locationLink.setOnAction(e -> openMapsLink(event.getLocalisation()));
        locationBox.getChildren().addAll(locationLabel, locationLink);

        // ----- PRICE -----
        Label priceLabel = new Label(String.format("Prix : %.2f DT", event.getPrix()));
        priceLabel.getStyleClass().add("event-price");

        // ----- RECOMMANDATION ÉTOILES -----
        if (classementEtoiles.containsKey(event.getId())) {
            int etoiles = classementEtoiles.get(event.getId());
            HBox starsBox = new HBox(3);
            starsBox.setAlignment(Pos.CENTER_LEFT);
            for (int i = 0; i < etoiles; i++) {
                ImageView star = new ImageView(new Image(getClass().getResourceAsStream("/icons/star.png")));
                star.setFitWidth(20);
                star.setFitHeight(20);
                starsBox.getChildren().add(star);
            }
            card.getChildren().add(starsBox);
        }

        // ----- FOOTER AVEC BOUTON RÉSERVER -----
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button reserverBtn = new Button("Réserver");
        reserverBtn.getStyleClass().add("delete-button");

        reserverBtn.setOnAction(e -> {
            Reservation reservation = new Reservation();
            reservation.setEvenementId(event.getId());
            try {
                if (reservationService.existeDeja(event.getId(), currentUserId)) {
                    showErrorAlert("Vous avez déjà réservé cet événement !");
                } else {
                    reservationService.ajouter(reservation);
                    showSuccessAlert("Réservation effectuée avec succès !");
                }
            } catch (SQLException ex) {
                showErrorAlert("Erreur lors de la réservation : " + ex.getMessage());
            }
        });

        footer.getChildren().add(reserverBtn);

        // ----- AJOUT DE TOUS LES ÉLÉMENTS DANS LA CARTE -----
        card.getChildren().addAll(header, titleLabel, descriptionArea, locationBox, priceLabel, footer);
        return card;
    }


    private void openMapsLink(String url) {
        try {
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            // HostServicesProvider.getHostServices().showDocument(url);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le lien de localisation");
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase();
        evenementTilePane.getChildren().clear();
        try {
            List<Evenement> evenements = evenementService.afficher();
            boolean found = false;
            for (Evenement event : evenements) {
                if (event.getTitre().toLowerCase().contains(keyword)) {
                    evenementTilePane.getChildren().add(createEventCard(event));
                    found = true;
                }
            }
            if (!found) {
                Label noResults = new Label("Aucun événement trouvé pour : " + keyword);
                noResults.setStyle("-fx-font-size: 16px; -fx-text-fill: red;");
                evenementTilePane.getChildren().add(noResults);
            }
        } catch (SQLException e) {
            showErrorAlert("Erreur lors de la recherche : " + e.getMessage());
        }
    }

    @FXML
    private void handleVoirMesReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/evenement/AfficherReservations.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Mes Réservations");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
