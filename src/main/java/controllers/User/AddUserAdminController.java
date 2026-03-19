package controllers.User;



import entities.Utilisateur;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;
import services.UtilisateurService;
import utils.CloudinaryService;

import java.io.File;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AddUserAdminController {

    @FXML
    private ImageView profileImage;
    @FXML
    private Button uploadButton, backButton, enregistrerButton;
    @FXML
    private TextField nomField, prenomField, numTelField, adresseField, emailField;
    @FXML
    private PasswordField mdpField;
    @FXML
    private DatePicker dateNaissanceField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Label nomError, prenomError, numTelError, adresseError,
            dateNaissanceError, emailError, mdpError, roleError;

    //image
    private File selectedImageFile;
    private String imagePath = "/avatar3.png";

    //notification
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        // Configuration de l'image
        Circle clip = new Circle(45, 45, 45);
        profileImage.setClip(clip);

        Image defaultImage = new Image(getClass().getResource(imagePath).toExternalForm());
        profileImage.setImage(defaultImage);

        // Validation de la date de naissance (doit être dans le passé)
        dateNaissanceField.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isAfter(LocalDate.now()));
            }
        });
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleRegister() {
        if (validerChamps()) {
            try {
                // 1. Upload de l'image si elle existe
                String imageUrl;
                CloudinaryService cloudinaryService = new CloudinaryService();
                String roleValue = "[\"" + roleComboBox.getValue() + "\"]";

                if (selectedImageFile != null) {
                    imageUrl = cloudinaryService.uploadProfileImage(selectedImageFile, "temp_" + System.currentTimeMillis());
                } else {
                    imageUrl = cloudinaryService.uploadProfileImage(
                            new File(getClass().getResource("/avatar3.png").toURI()),
                            "default_avatar"
                    );
                }

                // 2. Création de l'utilisateur
                Utilisateur user = new Utilisateur(
                        emailField.getText().trim(),
                        roleValue,
                        adresseField.getText().trim(),
                        BCrypt.hashpw(mdpField.getText(), BCrypt.gensalt()),
                        nomField.getText().trim(),
                        prenomField.getText().trim(),
                        Date.valueOf(dateNaissanceField.getValue()),
                        imageUrl,
                        LocalDateTime.now(),
                        "actif",
                        true,
                        Integer.parseInt(numTelField.getText().trim())
                );
                // Token factice pour admin (format spécial)
                user.setConfirmationToken("ADMIN_CREATED_" + System.currentTimeMillis());

                utilisateurService.ajouterUtilisateur(user);

                // Si nouvelle image, mise à jour avec l'ID réel
                if (selectedImageFile != null) {
                    String newUrl = cloudinaryService.updateProfileImage(
                            selectedImageFile,
                            imageUrl,
                            String.valueOf(user.getId())
                    );
                    user.setImage(newUrl);
                    utilisateurService.updateUserImage(user.getId(), newUrl);
                }

                showSuccessNotification("Utilisateur créé avec succès !");


                Stage stage = (Stage) enregistrerButton.getScene().getWindow();
                stage.close();

            } catch (Exception e) {
                showErrorNotification("Échec de la création: " + e.getMessage());

                e.printStackTrace();
            }
        }
    }

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            profileImage.setImage(new Image(selectedImageFile.toURI().toString()));
            showSuccessNotification("Image sélectionnée avec succès");

        }
    }

    private boolean validerChamps() {
        boolean isValid = true;
        String adresseRegex = "^[0-9]{1,4}\\s+[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+,\\s*[0-9]{4,5}$";


        // Validation du nom
        if (!Pattern.matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s-]+$", nomField.getText())) {
            nomError.setText("Le nom ne doit contenir que des lettres.");
            isValid = false;
        } else {
            nomError.setText("");
        }

        // Validation du prénom
        if (!Pattern.matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s-]+$", prenomField.getText())) {
            prenomError.setText("Le prénom ne doit contenir que des lettres.");
            isValid = false;
        } else {
            prenomError.setText("");
        }
        if (adresseField.getText().isEmpty()) {
            adresseError.setText("Adresse manquante.");
            isValid = false;
        } else if (!Pattern.matches(adresseRegex, adresseField.getText())) {
            adresseError.setText("Format: Numéro Rue, Code Postal");
            isValid = false;
        } else {
            adresseError.setText("");
        }

        // Validation du numéro de téléphone
        String numTel = numTelField.getText();
        if (numTel.isEmpty()) {
            numTelError.setText("Numéro manquant.");
            isValid = false;
        } else if (!Pattern.matches("^\\d{8}$", numTel)) {
            numTelError.setText("Le numéro doit contenir exactement 8 chiffres");
            isValid = false;
        }
        else if (!numTel.startsWith("2") && !numTel.startsWith("5") && !numTel.startsWith("9")) {
            numTelError.setText("Le numéro doit commencer par 2, 5 ou 9");
            isValid = false;
        }
        else {
            numTelError.setText("");
        }

        // Validation de l'email
        String email = emailField.getText();
        if (email.isEmpty()) {
            emailError.setText("Email manquant.");
            isValid = false;
        } else if (!Pattern.matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$", email)) {
            emailError.setText("Format email (ex : exemple@gmail.com).");
            isValid = false;
        } else {
            emailError.setText("");
        }

        // Validation de la date de naissance
        LocalDate dateNaissance = dateNaissanceField.getValue();
        if (dateNaissance == null) {
            dateNaissanceError.setText("Date de naissance manquante.");
            isValid = false;
        } else if (Period.between(dateNaissance, LocalDate.now()).getYears() < 18) {
            dateNaissanceError.setText("Vous devez avoir au moins 18 ans.");
            isValid = false;
        } else {
            dateNaissanceError.setText("");
        }

        // Validation du mot de passe
        String mdp = mdpField.getText();
        if (mdp.isEmpty()) {
            mdpError.setText("Mot de passe requis.");
            isValid = false;
        } else if (!Pattern.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$", mdp)) {
            mdpError.setText("Mot de passe au moisn 8 lettres chiffres et *@!?+-");
            isValid = false;
        } else {
            mdpError.setText("");
        }

        // Validation du rôle
        if (roleComboBox.getValue() == null) {
            roleError.setText("Veuillez sélectionner un rôle.");
            isValid = false;
        } else {
            roleError.setText("");
        }

        return isValid;
    }

    //notification
    private HBox createNotificationBox(String message, String color, String icon) {
        HBox notification = new HBox(10);
        notification.setAlignment(Pos.CENTER_LEFT);
        notification.setPrefSize(NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT);
        notification.setStyle("-fx-background-color: " + color + "; "
                + "-fx-background-radius: 5; "
                + "-fx-padding: 10 15 10 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);");

        // Icône
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(NOTIFICATION_WIDTH - 50);

        notification.getChildren().addAll(iconLabel, messageLabel);
        return notification;
    }

    private void showAnimatedNotification(HBox notification) {
        Platform.runLater(() -> {
            AnchorPane root = (AnchorPane) enregistrerButton.getScene().getRoot();

            // Position initiale (hors écran à droite)
            notification.setLayoutX(root.getWidth());
            notification.setLayoutY(NOTIFICATION_SPACING);
            notification.setOpacity(0);

            root.getChildren().add(notification);
            activeNotifications.add(notification);

            // Animation d'entrée combinée
            ParallelTransition enterTransition = new ParallelTransition();

            // Translation depuis la droite
            TranslateTransition slideIn = new TranslateTransition(NOTIFICATION_ANIMATION_DURATION, notification);
            slideIn.setFromX(NOTIFICATION_WIDTH);
            slideIn.setToX(-NOTIFICATION_WIDTH - 10);
            slideIn.setInterpolator(Interpolator.EASE_OUT);

            // Fondu entrant
            FadeTransition fadeIn = new FadeTransition(NOTIFICATION_ANIMATION_DURATION, notification);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            enterTransition.getChildren().addAll(slideIn, fadeIn);
            enterTransition.play();

            // Délai avant disparition
            PauseTransition displayDelay = new PauseTransition(Duration.seconds(3));
            displayDelay.setOnFinished(e -> removeNotificationWithAnimation(notification));
            displayDelay.play();
        });
    }

    private void removeNotificationWithAnimation(HBox notification) {
        ParallelTransition exitTransition = new ParallelTransition();

        // Translation vers la droite
        TranslateTransition slideOut = new TranslateTransition(NOTIFICATION_ANIMATION_DURATION, notification);
        slideOut.setToX(NOTIFICATION_WIDTH);
        slideOut.setInterpolator(Interpolator.EASE_IN);

        // Fondu sortant
        FadeTransition fadeOut = new FadeTransition(NOTIFICATION_ANIMATION_DURATION, notification);
        fadeOut.setToValue(0);

        exitTransition.getChildren().addAll(slideOut, fadeOut);
        exitTransition.setOnFinished(e -> {
            AnchorPane root = (AnchorPane) enregistrerButton.getScene().getRoot();
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}