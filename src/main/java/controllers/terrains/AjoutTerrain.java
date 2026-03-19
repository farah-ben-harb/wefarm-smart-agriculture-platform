package controllers.terrains;

import entities.Terrain;
import entities.Utilisateur;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import services.PredictService;
import services.ServiceTerrain;
import utils.SessionManager;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AjoutTerrain {
    @FXML private TextField adresseField;
    @FXML private TextField surfaceField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> typeSolCombo;
    @FXML private StackPane mapContainer; // Remplace WebView
    @FXML private ImageView imagePreview;

    private WebView mapView;
    private String photosPath;
    private Double latitude = null;
    private Double longitude = null;
    private ListeTerrain listeTerrainController;
    private final ServiceTerrain serviceTerrain = new ServiceTerrain();
    private final PredictService predictService = new PredictService("http://127.0.0.1:4000");
    private JavaBridge javaBridge = new JavaBridge();

    public void setListeTerrainController(ListeTerrain controller) {
        this.listeTerrainController = controller;
    }

    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation différée avec vérification
        Platform.runLater(() -> {
            if (isWebViewSupported()) {
                initializeMapView();
            } else {
                showMapError("La fonctionnalité carte n'est pas disponible sur ce système");
            }
        });
    }

    private boolean isWebViewSupported() {
        try {
            // Vérification explicite de la disponibilité de WebKit
            Class.forName("com.sun.webkit.WebPage");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void initializeMapView() {
        try {
            System.out.println("Initialisation de WebView...");

            // Configuration spéciale pour JavaFX 23
            System.setProperty("javafx.webkit.debug", "true");
            System.setProperty("javafx.verbose", "true");

            mapView = new WebView();
            mapView.setPrefSize(600, 450);

            WebEngine engine = mapView.getEngine();
            engine.setJavaScriptEnabled(true);

            // Gestion des erreurs
            engine.setOnError(event ->
                    System.err.println("WebEngine Error: " + event.getMessage()));

            engine.getLoadWorker().exceptionProperty().addListener((obs, oldEx, newEx) -> {
                if (newEx != null) {
                    System.err.println("Load Error:");
                    newEx.printStackTrace();
                    showMapError("Erreur de chargement de la carte");
                }
            });

            // Chargement simplifié
            String htmlContent = createBasicMapHtml();
            engine.loadContent(htmlContent);

            mapContainer.getChildren().add(mapView);

        } catch (Exception e) {
            System.err.println("Erreur critique lors de l'initialisation:");
            e.printStackTrace();
            showMapError("Impossible d'initialiser la carte");
        }
    }

    private String createBasicMapHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body, html { margin:0; padding:0; width:100%; height:100%; }
                    #map { height:100%; width:100%; background:#f0f0f0; }
                    .error { color:red; text-align:center; padding-top:50px; }
                </style>
            </head>
            <body>
                <div id="map">
                    <p class="error">Chargement de la carte en cours...</p>
                </div>
                <script>
                    setTimeout(function() {
                        document.getElementById('map').innerHTML = 
                            '<p class="error">La carte nécessite JavaScript</p>';
                    }, 3000);
                </script>
            </body>
            </html>
            """;
    }

    private void showMapError(String message) {
        Platform.runLater(() -> {
            Label errorLabel = new Label(message);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-padding: 20;");
            mapContainer.getChildren().add(errorLabel);
        });
    }
    private void initializeStatusCombo() {
        statusCombo.getItems().addAll("Disponible", "Réservé", "Vendu", "En maintenance");
        statusCombo.getSelectionModel().selectFirst();
    }

    private void setupNumericValidation() {
        surfaceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                surfaceField.setText(oldVal);
            }
        });

        prixField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                prixField.setText(oldVal);
            }
        });
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Tous fichiers", "*.*")
        );

        File file = fileChooser.showOpenDialog(imagePreview.getScene().getWindow());
        if (file != null) {
            photosPath = file.getAbsolutePath();
            imagePreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handlePredictPrice() {
        if (!validatePredictionInputs()) {
            return;
        }

        Task<Double> predictTask = new Task<>() {
            @Override
            protected Double call() throws Exception {
                double surface = Double.parseDouble(surfaceField.getText());
                String typeSol = typeSolCombo.getValue();
                String adresse = adresseField.getText();
                return predictService.predict(surface, typeSol, adresse);
            }
        };

        predictTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    Double predictedPrice = predictTask.getValue();
                    System.out.println("Predicted Price: " + predictedPrice);
                    if (predictedPrice == null || predictedPrice.isNaN() || predictedPrice.isInfinite()) {
                        showAlert("Erreur", "Valeur prédite invalide : " + predictedPrice, Alert.AlertType.ERROR);
                        return;
                    }
                    String formattedPrice = String.format("%.2f", predictedPrice);
                    prixField.setText(predictedPrice.toString().substring(0, 10));
                    System.out.println("TextField Updated with: " + formattedPrice);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la mise à jour du TextField: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });
        predictTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                showAlert("Erreur de prédiction",
                        "Impossible d'obtenir la prédiction : " + predictTask.getException().getMessage(),
                        Alert.AlertType.ERROR);
            });
        });

        new Thread(predictTask).start();
    }

    private boolean validatePredictionInputs() {
        StringBuilder errors = new StringBuilder();

        if (adresseField.getText().isBlank()) {
            errors.append("- Adresse requise\n");
        }
        if (surfaceField.getText().isBlank()) {
            errors.append("- Surface requise\n");
        }
        if (typeSolCombo.getValue().isBlank()) {
            errors.append("- Type de sol requis\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation", "Veuillez corriger les erreurs suivantes:\n" + errors, Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) {
            return;
        }

        Utilisateur currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Erreur", "Vous devez être connecté", Alert.AlertType.ERROR);
            return;
        }

        try {
            Terrain terrain = createTerrainFromForm();
            terrain.setOwnerId(currentUser.getId());

            serviceTerrain.ajouter(terrain);

            if (listeTerrainController != null) {
                listeTerrainController.refreshTable();
            }

            closeWindow();
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur lors de l'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (adresseField.getText().isBlank()) {
            errors.append("- Adresse requise\n");
        }
        if (surfaceField.getText().isBlank()) {
            errors.append("- Surface requise\n");
        }
        if (prixField.getText().isBlank()) {
            errors.append("- Prix requis\n");
        }
        if (typeSolCombo.getValue().isBlank()) {
            errors.append("- Type de sol requis\n");
        }
        if (latitude == null || longitude == null) {
            errors.append("- Veuillez sélectionner une position sur la carte\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation", "Veuillez corriger les erreurs suivantes:\n" + errors, Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private Terrain createTerrainFromForm() {
        Terrain terrain = new Terrain();
        terrain.setAdresse(adresseField.getText());
        terrain.setSurface(Double.parseDouble(surfaceField.getText()));
        terrain.setPrix(Double.parseDouble(prixField.getText()));
        terrain.setStatus(statusCombo.getValue());
        terrain.setDescription(descriptionArea.getText());
        terrain.setLatitude(latitude);
        terrain.setLongitude(longitude);
        terrain.setTypeSol(typeSolCombo.getValue());
        terrain.setPhotos(photosPath);

        return terrain;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) adresseField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public class JavaBridge {
        public void updateCoordinates(double lat, double lng) {
            Platform.runLater(() -> {
                latitude = lat;
                longitude = lng;
                System.out.println("Coordonnées mises à jour - Lat: " + lat + ", Lng: " + lng);
            });
        }
    }
}