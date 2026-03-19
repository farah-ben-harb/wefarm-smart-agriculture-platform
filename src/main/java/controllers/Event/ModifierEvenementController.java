package controllers.Event;

import entities.Evenement;
import entities.OffreEmploi;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.EvenementService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class ModifierEvenementController {
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private ComboBox<String> typeVisiteComboBox;
    @FXML private TextField localisationField;
    @FXML private TextField prixField;

    // Messages d'erreur
    @FXML private Label titreError;
    @FXML private Label descriptionError;
    @FXML private Label dateDebutError;
    @FXML private Label typeVisiteError;
    @FXML private Label localisationError;
    @FXML private Label prixError;

    private Evenement eventAModifier;
    private EvenementService serviceEvent = new EvenementService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void initialize() {
        typeVisiteComboBox.getItems().addAll("Sur place", "Virtuel");

        // Validation en temps réel
        titreField.textProperty().addListener((obs, oldVal, newVal) -> validateTitre());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateDescription());
        prixField.textProperty().addListener((obs, oldVal, newVal) -> validatePrix());
    }



    @FXML
    private void enregistrerModifications() {
        clearErrorMessages();

        if (!validateForm()) {
            showErrorAlert("Veuillez vérifier les données");
            return;
        }

        try {
            // Mise à jour de l'événement
            eventAModifier.setTitre(titreField.getText());
            eventAModifier.setDescription(descriptionField.getText());
            eventAModifier.setDateDebut(dateDebutPicker.getValue().atTime(12, 0)); // Heure par défaut
            eventAModifier.setTypeVisite(typeVisiteComboBox.getValue());
            eventAModifier.setLocalisation(localisationField.getText());
            eventAModifier.setPrix(Double.parseDouble(prixField.getText()));

            serviceEvent.modifier(eventAModifier);
            showSuccessAlert("Événement modifié avec succès !");

            // Retour à la liste après 2 secondes
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> retourALaListe());
            delay.play();

        } catch (SQLException e) {
            showErrorAlert("Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void annulerModifications() {
        retourALaListe();
    }

    private void retourALaListe() {
        // Récupération du rôle depuis SessionManager
        String rawRole = SessionManager.getInstance().getRoles();
        String role = rawRole.replaceAll("[\\[\\]\"]", "");

        try {
            FXMLLoader loader;
            String title;

            if ("ROLE_AGRICULTEUR".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/evenement/afficherEvenements.fxml"));
                title = "Liste des événements (Agriculteur)";
            } else if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/evenement/afficherEvenementsAdmin.fxml"));
                title = "Gestion des événements (Admin)";
            } else {
                System.out.println("Rôle non reconnu: " + role);
                showErrorAlert("Vous n'avez pas les droits nécessaires");
                return;
            }

            // Chargement de la vue
            Parent root = loader.load();

            // Configuration de la nouvelle fenêtre
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle(title);
            newStage.setMaximized(true);
            newStage.show();

            // Fermeture de la fenêtre actuelle
            Stage currentStage = (Stage) titreField.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors du chargement de la vue: " + e.getMessage());
        }
    }

    /* ******************** VALIDATIONS ********************/
    private void clearErrorMessages() {
        titreError.setText("");
        descriptionError.setText("");
        dateDebutError.setText("");
        typeVisiteError.setText("");
        localisationError.setText("");
        prixError.setText("");
    }

    private void validateTitre() {
        String titre = titreField.getText();
        if (titre.isEmpty()) {
            titreError.setText("Le titre est obligatoire");
        } else if (titre.length() < 3) {
            titreError.setText("Minimum 3 caractères");
        } else if (!titre.matches("^[A-Za-zÀ-ÿ' ]+$")) {
            titreError.setText("Caractères alphabétiques seulement");
        } else {
            titreError.setText("");
        }
    }

    private void validateDescription() {
        String description = descriptionField.getText();
        if (description.isEmpty()) {
            descriptionError.setText("La description est obligatoire");
        } else if (description.split("\\s+").length < 3) {
            descriptionError.setText("Minimum 3 mots");
        } else {
            descriptionError.setText("");
        }
    }

    private void validatePrix() {
        try {
            double prix = Double.parseDouble(prixField.getText());
            if (prix <= 0) {
                prixError.setText("Le prix doit être positif");
            } else {
                prixError.setText("");
            }
        } catch (NumberFormatException e) {
            prixError.setText("Nombre valide requis");
        }
    }

    private boolean validateForm() {
        validateTitre();
        validateDescription();
        validatePrix();

        if (dateDebutPicker.getValue() == null) {
            dateDebutError.setText("Date obligatoire");
        }
        if (typeVisiteComboBox.getValue() == null) {
            typeVisiteError.setText("Type obligatoire");
        }
        if (localisationField.getText().isEmpty()) {
            localisationError.setText("Localisation obligatoire");
        }

        return titreError.getText().isEmpty() &&
                descriptionError.getText().isEmpty() &&
                prixError.getText().isEmpty() &&
                dateDebutPicker.getValue() != null &&
                typeVisiteComboBox.getValue() != null &&
                !localisationField.getText().isEmpty();
    }

    /* ******************** NOTIFICATIONS ********************/
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
    public void setEventAModifier(Evenement evenement) {
        this.eventAModifier = evenement;

        // Pré-remplir les champs avec les données de l'événement
        titreField.setText(evenement.getTitre());
        descriptionField.setText(evenement.getDescription());
        dateDebutPicker.setValue(evenement.getDateDebut().toLocalDate());
        typeVisiteComboBox.setValue(evenement.getTypeVisite());
        localisationField.setText(evenement.getLocalisation());
        prixField.setText(String.valueOf(evenement.getPrix()));
    }
}