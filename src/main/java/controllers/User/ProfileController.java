package controllers.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.concurrent.Task;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.UtilisateurService;
import utils.SessionManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileController {

    @FXML
    private ImageView profileImage;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label adresseLabel;

    @FXML
    private Label numTelLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button modifierButton;

    @FXML
    private Button destinataireButton;

    @FXML
    private Button supprimerButton;

    @FXML
    private Circle statusCircle;
    @FXML
    private Label joinDateLabel;
    @FXML
    private Label dateNaissanceLabel;
    @FXML
    private Label rolesLabel;
    @FXML
    private Button logOutButton;

    //notification
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();

    private static final Map<String, String> roleMappings = new HashMap<>();
    static {
        roleMappings.put("ROLE_AGRICULTEUR", "Agriculteur");
        roleMappings.put("ROLE_FOURNISSEUR", "Fournisseur");
        roleMappings.put("ROLE_CLIENT", "Client");
        roleMappings.put("ROLE_EMPLOYE", "Employé");
    }


    private LocalDateTime reactivationDate;

    public void initialize() {
        // Récupérer  l'utilisateur connecté
        SessionManager session = SessionManager.getInstance();

        if (session != null) {
            if (session.getImage() != null && !session.getImage().isEmpty()) {
                loadImage(session.getImage());
            }

            fullNameLabel.setText(session.getFirstName() + " " + session.getLastName());
            emailLabel.setText(session.getEmail());
            adresseLabel.setText(session.getAdresse());
            numTelLabel.setText(String.valueOf(session.getNumTel()));
            statusLabel.setText(session.getStatus());
            updateStatus(session.getStatus());
            LocalDateTime dateCreation = session.getDateCreation();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            String formattedDate = dateCreation.format(formatter);

            joinDateLabel.setText(formattedDate);
            if (session.getDateNaissance() != null) {
                dateNaissanceLabel.setText(session.getDateNaissance().toString());
            } else {
                dateNaissanceLabel.setText("Date de naissance: Non renseignée");
            }
            String rolesJson = session.getRoles();
            System.out.println("Roles JSON: " + rolesJson);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> rolesList = objectMapper.readValue(rolesJson, List.class);
                System.out.println("Roles List: " + rolesList);


                List<String> readableRoles = new ArrayList<>();
                for (String role : rolesList) {
                    String readableRole = roleMappings.getOrDefault(role, role);
                    readableRoles.add(readableRole);
                }

                rolesLabel.setText(String.join(", ", readableRoles));
            } catch (Exception e) {
                e.printStackTrace();
                rolesLabel.setText("Erreur de chargement des rôles");
            }


        }

        // Ajouter des gestionnaires d'événements pour les boutons
        modifierButton.setOnAction(event -> handleModifier());
        destinataireButton.setOnAction(event -> handleDestinataire());
        supprimerButton.setOnAction(event -> handleSupprimer());
        // Gestion des événements dynamiques pour les boutons (survol et clic)
        addDynamicButtonStyle(modifierButton);
        addDynamicButtonStyle(destinataireButton);
        addDynamicButtonStyle(supprimerButton);


    }

    private void addDynamicButtonStyle(Button button) {
        // Lorsque le bouton est survolé
        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: #f6c23e; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-radius: 20; -fx-background-radius: 20;");
        });

        // Lorsque la souris quitte le bouton
        button.setOnMouseExited(event -> {
            button.setStyle("-fx-background-color: white; -fx-border-color: #f6c23e; -fx-text-fill: #f6c23e; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-radius: 20; -fx-background-radius: 20;");
        });

        // Lorsque le bouton est pressé (clic)
        button.setOnMousePressed(event -> {
            button.setStyle("-fx-background-color: #f6c23e; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-radius: 20; -fx-background-radius: 20;");
        });

        // Lorsque le bouton est relâché
        button.setOnMouseReleased(event -> {
            button.setStyle("-fx-background-color: white; -fx-border-color: #f6c23e; -fx-text-fill: #f6c23e; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-radius: 20; -fx-background-radius: 20;");
        });
    }
    private void updateStatus(String status) {
        statusLabel.setText(status);

        switch (status.toLowerCase()) {
            case "en attente de validation":
                statusCircle.setFill(Color.ORANGE);
                break;
            case "actif":
                statusCircle.setFill(Color.GREEN);
                break;
            case "désactivé":
                statusCircle.setFill(Color.RED);
                break;
            default:
                statusCircle.setFill(Color.GRAY);
        }
    }

    // Méthode pour charger l'image
    private void loadImage(String imageUrl) {
        // Créer une tâche pour charger l'image depuis l'URL Cloudinary
        Task<Image> loadImageTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                return new Image(imageUrl); // Charger l'image depuis l'URL Cloudinary
            }
        };


        loadImageTask.setOnSucceeded(event -> {
            Image image = loadImageTask.getValue();
            profileImage.setImage(image);
            profileImage.setFitHeight(150);
            profileImage.setFitWidth(150);

            Circle clip = new Circle(75, 75, 75); // Rayon = moitié de la largeur/hauteur
            profileImage.setClip(clip);
        });

        loadImageTask.setOnFailed(event -> {
            profileImage.setImage(null);
        });

        new Thread(loadImageTask).start();
    }

    private void handleModifier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/EditUserFront.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier le profil");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(modifierButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root, 1000, 600));

            dialogStage.showAndWait();

            showSuccessNotification("Profil mis à jour avec succès");
            initialize(); // Recharge les données

        } catch (IOException e) {
            showErrorNotification("Erreur lors de l'ouverture de l'éditeur");
            e.printStackTrace();
        }
    }

    private void handleDestinataire() {
        List<String> options = List.of("7 jours", "1 mois", "3 mois", "6 mois");

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("🌿 Suspension temporaire");
        dialog.setHeaderText("Choisissez la période de suspension de votre compte.");
        dialog.getDialogPane().setStyle("-fx-background-color: #e9f5e9; -fx-font-family: 'Segoe UI';");

        ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(options);
        comboBox.setValue("7 jours");
        comboBox.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-font-size: 14px;");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(new Label("Période de suspension souhaitée :"), comboBox);
        dialog.getDialogPane().setContent(content);

        // Définir l’action au clic sur "Confirmer"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return comboBox.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(choice -> {
            LocalDateTime reactivationDate = LocalDateTime.now();

            switch (choice) {
                case "7 jours" -> reactivationDate = reactivationDate.plus(7, ChronoUnit.DAYS);
                case "1 mois" -> reactivationDate = reactivationDate.plus(1, ChronoUnit.MONTHS);
                case "3 mois" -> reactivationDate = reactivationDate.plus(3, ChronoUnit.MONTHS);
                case "6 mois" -> reactivationDate = reactivationDate.plus(6, ChronoUnit.MONTHS);
            }

            UtilisateurService userService = new UtilisateurService();
            userService.updateReactivationDate(SessionManager.getInstance().getId(), reactivationDate);
            SessionManager.getInstance().setReactivationDate(reactivationDate);

            showSuccessNotification("✅ Compte désactivé\nRéactivation prévue le : " +
                    reactivationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> {
                SessionManager.logout();
                redirectToLogin();
            });
            delay.play();
        });
    }


    private void handleSupprimer() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("❗ Confirmation de suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce profil ?");
        alert.setContentText("Cette action est irréversible.");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #e9f5e9; -fx-font-family: 'Segoe UI';");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #c8e6c9;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #2d572c; -fx-font-size: 14px;");
        dialogPane.lookupButton(ButtonType.OK).setStyle("-fx-background-color: #388e3c; -fx-text-fill: white;");
        dialogPane.lookupButton(ButtonType.CANCEL).setStyle("-fx-background-color: #a5d6a7; -fx-text-fill: white;");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.initOwner(profileImage.getScene().getWindow());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionManager.logout();
                showSuccessNotification("Profil supprimé avec succès");

                PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                delay.setOnFinished(e -> redirectToLogin());
                delay.play();
            }
        });
    }


    @FXML
    public void handleLogOut(ActionEvent actionEvent) throws IOException {
        SessionManager.logout();
        showSuccessNotification("Déconnexion réussie");

        // Redirection après un délai
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(e -> redirectToLogin());
        delay.play();
    }

    private void redirectToLogin() {
        try {
            Stage currentStage = (Stage) profileImage.getScene().getWindow();

            double currentWidth = currentStage.getScene().getWidth();
            double currentHeight = currentStage.getScene().getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/login.fxml"));
            Parent root = loader.load();

            Scene loginScene = new Scene(root, currentWidth, currentHeight);

            currentStage.setScene(loginScene);
            currentStage.show();

        } catch (IOException e) {
            showErrorNotification("Erreur lors de la redirection vers la page de connexion.");
            e.printStackTrace();
        }
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
            AnchorPane root = (AnchorPane) profileImage.getScene().getRoot();

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
            AnchorPane root = (AnchorPane) profileImage.getScene().getRoot();
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


  //des effets de survol pour le bouton
  public void handleButtonHover(MouseEvent event) {
      Button button = (Button) event.getSource();
      button.setEffect(new DropShadow(10, Color.DARKGRAY));
  }

    public void handleButtonLeave(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setEffect(null);
    }

    // Gestion des effets de survol pour la carte (VBox)
    public void handleCardHover(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        card.setEffect(new DropShadow(15, Color.DARKGRAY));
        card.setStyle(card.getStyle() + "-fx-translate-y: -2;");
    }

    public void handleCardLeave(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        card.setEffect(new DropShadow(5, Color.DARKGRAY));
        card.setStyle(card.getStyle() + "-fx-translate-y: 0;");
    }

    //  des effets de survol pour l'en-tête
//    public void handleHeaderHover(MouseEvent event) {
//        HBox header = (HBox) event.getSource();
//        // Animation pour l'en-tête, vous pouvez ajouter une animation ici, comme un zoom ou une couleur changeante
//        header.setStyle("-fx-background-color: #D3D3D3;");
//    }

//    public void handleHeaderLeave(MouseEvent event) {
//        HBox header = (HBox) event.getSource();
//        // Fin de l'animation pour l'en-tête, revenir à l'état initial
//        header.setStyle("-fx-background-color: transparent;");
//    }
}
