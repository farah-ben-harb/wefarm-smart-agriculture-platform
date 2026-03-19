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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
public class ResetPasswordController {

    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button resetButton;

    @FXML private Rectangle strengthBar1;
    @FXML private Rectangle strengthBar2;
    @FXML private Rectangle strengthBar3;
    @FXML private Label strengthLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label confirmErrorLabel;
    @FXML private Label infoLabel;
    //notif
    // En haut de votre classe
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();


    @FXML
    private Label passwordStrengthLabel;

    @FXML
    private void initialize() {
        // Écouteur pour la force du mot de passe
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
        });

        // Masquer les messages d'erreur initialement
        passwordErrorLabel.setVisible(false);
        confirmErrorLabel.setVisible(false);
    }
    private void updatePasswordStrength(String password) {
        // Réinitialiser la barre
        strengthBar1.setFill(Color.LIGHTGRAY);
        strengthBar2.setFill(Color.LIGHTGRAY);
        strengthBar3.setFill(Color.LIGHTGRAY);

        if (password == null || password.isEmpty()) {
            strengthLabel.setText("");
            return;
        }

        // Calculer la force
        int strength = calculatePasswordStrength(password);

        // Mettre à jour la barre de force
        if (strength >= 1) {
            strengthBar1.setFill(Color.RED);
            strengthLabel.setText("Faible");
            strengthLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
        if (strength >= 2) {
            strengthBar2.setFill(Color.ORANGE);
            strengthLabel.setText("Moyen");
            strengthLabel.setStyle("-fx-text-fill: #f39c12;");
        }
        if (strength >= 3) {
            strengthBar3.setFill(Color.GREEN);
            strengthLabel.setText("Fort");
            strengthLabel.setStyle("-fx-text-fill: #27ae60;");
        }
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;

        // Longueur minimale
        if (password.length() >= 6) strength++;

        // Contient des chiffres
        if (password.matches(".*\\d.*")) strength++;

        // Contient des caractères spéciaux
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strength++;

        // Longueur importante
        if (password.length() >= 10) strength++;

        return Math.min(strength, 3); // Max 3 niveaux
    }

    @FXML
    private void handleResetPassword() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        // Validation du mot de passe
        String password = newPasswordField.getText();
        if (password == null || password.length() < 8) {
            errorMessage.append("• Le mot de passe doit contenir au moins 8 caractères\n");
            isValid = false;
        } else if (!isValidPassword(password)) {
            errorMessage.append("• Le mot de passe doit contenir au moins 1 majuscule, 1 minuscule et 1 chiffre\n");
            isValid = false;
        }

        // Validation de la confirmation
        if (!password.equals(confirmPasswordField.getText())) {
            errorMessage.append("• Les mots de passe ne correspondent pas\n");
            isValid = false;
        }

        if (!isValid) {
            showErrorNotification(errorMessage.toString());
            return;
        }

        showSuccessNotification("Mot de passe réinitialisé avec succès");

        // Redirection après un délai
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(event -> loadLoginPage());
        pause.play();
    }

    public void handleCancel(ActionEvent actionEvent) {
        showSuccessNotification("Réinitialisation annulée");

        // Redirection après un délai
        PauseTransition delay = new PauseTransition(Duration.millis(800));
        delay.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/login.fxml"));
                Parent root = loader.load();

                Stage currentStage = (Stage) resetButton.getScene().getWindow();
                double width = currentStage.getWidth();
                double height = currentStage.getHeight();

                Scene loginScene = new Scene(root, width, height);
                currentStage.setScene(loginScene);
                currentStage.show();
            } catch (IOException ex) {
                showErrorNotification("Erreur de chargement de la page de connexion");
                ex.printStackTrace();
            }
        });
        delay.play();
    }
    private void loadLoginPage() {
        try {
            // Charger le fichier FXML de la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/login.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène avec les mêmes dimensions que la fenêtre actuelle
            Stage currentStage = (Stage) resetButton.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            // Créer une scène avec la même taille que la fenêtre actuelle
            Scene loginScene = new Scene(root, width, height);

            // Changer la scène pour afficher la page de connexion
            currentStage.setScene(loginScene);
            currentStage.show();

            // Fermer la fenêtre de réinitialisation du mot de passe
            ((Stage) resetButton.getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de connexion.");
            e.printStackTrace();
        }
    }


    private boolean isValidPassword(String password) {
        // Vérifier que le mot de passe respecte les critères (minimum 8 caractères, 1 majuscule, 1 minuscule, 1 chiffre)
        return password.length() >= 8 && password.matches(".*[A-Z].*") && password.matches(".*[a-z].*") && password.matches(".*[0-9].*");
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
            StackPane root = ( StackPane) resetButton.getScene().getRoot();
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
