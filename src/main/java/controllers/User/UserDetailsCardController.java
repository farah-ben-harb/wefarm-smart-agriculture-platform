package controllers.User;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class UserDetailsCardController {
    @FXML
    private Label labelNom;
    @FXML
    private Label labelEmail;
    @FXML
    private Label labelAdresse;
    @FXML
    private Label labelNumTel;
    @FXML
    private Label labelStatus;
    @FXML
    private ImageView profileImage;

    private Stage dialogStage;

    public void setUtilisateur(Utilisateur utilisateur, Stage stage) {
        this.dialogStage = stage;
        labelNom.setText(utilisateur.getFirstName() + " " + utilisateur.getLastName());
        labelEmail.setText(utilisateur.getEmail());
        labelAdresse.setText(utilisateur.getAdresse());
        labelNumTel.setText(String.valueOf(utilisateur.getNumTel()));

        // Gestion des emojis et couleurs selon le statut
        String status = utilisateur.getStatus();
        String statusText = status;

        if (status != null) {
            switch (status.toLowerCase()) {
                case "actif":
                    statusText = "✅ " + status; // Emoji checkmark vert
                    labelStatus.setTextFill(Color.web("#2ecc71")); // Vert
                    break;
                case "désactivé":
                    statusText = "❌ " + status; // Emoji croix rouge
                    labelStatus.setTextFill(Color.web("#e74c3c")); // Rouge
                    break;
                case "en attente de validation":
                    statusText = "⏳ " + status; // Emoji sablier orange
                    labelStatus.setTextFill(Color.web("#f39c12")); // Orange
                    break;
                default:
                    statusText = "ℹ️ " + status; // Emoji info
                    labelStatus.setTextFill(Color.web("#7f8c8d")); // Gris
            }
        }

        labelStatus.setText(statusText);

        // Charger l'image de profil si disponible
        if (utilisateur.getImage() != null) {
            profileImage.setImage(new Image(utilisateur.getImage()));
        } else {
            profileImage.setImage(new Image("/images/default_profile.png")); // Image par défaut
        }

        // Créer un cercle de clip pour rendre l'image circulaire
        double radius = 60; // rayon du cercle
        Circle clip = new Circle(radius, radius, radius);
        profileImage.setClip(clip);
        profileImage.setFitWidth(radius * 2);
        profileImage.setFitHeight(radius * 2);
        profileImage.setPreserveRatio(false);
    }

    @FXML
    private void fermerPopup() {
        dialogStage.close();
    }
}