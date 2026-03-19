package controllers.Event;

import entities.Evenement;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.EvenementService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherEvenementsAdminController implements Initializable {

    public TableColumn<Evenement, String> statutCol;
    @FXML
    private TableView<Evenement> eventsTable;

    private EvenementService serviceEvenement;
    private ObservableList<Evenement> eventsList = FXCollections.observableArrayList();
    @FXML
    private TableColumn<Evenement, Void> actionsCol;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serviceEvenement = new EvenementService();
        refreshTable();
        eventsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        eventsTable.getStylesheets().add(getClass().getResource("/css/stylesOffresEmploi.css").toExternalForm());
        // Configurer les colonnes
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<Evenement, Void>() {
            private final HBox container = new HBox(5);
            private final Button modifierBtn = new Button("🖊");
            private final Button participantsBtn = new Button("👥");
            private final Button supprimerBtn = new Button("🗑");

            {
                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(modifierBtn, participantsBtn, supprimerBtn);

                modifierBtn.setStyle("-fx-font-size: 14px;-fx-background-color: white; -fx-text-fill: #f6c23e;-fx-border-color:#f6c23e; -fx-border-radius: 4px;");
                participantsBtn.setStyle("-fx-background-color: white; -fx-text-fill: #4e73df;-fx-border-color:#4e73df; -fx-border-radius: 4px;");
                supprimerBtn.setStyle("-fx-font-size: 14px;-fx-background-color: white; -fx-text-fill: #e74a3b;-fx-border-color:#e74a3b; -fx-border-radius: 4px;");

                modifierBtn.setOnAction(event -> {
                    Evenement evenement = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireModification(evenement);
                });

                participantsBtn.setOnAction(event -> {
                    Evenement evenement = getTableView().getItems().get(getIndex());
                    afficherParticipants(evenement);
                });

                supprimerBtn.setOnAction(event -> {
                    Evenement evenement = getTableView().getItems().get(getIndex());
                    supprimerEvenement(evenement);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }


    @FXML
    void refreshTable() {
        try {
            List<Evenement> evenements = serviceEvenement.afficher();
            eventsList.clear();
            eventsList.addAll(evenements);
            // Trier la liste par ID de l'événement (croissant)
            eventsList.sort(Comparator.comparing(Evenement::getId));
            eventsTable.setItems(eventsList);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des événements: " + e.getMessage());
        }
    }

    // Méthode pour ouvrir le formulaire d'ajout d'un événement
    public void ouvrirFormulaireAjout() {
        try {
            // Fermer la fenêtre actuelle
            Stage stageActuelle = (Stage) eventsTable.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/evenement/ajouterEvenementAdmin.fxml"));
            Parent root = loader.load();

            Stage nouvelleStage = new Stage();
            nouvelleStage.setTitle("Ajouter un Événement");
            nouvelleStage.setMaximized(true);
            nouvelleStage.setScene(new Scene(root, 600, 400));
            nouvelleStage.setOnHidden(e -> refreshEvents());

            // Fermer l'ancienne fenêtre
            stageActuelle.close();

            nouvelleStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour ouvrir le formulaire de modification d'un événement
    private void ouvrirFormulaireModification(Evenement evenement) {
        try {
            // Fermer la fenêtre actuelle
            Stage stageActuelle = (Stage) eventsTable.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/evenement/modifierEvenementAdmin.fxml"));
            Parent root = loader.load();

            ModifierEvenementController controller = loader.getController();
            controller.setEventAModifier(evenement);

            Stage nouvelleStage = new Stage();
            nouvelleStage.setOnHidden(e -> refreshEvents());
            nouvelleStage.setScene(new Scene(root));
            nouvelleStage.setTitle("Modifier l'événement");
            nouvelleStage.setMaximized(true);

            // Fermer l'ancienne fenêtre
            stageActuelle.close();

            nouvelleStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Suppression d'un événement
    private void supprimerEvenement(Evenement evenement) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmation de suppression");
        dialog.setHeaderText("Confirmation de suppression");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/stylesOffresEmploi.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("confirmation-dialog");

        Label content = new Label("Êtes-vous sûr de vouloir supprimer l'événement \"" + evenement.getTitre() + "\" ? Cette action est irréversible.");
        content.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        dialogPane.setContent(content);

        ButtonType nonButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.NO);
        ButtonType ouiButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.YES);
        dialogPane.getButtonTypes().addAll(nonButtonType, ouiButtonType);

        Button ouiButton = (Button) dialogPane.lookupButton(ouiButtonType);
        Button nonButton = (Button) dialogPane.lookupButton(nonButtonType);
        nonButton.getStyleClass().add("cancel-button");
        ouiButton.getStyleClass().add("confirm-button");

        dialog.showAndWait().ifPresent(response -> {
            if (response == ouiButtonType) {
                try {
                    serviceEvenement.supprimer(evenement.getId());
                    refreshTable(); // Rafraîchir le tableau après suppression
                    showSuccessAlert("Événement supprimé avec succès !");
                } catch (SQLException e) {
                    showErrorAlert("Erreur lors de la suppression : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // Afficher les participants à un événement
    private void afficherParticipants(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/evenement/participantsEvenement.fxml"));
            Parent root = loader.load();

            ParticipantsEvenementController controller = loader.getController();
            controller.setEvenement(evenement);
            controller.loadReservations();

            Stage stage = new Stage();
            stage.setTitle("Réservations pour " + evenement.getTitre());
            stage.setMaximized(true);
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Impossible d'ouvrir la vue des participants");
        }
    }


    /* ********************AMELIORATIONS************************* */
    // Rafraichir la page pour voir les modifications
    public void refreshEvents() {
        try {
            eventsList.clear();
            eventsList.addAll(serviceEvenement.afficher());
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
                .hideAfter(Duration.seconds(3))
                .position(Pos.TOP_RIGHT)
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
                .hideAfter(Duration.seconds(4))
                .position(Pos.TOP_RIGHT)
                .show();
    }
}