package controllers.OffreEmploi;

import entities.OffreEmploi;
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
import services.ServiceCandidature;
import services.ServiceOffreEmploi;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ListOffreEmploiAdminController implements Initializable {

    public TableColumn <OffreEmploi, String> statutCol;
    @FXML
    private TableView<OffreEmploi> offresTable;

    private ServiceOffreEmploi serviceOffreEmploi;
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();

    private ObservableList<OffreEmploi> offresList = FXCollections.observableArrayList();
    @FXML
    private TableColumn<OffreEmploi, Void> actionsCol;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serviceOffreEmploi = new ServiceOffreEmploi();
        refreshTable();
        offresTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        offresTable.getStylesheets().add(getClass().getResource("/css/stylesOffresEmploi.css").toExternalForm());
        // Configurer les colonnes
        setupActionsColumn();
        setupStatutColumn();


    }
    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<OffreEmploi, Void>() {
            private final HBox container = new HBox(5);
            private final Button modifierBtn = new Button("🖊");
            private final Button candidaturesBtn = new Button("📄");
            private final Button supprimerBtn = new Button("🗑");

            {
                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(modifierBtn, candidaturesBtn, supprimerBtn);

                modifierBtn.setStyle("-fx-font-size: 14px;-fx-background-color: white; -fx-text-fill: #f6c23e;-fx-border-color:#f6c23e; -fx-border-radius: 4px;");
                candidaturesBtn.setStyle("-fx-background-color: white; -fx-text-fill: #4e73df;-fx-border-color:#4e73df; -fx-border-radius: 4px;");
                supprimerBtn.setStyle("-fx-font-size: 14px;-fx-background-color: white; -fx-text-fill: #e74a3b;-fx-border-color:#e74a3b; -fx-border-radius: 4px;");

                modifierBtn.setOnAction(event -> {
                    OffreEmploi offre = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireModification(offre);
                });

                candidaturesBtn.setOnAction(event -> {
                    OffreEmploi offre = getTableView().getItems().get(getIndex());
                    afficherCandidatures(offre);
                });

                supprimerBtn.setOnAction(event -> {
                    OffreEmploi offre = getTableView().getItems().get(getIndex());
                    supprimerOffre(offre);

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
    private void setupStatutColumn() {
        statutCol.setCellFactory(column -> new TableCell<OffreEmploi, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);

                if (empty || statut == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label label = new Label(statut);
                label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 8 4 8; -fx-background-radius: 5px;");

                if (statut.equalsIgnoreCase("actif")) {
                    label.setStyle(label.getStyle() + "-fx-font-size: 12px; -fx-padding: 2px 6px; -fx-background-color: #1cc88a;");
                } else {
                    label.setStyle(label.getStyle() + "-fx-font-size: 12px; -fx-padding: 2px 6px; -fx-background-color: #e74a3b;");
                }

                setGraphic(label);
                setText(null);
            }

            @Override
            public void updateIndex(int i) {
                super.updateIndex(i);
                if (i >= 0 && i < getTableView().getItems().size()) {
                    OffreEmploi offre = getTableView().getItems().get(i);
                    updateItem(offre.getStatus(), false);
                }
            }
        });
    }

    @FXML
    private void refreshTable() {
        try {
            List<OffreEmploi> offres = serviceOffreEmploi.afficher();
            offresList.clear();
            offresList.addAll(offres);
            // Trier la liste par ID de l'offre (croissant)
            offresList.sort(Comparator.comparing(OffreEmploi::getId));
            offresTable.setItems(offresList);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des offres: " + e.getMessage());
        }
    }


    // Méthode pour ouvrir le formulaire d'ajout d'une offre
    public void ouvrirFormulaireAjout() {
        try {
            // Fermer la fenêtre actuelle
            Stage stageActuelle = (Stage) offresTable.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/ajouterOffreAdmin.fxml"));
            Parent root = loader.load();

            Stage nouvelleStage = new Stage();
            nouvelleStage.setTitle("Ajouter une Offre");
            nouvelleStage.setScene(new Scene(root, 600, 400));
            nouvelleStage.setOnHidden(e -> refreshOffres());

            // Fermer l'ancienne fenêtre
            stageActuelle.close();

            nouvelleStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Méthode pour ouvrir le formulaire de modification d'une offre d'emploi
    private void ouvrirFormulaireModification(OffreEmploi offre) {
        try {
            // Fermer la fenêtre actuelle
            Stage stageActuelle = (Stage) offresTable.getScene().getWindow(); // <- Ici

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/modifierOffreAdmin.fxml"));
            Parent root = loader.load();

            ModifierOffreEmploiController controller = loader.getController();
            controller.setOffreAModifier(offre);

            Stage nouvelleStage = new Stage();
            nouvelleStage.setOnHidden(e -> refreshOffres());
            nouvelleStage.setScene(new Scene(root));
            nouvelleStage.setTitle("Modifier l'offre");
            nouvelleStage.setMaximized(true);

            // Fermer l'ancienne fenêtre
            stageActuelle.close();

            nouvelleStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Suppression d'une offre d'emploi
    private void supprimerOffre(OffreEmploi offre) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmation de suppression");
        dialog.setHeaderText("Confirmation de suppression");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/stylesOffresEmploi.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("confirmation-dialog");

        Label content = new Label("Êtes-vous sûr de vouloir supprimer l'offre \"" + offre.getTitre() + "\" ? Cette action est irréversible.");
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
                    serviceOffreEmploi.supprimer(offre.getId());
                    refreshTable(); // Rafraîchir le tableau après suppression
                    showSuccessAlert("Offre supprimée avec succès !");
                } catch (SQLException e) {
                    showErrorAlert("Erreur lors de la suppression : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    //Afficher les candidatures relatives à une offres
    private void afficherCandidatures(OffreEmploi offre) {
        try {
            // Fermer la fenêtre actuelle
            Stage stageActuelle = (Stage) offresTable.getScene().getWindow();

            List<Map<String, Object>> candidaturesWithDetails = serviceCandidature.getCandidaturesWithDetailsByOffreId(offre.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/candidaturesOffreAdmin.fxml"));
            Parent root = loader.load();

            CandidaturesOffreAdminController controller = loader.getController();
            controller.setOffre(offre);
            controller.setCandidaturesWithDetails(candidaturesWithDetails);

            Stage nouvelleStage = new Stage();
            nouvelleStage.setTitle("Candidatures pour " + offre.getTitre().substring(0, 1).toUpperCase() + offre.getTitre().substring(1));
            nouvelleStage.setScene(new Scene(root, 800, 600));
            nouvelleStage.setMaximized(true);

            // Fermer l'ancienne fenêtre
            stageActuelle.close();

            nouvelleStage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    //Statistiques
    @FXML
    private void afficherStatistiques() {
        // Récupérer la fenêtre actuelle
        Stage currentStage = (Stage) offresTable.getScene().getWindow();

        // Charger dans un thread séparé pour éviter de bloquer l'UI
        new Thread(() -> {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/statistiquesOffres.fxml"));
                    Parent root = loader.load();

                    Stage newStage = new Stage();
                    newStage.setScene(new Scene(root));
                    newStage.setTitle("Statistiques des offres d'emploi 📊");
                    newStage.setMaximized(true); // Maximiser la nouvelle fenêtre

                    // Fermer l'ancienne fenêtre
                    currentStage.close();

                    // Afficher la nouvelle fenêtre
                    newStage.show();
                } catch (IOException e) {
                    showErrorAlert("Erreur de chargement : " + e.getMessage());
                }
            });
        }).start();
    }

    /* ********************AMELIORATIONS************************* */
    // Rafraichir la page pour voir les modifications
    public void refreshOffres() {
        try {
            offresList.clear();
            offresList.addAll(serviceOffreEmploi.afficher());
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