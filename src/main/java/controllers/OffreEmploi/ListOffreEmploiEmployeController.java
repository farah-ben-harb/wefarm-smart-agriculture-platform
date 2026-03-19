package controllers.OffreEmploi;

import entities.Candidature;
import entities.OffreEmploi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import services.ServiceCandidature;
import services.ServiceOffreEmploi;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import javafx.scene.input.KeyCode;

import javafx.geometry.Insets;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;

import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import utils.ArabicTranslator;
import utils.SessionManager;

public class ListOffreEmploiEmployeController {
    @FXML public ScrollPane mainScroll;
    @FXML public Button mesCandidaturesBtn;
    @FXML public Button traduirBtn;
    @FXML public Label titrePage;
    @FXML private TilePane offreTilePane;
    @FXML private TextField searchField;
    @FXML private ImageView toggleLocationFilterBtnIcon;


    private SessionManager session = SessionManager.getInstance();
    private final int userId = session.getId();

    private ServiceOffreEmploi serviceOffreEmploi = new ServiceOffreEmploi();
    private ServiceCandidature serviceCandidature = new ServiceCandidature();

    //Pour la recherche dynamique
    private ObservableList<OffreEmploi> offresList = FXCollections.observableArrayList();
    private FilteredList<OffreEmploi> filteredOffres;

    //Pour filtre selon Localisation
    private boolean isFilterActive = false;
    private String lastFilteredGovernorate = null;

    //Pour la Traduction
    private boolean isArabic = false;


    public void initialize() {
        try {
            updateButtonTexts(isArabic);

            mainScroll.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) newScene.getWindow();
                        if (stage != null) {
                            stage.setMaximized(true);
                        }
                    });
                }
            });

            // Charger toutes les offres par défaut
            offresList = FXCollections.observableArrayList(serviceOffreEmploi.afficher());
            filteredOffres = new FilteredList<>(offresList, p -> true);

            setupSearchFilter();
            afficherOffresFiltrees();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Méthode pour la traduction des offres d'emploi
    @FXML
    private void traduireOffres() {
        // Désactiver le bouton pendant la traduction
        traduirBtn.setDisable(true);

        // Créer et afficher le modal de chargement
        Stage loadingModal = createLoadingModal();
        loadingModal.show();

        // Exécuter la traduction dans un thread séparé
        new Thread(() -> {
            try {
                // Simuler un délai pour la traduction
                Thread.sleep(500);

                Platform.runLater(() -> {
                    isArabic = !isArabic;
                    try {
                        updateButtonTexts(isArabic);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    // Appliquer la nouvelle orientation
                    NodeOrientation orientation = isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT;
                    mainScroll.setNodeOrientation(orientation);
                    offreTilePane.setNodeOrientation(orientation);

                    afficherOffresFiltrees();

                    // Fermer le modal et réactiver le bouton
                    loadingModal.close();
                    traduirBtn.setDisable(false);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    loadingModal.close();
                    traduirBtn.setDisable(false);
                });
            }
        }).start();
    }


    // Méthode pour mettre à jour le texte des boutons lors de la traduction
    private void updateButtonTexts(boolean isArabic) throws SQLException {
        searchField.setPromptText(isArabic ? "ابحث عن عرض عمل حسب المهارات..." : "Rechercher une offre par compétences...");
        titrePage.setText(isArabic ? "عروض العمل" : "Les offres d'emploi");
        mesCandidaturesBtn.setText(isArabic ? "طلباتي للترشح" : "Mes candidatures");
        traduirBtn.setText(isArabic ? "ترجمة" : "Traduir");

        if (isArabic) {
            titrePage.setAlignment(Pos.CENTER_RIGHT);
            searchField.setAlignment(Pos.CENTER_RIGHT);
            traduirBtn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #158d54;");
        } else {
            traduirBtn.setStyle("-fx-background-color: #158d54; -fx-text-fill: white;");
            titrePage.setAlignment(Pos.CENTER_LEFT);
            searchField.setAlignment(Pos.CENTER_LEFT);
        }
    }

    //Méthodes de recherche dynamiques
    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredOffres.setPredicate(createPredicate(newValue));
            afficherOffresFiltrees();
        });
    }

    private Predicate<OffreEmploi> createPredicate(String searchText) {
        return offre -> {
            if (searchText == null || searchText.isEmpty()) return true;

            String normalizedSearch = normalizeString(searchText);
            String[] competences = offre.getCompetencesRequises().split(",");

            // Vérifier chaque compétence individuellement
            for (String competence : competences) {
                if (normalizeString(competence.trim()).contains(normalizedSearch)) {
                    return true;
                }
            }
            return false;
        };
    }

    private String normalizeString(String input) {
        if (input == null) return "";

        // Retirer les accents et convertir en minuscules
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    private void afficherOffresFiltrees() {
        offreTilePane.getChildren().clear();

        for (OffreEmploi offre : filteredOffres) {
            VBox card = createOfferCard(offre,isArabic);
            offreTilePane.getChildren().add(card);
        }
    }
    //Méthode pour la création des cartes
    private VBox createOfferCard(OffreEmploi offre, boolean enArabe) {
        VBox card = new VBox();
        card.setStyle("-fx-font-family: 'Arial Unicode MS'; -fx-border-color: #158d54; -fx-border-width: 2px; -fx-border-radius: 5px;");
        // Réinitialiser l'orientation correctement
        card.setNodeOrientation(enArabe ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        if (enArabe) {
            card.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
        card.getStyleClass().add("offer-card");

        // Header (date publication + statut)
        HBox header = new HBox();
        header.getStyleClass().add("card-header");

        // Date de publication
        Label publishDate = new Label();
        String dateText = getRelativeDate(offre.getDatePublication());
        publishDate.setText(enArabe ? "تاريخ النشر: " + dateText : "Publié: " + dateText);
        publishDate.getStyleClass().add("publish-date");

        // Cercle de statut
        Circle statusCircle = new Circle(5);
        statusCircle.getStyleClass().add("status-circle");
        boolean isActive = "actif".equals(offre.getStatus());
        if (isActive) {
            statusCircle.getStyleClass().add("active");
        }
        HBox.setMargin(statusCircle, new Insets(0, 0, 0, 70));
        header.getChildren().addAll(publishDate, statusCircle);

        // Titre
        Label title = new Label();
        title.setFont(Font.font("Arial Unicode MS", 14));

        String displayText = enArabe ?
                ArabicTranslator.translateToArabic(offre.getTitre()) :
                offre.getTitre();

        title.setText(displayText);
        title.getStyleClass().add("offer-title");

        // Dates
        VBox datesBox = new VBox(5);
        datesBox.getChildren().addAll(
                new Label(enArabe ? "تاريخ البدء: " + formatDate(offre.getDateDebut()) :
                        "Date de début: " + formatDate(offre.getDateDebut())),
                new Label(enArabe ? "تاريخ الانتهاء المتوقع: " + formatDate(offre.getDateFinEstimee()) :
                        "Date de fin estimée: " + formatDate(offre.getDateFinEstimee()))
        );
        datesBox.getStyleClass().add("date-label");

        // Compétences (tags)
        Label competencesLabel = new Label(enArabe ? "المهارات المطلوبة" : "Compétences requises");
        FlowPane skillsPane = new FlowPane();
        skillsPane.setHgap(5);
        skillsPane.setVgap(5);

        String[] skills = offre.getCompetencesRequises().split(",");
        for (String skill : skills) {
            String skillText = skill.trim();
            if (enArabe) {
                skillText = ArabicTranslator.translateToArabic(skillText);
            }
            Label skillTag = new Label(skillText);
            skillTag.getStyleClass().add("skill-tag");
            skillTag.setFont(Font.font("Arial Unicode MS", 12));
            skillsPane.getChildren().add(skillTag);
        }

        // Salaire
        Label salary = new Label(enArabe ? "الراتب: " + offre.getSalaire() + " دينار تونسي" :
                "Salaire: " + offre.getSalaire() + " TND");
        salary.getStyleClass().add("salary");

        // Footer (boutons)
        HBox footer = new HBox(10);
        footer.getStyleClass().add("card-footer");

        Button detailsBtn = new Button(enArabe ? "التفاصيل" : "Détails");
        detailsBtn.getStyleClass().add("details-button");
        detailsBtn.setOnAction(e -> afficherDetailsOffre(offre));

        Button postulerBtn = new Button(enArabe ? "تقديم" : "Postuler");
        if (isActive) {
            postulerBtn.getStyleClass().add("apply-button-active");
            postulerBtn.setDisable(false);
            postulerBtn.setOnAction(e -> ouvrirModalPostuler(offre));
        } else {
            postulerBtn.getStyleClass().add("apply-button-inactive");
            postulerBtn.setDisable(true);
        }

        footer.getChildren().addAll(detailsBtn, postulerBtn);

        // Assemblage de la carte
        card.getChildren().addAll(header, title, datesBox, competencesLabel, skillsPane, salary, footer);

        return card;
    }

    //Méthode pour gérer l'action:Postuler à une candidature
    //Méthode pour ouvrir le modal pour postuler
    public void ouvrirModalPostuler(OffreEmploi offre) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(isArabic ? "التقديم على العرض" : "Postuler à l'offre");

        // Layout principal
        VBox modalLayout = new VBox(15);
        modalLayout.setPadding(new Insets(20));
        modalLayout.setStyle("-fx-background-color: #f8f9fa;");

        // Appliquer l'orientation correcte
        modalLayout.setNodeOrientation(isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        if (isArabic) {
            modalLayout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }

        // Titre
        String titreOffre = isArabic ? ArabicTranslator.translateToArabic(offre.getTitre()) :
                offre.getTitre().substring(0, 1).toUpperCase() + offre.getTitre().substring(1);
        Label titreLabel = new Label((isArabic ? "التقديم على: " : "Postuler à : ") + titreOffre);
        titreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Conteneur pour ajout des compétences
        HBox addBox = new HBox(10);
        TextField competenceField = new TextField();
        competenceField.setPromptText(isArabic ? "المهارات" : "Compétences");
        Button addButton = new Button("+");
        addButton.setStyle("-fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30;");

        addBox.getChildren().addAll(isArabic ? new Node[] {addButton, competenceField} : new Node[] {competenceField, addButton});

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
            MenuItem deleteItem = new MenuItem(isArabic ? "حذف" : "Supprimer");
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
        buttonBox.setAlignment(isArabic ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);

        Button cancelButton = new Button(isArabic ? "إلغاء" : "Annuler");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");

        Button submitButton = new Button(isArabic ? "إرسال" : "Envoyer");
        submitButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        buttonBox.getChildren().addAll(isArabic ? new Node[] {submitButton, cancelButton} : new Node[] {cancelButton, submitButton});

        // Ajout des éléments au layout
        modalLayout.getChildren().addAll(
                titreLabel,
                new Label(isArabic ? "المهارات" : "Compétences"),
                addBox,
                competencesListView,
                errorLabel,
                buttonBox
        );

        // Gestion des événements
        addButton.setOnAction(e -> {
            addCompetence(competenceField, competencesList, errorLabel);
        });

        competenceField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                addCompetence(competenceField, competencesList, errorLabel);
            }
        });

        cancelButton.setOnAction(e -> modal.close());

        submitButton.setOnAction(e -> {
            if (competencesList.isEmpty()) {
                errorLabel.setText(isArabic ? "الرجاء إضافة مهارة واحدة على الأقل" : "Veuillez ajouter au moins une compétence");
                return;
            }

            try {
                String competences = String.join(", ", competencesList);
                if (serviceCandidature.existeCandidature(offre.getId(), userId)) {
                    errorLabel.setText(isArabic ? "لقد تقدمت بالفعل إلى هذا العرض!" : "Vous avez déjà postulé à cette offre!");
                    return;
                }

                serviceCandidature.ajouter(new Candidature(offre.getId(), competences));
                modal.close();
                showSuccessAlert(isArabic ? "تم تقديم طلبك بنجاح!" : "Votre candidature a été soumise avec succès !");
            } catch (SQLException ex) {
                errorLabel.setText(isArabic ? "خطأ: " + ex.getMessage() : "Erreur: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(modalLayout, isArabic ? 450 : 400, 350);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void addCompetence(TextField field, ObservableList<String> list, Label errorLabel) {
        String competence = field.getText().trim();
        if (!competence.isEmpty()) {
            list.add(competence);
            field.clear();
            errorLabel.setText("");
        } else {
            errorLabel.setText(isArabic ? "الرجاء إدخال مهارة صالحة" : "Veuillez entrer une compétence valide");
        }
    }

    //Affichage des détails d'une offre d'emploi
    private void afficherDetailsOffre(OffreEmploi offre) {
        try {
            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/detailsOffre.fxml"));
            AnchorPane page = loader.load();
            // Appliquer l'orientation avant d'afficher
            page.setNodeOrientation(isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

            // Créer et configurer la nouvelle fenêtre
            Stage newStage = new Stage();
            newStage.setTitle(isArabic ? "تفاصيل العرض" : "Détails de l'offre");
            newStage.setScene(new Scene(page));
            newStage.setMaximized(true);

            // Configurer le contrôleur
            DetailsOffreEmploiController controller = loader.getController();
            controller.setOffre(offre);
            controller.setDialogStage(newStage);

            // Passer l'état de la langue
            controller.setArabic(isArabic);

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) mainScroll.getScene().getWindow();
            currentStage.close();

            // Appliquer la direction du texte si arabe
            if (isArabic) {
                page.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            }

            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Impossible d'afficher les détails de l'offre");
        }
    }
    //Méthode pour ouvrir l'interface Mes candidatures
    @FXML
    private void ouvrirMesCandidatures() {
        try {
            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/mesCandidatures.fxml"));
            Parent root = loader.load();

            // Créer et configurer la nouvelle fenêtre
            Stage newStage = new Stage();
            newStage.setTitle("Mes Candidatures");
            newStage.setScene(new Scene(root));

            // Activer le mode plein écran
            newStage.setMaximized(true);

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) mainScroll.getScene().getWindow();
            currentStage.close();

            // Afficher la nouvelle fenêtre
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Impossible d'ouvrir la vue des candidatures: " + e.getMessage());
        }
    }

    /* ********************AMELIORATIONS************************* */
    //Méthodes pour manipulations des dates
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    private String getRelativeDate(Date publicationDate) {
        Date now = new Date();
        long diffInMillis = now.getTime() - publicationDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar publicationDay = Calendar.getInstance();
        publicationDay.setTime(publicationDate);
        publicationDay.set(Calendar.HOUR_OF_DAY, 0);
        publicationDay.set(Calendar.MINUTE, 0);
        publicationDay.set(Calendar.SECOND, 0);
        publicationDay.set(Calendar.MILLISECOND, 0);

        if (publicationDay.equals(today)) {
            return isArabic ? "اليوم" : "aujourd'hui";
        } else if (diffInDays == 1) {
            return isArabic ? "أمس" : "hier";
        } else if (diffInDays >= 2 && diffInDays <= 7) {
            return isArabic ? "منذ " + diffInDays + " أيام"
                    : "il y a " + diffInDays + " jours";
        } else {
            SimpleDateFormat formatter = isArabic
                    ? new SimpleDateFormat("d MMMM yyyy", new Locale("ar"))
                    : new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH);
            return formatter.format(publicationDate);
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
    //Méthode pour passer à l'état de langue
    public void setArabic(boolean isArabic) throws SQLException {
        this.isArabic = isArabic;
        updateButtonTexts(isArabic);

        // Appliquer la direction appropriée
        NodeOrientation orientation = isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT;

        mainScroll.setNodeOrientation(orientation);
        offreTilePane.setNodeOrientation(orientation);
        searchField.setNodeOrientation(orientation);
        titrePage.setNodeOrientation(orientation);

        // Forcer le rafraîchissement de l'affichage
        afficherOffresFiltrees();
    }
    //Méthode de création du modal de chargement lors de la traduction
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
        Label label = new Label("Traduction en cours...");
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
    //Méthodes de filtrage par Géolocalisation
    //Méthode pour détécter la localisation de l'employé connecté à partir de son adresse
    private String detectGovernorateFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        String[] governorates = {
                "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                "Jendouba", "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia",
                "Manouba", "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid",
                "Siliana", "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        };

        // Recherche du gouvernorat dans l'adresse (insensible à la casse)
        for (String gov : governorates) {
            Pattern pattern = Pattern.compile("\\b" + gov + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(address);
            if (matcher.find()) {
                return gov;
            }
        }

        return null; // Aucun gouvernorat trouvé
    }
    //Méthode de localisation (via l’adresse IP) pour déterminer le gouvernorat tunisien actuel de l'utilisateur
    private String getCurrentGovernorate() {
        try {
            URL url = new URL("https://ipwhois.app/json/");
            System.out.println("[DEBUG] Connexion à l'API: " + url); // Debug

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // Timeout de 5s

            int responseCode = conn.getResponseCode();
            System.out.println("[DEBUG] Code de réponse HTTP: " + responseCode); // Debug

            if (responseCode == 200) {
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                System.out.println("[DEBUG] Réponse brute de l'API: " + response); // Debug

                JSONObject json = new JSONObject(response);

                // Extraction des données
                String ip = json.getString("ip");
                String country = json.getString("country");
                String region = json.getString("region");
                String city = json.optString("city", "N/A");

                System.out.println("[DEBUG] Données API - IP: " + ip);
                System.out.println("[DEBUG] Données API - Pays: " + country);
                System.out.println("[DEBUG] Données API - Région: " + region);
                System.out.println("[DEBUG] Données API - Ville: " + city);

                if ("Tunisia".equalsIgnoreCase(country)) {
                    String governorate = translateRegionToGovernorate(region);
                    System.out.println("[DEBUG] Gouvernorat déterminé: " + governorate);
                    return governorate;
                } else {
                    System.out.println("[DEBUG] L'utilisateur n'est pas en Tunisie");
                    return null;
                }
            } else {
                System.out.println("[ERROR] Erreur API - Code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Erreur lors de l'appel API: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    private String translateRegionToGovernorate(String region) {
        // Cette méthode traduit les noms de région retournés par l'API
        // vers les noms standard des gouvernorats tunisiens

        if (region == null) return null;

        Map<String, String> translationMap = new HashMap<>();
        translationMap.put("Tunis", "Tunis");
        translationMap.put("Aryanah", "Ariana");
        translationMap.put("Bin 'Arus", "Ben Arous");
        translationMap.put("Manouba", "Manouba");
        translationMap.put("Nabul", "Nabeul");
        translationMap.put("Zaghwan", "Zaghouan");
        translationMap.put("Banzart", "Bizerte");
        translationMap.put("Bajah", "Béja");
        translationMap.put("Jundubah", "Jendouba");
        translationMap.put("Kef", "Le Kef");
        translationMap.put("Siliana", "Siliana");
        translationMap.put("Susah", "Sousse");
        translationMap.put("Monastir", "Monastir");
        translationMap.put("Al Mahdiyah", "Mahdia");
        translationMap.put("Al Munastir", "Monastir");
        translationMap.put("Sfax", "Sfax");
        translationMap.put("Qabis", "Gabès");
        translationMap.put("Madanin", "Medenine");
        translationMap.put("Tataouine", "Tataouine");
        translationMap.put("Gafsa", "Gafsa");
        translationMap.put("Qafsa", "Gafsa");
        translationMap.put("Qibili", "Kebili");
        translationMap.put("Tozeur", "Tozeur");
        translationMap.put("Kasserine", "Kasserine");
        translationMap.put("Sidi Bouzid", "Sidi Bouzid");
        translationMap.put("Kairouan", "Kairouan");

        return translationMap.getOrDefault(region, region);
    }
    private String getUserGovernorate() {
        try {
            // 1. D'abord on teste avec l'adresse de l'utilisateur
            String userAddress = session.getAdresse();
            System.out.println("[DEBUG] Adresse utilisateur: " + userAddress); // Debug

            String governorateFromAddress = detectGovernorateFromAddress(userAddress);
            if (governorateFromAddress != null) {
                System.out.println("[DEBUG] Gouvernorat trouvé dans l'adresse: " + governorateFromAddress); // Debug
                return governorateFromAddress;
            }

            // 2. Si rien trouvé dans l'adresse, on utilise l'API
            System.out.println("[DEBUG] Aucun gouvernorat trouvé dans l'adresse, utilisation de l'API..."); // Debug
            String governorateFromAPI = getCurrentGovernorate();
            System.out.println("[DEBUG] Résultat API: " + governorateFromAPI); // Debug

            return governorateFromAPI;
        } catch (Exception e) {
            System.err.println("[ERROR] Erreur dans getUserGovernorate: " + e.getMessage()); // Debug
            e.printStackTrace();
            return null;
        }
    }
    //Changement du bouton de filtre selon son état
    @FXML
    private void toggleLocationFilter() {
        try {
            List<OffreEmploi> allOffres = serviceOffreEmploi.afficher();

            if (!isFilterActive) {
                // Appliquer le filtre
                lastFilteredGovernorate = getUserGovernorate();
                if (lastFilteredGovernorate != null) {
                    offresList = FXCollections.observableArrayList(
                            allOffres.stream()
                                    .filter(offre -> lastFilteredGovernorate.equalsIgnoreCase(offre.getLocalisation()))
                                    .collect(Collectors.toList())
                    );
                    isFilterActive = true;
                    toggleLocationFilterBtnIcon.setImage(new Image(getClass().getResourceAsStream("/icons/annuler.png")));
                }
            } else {
                // Annuler le filtre
                offresList = FXCollections.observableArrayList(allOffres);
                isFilterActive = false;
                toggleLocationFilterBtnIcon.setImage(new Image(getClass().getResourceAsStream("/icons/location.png")));
            }

            filteredOffres = new FilteredList<>(offresList, createPredicate(searchField.getText()));
            afficherOffresFiltrees();

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert(isArabic ? "خطأ في تحميل العروض" : "Erreur de chargement des offres");
        }
    }

}

