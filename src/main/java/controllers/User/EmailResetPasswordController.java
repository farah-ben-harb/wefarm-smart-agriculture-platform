package controllers.User;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.UtilisateurService;
import utils.Mail;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class EmailResetPasswordController {//pour valider email de reset

    @FXML
    private TextField emailTextField;
    @FXML
    private Button resetButton;
    @FXML
    private Button backButton;

    private UtilisateurService utilisateurService = new UtilisateurService(); // Instance du service
    // notif
// En haut de votre classe
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();

    @FXML
    private void handleResetPassword() {
        String email = emailTextField.getText().trim();

        if (email.isEmpty() || !email.contains("@")) {
            showErrorNotification("Veuillez entrer un e-mail valide");
        } else {
            // Vérifiez si l'email existe dans la base de données
            if (utilisateurService.getUtilisateurByEmail(email) == null) {
                showErrorNotification("Email non associé à un compte");
                return;
            }

            try {
                String otpCode = generateOTP();
                Mail mail = new Mail();
                mail.sendOTPEmail(email, otpCode);
                showSuccessNotification("Code OTP envoyé à " + email);

                // Délai avant redirection
                PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
                pause.setOnFinished(e -> {
                    OTPController.setGeneratedOTP(otpCode);
                    loadOTPPage(email, otpCode);
                });
                pause.play();
            } catch (MessagingException e) {
                showErrorNotification("Échec d'envoi de l'email");
                e.printStackTrace();
            }

        }
    }

    private void loadOTPPage(String email, String otpCode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/otp.fxml"));
            Parent root = loader.load();

            // Passer les données au contrôleur OTP
            OTPController otpController = loader.getController();
            otpController.setGeneratedOTP(otpCode);
            otpController.setUserEmail(email);

            Stage currentStage = (Stage) resetButton.getScene().getWindow();
            Scene otpScene = new Scene(root, currentStage.getWidth(), currentStage.getHeight());

            currentStage.setScene(otpScene);
            currentStage.setTitle("Vérification OTP");
        } catch (IOException e) {
            showErrorNotification("Erreur lors du chargement de la page OTP");
            e.printStackTrace();
        }
    }


    private String generateOTP() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);  // Génère un code OTP à 4 chiffres
        return String.valueOf(otp);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) throws IOException {
        // Récupérer la fenêtre actuelle (stage)
        Stage currentStage = (Stage) backButton.getScene().getWindow();

        // Récupérer la taille actuelle de la fenêtre
        double width = currentStage.getWidth();
        double height = currentStage.getHeight();

        // Charger la scène login
        Parent root = FXMLLoader.load(getClass().getResource("/views/user/login.fxml"));

        // Créer une nouvelle scène avec la même taille que la fenêtre actuelle
        Scene loginScene = new Scene(root, width, height);

        // Appliquer la nouvelle scène au stage actuel
        currentStage.setScene(loginScene);

        // Optionnel: animation de transition
        FadeTransition ft = new FadeTransition(Duration.millis(300), root);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }
    private HBox createNotificationBox(String message, String color, String icon) {
        HBox notification = new HBox(10);
        notification.setAlignment(Pos.CENTER_LEFT);
        notification.setStyle("-fx-background-color: " + color + "; "
                + "-fx-background-radius: 5; "
                + "-fx-padding: 10 15 10 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);");

        // Taille fixe
        notification.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        notification.setPrefSize(NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT);
        notification.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

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
            try {
                // Récupère la scène et la fenêtre
                Scene scene = resetButton.getScene();
                Stage stage = (Stage) scene.getWindow();

                // Crée un nouveau AnchorPane comme overlay
                AnchorPane overlay = new AnchorPane();
                overlay.setStyle("-fx-background-color: transparent;");
                overlay.setPickOnBounds(false);

                // Positionne la notification
                AnchorPane.setTopAnchor(notification, 20.0);
                AnchorPane.setRightAnchor(notification, 20.0);
                overlay.getChildren().add(notification);

                // Ajoute l'overlay à la racine
                ((StackPane) scene.getRoot()).getChildren().add(overlay);

                // Initial state
                notification.setOpacity(0);
                notification.setTranslateY(-NOTIFICATION_HEIGHT);

                // Crée les transitions correctement
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notification);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notification);
                slideIn.setFromY(-NOTIFICATION_HEIGHT);
                slideIn.setToY(0);

                // Combine les animations
                ParallelTransition enterTransition = new ParallelTransition(fadeIn, slideIn);
                enterTransition.play();

                // Configure la disparition
                PauseTransition delay = new PauseTransition(Duration.seconds(3));
                delay.setOnFinished(e -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(ev -> {
                        ((StackPane) scene.getRoot()).getChildren().remove(overlay);
                    });
                    fadeOut.play();
                });
                delay.play();

            } catch (Exception e) {
                System.err.println("Erreur d'affichage de notification: " + e.getMessage());
            }
        });
    }
    private void removeNotification(HBox notification) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            StackPane root = (StackPane) resetButton.getScene().getRoot();
            root.getChildren().remove(notification);
            activeNotifications.remove(notification);
        });
        fadeOut.play();
    }
    private void showSuccessNotification(String message) {
        HBox notification = createNotificationBox(message, "#4CAF50", "✓");
        showAnimatedNotification(notification);
    }

    private void showErrorNotification(String message) {
        HBox notification = createNotificationBox(message, "#F44336", "✗");
        showAnimatedNotification(notification);
    }

}
