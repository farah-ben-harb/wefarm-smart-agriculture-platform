package controllers.OffreEmploi;

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
import services.ServiceOffreEmploi;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ModifierOffreEmploiController {
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField nombrePostesField;
    @FXML private Label dateDebutLabel;
    @FXML private Label dateFinLabel;
    @FXML private ComboBox<String> localisationComboBox;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private TextField competencesField;
    @FXML private TextField salaireField;
    //Pour les messages d'erreurs
    @FXML private Label titreError;
    @FXML private Label descriptionError;
    @FXML private Label nombrePostesError;
    @FXML private Label localisationError;
    @FXML private Label competencesError;
    @FXML private Label salaireError;

    private OffreEmploi offreAModifier;
    private ServiceOffreEmploi serviceOffreEmploi = new ServiceOffreEmploi();

    public void initialize() {
        // Remplissage du comboBox avec les gouvernorats
        List<String> gouvernorats = Arrays.asList(
                "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan",
                "Bizerte", "Béja", "Jendouba", "Le Kef", "Siliana", "Sousse",
                "Monastir", "Mahdia", "Sfax", "Kairouan", "Kasserine",
                "Sidi Bouzid", "Gabès", "Médenine", "Tataouine", "Gafsa",
                "Tozeur", "Kébili"
        );
        localisationComboBox.getItems().addAll(gouvernorats);
        statutComboBox.getItems().addAll("actif", "inactif");
        // Ajoutez des listeners pour la validation en temps réel
        titreField.textProperty().addListener((obs, oldVal, newVal) -> validateTitre());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateDescription());
        nombrePostesField.textProperty().addListener((obs, oldVal, newVal) -> validateNombrePostes());
        competencesField.textProperty().addListener((obs, oldVal, newVal) -> validateCompetences());
        salaireField.textProperty().addListener((obs, oldVal, newVal) -> validateSalaire());
    }
    //Récupération de l'offre d'emploi à modifier
    public void setOffreAModifier(OffreEmploi offre) {
        this.offreAModifier = offre;
        // Remplir les champs avec les données de l'offre
        titreField.setText(offre.getTitre().substring(0, 1).toUpperCase() + offre.getTitre().substring(1));
        descriptionField.setText(offre.getDescription());
        nombrePostesField.setText(String.valueOf(offre.getNombrePostes()));
        // Afficher les dates (format simple)
        dateDebutLabel.setText(offre.getDateDebut().toString());
        dateFinLabel.setText(offre.getDateFinEstimee().toString());

        localisationComboBox.setValue(offre.getLocalisation());
        statutComboBox.setValue(offre.getStatus());
        competencesField.setText(offre.getCompetencesRequises());
        salaireField.setText(String.valueOf(offre.getSalaire()));
    }
    //Enregistrement des modifications
    @FXML
    private void enregistrerModifications() {
        clearErrorMessages();

        if (!validateForm()) {
            showErrorAlert("Veuillez vérifier les données");
            return;
        }
        try {
            // Mettre à jour l'objet offre avec les nouvelles valeurs
            offreAModifier.setTitre(titreField.getText());
            offreAModifier.setDescription(descriptionField.getText());
            offreAModifier.setNombrePostes(Integer.parseInt(nombrePostesField.getText()));

            offreAModifier.setLocalisation(localisationComboBox.getValue());
            offreAModifier.setStatus(statutComboBox.getValue());
            offreAModifier.setCompetencesRequises(competencesField.getText());
            offreAModifier.setSalaire(Double.parseDouble(salaireField.getText()));

            // Appeler le service pour mettre à jour dans la base de données
            serviceOffreEmploi.modifier(offreAModifier);
            showSuccessAlert("Offre modifiée avec succès !");

            // Attendre 2 seconde avant de fermer la fenêtre
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
                delay.setOnFinished(e -> {
                    try {
                        String rawRole = SessionManager.getInstance().getRoles();
                        String role = rawRole.replaceAll("[\\[\\]\"]", "");

                        FXMLLoader loader;

                        if ("ROLE_AGRICULTEUR".equalsIgnoreCase(role)) {
                            loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffres.fxml"));
                        } else if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
                            loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffresAdmin.fxml"));
                        } else {
                            System.out.println("Rôle non reconnu !");
                            showErrorAlert("Rôle non reconnu !");
                            return;
                        }

                        Parent root = loader.load();

                        // Fermer l'ancienne fenêtre
                        Stage oldStage = (Stage) titreField.getScene().getWindow();
                        oldStage.close();

                        // Créer une nouvelle fenêtre maximisée
                        Stage newStage = new Stage();
                        newStage.setScene(new Scene(root));
                        newStage.setTitle("Liste des Offres");
                        newStage.setMaximized(true);
                        newStage.show();

                    } catch (IOException ex) {
                        ex.printStackTrace();
                        showErrorAlert("Erreur lors du retour à la liste des offres !");
                    }
                });
                delay.play();

        } catch (SQLException e) {
            showErrorAlert("Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
        }
    }
    //Méthode pour annuler
    @FXML
    private void annulerModifications() {
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
    /* ***************************** AMELIORATIONS******************************/

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
    //* *************************VALIDATIONS********************************** */
    private void clearErrorMessages() {
        titreError.setText("");
        descriptionError.setText("");
        nombrePostesError.setText("");
        localisationError.setText("");
        competencesError.setText("");
        salaireError.setText("");
    }

    // Méthodes de validation
    private void validateTitre() {
        String titre = titreField.getText();
        if (titre.isEmpty()) {
            titreError.setText("Le titre est obligatoire");
        } else if (titre.length() < 3) {
            titreError.setText("Minimum 3 caractères");
        } else if (titre.length() > 40) {
            titreError.setText("Maximum 40 caractères");
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
        } else if (description.length() < 40) {
            descriptionError.setText("Minimum 40 caractères");
        } else if (!description.matches("^(?!\\d+$)[A-Za-z0-9À-ÿ,.' ]+$")
                || description.split("\\s+").length < 3) {
            descriptionError.setText("3 mots minimum, pas que des chiffres");
        } else {
            descriptionError.setText("");
        }
    }

    private void validateNombrePostes() {
        try {
            int nombre = Integer.parseInt(nombrePostesField.getText());
            if (nombre <= 2) {
                nombrePostesError.setText("Le nombre de postes doit être supérieur à 2");
            } else {
                nombrePostesError.setText("");
            }
        } catch (NumberFormatException e) {
            nombrePostesError.setText("Nombre valide requis");
        }
    }

    private void validateCompetences() {
        String competences = competencesField.getText();
        if (competences.isEmpty()) {
            competencesError.setText("Compétences requises");
        } else if (!competences.contains(",")) {
            competencesError.setText("Au moins 2 compétences séparées par virgule");
        } else {
            competencesError.setText("");
        }
    }

    private void validateSalaire() {
        try {
            double salaire = Double.parseDouble(salaireField.getText());
            if (salaire <= 0) {
                salaireError.setText("Le salaire doit être supérieur à 0");
            } else {
                salaireError.setText("");
            }
        } catch (NumberFormatException e) {
            salaireError.setText("Nombre valide requis");
        }
    }

    private boolean validateForm() {
        validateTitre();
        validateDescription();
        validateNombrePostes();
        validateCompetences();
        validateSalaire();

        return titreError.getText().isEmpty() &&
                descriptionError.getText().isEmpty() &&
                nombrePostesError.getText().isEmpty() &&
                competencesError.getText().isEmpty() &&
                salaireError.getText().isEmpty() &&
                localisationComboBox.getValue() != null &&
                statutComboBox.getValue() != null;
    }

}