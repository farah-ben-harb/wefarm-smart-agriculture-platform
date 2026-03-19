package controllers.OffreEmploi;

import entities.Candidature;
import entities.OffreEmploi;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.ServiceCandidature;
import utils.ArabicTranslator;
import utils.SessionManager;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import java.net.URI;
public class DetailsOffreEmploiController {

    @FXML public Button retourButton;
    @FXML public Button postulerButton;
    @FXML public Label nbPostesTextLabel;
    @FXML public Label salaireProposeLabel;
    @FXML public Label descriptionPosteLabel;
    @FXML public Label competencesRequisesLabel;
    @FXML public Label dateDebutTextLabel;
    @FXML public Label dateFinTextLabel;
    @FXML public VBox conseilsSanteBox;
    @FXML private Label titreLabel;
    @FXML private Label datePublicationLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label nbPostesLabel;
    @FXML private Label salaireLabel;
    @FXML private Label dateDebutLabel;
    @FXML private Label dateFinLabel;
    @FXML private Label localisationLabel;
    @FXML private FlowPane competencesPane;

    //Pour le pop up conseils
    private static Stage conseilPopup;
    private static String currentAdvice;
    private static boolean isArabicMode;

    private OffreEmploi offre;
    private Stage dialogStage;
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();

    private SessionManager session = SessionManager.getInstance();
    private final int userId = session.getId();

    // Configuration API  nutritionix
    private static final String NUTRITIONIX_API_KEY = "06e43b4f3dc54c79570b5b8389ad2143";
    private static final String NUTRITIONIX_APP_ID = "f96f3eaa";
    private static final String NUTRITIONIX_API_URL = "https://trackapi.nutritionix.com/v2/natural/exercise";

    //Traduction
    private boolean isArabic ;
    public void setArabic(boolean isArabic) {
        this.isArabic = isArabic;
        if (offre != null) {  // Si l'offre est déjà chargée
            remplirDetails();  // Recharge avec la nouvelle langue
        }
    }

    // Méthode pour obtenir les conseils santé
    private void getHealthAdvice(String jobTitle) {
        String activity = mapJobToActivity(jobTitle);

        if (activity == null || activity.isEmpty()) {
            conseilsSanteBox.setVisible(false);
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            String requestBody = String.format("{\"query\":\"%s for 8 hours\"}", activity);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(NUTRITIONIX_API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-app-id", NUTRITIONIX_APP_ID)
                    .header("x-app-key", NUTRITIONIX_API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        Optional<Double> calories = parseCaloriesFromResponse(response);
                        javafx.application.Platform.runLater(() -> {
                            if (calories.isPresent()) {
                                String advice = generateHealthAdvice(calories.get(), isArabic);
                                currentAdvice = advice;
                                isArabicMode = isArabic;

                                // Afficher automatiquement le popup
                                showConseilPopup();
                            } else {
                                conseilsSanteBox.setVisible(false);
                            }
                        });
                    });
        } catch (Exception e) {
            conseilsSanteBox.setVisible(false);
        }
    }
    private void showConseilPopup() {
        if (conseilPopup == null) {
            conseilPopup = new Stage();
            conseilPopup.initStyle(StageStyle.UTILITY);
            conseilPopup.setAlwaysOnTop(true);
            conseilPopup.setTitle(isArabicMode ? "نصائح صحية" : "Conseils Santé"); // Titre de la fenêtre
            conseilPopup.setOnCloseRequest(event -> {
                conseilPopup.close();
                conseilPopup = null;
            });
        }

        // Positionnement en haut à droite (plus compact)
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        conseilPopup.setX(screenBounds.getMaxX() - 320); // Largeur réduite
        conseilPopup.setY(screenBounds.getMinY() + 50);

        VBox content = new VBox(10); // Espacement réduit entre éléments
        content.setPadding(new Insets(15)); // Padding réduit
        content.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #28a745;");

        // En-tête plus compact
        HBox header = new HBox(8);
        try {
            ImageView healthIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/sante.png")));
            healthIcon.setFitWidth(20); // Taille icône réduite
            healthIcon.setFitHeight(20);
            header.getChildren().add(healthIcon);
        } catch (Exception e) {
            System.out.println("Icône non trouvée");
        }

        Label title = new Label(isArabicMode ? "توصيات صحية" : "Recommandations");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745; -fx-font-size: 14px;"); // Taille police réduite

        header.getChildren().add(title);
        header.setAlignment(Pos.CENTER_LEFT);

        // Zone de texte compacte
        Text adviceText = new Text(currentAdvice);
        adviceText.setStyle("-fx-font-size: 13px;"); // Taille police réduite
        adviceText.setWrappingWidth(280); // Largeur réduite pour forcer le retour à la ligne

        content.getChildren().addAll(header, new Separator(), adviceText);

        Scene scene = new Scene(content, 300, 140);
        conseilPopup.setScene(scene);

        if (isArabicMode) {
            content.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            title.setStyle(title.getStyle() + "-fx-font-family: 'Arial Unicode MS';");
            adviceText.setStyle(adviceText.getStyle() + "-fx-font-family: 'Arial Unicode MS';");
        }

        conseilPopup.show();
    }

    private void closeConseilPopup() {
        if (conseilPopup != null) {
            conseilPopup.close();
            conseilPopup = null;
        }
    }

    // Méthode pour parser la réponse sans bibliothèque JSON
    private Optional<Double> parseCaloriesFromResponse(String jsonResponse) {
        try {
            //System.out.println("Parsing JSON: " + jsonResponse);

            int caloriesIndex = jsonResponse.indexOf("\"nf_calories\":");
            if (caloriesIndex == -1) {
                System.err.println("Champ nf_calories non trouvé");
                return Optional.empty();
            }

            int startIndex = jsonResponse.indexOf(":", caloriesIndex) + 1;
            int endIndex = jsonResponse.indexOf(",", startIndex);
            if (endIndex == -1) endIndex = jsonResponse.indexOf("}", startIndex);

            String caloriesStr = jsonResponse.substring(startIndex, endIndex).trim();
            //System.out.println("Calories extraites: " + caloriesStr);

            return Optional.of(Double.parseDouble(caloriesStr));
        } catch (Exception e) {
            //System.err.println("Erreur parsing JSON: " + e.getMessage());
            return Optional.empty();
        }
    }

    // Méthode pour mapper les métiers aux activités
    private String mapJobToActivity(String jobTitle) {
        jobTitle = jobTitle.toLowerCase().trim();

        // Agriculture et récolte
        if (jobTitle.contains("récolte") || jobTitle.contains("cueillette"))
            return "harvesting crops";
        if (jobTitle.contains("plantation") || jobTitle.contains("repiquage"))
            return "planting trees";
        if (jobTitle.contains("jardinage") || jobTitle.contains("entretien jardin"))
            return "gardening";
        if (jobTitle.contains("élevage") || jobTitle.contains("soins animaux"))
            return "animal farming";

        // Travail du sol
        if (jobTitle.contains("labour") || jobTitle.contains("labourage") || jobTitle.contains("bêchage"))
            return "plowing fields";
        if (jobTitle.contains("sarclage") || jobTitle.contains("désherbage"))
            return "weeding";

        // Entretien général
        if (jobTitle.contains("entretien") || jobTitle.contains("maintenance")) {
            if (jobTitle.contains("verger")) return "orchard maintenance";
            if (jobTitle.contains("vigne")) return "vineyard maintenance";
            if (jobTitle.contains("espace vert")) return "landscaping maintenance";
            return "general maintenance work";
        }

        // Pépinière et serres
        if (jobTitle.contains("pépinière") || jobTitle.contains("nursery"))
            return "nursery work";
        if (jobTitle.contains("serre") || jobTitle.contains("greenhouse"))
            return "greenhouse work";

        // Construction et manutention
        if (jobTitle.contains("construction") || jobTitle.contains("bâtiment"))
            return "construction work";
        if (jobTitle.contains("manutention") || jobTitle.contains("travail physique"))
            return "manual labor";

        // Autres activités spécifiques
        if (jobTitle.contains("taille") || jobTitle.contains("élagage"))
            return "tree pruning";
        if (jobTitle.contains("irrigation") || jobTitle.contains("arrosage"))
            return "watering plants";
        if (jobTitle.contains("semis") || jobTitle.contains("ensemencement"))
            return "sowing seeds";

        return null;
    }

    // Méthode pour générer le conseil santé
    private String generateHealthAdvice(double calories, boolean isArabic) {
        double water = Math.max(2.0, calories / 1000); // Au moins 2L

        if (isArabic) {
            return String.format("هذا العمل يتطلب حوالي %.0f سعرة حرارية في اليوم. اشرب %.1f لتر ماء على الأقل.",
                    calories, water);
        } else {
            return String.format("Ce travail demande ~%.0f kcal/jour. Pensez à boire %.1fL d'eau minimum.",
                    calories, water);
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    //Récupérations des détails de l'offre
    private void remplirDetails() {
        // Titres statiques
        salaireProposeLabel.setText(isArabic ? "الراتب المقترح" : "Salaire proposé");
        descriptionPosteLabel.setText(isArabic ? "وصف الوظيفة" : "Description du poste");
        competencesRequisesLabel.setText(isArabic ? "المهارات المطلوبة" : "Compétences requises");
        nbPostesTextLabel.setText(isArabic ? "عدد المناصب" : "Nombre de postes");

        dateDebutTextLabel.setText(isArabic ? "تاريخ البدء" : "Date de début");
        dateFinTextLabel.setText(isArabic ? "تاريخ الانتهاء المتوقع" : "Date fin estimée");


        // Titre
        titreLabel.setText((isArabic ? ArabicTranslator.translateToArabic(offre.getTitre())
                : offre.getTitre().substring(0, 1).toUpperCase() + offre.getTitre().substring(1)));

        // Date de publication
        datePublicationLabel.setText((isArabic ? "تاريخ النشر: " : "Publié: ") + formatDate(offre.getDatePublication()));

        // Description
        descriptionArea.setText(isArabic ? ArabicTranslator.translateToArabic(offre.getDescription())
                : offre.getDescription());

        // Nombre de postes
        nbPostesLabel.setText(String.valueOf(offre.getNombrePostes()));

        // Salaire
        salaireLabel.setText((isArabic ? "الراتب : " : "Salaire : ") + offre.getSalaire() + (isArabic ? " دينار تونسي" : " TND"));

        // Dates
        dateDebutLabel.setText(formatDate(offre.getDateDebut()));
        dateFinLabel.setText(formatDate(offre.getDateFinEstimee()));

        // Localisation
        localisationLabel.setText(isArabic ? ArabicTranslator.translateToArabic(offre.getLocalisation())
                : offre.getLocalisation());

        // Compétences
        competencesPane.getChildren().clear();
        String[] competences = offre.getCompetencesRequises().split(",");
        for (String competence : competences) {
            String competenceText = isArabic ? ArabicTranslator.translateToArabic(competence.trim())
                    : competence.trim();
            Label tag = new Label(competenceText);
            tag.getStyleClass().add("skill-tag");

            if (isArabic) {
                tag.setStyle("-fx-font-family: 'Arial Unicode MS';");
            }

            competencesPane.getChildren().add(tag);
        }

        // Boutons
        retourButton.setText(isArabic ? "رجوع" : "Retour");
        postulerButton.setText(isArabic ? "تقديم" : "Postuler");
    }
    //Méthode pour le format des dates
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }
    //Méthode pour le retour
    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            String rawRole = SessionManager.getInstance().getRoles();
            String role = rawRole.replaceAll("[\\[\\]\"]", "");

            FXMLLoader loader;

            boolean wasPopupShowing = (conseilPopup != null && conseilPopup.isShowing());


            // Vérifier le rôle et charger la vue correspondante
            if ("ROLE_AGRICULTEUR".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffres.fxml"));
            } else if ("ROLE_EMPLOYE".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffresEmploye.fxml"));
            } else {
                System.out.println("Rôle non reconnu !");
                return;
            }

            // Charger la nouvelle scène
            Parent root = loader.load();

            // Passer l'état de la langue au contrôleur parent
            if (loader.getController() instanceof ListOffreEmploiEmployeController) {
                ((ListOffreEmploiEmployeController) loader.getController()).setArabic(isArabic);
            }

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle(isArabic ? "قائمة العروض" : "Liste des Offres");

            // Appliquer la direction RTL si arabe
            if (isArabic) {
                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            }
            if (!isArabic) {
                root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }


            // Ouvrir la scène en plein écran
            newStage.setMaximized(true);
            newStage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();


            // Réafficher le popup après la navigation si nécessaire
            if (wasPopupShowing && currentAdvice != null) {
                PauseTransition delay = new PauseTransition(Duration.millis(100));
                delay.setOnFinished(e -> showConseilPopup());
                delay.play();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert(isArabic ? "خطأ في تحميل الصفحة السابقة" : "Erreur lors du chargement de la page précédente");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    //Méthode pour ouvrir le modal pour postuler
    public void ouvrirModalPostuler(OffreEmploi offre) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Postuler à l'offre");

        // Layout principal
        VBox modalLayout = new VBox(15);
        modalLayout.setPadding(new Insets(20));
        modalLayout.setStyle("-fx-background-color: #f8f9fa;");

        // Titre
        Label titreLabel = new Label("Postuler à : " + offre.getTitre().substring(0, 1).toUpperCase() + offre.getTitre().substring(1));
        titreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Conteneur pour ajout des compétences
        HBox addBox = new HBox(10);
        TextField competenceField = new TextField();
        competenceField.setPromptText("Compétences");
        Button addButton = new Button("+");
        addButton.setStyle("-fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30;");

        addBox.getChildren().addAll(competenceField, addButton);

        // ListView pour afficher les compétences
        ListView<String> competencesListView = new ListView<>();
        ObservableList<String> competencesList = FXCollections.observableArrayList();
        competencesListView.setItems(competencesList);
        competencesListView.setPrefHeight(120);
        competencesListView.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        // Menu contextuel pour suppression
        competencesListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Supprimer");
            deleteItem.setOnAction(event -> competencesList.remove(cell.getItem()));
            contextMenu.getItems().add(deleteItem);

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });

        // Message d'erreur
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setStyle("-fx-font-weight: bold;");

        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");

        Button submitButton = new Button("Envoyer");
        submitButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        buttonBox.getChildren().addAll(cancelButton, submitButton);

        // Ajout des éléments au layout
        modalLayout.getChildren().addAll(
                titreLabel,
                new Label("Compétences"),
                addBox,
                competencesListView,
                errorLabel,
                buttonBox
        );

        // Gestion des événements

        // Ajout par bouton
        addButton.setOnAction(e -> {
            addCompetence(competenceField, competencesList, errorLabel);
        });

        // Ajout par Entrée
        competenceField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                addCompetence(competenceField, competencesList, errorLabel);
            }
        });

        // Annulation
        cancelButton.setOnAction(e -> modal.close());

        // Soumission
        submitButton.setOnAction(e -> {
            if (competencesList.isEmpty()) {
                errorLabel.setText("Veuillez ajouter au moins une compétence");
                return;
            }

            try {
                String competences = String.join(", ", competencesList);
                if (serviceCandidature.existeCandidature(offre.getId(), userId)) {
                    errorLabel.setText("Vous avez déjà postulé à cette offre!");
                    return;
                }

                serviceCandidature.ajouter(new Candidature(offre.getId(), competences));
                modal.close();
                showSuccessAlert("Votre candidature a été soumise avec succès !");
            } catch (SQLException ex) {
                errorLabel.setText("Erreur: " + ex.getMessage());
            }
        });

        // Configuration de la scène
        Scene scene = new Scene(modalLayout, 400, 350);
        modal.setScene(scene);
        modal.showAndWait();
    }
    // Méthode pour initialiser l'offre
    public void setOffre(OffreEmploi offre) {
        this.offre = offre;

        String rawRole = SessionManager.getInstance().getRoles();
        String role = rawRole.replaceAll("[\\[\\]\"]", "");
        boolean isEmploye = "ROLE_EMPLOYE".equalsIgnoreCase(role);

        postulerButton.setVisible(isEmploye);

        if (isArabic) {
            applyRtlLayout();
            traduireInterface();
        }

        afficherDetailsEtBouton();

        // Afficher le conseil santé après un léger délai
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        delay.setOnFinished(e -> getHealthAdvice(offre.getTitre()));
        delay.play();
    }

    private void traduireInterface() {
        // Appliquer la direction RTL aux éléments principaux
        Node[] rtlNodes = {
                titreLabel, datePublicationLabel, descriptionArea,
                nbPostesLabel, salaireLabel, dateDebutLabel,
                dateFinLabel, localisationLabel, competencesPane,
                salaireProposeLabel, descriptionPosteLabel, competencesRequisesLabel,
                nbPostesTextLabel, dateDebutTextLabel, dateFinTextLabel
        };

        for (Node node : rtlNodes) {
            node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            if (node instanceof Label) {
                ((Label) node).setAlignment(Pos.CENTER_RIGHT);
            }
        }

        // Configuration du TextArea
        descriptionArea.setStyle("-fx-font-family: 'Arial Unicode MS'; -fx-text-alignment: right;");
    }

    private void applyRtlLayout() {
        // Appliquer la police arabe aux labels
        Label[] arabicLabels = {
                salaireProposeLabel, descriptionPosteLabel, competencesRequisesLabel,
                nbPostesTextLabel, dateDebutTextLabel, dateFinTextLabel
        };

        for (Label label : arabicLabels) {
            label.setStyle("-fx-font-family: 'Arial Unicode MS';");
        }

        // Configuration du TextArea
        descriptionArea.setStyle("-fx-font-family: 'Arial Unicode MS'; -fx-text-alignment: right;");

        // Boutons
        retourButton.setText("رجوع");
        postulerButton.setText("تقديم");
        retourButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        postulerButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    }
    private void afficherDetailsEtBouton() {
        // Récupérer le rôle
        String rawRole = SessionManager.getInstance().getRoles();
        String role = rawRole.replaceAll("[\\[\\]\"]", "");

        boolean isEmploye = "ROLE_EMPLOYE".equalsIgnoreCase(role);
        postulerButton.setVisible(isEmploye);

        if (isEmploye) {
            String statutOffre = offre.getStatus();  // ici, offre n'est plus null

            if ("actif".equalsIgnoreCase(statutOffre)) {
                postulerButton.setDisable(false);
                postulerButton.setStyle("-fx-background-color: #158d54; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(21,141,84,0.3), 5, 0, 0, 2);");
            } else {
                postulerButton.setDisable(true);
                postulerButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20; -fx-cursor: hand;");
            }
        }

        remplirDetails(); // appeler aussi ici pour afficher les détails
    }

    // Gestionnaire du bouton Postuler
    @FXML
    private void handlePostuler(ActionEvent event) {
        if (offre != null) {
            ouvrirModalPostuler(offre);
        }
    }


    // Méthode utilitaire pour ajouter une compétence
    private void addCompetence(TextField field, ObservableList<String> list, Label errorLabel) {
        String competence = field.getText().trim();
        if (!competence.isEmpty()) {
            list.add(competence);
            field.clear();
            errorLabel.setText("");
        } else {
            errorLabel.setText("Veuillez entrer une compétence valide");
        }
    }
    // Méthodes d'alerte améliorées
    // Nouvelle alerte de succès (toast notification)
    private void showSuccessAlert(String message) {
        ImageView icon = new ImageView(new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/success.png")));
        icon.setFitWidth(30);
        icon.setFitHeight(30);

        Notifications.create()
                .title("Succès")
                .text(message)
                .graphic(icon)
                .hideAfter(Duration.seconds(3)) // Se ferme automatiquement après 3 secondes
                .position(Pos.TOP_RIGHT) // Position en haut à droite
                .show();
    }
    // Nouvelle alerte d'erreur (toast notification)
    private void showErrorAlert(String message) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/error.png")));
        icon.setFitWidth(30);
        icon.setFitHeight(30);

        Notifications.create()
                .title("Erreur")
                .text(message)
                .graphic(icon)
                .hideAfter(Duration.seconds(4)) // Se ferme automatiquement après 5 secondes
                .position(Pos.TOP_RIGHT)
                .show();
    }
    public static void cleanup() {
        if (conseilPopup != null) {
            conseilPopup.close();
            conseilPopup = null;
        }
    }



}