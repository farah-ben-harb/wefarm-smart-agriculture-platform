package controllers.OffreEmploi;

import entities.OffreEmploi;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.ServiceCandidature;
import services.ServiceOffreEmploi;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import utils.SessionManager;

import java.util.function.Predicate;

public class ListOffreEmploiController {

    public ScrollPane mainScroll;
    @FXML
    private TilePane offreTilePane;
    @FXML private TextField searchField;
    private SessionManager session = SessionManager.getInstance();
    private final int userId = session.getId();

    private ServiceOffreEmploi serviceOffreEmploi = new ServiceOffreEmploi();
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();

    //Pour la recherche dynamique
    private ObservableList<OffreEmploi> offresList = FXCollections.observableArrayList();
    private FilteredList<OffreEmploi> filteredOffres;
    @FXML
    private Button ajouterBtn;

    public void initialize() {
        try {
            Platform.runLater(() -> {
                Stage stage = (Stage) mainScroll.getScene().getWindow();
                stage.setMaximized(true);
            });
            // Charger les offres dans la liste observable
            offresList.addAll(serviceOffreEmploi.afficher());

            // Initialiser la liste filtrée
            filteredOffres = new FilteredList<>(offresList, p -> true);

            // Configurer la recherche dynamique
            setupSearchFilter();

            // Afficher toutes les offres initialement
            afficherOffresFiltrees();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //Méthodes de recherche dynamiques
    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredOffres.setPredicate(createPredicate(newValue));
            afficherOffresFiltrees();
        });
    }

    private Predicate<OffreEmploi> createPredicate(String searchText) {
        return offre -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            // Normaliser le texte de recherche et le titre de l'offre
            String normalizedSearch = normalizeString(searchText);
            String normalizedTitle = normalizeString(offre.getTitre());

            return normalizedTitle.contains(normalizedSearch);
        };
    }

    private String normalizeString(String input) {
        if (input == null) return "";

        // Retirer les accents et convertir en minuscules
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    private void afficherOffresFiltrees() {
        offreTilePane.getChildren().clear();

        for (OffreEmploi offre : filteredOffres) {
            VBox card = createOfferCard(offre);
            offreTilePane.getChildren().add(card);
        }
    }
    //Méthode pour la création des cartes
    private VBox createOfferCard(OffreEmploi offre) {
        VBox card = new VBox();
        card.setStyle("-fx-border-color: #158d54; -fx-border-width: 2px; -fx-border-radius: 5px;");
        card.getStyleClass().add("offer-card");

        // Header (date publication + statut)
        HBox header = new HBox();
        header.getStyleClass().add("card-header");


        // Date de publication
        Label publishDate = new Label();
        String dateText = getRelativeDate(offre.getDatePublication());
        publishDate.setText("Publié: " + dateText);
        publishDate.getStyleClass().add("publish-date");

        // Cercle de statut
        Circle statusCircle = new Circle(5);
        statusCircle.getStyleClass().add("status-circle");
        if ("actif".equals(offre.getStatus())) {
            statusCircle.getStyleClass().add("active");
        }
        HBox.setMargin(statusCircle, new Insets(0, 0, 0, 70));
        // Espaceur flexible pour pousser le statut à droite
        Region spacer = new Region();
        HBox.setHgrow(publishDate, Priority.ALWAYS);
        header.getChildren().addAll(publishDate, statusCircle);

        // Titre
        Label title = new Label("🌱"+offre.getTitre().substring(0, 1).toUpperCase() + offre.getTitre().substring(1));
        title.getStyleClass().add("offer-title");

        // Dates
        VBox datesBox = new VBox(5);
        datesBox.getChildren().addAll(
                new Label("Date de début: " + formatDate(offre.getDateDebut())),
                new Label("Date de fin estimée: " + formatDate(offre.getDateFinEstimee()))
        );
        datesBox.getStyleClass().add("date-label");

        // Compétences (tags)
        Label competences = new Label("Compétences requises: ");
        FlowPane skillsPane = new FlowPane();
        skillsPane.setHgap(5);
        skillsPane.setVgap(5);
        String[] skills = offre.getCompetencesRequises().split(",");
        for (String skill : skills) {
            Label skillTag = new Label(skill.trim());
            skillTag.getStyleClass().add("skill-tag");
            skillsPane.getChildren().add(skillTag);
        }

        // Salaire
        Label salary = new Label("Salaire: " + offre.getSalaire() + " TND");
        salary.getStyleClass().add("salary");

        // Footer (boutons)
        HBox footer = new HBox(10);
        footer.getStyleClass().add("card-footer");

        Button detailsBtn = new Button("Détails");
        detailsBtn.getStyleClass().add("details-button");
        detailsBtn.setOnAction(e  -> {
            // Récupération de la fenêtre contenant ce bouton
            Stage currentStage = (Stage) detailsBtn.getScene().getWindow();

            // Appel de la méthode avec passage de la stage actuelle
            afficherDetailsOffre(offre, currentStage);
        });

        Button viewCandidatesBtn = new Button("👥"); // Ajout de l'icône
        viewCandidatesBtn.getStyleClass().add("view-candidates-button");
        viewCandidatesBtn.setOnAction(e -> afficherCandidatures(offre));

            Button editBtn = new Button("Modifier");
            editBtn.getStyleClass().add("edit-button");
            editBtn.setOnAction(e -> ouvrirFormulaireModification(offre));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.getStyleClass().add("delete-button");
            deleteBtn.setOnAction(e -> supprimerOffre(offre, card));
        if (offre.getUserId() == userId) {  //Afficher modifier et supprimer que pour les cartes de l'utilisateur connecté
            footer.getChildren().addAll(detailsBtn,viewCandidatesBtn, editBtn, deleteBtn);
        }
        else
        {
            footer.getChildren().addAll(detailsBtn);
        }

        // Assemblage de la carte
        card.getChildren().addAll(header, title, datesBox, competences,skillsPane, salary, footer);

        return card;
    }
    // Suppression d'une offre d'emploi
    private void supprimerOffre(OffreEmploi offre, VBox card) {
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
        Label content = new Label("Êtes-vous sûr de vouloir supprimer cette offre d'emploi ? Cette action est irréversible.");
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
                    serviceOffreEmploi.supprimer(offre.getId());
                    offreTilePane.getChildren().remove(card);
                    showSuccessAlert("Offre supprimée avec succès !");

                } catch (SQLException e) {
                    showErrorAlert("Erreur lors de la suppression : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    // Méthode pour ouvrir le formulaire d'ajout d'une offre
    public void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/ajouterOffre.fxml"));
            Parent root = loader.load();

            // Récupérer la scène actuelle à partir de n’importe quel composant de ta vue
            Stage currentStage = (Stage) ajouterBtn.getScene().getWindow();

            // Changer la scène de la fenêtre actuelle
            currentStage.setScene(new Scene(root, 600, 400));
            currentStage.setTitle("Ajouter une Offre");

            currentStage.setOnHidden(e -> refreshOffres());
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Méthode pour ouvrir le formulaire de modification d'une offre d'emploi
    private void ouvrirFormulaireModification(OffreEmploi offre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/modifierOffre.fxml"));
            Parent root = loader.load();

            // Injecter l'offre dans le contrôleur
            ModifierOffreEmploiController controller = loader.getController();
            controller.setOffreAModifier(offre);

            // Créer une nouvelle scène et fenêtre
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("Modifier l'offre");

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
    //Affichage des détails d'une offre d'emploi
    private void afficherDetailsOffre(OffreEmploi offre, Stage currentStage) {
        try {
            // Fermer la fenêtre actuelle
            currentStage.close();

            // Charger la fenêtre de détails
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/detailsOffre.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Détails de l'offre");
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            DetailsOffreEmploiController controller = loader.getController();
            controller.setOffre(offre);
            controller.setDialogStage(dialogStage);
            dialogStage.setMaximized(true);

            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Afficher les candidatures relatives à une offres
    private void afficherCandidatures(OffreEmploi offre) {
        try {
            // Récupérer les données des candidatures
            List<Map<String, Object>> candidaturesWithDetails = serviceCandidature.getCandidaturesWithDetailsByOffreId(offre.getId());

            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/candidaturesOffre.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            CandidaturesOffreController controller = loader.getController();
            controller.setOffre(offre);
            controller.setCandidaturesWithDetails(candidaturesWithDetails);

            // Créer et configurer la nouvelle fenêtre
            Stage newStage = new Stage();
            newStage.setTitle("Candidatures pour " + capitalizeFirstLetter(offre.getTitre()));
            newStage.setScene(new Scene(root));

            // Maximiser la nouvelle fenêtre
            newStage.setMaximized(true);

            // Fermer l'ancienne fenêtre
            Stage currentStage = (Stage) offreTilePane.getScene().getWindow();
            currentStage.close();

            // Afficher la nouvelle fenêtre
            newStage.show();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            // Gestion d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'afficher les candidatures");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // Méthode utilitaire pour capitaliser la première lettre
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    /* ********************AMELIORATIONS************************* */
    // Rafraichir la page pour voir les modifications
    public void refreshOffres() {
        try {
            offresList.clear();
            offresList.addAll(serviceOffreEmploi.afficher());
            afficherOffresFiltrees();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //Méthodes pour manipulations des dates
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }
    private String getRelativeDate(Date publicationDate) {
        Date now = new Date();
        long diffInMillis = now.getTime() - publicationDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar publicationDay = Calendar.getInstance();
        publicationDay.setTime(publicationDate);
        publicationDay.set(Calendar.HOUR_OF_DAY, 0);
        publicationDay.set(Calendar.MINUTE, 0);
        publicationDay.set(Calendar.SECOND, 0);
        publicationDay.set(Calendar.MILLISECOND, 0);

        if (publicationDay.equals(today)) {
            return "aujourd'hui";
        } else if (diffInDays == 1) {
            return "hier";
        } else if (diffInDays >= 2 && diffInDays <= 7) {
            return "il y a " + diffInDays + " jours";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH);
            return formatter.format(publicationDate);
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
