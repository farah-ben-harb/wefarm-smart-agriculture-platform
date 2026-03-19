package controllers.User;

import entities.Utilisateur;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EditUserController {
    public Label firstNameError;
    public Label lastNameError;
    public Label adresseError;
    public Label numTelError;
    public Label emailError;
    @FXML private ImageView userImageView;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField adresseField;
    @FXML private TextField numTelField;

    private Stage dialogStage;
    private Utilisateur user;
    private boolean saveClicked = false;
    //notification
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setUser(Utilisateur user) {
        this.user = user;
        // Pré-remplir les champs
        double radius = userImageView.getFitWidth() / 2;
        Circle clip = new Circle(radius, radius, radius);
        userImageView.setClip(clip);
        loadImage(user.getImage());
        emailField.setText(user.getEmail());
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        adresseField.setText(user.getAdresse());
        numTelField.setText(String.valueOf(user.getNumTel()));

    }

    private void loadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            userImageView.setImage(null);
            return;
        }

        Task<Image> loadImageTask = new Task<>() {
            @Override
            protected Image call() {
                return new Image(imageUrl, false); // Chargement normal
            }
        };

        loadImageTask.setOnSucceeded(event -> {
            Image img = loadImageTask.getValue();
            if (img != null) {
                userImageView.setImage(img);

                double imgWidth = img.getWidth();
                double imgHeight = img.getHeight();

                double size = Math.min(imgWidth, imgHeight);
                double x = (imgWidth - size) / 2;
                double y = (imgHeight - size) / 2;

                userImageView.setViewport(new Rectangle2D(x, y, size, size));
            }
        });

        loadImageTask.setOnFailed(event -> userImageView.setImage(null));

        new Thread(loadImageTask).start();
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }


    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return;
        }

        try {
            user.setEmail(emailField.getText());
            user.setFirstName(firstNameField.getText());
            user.setLastName(lastNameField.getText());
            user.setAdresse(adresseField.getText());
            user.setNumTel(Integer.parseInt(numTelField.getText()));

            saveClicked = true;

            showSuccessNotification("Profil mis à jour avec succès");
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(e -> dialogStage.close());
            delay.play();

        } catch (Exception e) {
            showErrorNotification("Erreur lors de la mise à jour du profil");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        showSuccessNotification("Modifications annulées");
        dialogStage.close();
    }

    private boolean isInputValid() {
        boolean isValid = true;
        String adresseRegex = "^[0-9]{1,4}\\s+[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+,\\s*[0-9]{4,5}$";


        emailError.setText("");
        firstNameError.setText("");
        lastNameError.setText("");
        adresseError.setText("");
        numTelError.setText("");

        // Validation email (doit contenir @gmail.com)
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            emailError.setText("Email requis");
            isValid = false;
        } else {
            String email = emailField.getText().trim().toLowerCase();
            if (!email.matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
                emailError.setText("Format email invalide (ex: exemple@domain.com)");
                isValid = false;
            } else if (email.length() > 50) {
                emailError.setText("Maximum 50 caractères");
                isValid = false;
            } else {
                emailError.setText("");
            }
        }

        // Validation prénom (lettres et espaces)
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            firstNameError.setText("Prénom requis");
            isValid = false;
        } else {
            String firstName = firstNameField.getText().trim();
            if (!firstName.matches("^[a-zA-ZÀ-ÿ\\s]+$")) {  // Ajout de \\s pour les espaces
                firstNameError.setText("Lettres et espaces seulement");
                isValid = false;
            } else if (firstName.length() < 2) {
                firstNameError.setText("Minimum 2 caractères");
                isValid = false;
            } else if (firstName.length() > 30) {
                firstNameError.setText("Maximum 30 caractères");
                isValid = false;
            } else if (firstName.matches(".*\\s{2,}.*")) {  // Vérifie les espaces multiples
                firstNameError.setText("Un seul espace autorisé entre les mots");
                isValid = false;
            } else {
                firstNameError.setText("");
            }
        }

        // Validation nom (lettres et espaces)
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            lastNameError.setText("Nom requis");
            isValid = false;
        } else {
            String lastName = lastNameField.getText().trim();
            if (!lastName.matches("^[a-zA-ZÀ-ÿ\\s]+$")) {  // Ajout de \\s pour les espaces
                lastNameError.setText("Lettres et espaces seulement");
                isValid = false;
            } else if (lastName.length() < 2) {
                lastNameError.setText("Minimum 2 caractères");
                isValid = false;
            } else if (lastName.length() > 30) {
                lastNameError.setText("Maximum 30 caractères");
                isValid = false;
            } else if (lastName.matches(".*\\s{2,}.*")) {  // Vérifie les espaces multiples
                lastNameError.setText("Un seul espace autorisé entre les mots");
                isValid = false;
            } else {
                lastNameError.setText("");
            }
        }


            // Validation de l'adresse
            if (adresseField.getText().isEmpty()) {
                adresseError.setText("Adresse manquante.");
                isValid = false;
            } else if (!Pattern.matches(adresseRegex, adresseField.getText())) {
                adresseError.setText("Format attendu : Numéro Rue, Code Postal (ex: 20 Rue Habib, 2022)");
                isValid = false;
            } else {
                adresseError.setText("");
            }


        // Validation téléphone (Tunisien: commence par 2,5,9 et contient 8 chiffres)
        if (numTelField.getText() == null || numTelField.getText().trim().isEmpty()) {
            numTelError.setText("Téléphone requis");
            isValid = false;
        } else {
            String phone = numTelField.getText().trim();
            if (phone.startsWith("+216")) {
                // Format international Tunisien
                if (!phone.matches("^\\+216[259]\\d{7}$")) {
                    numTelError.setText("Format: +2162XXXXXXX, +2165XXXXXXX ou +2169XXXXXXX");
                    isValid = false;
                }
            } else {
                // Format local Tunisien
                if (!phone.matches("^[259]\\d{7}$")) {
                    numTelError.setText("Doit commencer par 2,5 ou 9 et contenir 8 chiffres");
                    isValid = false;
                }
            }
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
            AnchorPane root = (AnchorPane) dialogStage.getScene().getRoot();

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
            AnchorPane root = (AnchorPane) dialogStage.getScene().getRoot();
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