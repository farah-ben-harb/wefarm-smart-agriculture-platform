package controllers.OffreEmploi;

import entities.OffreEmploi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.ServiceOffreEmploi;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javafx.animation.PauseTransition;
import utils.SessionManager;


public class AjouterOffreEmploiController {

    @FXML
    private TextField titreField, descriptionField, nombrePostesField, competencesField, salaireField;

    @FXML
    private DatePicker dateDebutPicker, dateFinPicker;

    @FXML
    private ComboBox<String> localisationComboBox;

    // Labels d'erreur
    @FXML
    private Text titreError, descriptionError, nombrePostesError, competencesError, salaireError;

    @FXML
    private ListView<String> competencesListView;

    private ObservableList<String> competences = FXCollections.observableArrayList();
    @FXML
    private Text dateDebutError, dateFinError, localisationError;

    private ServiceOffreEmploi serviceOffreEmploi = new ServiceOffreEmploi();

    @FXML
    public void initialize() {
        // Activer le plein écran
        Platform.runLater(() -> {
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setFullScreen(true);
            // Optionnel : personnaliser le raccourci de sortie
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // Désactive Echap
        });
        competencesListView.setItems(competences);
        // Remplissage du comboBox avec les 24 gouvernorats de la Tunisie
        List<String> gouvernorats = Arrays.asList(
                "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan", "Bizerte", "Béja", "Jendouba", "Le Kef", "Siliana",
                "Sousse", "Monastir", "Mahdia", "Sfax", "Kairouan", "Kasserine", "Sidi Bouzid", "Gabès", "Médenine", "Tataouine",
                "Gafsa", "Tozeur", "Kébili"
        );
        localisationComboBox.getItems().addAll(gouvernorats);
        // Initialiser les messages d'erreur comme vides
        clearErrorMessages();
        // Ajout des listeners pour la validation dynamique
        titreField.textProperty().addListener((obs, oldVal, newVal) -> validateTitre());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateDescription());
        nombrePostesField.textProperty().addListener((obs, oldVal, newVal) -> validateNombrePostes());
        salaireField.textProperty().addListener((obs, oldVal, newVal) -> validateSalaire());
        localisationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateLocalisation());
        dateDebutPicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
        competencesField.textProperty().addListener((obs, oldVal, newVal) -> validateCompetenceField());
    }
    private void clearErrorMessages() {
        titreError.setText("");
        descriptionError.setText("");
        nombrePostesError.setText("");
        competencesError.setText("");
        salaireError.setText("");
        dateDebutError.setText("");
        dateFinError.setText("");
        localisationError.setText("");
    }

    //Ajout d'une offre d'emploi
    @FXML
    private void ajouterOffre(ActionEvent event) {
        // Valider tous les champs une dernière fois
        validateTitre();
        validateDescription();
        validateNombrePostes();
        validateSalaire();
        validateLocalisation();
        validateDates();
        validateCompetencesList();

        // Vérifier s'il y a des erreurs
        if (titreError.getText().isEmpty() && descriptionError.getText().isEmpty() &&
                nombrePostesError.getText().isEmpty() && salaireError.getText().isEmpty() &&
                localisationError.getText().isEmpty() && dateDebutError.getText().isEmpty() &&
                dateFinError.getText().isEmpty() && competencesError.getText().isEmpty()) {

            try {
                OffreEmploi offre = new OffreEmploi(
                        titreField.getText(),
                        descriptionField.getText(),
                        Integer.parseInt(nombrePostesField.getText()),
                        Date.valueOf(dateDebutPicker.getValue()),
                        Date.valueOf(dateFinPicker.getValue()),
                        localisationComboBox.getValue(),
                        String.join(", ", competences),
                        Double.parseDouble(salaireField.getText())
                );

                // Vérifier si l'offre existe déjà
                if (serviceOffreEmploi.offreExisteDeja(offre)) {
                    showConfirmationDialog(offre);
                } else {
                    ajouterOffreFinal(offre);
                }

            } catch (SQLException e) {
                showErrorAlert("Erreur base de données : " + e.getMessage());
            }
        }
    }

    private void ajouterOffreFinal(OffreEmploi offre) {
        try {
            serviceOffreEmploi.ajouter(offre);
            showSuccessAlert("Offre ajoutée avec succès !");

            // Fermer la fenêtre actuelle après un délai
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> {
                try {
                    // Charger la vue afficherOffres
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffres.fxml"));
                    Parent root = loader.load();

                    // Créer une nouvelle scène
                    Scene scene = new Scene(root);

                    // Récupérer la fenêtre actuelle
                    Stage currentStage = (Stage) titreField.getScene().getWindow();

                    // Changer la scène de la fenêtre actuelle
                    currentStage.setScene(scene);
                    currentStage.setMaximized(true);
                    Platform.runLater(() -> {
                        currentStage.setMaximized(true);
                        currentStage.show();
                    });

                } catch (IOException ex) {
                    ex.printStackTrace();
                    showErrorAlert("Erreur lors du chargement de la page des offres");
                }
            });
            delay.play();

        } catch (SQLException e) {
            showErrorAlert("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void showConfirmationDialog(OffreEmploi offre) {
        // Création de la boîte de dialogue
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(titreField.getScene().getWindow());

        // Personnalisation des boutons
        ButtonType buttonTypeOui = new ButtonType("Oui, ajouter", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNon = new ButtonType("Non, annuler", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

        // Configuration du style
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/modern-dialog.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");

        // Ajout d'une icône
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/error.png")));
        icon.setFitWidth(48);
        icon.setFitHeight(48);
        alert.setGraphic(icon);

        // Personnalisation du contenu
        alert.setTitle("Confirmation requise");
        alert.setHeaderText("Attention");
        alert.setContentText("Une offre d'emploi avec un titre similaire existe déjà.\n\n" +
                "Titre: " + offre.getTitre() + "\n" +
                "Voulez-vous vraiment ajouter cette offre ?");

        // Application des styles aux boutons
        Node ouiButton = dialogPane.lookupButton(buttonTypeOui);
        ouiButton.getStyleClass().add("oui-button");

        Node nonButton = dialogPane.lookupButton(buttonTypeNon);
        nonButton.getStyleClass().add("non-button");

        // Affichage et gestion de la réponse
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeOui) {
            ajouterOffreFinal(offre);
        } else {
            showNotification("Ajout annulé", "L'offre n'a pas été ajoutée.");
        }
    }
    private void showNotification(String title, String message) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/error.png")));
        icon.setFitWidth(32);
        icon.setFitHeight(32);

        Notifications.create()
                .title(title)
                .text(message)
                .graphic(icon)
                .hideAfter(Duration.seconds(3))
                .position(Pos.BOTTOM_RIGHT)
                .owner(titreField.getScene().getWindow())
                .darkStyle()
                .show();
    }
    //Méthode pour annuler l'ajout
    @FXML
    private void annulerAjout(ActionEvent event) {
        String rawRole = SessionManager.getInstance().getRoles();
        String role = rawRole.replaceAll("[\\[\\]\"]", "");

        try {
            FXMLLoader loader;

            if ("ROLE_AGRICULTEUR".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffres.fxml"));
            } else if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffresAdmin.fxml"));
            } else {
                System.out.println("Rôle non reconnu !");
                return;
            }

            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.setTitle("Liste des Offres");
            newStage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) titreField.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ************************AMELIORATIONS ****************************/
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

    @FXML
    private void ajouterCompetence(ActionEvent event) {
        String competence = competencesField.getText().trim();
        if (!competence.isEmpty()) {
            competences.add(competence);
            competencesField.clear();
            competencesError.setText(""); // Efface l'erreur si existante
        } else {
            competencesError.setText("Veuillez saisir une compétence valide");
        }
    }
    private void validateCompetencesList() {
        if (competences.isEmpty()) {
            showError(competencesError, "Au moins une compétence requise");
        } else {
            clearError(competencesError);
        }
    }
    //* *************************VALIDATIONS************* */
    private void validateTitre() {
        String titre = titreField.getText();
        if (titre.isEmpty()) {
            showError(titreError, "Le titre est obligatoire.");
        } else if (titre.length() < 3) {
            showError(titreError, "Minimum 3 caractères");
        } else if (titre.length() > 40) {
            showError(titreError, "Maximum 40 caractères");
        } else if (!Pattern.matches("^[A-Za-zÀ-ÿ' ]+$", titre)) {
            showError(titreError, "Lettres seulement");
        } else {
            clearError(titreError);
        }
    }

    private void validateDescription() {
        String description = descriptionField.getText();
        if (description.isEmpty()) {
            showError(descriptionError, "La description est obligatoire");
        } else if (description.length() < 40) {
            showError(descriptionError, "Minimum 40 caractères");
        } else if (!Pattern.matches("^(?!\\d+$)[A-Za-z0-9À-ÿ,.' ]+$", description)
                || description.split("\\s+").length < 3) {
            showError(descriptionError, "3 mots minimum");
        } else {
            clearError(descriptionError);
        }
    }

    private void validateNombrePostes() {
        try {
            int nombre = Integer.parseInt(nombrePostesField.getText());
            if (nombre <= 2) {
                showError(nombrePostesError, "Le nombre de poste doit être supérieur à 2");
            } else {
                clearError(nombrePostesError);
            }
        } catch (NumberFormatException e) {
            showError(nombrePostesError, "Nombre valide requis");
        }
    }

    private void validateSalaire() {
        try {
            double salaire = Double.parseDouble(salaireField.getText());
            if (salaire <= 0) {
                showError(salaireError, "Le salaire doit être supérieur à 0");
            } else {
                clearError(salaireError);
            }
        } catch (NumberFormatException e) {
            showError(salaireError, "Nombre valide requis");
        }
    }

    private void validateLocalisation() {
        if (localisationComboBox.getValue() == null) {
            showError(localisationError, "La localisation est requise");
        } else {
            clearError(localisationError);
        }
    }

    private void validateDates() {
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin = dateFinPicker.getValue();

        if (debut == null) {
            showError(dateDebutError, "La date de début est requise");
        } else if (debut.isBefore(LocalDate.now())) {
            showError(dateDebutError, "La date doit  être future");
        } else {
            clearError(dateDebutError);
        }

        if (fin == null) {
            showError(dateFinError, "La date de fin est requise");
        } else if (debut != null && fin.isBefore(debut)) {
            showError(dateFinError, "La date doit être supérieure à la date de début");
        } else {
            clearError(dateFinError);
        }
    }

    private void validateCompetenceField() {
        if (competencesField.getText().isEmpty()) {
            clearError(competencesError);
        }
    }

    // Méthodes utilitaires
    private void showError(Text errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().add("visible");
    }

    private void clearError(Text errorLabel) {
        errorLabel.setText("");
        errorLabel.getStyleClass().remove("visible");
    }

    }

