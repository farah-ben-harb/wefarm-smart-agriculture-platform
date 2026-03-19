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
import utils.Mail;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OTPController {//pour gere code otp

    @FXML
    private TextField otpField;
    @FXML
    private Button validateButton;
    @FXML private Button backButton;

    @FXML private TextField otpField1;
    @FXML private TextField otpField2;
    @FXML private TextField otpField3;
    @FXML private TextField otpField4;

    private static String generatedOTP;  // Stocker l'OTP généré
    //notif
    // En haut de votre classe
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();

    @FXML private Button resendButton;
    private String userEmail; // Pour stocker l'email de l'utilisateur
    private Mail mailService = new Mail();
    public void setUserEmail(String email) {
        this.userEmail = email;
    }
    @FXML
    private void handleResendCode() {
        try {
            String newOtp = generateNewOTP();
            generatedOTP = newOtp;

            Mail mail = new Mail();
            mail.sendOTPEmail(userEmail, newOtp);

            showSuccessNotification("Nouveau code envoyé à " + userEmail);

            // Correction de la timeline
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(60), event -> {
                        resendButton.setDisable(false);
                    })
            );
            resendButton.setDisable(true);
            timeline.play();

        } catch (MessagingException e) {
            showErrorNotification("Échec d'envoi du nouveau code");
            e.printStackTrace();
        }
    }
    private String generateNewOTP() {
        // Génère un nouveau code OTP à 4 chiffres
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

    public static void setGeneratedOTP(String otp) {
        generatedOTP = otp;
    }
    // Ajoutez cette méthode pour gérer la navigation entre les champs
    public void initialize() {
        // Limite chaque champ à 1 caractère
        otpField1.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 1) otpField1.setText(newVal.substring(0, 1));
            if (newVal.length() == 1) otpField2.requestFocus();
        });

        otpField2.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 1) otpField2.setText(newVal.substring(0, 1));
            if (newVal.length() == 1) otpField3.requestFocus();
        });

        otpField3.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 1) otpField3.setText(newVal.substring(0, 1));
            if (newVal.length() == 1) otpField4.requestFocus();
        });

        otpField4.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 1) otpField4.setText(newVal.substring(0, 1));
        });
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
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


    @FXML
    private void handleValidateOTP() {
        try {
            String enteredOTP = otpField1.getText() + otpField2.getText()
                    + otpField3.getText() + otpField4.getText();

            if (enteredOTP.length() != 4) {
                showErrorNotification("Veuillez entrer les 4 chiffres du code OTP");
                return;
            }

            if (enteredOTP.equals(generatedOTP)) {
                showSuccessNotification("Code OTP valide !");

                PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                delay.setOnFinished(e -> {
                    Platform.runLater(() -> {
                        try {
                            loadResetPasswordPage();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showErrorNotification("Erreur lors de la redirection");
                        }
                    });
                });
                delay.play();
            } else {
                showErrorNotification("Code OTP incorrect");
                clearOtpFields();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorNotification("Erreur lors de la validation");
        }
    }

    private void clearOtpFields() {
        otpField1.clear();
        otpField2.clear();
        otpField3.clear();
        otpField4.clear();
        otpField1.requestFocus();
    }
    private void loadResetPasswordPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/resetpassword.fxml"));
        Parent root = loader.load();

        // Récupérer la fenêtre actuelle
        Stage currentStage = (Stage) otpField1.getScene().getWindow();

        // Sauvegarder la taille et position actuelle
        double currentWidth = currentStage.getWidth();
        double currentHeight = currentStage.getHeight();
        double currentX = currentStage.getX();
        double currentY = currentStage.getY();

        // Créer la nouvelle scène avec la même taille
        Scene resetPasswordScene = new Scene(root, currentWidth, currentHeight);

        // Appliquer la nouvelle scène
        currentStage.setScene(resetPasswordScene);

        // Restaurer la position
        currentStage.setX(currentX);
        currentStage.setY(currentY);

        // Conserver les autres propriétés
        currentStage.setTitle("Réinitialisation du mot de passe");
        currentStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    //notif
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
                Scene scene = otpField1.getScene();
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
            StackPane root = (StackPane) otpField.getScene().getRoot();
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
}
