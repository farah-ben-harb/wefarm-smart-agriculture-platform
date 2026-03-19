package controllers.OffreEmploi;

import entities.Candidature;
import entities.OffreEmploi;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.ServiceCandidature;
import services.ServiceOffreEmploi;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

public class MesCandidaturesController implements Initializable {
    @FXML private TableView<Candidature> candidaturesTable;
    @FXML private TableColumn<Candidature, String> offreColumn;
    @FXML private TableColumn<Candidature, String> dateColumn;
    @FXML private TableColumn<Candidature, String> etatColumn;
    @FXML private TableColumn<Candidature, Void> competencesColumn;
    @FXML private TableColumn<Candidature, Void> actionsColumn;

    // Nouveaux éléments pour le calendrier
    @FXML private StackPane contentPane;
    @FXML private VBox tableView;
    @FXML private VBox calendarView;
    @FXML private GridPane calendarGrid;
    @FXML private Label moisAnneeLabel;
    private YearMonth currentYearMonth;

    @FXML private VBox statsBox;
    @FXML private Label totalLabel;
    @FXML private Label accepteesLabel;
    @FXML private Label enAttenteLabel;
    @FXML private HBox toggleViewBox;
    @FXML private Label toggleViewLabel;
    @FXML private ImageView toggleViewIcon;

    private SessionManager session = SessionManager.getInstance();
    private final int userId = session.getId();

    private ServiceCandidature serviceCandidature = new ServiceCandidature();
    private ServiceOffreEmploi serviceOffreEmploi = new ServiceOffreEmploi();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        candidaturesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        candidaturesTable.getStylesheets().add(getClass().getResource("/css/stylesOffresEmploi.css").toExternalForm());
        setupTableColumns();
        loadCandidatures();

        // Initialisation du calendrier
        currentYearMonth = YearMonth.now();
        if (calendarGrid != null) {
            updateCalendar();
        }

        // Configurer l'icône initiale
        toggleViewIcon.setVisible(true); // Visible par défaut (mode tableau)
        updateToggleViewIcon();
    }
    @FXML
    private void toggleView() {
        if (tableView.isVisible()) {
            afficherCalendrier(); // Cache l'icône automatiquement
        } else {
            retourTableau(); // Réaffiche l'icône automatiquement
        }
    }
    private void playTransition(boolean showCalendar) {
        if (showCalendar) {
            // Animation pour afficher le calendrier
            calendarView.setOpacity(0);
            calendarView.setTranslateX(50);
            calendarView.setVisible(true);

            ParallelTransition parallelTransition = new ParallelTransition();

            // Fade-in + slide-in pour le calendrier
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), calendarView);
            fadeIn.setToValue(1);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), calendarView);
            slideIn.setToX(0);

            // Fade-out + slide-out pour le tableau
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), tableView);
            fadeOut.setToValue(0);

            TranslateTransition slideOut = new TranslateTransition(Duration.millis(500), tableView);
            slideOut.setToX(-50);

            parallelTransition.getChildren().addAll(fadeIn, slideIn, fadeOut, slideOut);
            parallelTransition.play();

            parallelTransition.setOnFinished(e -> {
                tableView.setVisible(false);
                tableView.setOpacity(1);
                tableView.setTranslateX(0);
            });
        } else {
            // Animation pour revenir au tableau
            tableView.setOpacity(0);
            tableView.setTranslateX(-50);
            tableView.setVisible(true);

            ParallelTransition parallelTransition = new ParallelTransition();

            // Fade-in + slide-in pour le tableau
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), tableView);
            fadeIn.setToValue(1);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), tableView);
            slideIn.setToX(0);

            // Fade-out + slide-out pour le calendrier
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), calendarView);
            fadeOut.setToValue(0);

            TranslateTransition slideOut = new TranslateTransition(Duration.millis(500), calendarView);
            slideOut.setToX(50);

            parallelTransition.getChildren().addAll(fadeIn, slideIn, fadeOut, slideOut);
            parallelTransition.play();

            parallelTransition.setOnFinished(e -> {
                calendarView.setVisible(false);
                calendarView.setOpacity(1);
                calendarView.setTranslateX(0);
            });
        }
    }


    /* ********************** MISE EN PLACE DU TABLEAU **************************** */
    private void setupTableColumns() {
        // Colonne Offre
        offreColumn.setCellValueFactory(cellData -> {
            Candidature c = cellData.getValue();
            String titre = (String) c.getMetadata().getOrDefault("offre_titre", "Offre inconnue");
            return new SimpleStringProperty(titre);
        });

        // Colonne Date
        dateColumn.setCellValueFactory(cellData -> {
            Candidature c = cellData.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return new SimpleStringProperty(c.getDateCandidature().format(formatter));
        });

        // Colonne Etat
        etatColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        etatColumn.setCellFactory(column -> new TableCell<Candidature, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if ("en attente".equals(item)) {
                        setTextFill(Color.BLACK);
                    } else if ("acceptée".equals(item)) {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });

        // Colonne Compétences (tags)
        competencesColumn.setCellFactory(column -> new TableCell<Candidature, Void>() {
            private final FlowPane flowPane = new FlowPane();
            {
                flowPane.setHgap(5);
                flowPane.setVgap(5);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    flowPane.getChildren().clear();
                    Candidature candidature = getTableView().getItems().get(getIndex());
                    String[] competences = candidature.getCompetences().split(",");
                    for (String competence : competences) {
                        Label tag = new Label(competence.trim());
                        tag.setStyle("-fx-background-color: #00B074; -fx-text-fill: white; -fx-padding: 3 7 3 7; -fx-background-radius: 10;");
                        flowPane.getChildren().add(tag);
                    }
                    setGraphic(flowPane);
                }
            }
        });

        // Colonne Actions
        actionsColumn.setCellFactory(column -> new TableCell<Candidature, Void>() {
            private final Button detailsBtn = new Button("Voir les détails de l'offre");
            private final Button annulerBtn = new Button("❌");
            private final HBox pane = new HBox(5, detailsBtn, annulerBtn);

            {
                pane.setAlignment(Pos.CENTER);
                detailsBtn.setStyle("-fx-background-color: #158d54; -fx-text-fill: white;");
                annulerBtn.setStyle("-fx-background-color: Red; -fx-text-fill: white;");

                detailsBtn.setOnAction(event -> {
                    Candidature candidature = getTableView().getItems().get(getIndex());
                    afficherDetailsOffre(candidature.getOffreId());
                });

                annulerBtn.setOnAction(event -> {
                    Candidature candidature = getTableView().getItems().get(getIndex());
                    annulerCandidature(candidature);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Candidature candidature = getTableView().getItems().get(getIndex());

                    // Réinitialiser le HBox à chaque mise à jour
                    pane.getChildren().clear();
                    pane.getChildren().add(detailsBtn);

                    // Ajouter le bouton annuler seulement si l'état est "en attente"
                    if ("en attente".equalsIgnoreCase(candidature.getEtat())) {
                        pane.getChildren().add(annulerBtn);
                    }

                    setGraphic(pane);
                }
            }
        });
    }


    /* ********************** GESTION DU CALENDRIER **************************** */
    @FXML
    private void afficherCalendrier() {
        playTransition(true);
        tableView.setVisible(false);
        calendarView.setVisible(true);
        toggleViewIcon.setVisible(false); // Cacher l'icône
        toggleViewLabel.setVisible(false); // Optionnel : vider le texte
        updateCalendar();
    }


    @FXML
    private void retourTableau() {
        playTransition(false);
        tableView.setVisible(true);
        calendarView.setVisible(false);
        toggleViewIcon.setVisible(true); // Réafficher l'icône
        toggleViewLabel.setVisible(true); // Optionnel : remettre le texte
    }

    @FXML
    private void moisPrecedent() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void moisSuivant() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        // Remplir les jours
        LocalDate date = currentYearMonth.atDay(1);
        int row = 1;
        while (date.getMonth() == currentYearMonth.getMonth()) {
            int col = date.getDayOfWeek().getValue() - 1; // Lundi=0

            StackPane dayCell = new StackPane();
            dayCell.setAlignment(Pos.TOP_RIGHT);
            dayCell.getStyleClass().add("calendar-day");

            // Conteneur pour le numéro du jour
            HBox dayContent = new HBox();
            dayContent.setAlignment(Pos.TOP_RIGHT);
            dayContent.setPadding(new Insets(2, 5, 0, 0));

            Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
            dayNumber.setStyle("-fx-font-size: 10px;");

            try {
                LocalDate finalDate1 = date;
                List<Candidature> candidatures = serviceCandidature.getCandidaturesByUser(userId)
                        .stream()
                        .filter(c -> c.getDateCandidature().toLocalDate().equals(finalDate1))
                        .collect(Collectors.toList());

                if (!candidatures.isEmpty()) {
                    Candidature candidature = candidatures.get(0);

                    ImageView statusIcon = new ImageView();
                    statusIcon.setFitWidth(16);
                    statusIcon.setFitHeight(16);
                    statusIcon.setPreserveRatio(true);
                    statusIcon.setSmooth(true);

                    // Création du Tooltip
                    Tooltip tooltip = new Tooltip();
                    StringBuilder tooltipContent = new StringBuilder();

                    // Style du Tooltip
                    tooltip.setStyle("-fx-font-size: 12px; -fx-text-fill: #5a5c69;");

                    if (candidature.getEtat().equalsIgnoreCase("acceptée")) {
                        statusIcon.setImage(new Image(getClass().getResourceAsStream("/icons/accepte.png")));
                        dayCell.setStyle("-fx-background-color: rgba(28, 200, 138, 0.2);");
                        tooltipContent.append("Candidature acceptée\n");
                    } else if (candidature.getEtat().equalsIgnoreCase("en attente")) {
                        statusIcon.setImage(new Image(getClass().getResourceAsStream("/icons/en-attente.png")));
                        dayCell.setStyle("-fx-background-color: rgba(255, 193, 7, 0.2);");
                        tooltipContent.append("Candidature en attente\n");
                    }

                    // Ajouter les détails des candidatures au Tooltip
                    for (Candidature c : candidatures) {
                        tooltipContent.append("• ")
                                .append(c.getMetadata().get("offre_titre"))
                                .append("\n");
                    }

                    tooltip.setText(tooltipContent.toString());
                    Tooltip.install(dayCell, tooltip);

                    // Ajouter l'icône à côté du numéro plutôt que par-dessus
                    dayContent.getChildren().addAll(dayNumber, statusIcon);
                } else {
                    dayContent.getChildren().add(dayNumber);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                dayContent.getChildren().add(dayNumber);
            }

            dayCell.getChildren().add(dayContent);

            // Garder le clic pour plus de détails
            LocalDate finalDate = date;
            dayCell.setOnMouseClicked(e -> showDayDetails(finalDate));
            calendarGrid.add(dayCell, col, row);

            date = date.plusDays(1);
            if (date.getDayOfWeek() == DayOfWeek.MONDAY) {
                row++;
            }
        }

        moisAnneeLabel.setText(currentYearMonth.getMonth().toString() + " " + currentYearMonth.getYear());
        highlightToday();
        updateStatsSummary();
    }
    private void updateStatsSummary() {
        try {
            List<Candidature> candidatures = serviceCandidature.getCandidaturesByUserAndMonth(
                    userId,
                    currentYearMonth.getMonthValue(),
                    currentYearMonth.getYear()
            );

            long total = candidatures.size();
            long acceptees = candidatures.stream().filter(c -> c.getEtat().equalsIgnoreCase("acceptée")).count();
            long enAttente = candidatures.stream().filter(c -> c.getEtat().equalsIgnoreCase("en attente")).count();

            totalLabel.setText("Total: " + total);
            accepteesLabel.setText("Acceptées: " + acceptees);
            enAttenteLabel.setText("En attente: " + enAttente);

            // Afficher le résumé seulement s'il y a des candidatures
            statsBox.setVisible(total > 0);

        } catch (SQLException e) {
            e.printStackTrace();
            statsBox.setVisible(false);
        }
    }
    private void showDayDetails(LocalDate date) {
        try {
            List<Candidature> candidatures = serviceCandidature.getCandidaturesByUser(userId)
                    .stream()
                    .filter(c -> c.getDateCandidature().toLocalDate().equals(date))
                    .collect(Collectors.toList());

            if (candidatures.isEmpty()) {
                showInfoAlert("Aucune candidature pour le " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                return;
            }

            // Créer une boîte de dialogue personnalisée
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Candidatures - " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Style CSS
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/css/stylesOffresEmploi.css").toExternalForm());
            dialogPane.getStyleClass().add("event-dialog");

            // Contenu
            VBox content = new VBox(10);
            content.setPadding(new Insets(10));

            for (Candidature c : candidatures) {
                HBox eventBox = new HBox(10);
                eventBox.setAlignment(Pos.CENTER_LEFT);

                // Icône selon l'état
                ImageView icon = new ImageView();
                icon.setFitWidth(20);
                icon.setFitHeight(20);

                String etat = c.getEtat().toLowerCase();
                if (etat.contains("acceptée")) {
                    icon.setImage(new Image(getClass().getResourceAsStream("/icons/accepte.png")));
                } else if (etat.contains("attente")) {
                    icon.setImage(new Image(getClass().getResourceAsStream("/icons/en-attente.png")));
                }

                // Détails de l'offre
                Label eventLabel = new Label(c.getMetadata().get("offre_titre") + " (" + c.getEtat() + ")");
                eventLabel.setStyle("-fx-text-fill: #5a5c69;");

                eventBox.getChildren().addAll(icon, eventLabel);
                content.getChildren().add(eventBox);
            }

            dialogPane.setContent(content);
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de la récupération des candidatures");
        }
    }

    /* ********************** METHODES UTILITAIRES **************************** */
    private void loadCandidatures() {
        try {
            List<Candidature> candidatures = serviceCandidature.getCandidaturesByUser(userId);
            ObservableList<Candidature> data = FXCollections.observableArrayList(candidatures);
            if (data.isEmpty()) {
                Label emptyLabel = new Label("Aucune candidature trouvée");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666; -fx-font-style: italic;");
                StackPane emptyPane = new StackPane(emptyLabel);
                emptyPane.setPadding(new Insets(20));
                candidaturesTable.setPlaceholder(emptyPane);
            }
            candidaturesTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Impossible de charger les candidatures");
        }
    }

    private void afficherDetailsOffre(int idOffre) {
        try {
            OffreEmploi offre = serviceOffreEmploi.getById(idOffre);
            if (offre == null) {
                showErrorAlert("Offre introuvable");
                return;
            }

            Stage currentStage = (Stage) candidaturesTable.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/detailsOffre.fxml"));
            AnchorPane page = loader.load();

            Stage detailsStage = new Stage();
            detailsStage.setTitle("Détails de l'offre: " + offre.getTitre());
            detailsStage.setScene(new Scene(page));
            detailsStage.setMaximized(true);

            DetailsOffreEmploiController controller = loader.getController();
            controller.setOffre(offre);
            controller.setDialogStage(detailsStage);

            currentStage.close();
            detailsStage.show();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showErrorAlert(e instanceof SQLException ?
                    "Erreur base de données: " + e.getMessage() : "Erreur de chargement de l'interface");
        }
    }

    private void annulerCandidature(Candidature candidature) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Confirmation de suppression");
            dialog.setHeaderText(null);

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/css/stylesOffresEmploi.css").toExternalForm());
            dialogPane.getStyleClass().add("confirmation-dialog");

            Text content = new Text("Êtes-vous sûr de vouloir annuler cette candidature ?\nCette action est irréversible.");
            content.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            content.setFill(Color.web("#333333"));

            VBox contentBox = new VBox(content);
            contentBox.setSpacing(10);
            dialogPane.setContent(contentBox);

            ButtonType nonButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType ouiButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
            dialogPane.getButtonTypes().addAll(nonButtonType, ouiButtonType);

            Button ouiButton = (Button) dialogPane.lookupButton(ouiButtonType);
            Button nonButton = (Button) dialogPane.lookupButton(nonButtonType);
            ouiButton.getStyleClass().add("confirm-button");
            nonButton.getStyleClass().add("cancel-button");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ouiButtonType) {
                boolean success = serviceCandidature.supprimerCandidature(candidature.getId());
                if (success) {
                    candidaturesTable.getItems().remove(candidature);
                    showSuccessAlert("Votre candidature a été supprimée avec succès !");
                } else {
                    throw new SQLException("La suppression n'a pas affecté de lignes");
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Une erreur est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ********************** NOTIFICATIONS **************************** */
    private void showSuccessAlert(String message) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/success.png")));
        icon.setFitWidth(30);
        icon.setFitHeight(30);

        Notifications.create()
                .title("Succès")
                .text(message)
                .graphic(icon)
                .hideAfter(Duration.seconds(3))
                .position(Pos.TOP_RIGHT)
                .show();
    }

    private void showErrorAlert(String message) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/error.png")));
        icon.setFitWidth(30);
        icon.setFitHeight(30);

        Notifications.create()
                .title("Erreur")
                .text(message)
                .graphic(icon)
                .hideAfter(Duration.seconds(4))
                .position(Pos.TOP_RIGHT)
                .show();
    }

    private void showInfoAlert(String message) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/error.png")));
        icon.setFitWidth(30);
        icon.setFitHeight(30);

        Notifications.create()
                .title("Information")
                .text(message)
                .graphic(icon)
                .hideAfter(Duration.seconds(3))
                .position(Pos.TOP_RIGHT)
                .show();
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffresEmploye.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Liste des Offres");
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Impossible de retourner à la liste des offres: " + e.getMessage());
        }
    }
    public void highlightToday() {
        LocalDate today = LocalDate.now();

        for (Node node : calendarGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane dayCell = (StackPane) node;
                if (!dayCell.getChildren().isEmpty() && dayCell.getChildren().get(0) instanceof Label) {
                    Label dayLabel = (Label) dayCell.getChildren().get(0);
                    if (!dayLabel.getText().isEmpty()) {
                        int day = Integer.parseInt(dayLabel.getText());
                        if (day == today.getDayOfMonth()) {
                            // Créer un fond arrondi
                            Rectangle background = new Rectangle(35, 35);
                            background.setArcWidth(20);
                            background.setArcHeight(20);
                            background.setFill(Color.TRANSPARENT);
                            background.setStroke(Color.web("#e74a3b"));
                            background.setStrokeWidth(2);

                            dayCell.getChildren().add(0, background);
                        }
                    }
                }
            }
        }
    }
    private void updateToggleViewIcon() {
        boolean isTableViewVisible = tableView.isVisible();
        toggleViewIcon.setVisible(isTableViewVisible);
        toggleViewLabel.setVisible(isTableViewVisible);

        if (isTableViewVisible) {
            toggleViewLabel.setText("Vue Calendrier");
            toggleViewLabel.setStyle("-fx-border-color: transparent");

        }
    }
}