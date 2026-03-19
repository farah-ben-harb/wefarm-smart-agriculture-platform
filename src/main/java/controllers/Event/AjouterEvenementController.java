package controllers.Event;

import entities.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.EvenementService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Pattern;

public class AjouterEvenementController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private ComboBox<String> typeVisiteComboBox;
    @FXML private TextField localisationField;
    @FXML private TextField prixField;
    @FXML private Text titreError;
    @FXML private Text descriptionError;
    @FXML private Text dateDebutError;
    @FXML private Text typeVisiteError;
    @FXML private Text localisationError;
    @FXML private Text prixError;

    private final EvenementService evenementService = new EvenementService();

    // Patterns de validation
    private static final Pattern LETTERS_ONLY = Pattern.compile("^[a-zA-ZÀ-ÿ\\s]+$");
    private static final Pattern GOOGLE_MAPS_LINK = Pattern.compile(
            "^https://maps\\.app\\.goo\\.gl/[a-zA-Z0-9]+$",
            Pattern.CASE_INSENSITIVE);

    @FXML
    private void initialize() {
        typeVisiteComboBox.getItems().addAll("Sur place", "Virtuel");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        try {
            Evenement newEvent = createEvenementFromForm();

            if (checkForDuplicateEvent(newEvent)) {
                if (!showConfirmation("Doublon détecté",
                        "Un événement similaire existe déjà. Voulez-vous quand même l'ajouter ?")) {
                    return;
                }
            }

            evenementService.ajouter(newEvent);
            showSuccessAlert("Événement ajouté avec succès !");
            clearForm();

            // Retour à la liste avec rafraîchissement
            retourALaListeEtRafraichir();

        } catch (SQLException e) {
            showErrorAlert(e.getMessage());
            e.printStackTrace();
        }
    }

    private void retourALaListeEtRafraichir() {
        try {
            // Récupération du rôle
            String rawRole = SessionManager.getInstance().getRoles();
            String role = rawRole.replaceAll("[\\[\\]\"]", "");

            FXMLLoader loader;
            String title;

            if ("ROLE_AGRICULTEUR".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/evenement/afficherEvenements.fxml"));
                title = "Liste des événements (Agriculteur)";
            } else if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/evenement/afficherEvenementsAdmin.fxml"));
                title = "Gestion des événements (Admin)";
            } else {
                showErrorAlert("Rôle non reconnu");
                return;
            }

            Parent root = loader.load();

            // Rafraîchissement de la liste
            if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
                AfficherEvenementsAdminController controller = loader.getController();
                controller.refreshTable();
            } else {
                AfficherEvenementsController controller = loader.getController();
                //controller.refreshTable();
            }

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle(title);
            newStage.setMaximized(true);
            newStage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) titreField.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors du retour à la liste: " + e.getMessage());
        }
    }

    private Evenement createEvenementFromForm() {
        Evenement event = new Evenement();
        event.setTitre(titreField.getText().trim());
        event.setDescription(descriptionField.getText().trim());
        event.setDateDebut(dateDebutPicker.getValue().atTime(LocalTime.now()));
        event.setTypeVisite(typeVisiteComboBox.getValue());
        event.setLocalisation(localisationField.getText().trim());
        event.setPrix(Double.parseDouble(prixField.getText()));
        return event;
    }

    private boolean checkForDuplicateEvent(Evenement event) throws SQLException {
        List<Evenement> duplicates = evenementService.getByCriteria(
                event.getTitre(),
                event.getDateDebut(),
                event.getTypeVisite()
        );
        return !duplicates.isEmpty();
    }

    private boolean validateInputs() {
        clearErrorMessages();
        boolean isValid = true;

        // Validation titre
        String titre = titreField.getText().trim();
        if (titre.isEmpty()) {
            titreError.setText("Champ obligatoire");
            isValid = false;
        } else if (titre.length() < 3) {
            titreError.setText("3 caractères minimum");
            isValid = false;
        } else if (!LETTERS_ONLY.matcher(titre).matches()) {
            titreError.setText("Lettres seulement");
            isValid = false;
        }

        // Validation description
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            descriptionError.setText("Champ obligatoire");
            isValid = false;
        } else if (description.split("\\s+").length < 3) {
            descriptionError.setText("3 mots minimum");
            isValid = false;
        }

        // Validation date
        if (dateDebutPicker.getValue() == null) {
            dateDebutError.setText("Champ obligatoire");
            isValid = false;
        } else if (!dateDebutPicker.getValue().isAfter(LocalDate.now())) {
            dateDebutError.setText("Doit être future");
            isValid = false;
        }

        // Validation type visite
        if (typeVisiteComboBox.getValue() == null) {
            typeVisiteError.setText("Sélection requis");
            isValid = false;
        }

        // Validation localisation
        // Validation localisation
        String localisation = localisationField.getText().trim();
        if (localisation.isEmpty()) {
            localisationError.setText("Champ obligatoire");
            isValid = false;
        } else if (!GOOGLE_MAPS_LINK.matcher(localisation).matches()) {
            localisationError.setText("Format: https://maps.app.goo.gl/xxx");
            isValid = false;
        }

        // Validation prix
        String prixText = prixField.getText().trim();
        if (prixText.isEmpty()) {
            prixError.setText("Champ obligatoire");
            isValid = false;
        } else {
            try {
                double prix = Double.parseDouble(prixText);
                if (prix <= 0) {
                    prixError.setText("Doit être > 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                prixError.setText("Nombre valide requis");
                isValid = false;
            }
        }

        return isValid;
    }

    //Méthode pour annuler l'ajout
    @FXML
    private void annulerAjout(ActionEvent event) {
        String rawRole = SessionManager.getInstance().getRoles();
        String role = rawRole.replaceAll("[\\[\\]\"]", "");

        try {
            FXMLLoader loader;

            if ("ROLE_AGRICULTEUR".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/evenement/afficherEvenements.fxml"));
            } else if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/evenement/afficherEvenementsAdmin.fxml"));
            } else {
                System.out.println("Rôle non reconnu !");
                return;
            }

            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.setTitle("Liste des événements");
            newStage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) titreField.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        titreField.clear();
        descriptionField.clear();
        dateDebutPicker.setValue(null);
        typeVisiteComboBox.getSelectionModel().clearSelection();
        localisationField.clear();
        prixField.clear();
        clearErrorMessages();
    }

    private void clearErrorMessages() {
        titreError.setText("");
        descriptionError.setText("");
        dateDebutError.setText("");
        typeVisiteError.setText("");
        localisationError.setText("");
        prixError.setText("");
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
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