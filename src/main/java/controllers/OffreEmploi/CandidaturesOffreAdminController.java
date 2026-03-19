package controllers.OffreEmploi;

import entities.OffreEmploi;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.ServiceCandidature;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CandidaturesOffreAdminController implements Initializable {
    @FXML private Label titreOffreLabel;
    @FXML private TableView<Map<String, Object>> candidaturesTable;
    @FXML private TableColumn<Map<String, Object>, String> colIdCandidature;
    @FXML private TableColumn<Map<String, Object>, String> colCompetences;
    @FXML private TableColumn<Map<String, Object>, String> colOffreTitre;
    @FXML private TableColumn<Map<String, Object>, String> colNomPrenom;
    @FXML private TableColumn<Map<String, Object>, String> colDate;
    @FXML private TableColumn<Map<String, Object>, String> colEtat;
    @FXML private TableColumn<Map<String, Object>, Void> colActions;

    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private OffreEmploi offre;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        candidaturesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        candidaturesTable.getStylesheets().add(getClass().getResource("/css/stylesOffresEmploi.css").toExternalForm());

    }

    public void setOffre(OffreEmploi offre) {
        this.offre = offre;
        // Création d'un conteneur horizontal
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(8); // Espace entre icône et texte

        // Chargement de l'image PNG
        ImageView iconView = new ImageView(
                new Image(getClass().getResourceAsStream("/icons/candidature.png"))
        );
        iconView.setFitHeight(40);
        iconView.setFitWidth(40);

        // Texte du label
        Label texte = new Label("Candidatures pour l'offre : " +
                offre.getTitre().substring(0, 1).toUpperCase() +
                offre.getTitre().substring(1));

        // Assemblage
        container.getChildren().addAll(iconView, texte);

        // Remplacement du texte simple par notre composition
        titreOffreLabel.setGraphic(container);
        titreOffreLabel.setText("");
    }

    public void setCandidaturesWithDetails(List<Map<String, Object>> candidaturesWithDetails) {
        // Vider le tableau actuel
        candidaturesTable.getItems().clear();

        if (candidaturesWithDetails == null || candidaturesWithDetails.isEmpty()) {
            Label emptyLabel = new Label("Aucune candidature trouvée pour cette offre");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666; -fx-font-style: italic;");

            StackPane emptyPane = new StackPane(emptyLabel);
            emptyPane.setPadding(new Insets(20));

            candidaturesTable.setPlaceholder(emptyPane);
        } else {
            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(candidaturesWithDetails);

            // ID de la Candidature
            colIdCandidature.setCellValueFactory(param ->
                    new SimpleStringProperty(String.valueOf(param.getValue().get("candidature_id")))
            );

            // Compétences
            colCompetences.setCellValueFactory(param ->
                    new SimpleStringProperty((String) param.getValue().get("competences"))
            );

            // Titre de l'Offre
            colOffreTitre.setCellValueFactory(param -> {
                Map<String, Object> offreDetails = (Map<String, Object>) param.getValue().get("offre");
                return new SimpleStringProperty((String) offreDetails.get("titre"));
            });

            // Nom et Prénom de l'employé
            colNomPrenom.setCellValueFactory(param -> {
                Map<String, Object> userDetails = (Map<String, Object>) param.getValue().get("user");
                return new SimpleStringProperty(
                        userDetails.get("nom") + " " +
                                userDetails.get("prenom")
                );
            });

            // Date de Candidature
            colDate.setCellValueFactory(param ->
                    new SimpleStringProperty(formatDate((LocalDateTime) param.getValue().get("date_candidature")))
            );

            // Etat
            colEtat.setCellValueFactory(param ->
                    new SimpleStringProperty((String) param.getValue().get("etat"))
            );

            // Actions (Boutons Supprimer)
            colActions.setCellFactory(param -> new TableCell<Map<String, Object>, Void>() {
                private final Button supprimerBtn = new Button("🗑");
                private final HBox container = new HBox(supprimerBtn);

                {
                    // Style du bouton
                    supprimerBtn.setStyle("-fx-font-size: 14px;-fx-background-color: #e74a3b; -fx-text-fill: white;-fx-border-color:#e74a3b; -fx-border-radius: 4px;");
                    container.setAlignment(Pos.CENTER);
                    container.setSpacing(10);

                    // Action du bouton
                    supprimerBtn.setOnAction(e -> {
                        Map<String, Object> data = getTableRow().getItem();
                        int idCandidature = (int) data.get("candidature_id");
                        supprimerCandidature(idCandidature);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Map<String, Object> data = getTableRow().getItem();
                        String etat = (String) data.get("etat");
                        supprimerBtn.setDisable("acceptée".equalsIgnoreCase(etat));
                        setGraphic(container);
                    }
                }
            });

            candidaturesTable.setItems(data);
        }
    }


    // Méthode pour la suppression d'une candidature
    private void supprimerCandidature(int idCandidature) {
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
                    if (serviceCandidature.supprimerCandidature(idCandidature)) {
                        showSuccessAlert("Candidature supprimée avec succès !");
                        refresh();
                    } else {
                        showErrorAlert("Échec de la suppression");
                    }

                } catch (SQLException e) {
                    showErrorAlert("Erreur lors de la suppression : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    // Méthode pour rafraîchir le tableau
    private void refresh() {
        try {
            List<Map<String, Object>> nouvellesCandidatures = serviceCandidature.getCandidaturesWithDetailsByOffreId(offre.getId());
            setCandidaturesWithDetails(nouvellesCandidatures);
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors du rafraîchissement: " + e.getMessage());
        }
    }
    /* ********************AMELIORATIONS****************************** */
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
    //Méthode pour format de date
    private String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }

}

