package controllers.User;

import entities.Activites;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ActivitiesService;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ActivitiesController implements Initializable {
    @FXML private TableView<Activites> tableViewActivities;
    @FXML private TableColumn<Activites, Integer> colId;
    @FXML private TableColumn<Activites, Integer> colUserId;
    @FXML private TableColumn<Activites, String> colAction;
    @FXML private TableColumn<Activites, String> colMetadata;
    @FXML private TableColumn<Activites, Date> colDate;
    @FXML private Button retour;
    @FXML private TableColumn<Activites, Void> colActions;
    @FXML private TextField searchField;


    private final ActivitiesService activitesService = new ActivitiesService();
    private final ObservableList<Activites> activitesList = FXCollections.observableArrayList();
    private FilteredList<Activites> filteredData;

    //notification
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableView();
        loadActivites();
        setupSearch(); //initialiser la recherche

    }

    private void setupTableView() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colMetadata.setCellValueFactory(new PropertyValueFactory<>("metaData"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");

            {
                deleteBtn.getStyleClass().add("delete-button");
                deleteBtn.setOnAction(event -> {
                    Activites activite = getTableView().getItems().get(getIndex());
                    supprimerActivite(activite);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });


        colDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM", item));
            }
        });
    }

    //recherche en temps reel
    private void setupSearch() {
        // 1. Envelopper la ObservableList dans un FilteredList
        filteredData = new FilteredList<>(activitesList, p -> true);

        // 2. Écouter les changements du champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(activite -> {
                // Si le champ de recherche est vide, afficher tous les éléments
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Convertir en minuscules pour une recherche insensible à la casse
                String lowerCaseFilter = newValue.toLowerCase();

                // Vérifier si l'un des champs contient la recherche
                if (activite.getAction().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (activite.getMetaData().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM", activite.getDate())
                        .toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        // 3. Envelopper le FilteredList dans un SortedList
        SortedList<Activites> sortedData = new SortedList<>(filteredData);

        // 4. Lier le SortedList au comparateur du TableView
        sortedData.comparatorProperty().bind(tableViewActivities.comparatorProperty());

        // 5. Ajouter les données triées et filtrées au TableView
        tableViewActivities.setItems(sortedData);
    }



    //charger les activities
    private void loadActivites() {
        activitesList.setAll(activitesService.getAllActivites());
        tableViewActivities.setItems(activitesList);
    }


    @FXML
    public void retourAdmin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/user/admin.fxml"));

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            Scene adminScene = new Scene(root, width, height);

            currentStage.setScene(adminScene);
            currentStage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du retour à l'interface admin: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void supprimerActivite(Activites activite) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer cette activité ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (activitesService.supprimerActivite(activite.getId())) {
                activitesList.remove(activite);
                showSuccessNotification("Activité supprimée avec succès");
            } else {
                showErrorNotification("Échec de la suppression de l'activité");
            }
        }
    }



    @FXML
    private void handleStatsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/StatsActivity.fxml"));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(loader.load());
            stage.setScene(scene);

            stage.setWidth(720);
            stage.setHeight(520);

            stage.setTitle("Statistiques des Activités");
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir les statistiques !");
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
            AnchorPane root = (AnchorPane) tableViewActivities.getScene().getRoot();

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
            AnchorPane root = (AnchorPane) tableViewActivities.getScene().getRoot();
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}