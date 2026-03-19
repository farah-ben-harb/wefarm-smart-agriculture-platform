package controllers.User;

import entities.Utilisateur;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;
import services.UtilisateurService;
import utils.CaptchaGenerator;
import utils.CloudinaryService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import utils.Mail;

import javax.mail.MessagingException;
import java.util.UUID;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;

public class RegisterController {

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
    private Label nomError, prenomError, numTelError, adresseError, dateNaissanceError, emailError, mdpError, roleError;
    private File selectedImageFile;

    private String imagePath = "/img/avatar3.png";
    private final UtilisateurService utilisateurService = new UtilisateurService();

    //CAPTCHA
    @FXML
    private ImageView captchaImage;
    @FXML
    private TextField captchaInput;
    @FXML
    private Label captchaError;

    private String currentCaptchaText;

    private int captchaAttempts = 0;
    private static final int MAX_CAPTCHA_ATTEMPTS = 3;

    //notification
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();

    @FXML
    private ImageView backgroundImage;
    private final String[] backgroundImages = {
            "/img/signup1.jpg",
            "/img/signup2.jpg",
            "/img/signup4.png",
            "/img/signup5.png",
            "/img/signup6.png",
            "/img/farm2.jpg"
    };
    private int currentBackgroundIndex = 0;
    private Timeline backgroundTimeline;
    @FXML
    private StackPane leftPane;




    @FXML
    public void initialize() {
        Circle clip = new Circle(45, 45, 45);
        profileImage.setClip(clip);


        Image defaultImage = new Image(getClass().getResource(imagePath).toExternalForm());
        profileImage.setImage(defaultImage);

        dateNaissanceField.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (empty || date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f8d7da;");
                } else {
                    setStyle("-fx-background-color: #d4edda;");
                }
            }
        });


        uploadButton.setOnAction(event -> choisirImage());

        refreshCaptcha();
        setupBackgroundSlider();
        setupBackgroundImage();

        backButton.setStyle("-fx-background-color: #f6c23e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: #e0e0e0;");

        // Quand la souris entre
        backButton.setOnMouseEntered(e -> {
            backButton.setStyle("-fx-background-color: white; -fx-text-fill: #f6c23e; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: #f6c23e; -fx-border-width: 2px;");
        });

        // Quand la souris sort
        backButton.setOnMouseExited(e -> {
            backButton.setStyle("-fx-background-color: #f6c23e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
        });

        // Style de base pour le bouton "Enregistrer"
        enregistrerButton.setStyle("-fx-background-color: #2E8B57; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: #e0e0e0;");

        // Quand la souris entre
        enregistrerButton.setOnMouseEntered(e -> {
            enregistrerButton.setStyle("-fx-background-color: white; -fx-text-fill: #2E8B57; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: #2E8B57; -fx-border-width: 2px;");
        });

        // Quand la souris sort
        enregistrerButton.setOnMouseExited(e -> {
            enregistrerButton.setStyle("-fx-background-color: #2E8B57; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
        });
    }

    @FXML
    private void refreshCaptcha() {
        CaptchaGenerator.CaptchaResult captcha = CaptchaGenerator.generateCaptcha();
        currentCaptchaText = captcha.getText();

        try (InputStream imageStream = captcha.getImageStream()) {
            captchaImage.setImage(new Image(imageStream));
        } catch (IOException e) {
            captchaError.setText("Erreur lors du chargement du CAPTCHA.");
            e.printStackTrace();
        }

        captchaInput.clear();
        captchaError.setText("");
    }


    @FXML
    private void handleBack() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/login.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) backButton.getScene().getWindow();

            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            Scene loginScene = new Scene(root, width, height);

            currentStage.setScene(loginScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disableFormTemporarily() {
        // Désactiver tous les champs et boutons
        setFormEnabled(false);

        captchaError.setText("Trop de tentatives erronées. Veuillez patienter 10 secondes.");

        // Créer un timer pour réactiver le formulaire après 30 secondes
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            setFormEnabled(true);
            refreshCaptcha();
            captchaAttempts = 0;
            captchaError.setText("");
        }));
        timeline.play();
    }
    private void setFormEnabled(boolean enabled) {
        // Désactiver/activer tous les champs du formulaire
        nomField.setDisable(!enabled);
        prenomField.setDisable(!enabled);
        numTelField.setDisable(!enabled);
        adresseField.setDisable(!enabled);
        emailField.setDisable(!enabled);
        mdpField.setDisable(!enabled);
        dateNaissanceField.setDisable(!enabled);
        roleComboBox.setDisable(!enabled);
        uploadButton.setDisable(!enabled);
        enregistrerButton.setDisable(!enabled);
        captchaInput.setDisable(!enabled);
        backButton.setDisable(!enabled);

        String disabledStyle = "-fx-opacity: 0.7; -fx-background-color: #f5f5f5;";
        String enabledStyle = "";

        nomField.setStyle(enabled ? enabledStyle : disabledStyle);
        prenomField.setStyle(enabled ? enabledStyle : disabledStyle);
        numTelField.setStyle(enabled ? enabledStyle : disabledStyle);
        adresseField.setStyle(enabled ? enabledStyle : disabledStyle);
        emailField.setStyle(enabled ? enabledStyle : disabledStyle);
        mdpField.setStyle(enabled ? enabledStyle : disabledStyle);
        dateNaissanceField.setStyle(enabled ? enabledStyle : disabledStyle);
        captchaInput.setStyle(enabled ? enabledStyle : disabledStyle);

        String buttonDisabledStyle = "-fx-opacity: 0.7; -fx-background-color: #cccccc;";
        String buttonEnabledStyleBack = "-fx-background-color: #F4A300; -fx-text-fill: white; -fx-font-weight: bold;";
        String buttonEnabledStyleRegister = "-fx-background-color: #6DBE45; -fx-text-fill: white; -fx-font-weight: bold;";

        backButton.setStyle(enabled ? buttonEnabledStyleBack : buttonDisabledStyle);
        enregistrerButton.setStyle(enabled ? buttonEnabledStyleRegister : buttonDisabledStyle);
        uploadButton.setStyle(enabled ? enabledStyle : disabledStyle);

        String comboBoxDisabledStyle = "-fx-opacity: 0.7; -fx-background-color: #f5f5f5;";
        roleComboBox.setStyle(enabled ? enabledStyle : comboBoxDisabledStyle);

        if (dateNaissanceField.getEditor() != null) {
            dateNaissanceField.getEditor().setStyle(enabled ? enabledStyle : disabledStyle);
        }

        if (captchaImage != null) {
            captchaImage.setOpacity(enabled ? 1.0 : 0.7);
        }
    }
    @FXML
    private void handleRegister() {
        resetErrorMessages();

        if (!validerChamps()) {
          //  showErrorNotification("Veuillez corriger les erreurs dans le formulaire");
            return;
        }

        // Ensuite vérifier le CAPTCHA
        if (captchaInput.getText() == null || !captchaInput.getText().equalsIgnoreCase(currentCaptchaText)) {
            captchaAttempts++;
            captchaError.setText("Code CAPTCHA incorrect (" + (MAX_CAPTCHA_ATTEMPTS - captchaAttempts) + " tentatives restantes)");

            if (captchaAttempts >= MAX_CAPTCHA_ATTEMPTS) {
                disableFormTemporarily();
                return;
            }
            return;
        }

        // Si tout est valide
        try {
            // Upload de l'image
            String imageUrl;
            CloudinaryService cloudinaryService = new CloudinaryService();
            String roleValue = "[\"" + roleComboBox.getValue() + "\"]";

            if (selectedImageFile != null) {
                imageUrl = cloudinaryService.uploadProfileImage(selectedImageFile, "temp_" + System.currentTimeMillis());
            } else {
                imageUrl = cloudinaryService.uploadProfileImage(
                        new File(getClass().getResource("/img/avatar3.png").toURI()),
                        "default_avatar"
                );
            }

            // Création de l'utilisateur
            String confirmationToken = UUID.randomUUID().toString();
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
                    "en attente de validation",
                    true,
                    Integer.parseInt(numTelField.getText().trim())
            );
            user.setConfirmationToken(confirmationToken);

            utilisateurService.ajouterUtilisateur(user);

            Utilisateur savedUser = utilisateurService.findByEmail(user.getEmail());
            if (savedUser == null) {
                showErrorNotification("Erreur: L'utilisateur n'a pas été enregistré");
                return;
            }
            try {
                Mail mailService = new Mail();
                mailService.sendConfirmationEmail(user.getEmail(), confirmationToken);
                showSuccessNotification("Inscription réussie ! Un email de confirmation a été envoyé");
                showConfirmationCodeDialog(confirmationToken, user.getEmail());
            } catch (MessagingException e) {
                showErrorNotification("Inscription réussie mais échec d'envoi de l'email");
                e.printStackTrace();
                showConfirmationCodeDialog(confirmationToken, user.getEmail());
            }
        } catch (Exception e) {
            showErrorNotification("Erreur lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetErrorMessages() {
        nomError.setText("");
        prenomError.setText("");
        numTelError.setText("");
        adresseError.setText("");
        dateNaissanceError.setText("");
        emailError.setText("");
        mdpError.setText("");
        roleError.setText("");
        captchaError.setText("");
    }

    private void showConfirmationCodeDialog(String code, String email) {
        TextInputDialog dialogue = new TextInputDialog();
        dialogue.setTitle("Vérification de votre compte");
        dialogue.setHeaderText("Un code de confirmation a été envoyé à l'adresse suivante :\n" + email);
        dialogue.setContentText("Veuillez entrer le code reçu pour activer votre compte.");

        DialogPane dialogPane = dialogue.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #f4f6f8;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-effect: innershadow(gaussian, #aaaaaa, 10, 0, 0, 0);"
        );

        dialogPane.lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: #4CAF50;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-padding: 10px;"
        );
        dialogPane.lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: #F44336;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-padding: 10px;"
        );

        System.out.println("Code de confirmation (TEST): " + code);

        Optional<String> resultat = dialogue.showAndWait();
        resultat.ifPresent(codeSaisi -> {
            if (codeSaisi.equals(code)) {
                // Récupération de l'utilisateur
                Utilisateur utilisateur = utilisateurService.findByConfirmationToken(code);
                if (utilisateur != null) {
                    utilisateur.setVerified(true);
                    utilisateurService.updateUser(utilisateur);

                    showSuccessNotification("Félicitations ! Votre compte a été confirmé avec succès.");

                    // ⏳ Attendre 3 secondes avant de rediriger
                    PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                    delay.setOnFinished(event -> {
                        redirigerVersLogin(); // Redirection
                        Stage stage = (Stage) enregistrerButton.getScene().getWindow();
                        stage.close();        // Fermeture de la fenêtre
                    });
                    delay.play();

                }
            } else {
                showErrorNotification("Le code saisi est incorrect. Vérifiez votre boîte mail ou demandez un nouveau code.");
            }
        });
    }



    private void redirigerVersLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/login.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) backButton.getScene().getWindow();

            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            Scene loginScene = new Scene(root, width, height);

            currentStage.setScene(loginScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            // Charger l'image sélectionnée
            Image image = new Image(selectedImageFile.toURI().toString());

            profileImage.setImage(image);

            profileImage.setPreserveRatio(false);
            profileImage.setFitHeight(90);
            profileImage.setFitWidth(90);
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


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
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
                Scene scene = enregistrerButton.getScene();
                BorderPane root = (BorderPane) scene.getRoot();

                // Crée un StackPane comme overlay (meilleur pour le positionnement)
                StackPane overlay = new StackPane();
                overlay.setAlignment(Pos.TOP_RIGHT);
                overlay.setStyle("-fx-background-color: transparent;");
                overlay.setPickOnBounds(false);
                overlay.setViewOrder(-1); // Premier plan

                // Ajoute la notification avec une marge
                StackPane.setMargin(notification, new Insets(20, 20, 0, 0));
                overlay.getChildren().add(notification);

                // Ajoute l'overlay au BorderPane (dans la zone top)
                if (root.getTop() == null) {
                    root.setTop(overlay);
                } else {
                    ((StackPane) root.getTop()).getChildren().add(overlay);
                }

                // Animation d'entrée
                notification.setOpacity(0);
                notification.setTranslateY(-50);

                ParallelTransition enterTransition = new ParallelTransition(
                        new FadeTransition(Duration.millis(300), notification),
                        new TranslateTransition(Duration.millis(300), notification)
                );
                ((FadeTransition)enterTransition.getChildren().get(0)).setFromValue(0);
                ((FadeTransition)enterTransition.getChildren().get(0)).setToValue(1);
                ((TranslateTransition)enterTransition.getChildren().get(1)).setFromY(-50);
                ((TranslateTransition)enterTransition.getChildren().get(1)).setToY(0);

                enterTransition.play();

                // Animation de sortie après délai
                PauseTransition delay = new PauseTransition(Duration.seconds(3));
                delay.setOnFinished(e -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(ev -> {
                        overlay.getChildren().remove(notification);
                        if (root.getTop() != null) {
                            ((StackPane) root.getTop()).getChildren().remove(overlay);
                        }
                    });
                    fadeOut.play();
                });
                delay.play();

            } catch (Exception e) {
                System.err.println("Erreur d'affichage de notification: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    private void removeNotification(HBox notification) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            BorderPane root = (BorderPane) enregistrerButton.getScene().getRoot();
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

    private void setupBackgroundImage() {
        // Charger la première image
        loadCurrentBackground();

        // Écouteurs pour le redimensionnement
        leftPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            backgroundImage.setFitWidth(newVal.doubleValue());
        });

        leftPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            backgroundImage.setFitHeight(newVal.doubleValue());
        });
    }

    private void loadCurrentBackground() {
        Image image = new Image(
                getClass().getResourceAsStream(backgroundImages[currentBackgroundIndex]),
                leftPane.getWidth(),  // double
                leftPane.getHeight(), // double
                false,                // boolean
                true                  // boolean
        );

        backgroundImage.setImage(image);
    }

    private void setupBackgroundSlider() {
        backgroundTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> {
                    currentBackgroundIndex = (currentBackgroundIndex + 1) % backgroundImages.length;
                    transitionToNextBackground();
                })
        );
        backgroundTimeline.setCycleCount(Timeline.INDEFINITE);
        backgroundTimeline.play();
    }

    private void transitionToNextBackground() {
        Image image = new Image(
                getClass().getResourceAsStream(backgroundImages[currentBackgroundIndex]),
                leftPane.getWidth(),  // double
                leftPane.getHeight(), // double
                false,                // boolean
                true                  // boolean
        );


        FadeTransition fadeOut = new FadeTransition(Duration.millis(800), backgroundImage);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(e -> {
            backgroundImage.setImage(image);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), backgroundImage);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }
    //buttons
    private void playClickAnimation(Button button, String originalColor) {
        // 1. Changer temporairement le style du bouton
        button.setStyle("-fx-background-color: white; -fx-text-fill: " + originalColor + "; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8px;");

        // 2. Attendre un petit moment, puis restaurer le style original
        PauseTransition pause = new PauseTransition(Duration.millis(200));
        pause.setOnFinished(event -> {
            button.setStyle("-fx-background-color: " + originalColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8px;");
        });
        pause.play();

        // 3. (Optionnel) Ajouter une animation de zoom pour plus d’effet
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

}
