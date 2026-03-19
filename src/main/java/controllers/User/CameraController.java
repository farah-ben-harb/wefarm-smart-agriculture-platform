package controllers.User;

import entities.Utilisateur;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import services.UtilisateurService;
import utils.CameraService;
import utils.FaceIdService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CameraController {
    @FXML private ImageView cameraView;
    @FXML private Button captureButton;

    private CameraService cameraService;
    private LoginController parentController;
    private final FaceIdService faceIdService = new FaceIdService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    //notif
    // En haut de votre classe
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();

    public void setParentController(LoginController controller) {
        this.parentController = controller;
    }
//    @FXML
//    public void initialize() {
//        showFacialRecognitionIntroModal(() -> {
//            try {
//                cameraService = new CameraService(cameraView);
//                cameraService.startCamera();
//            } catch (Exception e) {
//                showErrorNotification("Impossible d'accéder à la caméra");
//                captureButton.setDisable(true);
//                e.printStackTrace();
//            }
//        });
//    }
public void closeCamera() {
    if (cameraService != null) {
        cameraService.closeCamera();
    }
}
    public void showFacialRecognitionIntroModal(Runnable onContinue) {
            Platform.runLater(() -> {
                Stage introStage = new Stage();
                introStage.initModality(Modality.APPLICATION_MODAL);
                introStage.initStyle(StageStyle.UNDECORATED);
                introStage.setResizable(false);

                VBox root = new VBox(20);
                root.setAlignment(Pos.CENTER);
                root.setPadding(new Insets(30));
                root.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-background-radius: 15;");

            // Conteneur pour l'animation du visage
            StackPane faceContainer = new StackPane();
            faceContainer.setPrefSize(120, 120);

            // Cercle extérieur
            Circle outerCircle = new Circle(50, Color.TRANSPARENT);
            outerCircle.setStroke(Color.WHITE);
            outerCircle.setStrokeWidth(2);

            // Points pour le scan (animation)
            Circle[] scanDots = new Circle[8];
            for (int i = 0; i < scanDots.length; i++) {
                scanDots[i] = new Circle(4, Color.WHITE);
                scanDots[i].setOpacity(0);
            }

            // Configuration des positions des points
            PositionDots(scanDots);

            faceContainer.getChildren().addAll(outerCircle);
            faceContainer.getChildren().addAll(scanDots);

            // Animation de scan
            SequentialTransition scanAnimation = createScanAnimation(scanDots);

            // Titre et instructions
            Label title = new Label("Scan facial");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

            Label instruction = new Label("La caméra va analyser votre visage");
            instruction.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 14px;");

            // Bouton
                Button startBtn = new Button("DÉmarrer le scan");
                startBtn.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold;");
                startBtn.setOnAction(e -> {
                    scanAnimation.stop();
                    introStage.close();
                    onContinue.run(); // Ceci ouvrira la fenêtre caméra
                });

                root.getChildren().addAll(faceContainer, title, instruction, startBtn);

                Scene scene = new Scene(root, 400, 400);
                scene.setFill(Color.TRANSPARENT);
                introStage.setScene(scene);
                introStage.centerOnScreen();
                introStage.setOnShown(e -> scanAnimation.play());
                introStage.show();
            });
    }

    @FXML
    public void initialize() {
        // Initialiser la caméra seulement quand la fenêtre est ouverte
        Platform.runLater(() -> {
            try {
                cameraService = new CameraService(cameraView);
                cameraService.startCamera();
            } catch (Exception e) {
                showErrorNotification("Impossible d'accéder à la caméra");
                captureButton.setDisable(true);
                e.printStackTrace();
            }
        });
    }

    private void PositionDots(Circle[] dots) {
        double radius = 50;
        double center = 60;
        double angle = 2 * Math.PI / dots.length;

        for (int i = 0; i < dots.length; i++) {
            double x = center + radius * Math.cos(i * angle);
            double y = center + radius * Math.sin(i * angle);
            dots[i].setCenterX(x);
            dots[i].setCenterY(y);
        }
    }

    private SequentialTransition createScanAnimation(Circle[] dots) {
        SequentialTransition sequence = new SequentialTransition();

        for (int i = 0; i < dots.length; i++) {
            int finalI = i;
            Timeline showDot = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(dots[finalI].opacityProperty(), 0),
                            new KeyValue(dots[finalI].radiusProperty(), 2)
                    ),
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(dots[finalI].opacityProperty(), 1),
                            new KeyValue(dots[finalI].radiusProperty(), 4)
                    ),
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(dots[finalI].opacityProperty(), 0),
                            new KeyValue(dots[finalI].radiusProperty(), 2)
                    )
            );

            sequence.getChildren().add(showDot);
        }

        sequence.setCycleCount(Animation.INDEFINITE);
        return sequence;
    }
    @FXML
    private void capturePhoto() {
        try {
            // 1. Capturer la photo
            File capturedFile = cameraService.capturePhoto();
            showAlert("Succès", "Photo capturée avec succès");

            // 2. Fermer la caméra
            cameraService.closeCamera();
            ((Stage)cameraView.getScene().getWindow()).close();

            // 3. Afficher un modal de chargement
            Platform.runLater(() -> {
                Stage loadingStage = createLoadingModal();
                loadingStage.show();

                // 4. Détecter le visage (en arrière-plan)
                new Thread(() -> {
                    try {
                        String detectResult = faceIdService.detectFace(capturedFile);
                        if (detectResult == null || !isFaceDetected(detectResult)) {
                            Platform.runLater(() -> {
                                loadingStage.close();
                                showAlert("Erreur de détection", "Aucun visage détecté sur la photo");
                                capturedFile.delete();
                            });
                            return;
                        }

                        // 5. Comparaison avec les utilisateurs
                        for (Utilisateur user : parentController.getAllUsers()) {
                            String imageUrl = user.getImage();
                            if (imageUrl == null || !isValidUrl(imageUrl)) continue;

                            File profileImageFile = loadImageFromUrl(imageUrl);
                            if (profileImageFile == null) continue;

                            try {
                                String compareResult = faceIdService.compareFaces(profileImageFile, capturedFile);
                                if (isFaceMatch(compareResult)) {
                                    Platform.runLater(() -> {
                                        loadingStage.close();
                                        showAlert("Authentification réussie", "Reconnaissance faciale validée");
                                        try {
                                            parentController.handleFaceAuthSuccess(user);
                                        } finally {
                                            cameraService.closeCamera();
                                        }
                                    });
                                    return;
                                }
                            } finally {
                                profileImageFile.delete();
                            }
                        }

                        Platform.runLater(() -> {
                            loadingStage.close();
                            showAlert("Authentification échouée", "Aucun utilisateur correspondant trouvé");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            loadingStage.close();
                            showAlert("Erreur technique", "Un problème est survenu lors de la reconnaissance faciale");
                            e.printStackTrace();
                        });
                    } finally {
                        //capturedFile.delete();
                    }
                }).start();
            });

        } catch (Exception e) {
            showAlert("Erreur", "La capture de la photo a échoué");
            e.printStackTrace();
        }
    }

    private Stage createLoadingModal() {
        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.setResizable(false);

        // Créer un conteneur avec une animation de chargement
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-background-radius: 10;");

        // Ajouter un indicateur de chargement (spinner)
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(-1); // Spinner indéterminé
        progressIndicator.setStyle("-fx-progress-color: white;");

        // Ajouter un label
        Label label = new Label("Traitement en cours...");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        root.getChildren().addAll(progressIndicator, label);

        // Créer la scène
        Scene scene = new Scene(root, 300, 200);
        scene.setFill(Color.TRANSPARENT);
        loadingStage.setScene(scene);

        // Centrer la fenêtre
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        loadingStage.setX((screenBounds.getWidth() - 300) / 2);
        loadingStage.setY((screenBounds.getHeight() - 200) / 2);

        return loadingStage;
    }
    // Remplacez showAlert par showErrorNotification dans initialize()
//    @FXML
//    public void initialize() {
//        try {
//            cameraService = new CameraService(cameraView);
//            cameraService.startCamera();
//        } catch (Exception e) {
//            showErrorNotification("Impossible d'accéder à la caméra");
//            captureButton.setDisable(true);
//        }
//    }

    /**
     * Vérifie si une URL est valide et commence bien par http:// ou https://
     */
    private boolean isValidUrl(String url) {
        return url.matches("^(https?://).*");
    }

    /**
     * Télécharge une image depuis une URL et la sauvegarde temporairement.
     */
    private File loadImageFromUrl(String imageUrl) throws IOException {
        if (!isValidUrl(imageUrl)) {
            System.err.println("URL invalide: " + imageUrl);
            return null;
        }

        System.out.println("Chargement de l'image depuis : " + imageUrl);
        URL url = new URL(imageUrl);
        File tempFile = File.createTempFile("profile_", ".jpg");

        try (InputStream in = url.openStream();
             FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        System.out.println("Image téléchargée avec succès : " + tempFile.getAbsolutePath());
        return tempFile;
    }

    private boolean isFaceDetected(String detectResult) {
        try {
            JsonObject json = JsonParser.parseString(detectResult).getAsJsonObject();
            return json.has("faces") && json.getAsJsonArray("faces").size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isFaceMatch(String compareResult) {
        try {
            JsonObject json = JsonParser.parseString(compareResult).getAsJsonObject();
            double confidence = json.get("confidence").getAsDouble();
            return confidence > 60.0; // Seuil ajustable
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Affiche une boîte de dialogue personnalisée
     * @param title Titre de la fenêtre
     * @param message Message à afficher
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuration de base
        alert.setTitle(title);
        alert.setHeaderText(null); // Supprime le header inutile
        alert.setContentText(message);

        // Optimisation de la taille
        alert.getDialogPane().setPrefSize(400, 150);

        // Personnalisation minimaliste
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();

        // Force le focus sur l'alerte
        alert.initModality(Modality.APPLICATION_MODAL);

        // Amélioration de l'expérience utilisateur
        alert.getDialogPane().setStyle(
                "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 20px;"
        );

        // Personnalisation des boutons
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle(
                    "-fx-background-color: #2196F3;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;"
            );
        }

        alert.showAndWait();
    }
    //notif
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
            AnchorPane root = (AnchorPane) cameraView.getScene().getRoot();

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
            AnchorPane root = (AnchorPane) cameraView.getScene().getRoot();
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
