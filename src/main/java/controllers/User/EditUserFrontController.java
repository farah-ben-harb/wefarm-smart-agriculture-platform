package controllers.User;

import entities.Utilisateur;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.UtilisateurService;
import utils.SessionManager;
import utils.CloudinaryService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EditUserFrontController {

    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField adresseField;
    @FXML private TextField numTelField;
    @FXML private ImageView profileImage;

    @FXML private Label emailError;
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label adresseError;
    @FXML private Label numTelError;

    private UtilisateurService utilisateurService = new UtilisateurService();
    CloudinaryService cloudinaryService = new CloudinaryService();

    // Configuration des notifications
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            SessionManager session = SessionManager.getInstance();
            emailField.setText(session.getEmail());
            firstNameField.setText(session.getFirstName());
            lastNameField.setText(session.getLastName());
            adresseField.setText(session.getAdresse());
            numTelField.setText(String.valueOf(session.getNumTel()));
            loadImage(session.getImage());

            emailError.setText("");
            firstNameError.setText("");
            lastNameError.setText("");
            adresseError.setText("");
            numTelError.setText("");

        } catch (IllegalStateException e) {
            System.out.println("Aucune session utilisateur active !");
        }
    }

    private void loadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            profileImage.setImage(null);
            return;
        }

        Image image = new Image(imageUrl);
        profileImage.setImage(image);
        profileImage.setFitHeight(150);
        profileImage.setFitWidth(150);
    }

    @FXML
    private void handleImageClick(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(profileImage.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // 1. Charger l'image dans l'ImageView
                Image image = new Image(selectedFile.toURI().toString());
                profileImage.setImage(image);
                profileImage.setFitHeight(150);
                profileImage.setFitWidth(150);

                // 2. Upload vers Cloudinary
                SessionManager session = SessionManager.getInstance();
                String imageUrl = cloudinaryService.uploadProfileImage(selectedFile, String.valueOf(session.getId()));

                // 3. Mettre à jour la session ET la base de données
                session.setImage(imageUrl);

                Utilisateur utilisateur = utilisateurService.getUtilisateurById(session.getId());
                utilisateur.setImage(imageUrl);
                utilisateurService.updateUserImage(session.getId(), imageUrl);
                showSuccessNotification("Photo de profil mise à jour avec succès");
            } catch (Exception e) {
                showErrorNotification("Erreur lors du chargement de l'image");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return;
        }

        try {
            SessionManager session = SessionManager.getInstance();

            Utilisateur utilisateur = new Utilisateur(
                    session.getId(),
                    emailField.getText(),
                    session.getRoles(),
                    adresseField.getText(),
                    session.getPassword(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    new java.sql.Date(session.getDateNaissance().getTime()),
                    session.getImage(),
                    session.getDateCreation(),
                    session.getStatus(),
                    session.isVerified(),
                    session.getConfirmationToken(),
                    session.getReactivationDate(),
                    Integer.parseInt(numTelField.getText())
            );

            utilisateurService.modifierUtilisateur(utilisateur);

            SessionManager.startSession(
                    utilisateur.getId(),
                    utilisateur.getEmail(),
                    utilisateur.getRoles(),
                    utilisateur.getAdresse(),
                    utilisateur.getPassword(),
                    utilisateur.getFirstName(),
                    utilisateur.getLastName(),
                    utilisateur.getDateNaissance(),
                    utilisateur.getImage(),
                    utilisateur.getStatus(),
                    utilisateur.isVerified(),
                    utilisateur.getConfirmationToken(),
                    utilisateur.getReactivationDate(),
                    utilisateur.getNumTel()
            );

            showSuccessNotification("Profil mis à jour avec succès");

            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(e -> {
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.close();
            });
            delay.play();

        } catch (Exception e) {
            showErrorNotification("Erreur lors de la mise à jour du profil");
            e.printStackTrace();
        }
    }

    private boolean isInputValid() {
        boolean isValid = true;
        String adresseRegex = "^[0-9]{1,4}\\s+[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+,\\s*[0-9]{4,5}$";

        // Réinitialiser les messages d'erreur
        emailError.setText("");
        firstNameError.setText("");
        lastNameError.setText("");
        adresseError.setText("");
        numTelError.setText("");

        // Validation email
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            emailError.setText("Email requis");
            isValid = false;
        } else {
            String email = emailField.getText().trim().toLowerCase();
            if (!email.matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
                emailError.setText("Format email invalide:exemple@gmail.com");
                isValid = false;
            } else if (email.length() > 50) {
                emailError.setText("Maximum 50 caractères");
                isValid = false;
            }
        }

        // Validation prénom
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            firstNameError.setText("Prénom requis");
            isValid = false;
        } else {
            String firstName = firstNameField.getText().trim();
            if (!firstName.matches("^[a-zA-ZÀ-ÿ\\s]+$")) {
                firstNameError.setText("Lettres et espaces seulement");
                isValid = false;
            } else if (firstName.length() < 2) {
                firstNameError.setText("Minimum 2 caractères");
                isValid = false;
            } else if (firstName.length() > 30) {
                firstNameError.setText("Maximum 30 caractères");
                isValid = false;
            } else if (firstName.matches(".*\\s{2,}.*")) {
                firstNameError.setText("Un seul espace autorisé");
                isValid = false;
            }
        }

        // Validation nom
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            lastNameError.setText("Nom requis");
            isValid = false;
        } else {
            String lastName = lastNameField.getText().trim();
            if (!lastName.matches("^[a-zA-ZÀ-ÿ\\s]+$")) {
                lastNameError.setText("Lettres et espaces seulement");
                isValid = false;
            } else if (lastName.length() < 2) {
                lastNameError.setText("Minimum 2 caractères");
                isValid = false;
            } else if (lastName.length() > 30) {
                lastNameError.setText("Maximum 30 caractères");
                isValid = false;
            } else if (lastName.matches(".*\\s{2,}.*")) {
                lastNameError.setText("Un seul espace autorisé");
                isValid = false;
            }
        }

        // Validation adresse
        if (adresseField.getText().isEmpty()) {
            adresseError.setText("Adresse manquante");
            isValid = false;
        } else if (!Pattern.matches(adresseRegex, adresseField.getText())) {
            adresseError.setText("Format: Numéro Rue, Code Postal");
            isValid = false;
        }

        if (numTelField.getText() == null || numTelField.getText().trim().isEmpty()) {
            numTelError.setText("Téléphone requis");
            isValid = false;
        } else {
            String phone = numTelField.getText().trim();

            // Vérifie si c'est bien 8 chiffres
            if (!phone.matches("^\\d{8}$")) {
                numTelError.setText("Le numéro doit contenir exactement 8 chiffres");
                isValid = false;
            }
            // Vérifie s’il commence par 2, 5 ou 9
            else if (!phone.startsWith("2") && !phone.startsWith("5") && !phone.startsWith("9")) {
                numTelError.setText("Le numéro doit commencer par 2, 5 ou 9");
                isValid = false;
            } else {
                numTelError.setText(""); // Tout est OK
            }


    }

        return isValid;
    }


    //  notifications restent
    private HBox createNotificationBox(String message, String color, String icon) {
        HBox notification = new HBox(10);
        notification.setAlignment(Pos.CENTER_LEFT);
        notification.setPrefSize(NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT);
        notification.setStyle("-fx-background-color: " + color + "; "
                + "-fx-background-radius: 5; "
                + "-fx-padding: 10 15 10 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(NOTIFICATION_WIDTH - 50);

        notification.getChildren().addAll(iconLabel, messageLabel);
        return notification;
    }

    private void showAnimatedNotification(HBox notification) {
        Platform.runLater(() -> {
            AnchorPane root = (AnchorPane) emailField.getScene().getRoot();
            notification.setLayoutX(root.getWidth());
            notification.setLayoutY(NOTIFICATION_SPACING);
            notification.setOpacity(0);

            root.getChildren().add(notification);
            activeNotifications.add(notification);

            ParallelTransition enterTransition = new ParallelTransition();
            TranslateTransition slideIn = new TranslateTransition(NOTIFICATION_ANIMATION_DURATION, notification);
            slideIn.setFromX(NOTIFICATION_WIDTH);
            slideIn.setToX(-NOTIFICATION_WIDTH - 10);
            slideIn.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeIn = new FadeTransition(NOTIFICATION_ANIMATION_DURATION, notification);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            enterTransition.getChildren().addAll(slideIn, fadeIn);
            enterTransition.play();

            PauseTransition displayDelay = new PauseTransition(Duration.seconds(3));
            displayDelay.setOnFinished(e -> removeNotificationWithAnimation(notification));
            displayDelay.play();
        });
    }

    private void removeNotificationWithAnimation(HBox notification) {
        ParallelTransition exitTransition = new ParallelTransition();
        TranslateTransition slideOut = new TranslateTransition(NOTIFICATION_ANIMATION_DURATION, notification);
        slideOut.setToX(NOTIFICATION_WIDTH);
        slideOut.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(NOTIFICATION_ANIMATION_DURATION, notification);
        fadeOut.setToValue(0);

        exitTransition.getChildren().addAll(slideOut, fadeOut);
        exitTransition.setOnFinished(e -> {
            AnchorPane root = (AnchorPane) emailField.getScene().getRoot();
            root.getChildren().remove(notification);
            activeNotifications.remove(notification);
        });
        exitTransition.play();
    }

    private void showSuccessNotification(String message) {
        HBox notification = createNotificationBox(message, "#4CAF50", "✓");
        showAnimatedNotification(notification);
    }

    private void showErrorNotification(String message) {
        HBox notification = createNotificationBox(message, "#F44336", "✗");
        showAnimatedNotification(notification);
    }

    @FXML
    private void handleCancel() {
        showSuccessNotification("Modifications annulées");
        PauseTransition delay = new PauseTransition(Duration.millis(500));
        delay.setOnFinished(e -> {
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.close();
        });
        delay.play();
    }
}