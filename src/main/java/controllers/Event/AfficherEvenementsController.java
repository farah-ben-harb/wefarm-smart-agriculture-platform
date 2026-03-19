package controllers.Event;

import controllers.OffreEmploi.ModifierOffreEmploiController;
import entities.Evenement;
import entities.OffreEmploi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.EvenementService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.apache.http.client.utils.DateUtils.formatDate;

public class AfficherEvenementsController {

    public Button ajouterBtn;
    public ScrollPane mainScroll;
    @FXML
    private TableView<Evenement> evenementTable;

    @FXML
    private TableColumn<Evenement, Integer> idColumn;

    @FXML
    private TableColumn<Evenement, String> titreColumn;

    @FXML
    private TableColumn<Evenement, String> descriptionColumn;

    @FXML
    private TableColumn<Evenement, String> dateColumn;

    @FXML
    private TableColumn<Evenement, String> typeColumn;

    @FXML
    private TableColumn<Evenement, String> localisationColumn;

    @FXML
    private TableColumn<Evenement, Double> prixColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button modifierButton;

    @FXML
    private Button supprimerButton;

    private final EvenementService evenementService = new EvenementService();
    private final ObservableList<Evenement> evenementList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    @FXML private TilePane evenementTilePane;
    @FXML private TextField searchField;
    private SessionManager session = SessionManager.getInstance();
    private final int currentUserId = session.getId();

    public void initialize() {
        chargerEvenements();
        searchField.setOnKeyReleased(e -> handleSearch());
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

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase().trim();
        evenementTilePane.getChildren().clear();

        try {
            List<Evenement> evenements = evenementService.afficher();
            for (Evenement event : evenements) {
                if (keyword.isEmpty() ||
                        event.getTitre().toLowerCase().contains(keyword)) {
                    evenementTilePane.getChildren().add(createEventCard(event));
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Erreur lors de la recherche : " + e.getMessage());
        }
    }

    private VBox createEventCard(Evenement event) {
        VBox card = new VBox(8);
        card.getStyleClass().add("event-card");
        card.setPadding(new Insets(15));
        card.setPrefWidth(360);
        card.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-background-radius: 8;");

        // ⭐ Étoiles dynamiques


        // 🏡 Titre
        Label titleLabel = new Label("🏡 " + event.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 📝 Description
        Label desc = new Label(event.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 13px;");

        // 📅 Date
        Label dateLabel = new Label("Date de début: " + dateFormatter.format(event.getDateDebut()));

        // 📍 Type
        Label typeLabel = new Label("Type de visite: " + event.getTypeVisite());

        // 🌐 Localisation
        HBox locationBox = new HBox(5);
        Label locLabel = new Label("Localisation:");
        Hyperlink locLink = new Hyperlink(event.getLocalisation());
        locLink.setOnAction(e -> openMapsLink(event.getLocalisation()));
        locationBox.getChildren().addAll(locLabel, locLink);

        // 💵 Prix
        Label priceLabel = new Label("Prix: " + event.getPrix() + " DT");

        // 🔘 Boutons
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        if (event.getAgriculteurId() == currentUserId) {
            Button voirReservationsBtn = new Button("👥 Voir les réservations");
            voirReservationsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00B074; -fx-font-weight: bold;");
            voirReservationsBtn.setOnAction(e -> ouvrirListeReservations(event.getId()));

            Button editBtn = new Button("✏ Modifier");
            editBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
            editBtn.setOnAction(e -> ouvrirFormulaireModification(event));

            Button deleteBtn = new Button("🗑 Supprimer");
            deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            deleteBtn.setOnAction(e -> supprimerEvent(event, card));

            footer.getChildren().addAll(voirReservationsBtn, editBtn, deleteBtn);
        } else {
            Button reserverBtn = new Button("📩 Réserver");
            reserverBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
            reserverBtn.setOnAction(e -> {
                // Appeler la méthode de réservation ici
            });
            footer.getChildren().add(reserverBtn);
        }

        card.getChildren().addAll(
                titleLabel, desc,
                dateLabel, typeLabel, locationBox,
                priceLabel, footer
        );

        return card;
    }

    // Suppression d'une offre d'emploi
    private void supprimerEvent(Evenement evenement, VBox card) {
        // Création de la boîte de dialogue personnalisée
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmation de suppression");
        dialog.setHeaderText("Confirmation de suppression");

        // Style CSS personnalisé
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/stylesOffresEmploi.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("confirmation-dialog");

        // Contenu personnalisé
        Label content = new Label("Êtes-vous sûr de vouloir supprimer cet événement ? Cette action est irréversible.");
        content.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        dialogPane.setContent(content);

        // Boutons personnalisés
        ButtonType nonButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.NO);
        ButtonType ouiButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.YES);
        dialogPane.getButtonTypes().addAll(nonButtonType, ouiButtonType);

        // Personnalisation des boutons
        Button ouiButton = (Button) dialogPane.lookupButton(ouiButtonType);
        Button nonButton = (Button) dialogPane.lookupButton(nonButtonType);
        nonButton.getStyleClass().add("cancel-button");
        ouiButton.getStyleClass().add("confirm-button");


        // Affichage et traitement
        dialog.showAndWait().ifPresent(response -> {
            if (response == ouiButtonType) {
                try {
                    evenementService.supprimer(evenement.getId());
                    evenementTilePane.getChildren().remove(card);
                    showSuccessAlert("Evénement supprimé avec succès !");

                } catch (SQLException e) {
                    showErrorAlert("Erreur lors de la suppression : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // Méthode helper pour ouvrir les liens de localisation
    private void openMapsLink(String url) {
        try {
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            //HostServicesProvider.getHostServices().showDocument(url);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le lien de localisation");
        }
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    void refreshTable() {
        try {
            evenementList.clear();
            evenementList.addAll(evenementService.afficher());
            evenementTable.setItems(evenementList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }


    private void updateButtonsState(Evenement selected) {
        boolean isSelected = selected != null;
        modifierButton.setDisable(!isSelected);
        supprimerButton.setDisable(!isSelected);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Méthode pour ouvrir le formulaire d'un événement
    public void ouvrirModalAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/evenement/ajouterEvenement.fxml"));
            Parent root = loader.load();

            // Récupérer la scène actuelle à partir de n'importe quel composant de ta vue
            Stage currentStage = (Stage) ajouterBtn.getScene().getWindow();

            // Changer la scène de la fenêtre actuelle
            currentStage.setScene(new Scene(root, 600, 400));
            currentStage.setTitle("Ajouter un événement");

            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Méthode pour ouvrir le formulaire de modification d'un événement
    private void ouvrirFormulaireModification(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/evenement/modifierEvenement.fxml"));
            Parent root = loader.load();

            // Injecter l'offre dans le contrôleur
            ModifierEvenementController controller = loader.getController();
            controller.setEventAModifier(evenement);

            // Créer une nouvelle scène et fenêtre
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("Modifier l'événement");

            // Configuration du plein écran
            newStage.setMaximized(true);
            // OU pour permettre la sortie avec un message
            // newStage.setFullScreenExitHint("Appuyez sur Echap pour quitter le mode plein écran");

            newStage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) mainScroll.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            // Gestion d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir l'éditeur");
            alert.setContentText("Une erreur est survenue lors de l'ouverture du formulaire de modification.");
            alert.showAndWait();
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
    private void ouvrirListeReservations(int evenementId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/evenement/AfficherReservationsParEvenement.fxml"));
            Parent root = loader.load();

            // Passer l'ID de l'événement au contrôleur de la nouvelle page
            AfficherReservationsParEvenementController controller = loader.getController();
            controller.setEvenementId(evenementId);
            controller.chargerReservations();

            Stage stage = new Stage();
            stage.setTitle("Réservations pour l'événement");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void afficherStatistiques() {
        try {
            // Correction du chemin FXML (adaptez selon votre structure de projet)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Evenement/StatistiquesReservationsClient.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Statistiques des réservations");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur lors du chargement de l'interface: " + e.getMessage());
            e.printStackTrace();
        }
    }
    }



