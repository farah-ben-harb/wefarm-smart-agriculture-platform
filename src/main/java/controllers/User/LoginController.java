package controllers.User;
import javafx.animation.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.*;
import java.util.prefs.Preferences;


import entities.Activites;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Node;

import entities.Utilisateur;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;
import services.ActivitiesService;
import services.UtilisateurService;
import utils.Quotes;
import utils.SessionManager;

// Pour l'analyse JSON (ajoutez GSON à votre pom.xml)

import java.io.IOException;
import java.util.List;


public class LoginController {

    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();
    public ImageView capturedImageView;
    public Button cameraButton;
    public Label quoteLabel;
    public StackPane quoteContainer;
    public ImageView quoteIcon;

    @FXML
    private Label forgetpasswordLabel;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;


    @FXML
    private Label registerLabel;

    private UtilisateurService utilisateurService; // Service pour interagir avec la base de données

    public LoginController() {
        utilisateurService = new UtilisateurService();
    }

    @FXML
    private Label emailErrorLabel;
    @FXML
    private Label passwordErrorLabel;

    @FXML
    private TextField passwordTextField;
    @FXML
    private Button togglePasswordButton;
    @FXML
    private ImageView eyeIcon;
    private boolean isPasswordVisible = false;

    //Media
    private MediaPlayer mediaPlayer;
    private static final String BACKGROUND_MUSIC = "/sound/baackground.mp3";
    public Button sensibilisationButton;



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

    //REMEMBER ME
    @FXML
    private CheckBox rememberMeCheckBox;
    private static final String COOKIE_NAME = "login_remember";
    private static final String PREFS_NAME = "login_prefs";
    private static final String PREFS_USERNAME = "username";
    private static final String PREFS_PASSWORD = "password";
    //face id
    @FXML
    private Button faceLoginButton;


    // Mettez à jour handleRememberMe()
    private void handleRememberMe(String email, String password) {
        Preferences prefs = Preferences.userRoot().node(PREFS_NAME);

        if (rememberMeCheckBox.isSelected()) {
            prefs.put(PREFS_USERNAME, email);
            prefs.put(PREFS_PASSWORD, password);
            prefs.putBoolean("remember_me", true);
            prefs.putLong("expiry_time", System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000));
        } else {
            prefs.remove(PREFS_USERNAME);
            prefs.remove(PREFS_PASSWORD);
            prefs.remove("remember_me");
            prefs.remove("expiry_time");
        }
    }

    // Mettez à jour loadSavedCredentials()
    private void loadSavedCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREFS_NAME);
        long expiryTime = prefs.getLong("expiry_time", 0);

        if (expiryTime > System.currentTimeMillis()) {
            emailField.setText(prefs.get(PREFS_USERNAME, ""));
            passwordField.setText(prefs.get(PREFS_PASSWORD, ""));
            rememberMeCheckBox.setSelected(true);
        } else {
            prefs.remove(PREFS_USERNAME);
            prefs.remove(PREFS_PASSWORD);
            prefs.remove("remember_me");
            prefs.remove("expiry_time");
        }
    }

    private void redirectBasedOnRole() {
        try {
            SessionManager session = SessionManager.getInstance();
            String fxmlPath = session.getRoles().contains("ADMIN")
                    ? "/views/Back.fxml"
                    : "/views/user/Home.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));

            // Configurer la nouvelle fenêtre
            if (session.getRoles().contains("ADMIN")) {
                newStage.setTitle("Admin Dashboard");
                newStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/fermier.png")));
            } else {
                newStage.setTitle("Home");
            }

            newStage.show();

            // Fermer la fenêtre de login si elle existe
            if (emailField != null && emailField.getScene() != null) {
                ((Stage) emailField.getScene().getWindow()).close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorNotification("Erreur de chargement de la page");
        }
    }

    @FXML
    public void initialize() {
        // Initialisation UI d'abord
        passwordTextField.setVisible(false);
        eyeIcon.setImage(new Image(getClass().getResourceAsStream("/img/eye_closed.png")));
        setupBackgroundSlider();
        setupBackgroundImage();

        // Si pas de session, charger les identifiants sauvegardés
        loadSavedCredentials();
        setupQuoteSystem();
        // Initialiser et jouer le son de fond
        setupBackgroundMusic();
        ScaleTransition pulse = new ScaleTransition(Duration.millis(1000), sensibilisationButton);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.play();

    }
    private void setupQuoteSystem() {
        // Configuration du conteneur - version compacte et élégante
        quoteContainer.setStyle("-fx-background-color: rgba(245,255,245,0.95); " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: #66bb6a; " +  // Vert plus clair
                "-fx-border-radius: 12; " +
                "-fx-border-width: 1.5px; " +
                "-fx-padding: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(46,139,87,0.15), 8, 0.1, 0, 2);");

        // Configuration de l'icône
        quoteIcon.setImage(new Image(getClass().getResourceAsStream("/img/message.png")));
        quoteIcon.setStyle("-fx-opacity: 0.7;");

        // Style optimisé pour le label arabe
        quoteLabel.setStyle("-fx-font-family: 'Segoe UI', 'Arial'; " +
                "-fx-font-size: 14px; " +
                "-fx-text-fill: #2e7d32; " +  // Vert plus foncé
                "-fx-alignment: center-right; " +
                "-fx-wrap-text: true; " +
                "-fx-font-weight: 500; " +    // Semi-gras
                "-fx-line-spacing: 3px; " +
                "-fx-padding: 0 0 0 8px;");

        // Animation d'apparition raffinée
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), quoteContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(600), quoteContainer);
        scaleIn.setFromX(0.95); scaleIn.setFromY(0.95);
        scaleIn.setToX(1); scaleIn.setToY(1);

        ParallelTransition appear = new ParallelTransition(fadeIn, scaleIn);

        // Animation de disparition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), quoteContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Chargement asynchrone
        new Thread(() -> {
            try {
                String quote = Quotes.fetchAgricultureQuote();

                Platform.runLater(() -> {
                    quoteLabel.setText(quote);
                    appear.play();

                    // Animation de pulsation subtile
                    ScaleTransition pulse = new ScaleTransition(Duration.seconds(4), quoteContainer);
                    pulse.setByX(0.02); pulse.setByY(0.02);
                    pulse.setCycleCount(Animation.INDEFINITE);
                    pulse.setAutoReverse(true);
                    appear.setOnFinished(e -> pulse.play());

                    // Disparition après 10 secondes
                    PauseTransition displayTime = new PauseTransition(Duration.seconds(20));
                    displayTime.setOnFinished(e -> {
                        pulse.stop();
                        fadeOut.play();
                    });
                    displayTime.play();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    quoteLabel.setText("« الأرض تعطي بالصبر أغلى الثمار » - مثل فلاحي");
                    appear.play();
                });
            }
        }).start();
    }

    @FXML
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Cacher le mot de passe
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            eyeIcon.setImage(new Image(getClass().getResourceAsStream("/img/eye_closed.png")));
        } else {
            // Afficher le mot de passe
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            eyeIcon.setImage(new Image(getClass().getResourceAsStream("/img/eye_open.jpg")));
        }
        isPasswordVisible = !isPasswordVisible;
    }


    @FXML
    private void handleRegisterClick(MouseEvent event) {
        try {
            stopBackgroundMusic();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double width = stage.getWidth();
            double height = stage.getHeight();

            stage.setScene(new Scene(root, width, height));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Register not clicked");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();
        boolean isValid = true;

        emailErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);

        // Vérifier format email
        if (email.isEmpty()) {
            emailErrorLabel.setText("Email requis ");
            emailErrorLabel.setVisible(true);
            applyShakeAnimation(emailField);
            isValid = false;
        } else if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
            emailErrorLabel.setText("Format email invalide ");
            emailErrorLabel.setVisible(true);
            applyShakeAnimation(emailField);
            isValid = false;
        }

        // Vérifier mot de passe
        if (password.isEmpty()) {
            passwordErrorLabel.setText("Mot de passe requis ");
            passwordErrorLabel.setVisible(true);
            applyShakeAnimation(passwordField);
            isValid = false;
        }

        if (!isValid) return;

        Utilisateur user = utilisateurService.getUtilisateurByEmail(email);

        if (user != null) {
            String hashedPassword = user.getPassword();

            // Correction : remplacer $2y$ par $2a$ pour compatibilité avec BCrypt Java
            if (hashedPassword.startsWith("$2y$")) {
                hashedPassword = "$2a$" + hashedPassword.substring(4);
            }


            if (BCrypt.checkpw(password, hashedPassword)) {
                // Sauvegarder les identifiants si Remember Me est coché
                handleRememberMe(email, password);

                // Démarrer la session complète
                SessionManager.startSession(
                        user.getId(),
                        user.getEmail(),
                        user.getRoles(),
                        user.getAdresse(),
                        user.getPassword(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getDateNaissance(),
                        user.getImage(),
                        user.getStatus(),
                        user.isVerified(),
                        user.getConfirmationToken(),
                        user.getReactivationDate(),
                        user.getNumTel()
                );              //creer une session
//                SessionManager.startSession(
//                        user.getId(),
//                        user.getEmail(),
//                        user.getRoles(),
//                        user.getAdresse(),
//                        user.getPassword(),
//                        user.getFirstName(),
//                        user.getLastName(),
//                        user.getDateNaissance(),
//                        user.getImage(),
//                        user.getStatus(),
//                        user.isVerified(),
//                        user.getConfirmationToken(),
//                        user.getReactivationDate(),
//                        user.getNumTel()
//                );
                // Si Remember Me est coché, créer un token de session
                if (rememberMeCheckBox.isSelected()) {
                    SessionManager.generateRememberMeToken(user.getId());
                }
                showSuccessNotification("Connexion réussie !");


                // Ajouter activité
                ActivitiesService activitiesService = new ActivitiesService();
                Activites activity = new Activites(user.getId(), "Connexion réussie",
                        "{\"email\": \"" + user.getEmail() + "\"}", new Date());
                activitiesService.ajouterActivite(activity);

                // Redirection après un délai
                PauseTransition delay = new PauseTransition(Duration.seconds(1)); // 1 seconde
                delay.setOnFinished(e -> {
                    if (user.getRoles().contains("ADMIN")) {
                        loadAdminDashboard();
                    } else {
                        loadUserProfile();
                    }
                });
                delay.play();
            } else {
                showErrorNotification("Mot de passe incorrect !");
            }
        } else {
            showErrorNotification("Utilisateur non trouvé !");
        }
    }

    private void loadUserProfile() {
        try {
            stopBackgroundMusic();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/Home.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) emailField.getScene().getWindow();

            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            Stage stage = new Stage();
            stage.setTitle("Home");

            stage.setScene(new Scene(root, width, height));
            stage.show();

            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadAdminDashboard() {
        try {
            stopBackgroundMusic();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Back.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) emailField.getScene().getWindow();

            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            Stage stage = new Stage();
            stage.setTitle("Admin Dashboard");
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            currentStage.getIcons().add(icon);

            stage.setScene(new Scene(root, width, height));
            stage.show();

            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<Utilisateur> getAllUsers() {
        return utilisateurService.getAllUtilisateurs();
    }



// Méthodes de notification
private void showSuccessNotification(String message) {
    HBox notification = createNotificationBox(message, "#4CAF50", "✓");
    showAnimatedNotification(notification);
}


    private void showErrorNotification(String message) {
        HBox notification = createNotificationBox(message, "#F44336", "✗");
        showAnimatedNotification(notification);
    }

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
            AnchorPane root = (AnchorPane) emailField.getScene().getRoot();

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
            AnchorPane root = (AnchorPane) emailField.getScene().getRoot();
            root.getChildren().remove(notification);
            activeNotifications.remove(notification);
        });
        exitTransition.play();
    }
    private void rearrangeNotifications() {
        for (int i = 0; i < activeNotifications.size(); i++) {
            HBox notification = activeNotifications.get(i);
            notification.setLayoutY(10 + (i * (NOTIFICATION_HEIGHT + 5)));
        }
    }

    //controle de saisie login
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }
    //aniamtion controle de saisie
    private void applyShakeAnimation(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(70), node);
        tt.setFromX(0f);
        tt.setByX(10f);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }

//diapo
    private void setupBackgroundImage() {
        loadCurrentBackground();

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

    //forget password
    @FXML
    private void handleForgotPassword(MouseEvent event) {
        try {
            // Charger le fichier FXML de la page "emailresetpassword.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/emailresetpassword.fxml"));
            Parent root = loader.load();

            // Récupérer le stage actuel (fenêtre de connexion)
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Récupérer la taille actuelle de la fenêtre
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            // Créer un nouveau stage pour afficher "emailresetpassword.fxml"
            Stage stage = new Stage();
            stage.setTitle("Forgot Password");

            // Définir la scène avec la même taille que l'ancienne fenêtre
            stage.setScene(new Scene(root, width, height));
            stage.show();

            // Fermer la fenêtre actuelle
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la page emailresetpassword.fxml");
        }
    }

    //face id

    public void handleFaceAuthSuccess(Utilisateur user) {
        if (user != null) {
            // Démarrer la session
            SessionManager.startSession(
                    user.getId(),
                    user.getEmail(),
                    user.getRoles(),
                    user.getAdresse(),
                    user.getPassword(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getDateNaissance(),
                    user.getImage(),
                    user.getStatus(),
                    user.isVerified(),
                    user.getConfirmationToken(),
                    user.getReactivationDate(),
                    user.getNumTel()
            );

            // Redirection selon le rôle
            if (user.getRoles().contains("ADMIN")) {
                loadAdminDashboard();
            } else {
                loadUserProfile();
            }
        }
    }

    @FXML
    private void openCameraWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/camera.fxml"));
            Parent root = loader.load();

            CameraController cameraController = loader.getController();
            cameraController.setParentController(this);

            cameraController.showFacialRecognitionIntroModal(() -> {
                Stage cameraStage = new Stage();
                cameraStage.setScene(new Scene(root));
                cameraStage.setTitle("Reconnaissance faciale");

                // Utilisez la nouvelle méthode publique au lieu d'accéder au champ privé
                cameraStage.setOnCloseRequest(e -> cameraController.closeCamera());

                cameraStage.show();
            });

        } catch (IOException e) {
            showErrorNotification("Erreur lors du chargement de la caméra");
            e.printStackTrace();
        }
    }
    //Sensibilisation
    @FXML

    public void goToSensibilisation(javafx.event.ActionEvent actionEvent) {
        try {
            stopBackgroundMusic();

            stopBackgroundMusic();
            Parent root = FXMLLoader.load(getClass().getResource("/views/sensibilisation.fxml"));
            Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setupBackgroundMusic() {
        try {
            String musicFile = getClass().getResource("/sound/background.mp3").toString(); // Assurez-vous d'avoir ce fichier dans src/sound/
            Media sound = new Media(musicFile);
            mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Boucle infinie
            mediaPlayer.setVolume(0.3); // Volume à 30%
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la musique de fond: " + e.getMessage());
        }
    }

    // Pour arrêter la musique lorsque nécessaire
    private void stopBackgroundMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }


}
