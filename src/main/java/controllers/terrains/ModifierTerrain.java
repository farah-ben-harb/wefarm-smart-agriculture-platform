package controllers.terrains;

import entities.Terrain;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import services.ServiceTerrain;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class ModifierTerrain {
    // Champs FXML
    @FXML private TextField idField;
    @FXML private TextField adresseField;
    @FXML private TextField surfaceField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField typeSolField;
    @FXML private WebView mapView;
    @FXML private ImageView imagePreview;

    // Données
    private double latitude;
    private double longitude;
    private String photoPath;
    private Terrain terrain;
    private ListeTerrain listeTerrainController;
    private final ServiceTerrain serviceTerrain = new ServiceTerrain();

    public void initData(Terrain terrain, ListeTerrain controller) {
        this.terrain = terrain;
        this.listeTerrainController = controller;
        this.latitude = terrain.getLatitude();
        this.longitude = terrain.getLongitude();
        this.photoPath = terrain.getPhotos();

        initializeWebEngine();
        populateFields();
    }

    private void initializeWebEngine() {
        WebEngine webEngine = mapView.getEngine();
        URL mapUrl = getClass().getResource("/views/html/map.html");
        if (mapUrl == null) {
            System.err.println("HTML map not found at: /views/html/map.html");
            return;
        }
        webEngine.load(mapUrl.toExternalForm());
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("java", new JavaBridge());

                // Délai pour garantir le chargement complet
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> {
                            String script = String.format(
                                    "initializeExistingMap(%f, %f);",
                                    latitude,
                                    longitude
                            );
                            webEngine.executeScript(script);
                        });
                    }
                }, 500);
            }
        });
    }

    private void populateFields() {
        idField.setText(String.valueOf(terrain.getId()));
        adresseField.setText(terrain.getAdresse());
        surfaceField.setText(String.valueOf(terrain.getSurface()));
        prixField.setText(String.valueOf(terrain.getPrix()));
        statusCombo.getSelectionModel().select(terrain.getStatus());
        descriptionArea.setText(terrain.getDescription());
        typeSolField.setText(terrain.getTypeSol());

        if (photoPath != null && !photoPath.isEmpty()) {
            loadImagePreview();
        }
    }

    private void loadImagePreview() {
        try {
            Image image = new Image(new File(photoPath).toURI().toString(), true);
            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0 && !image.isError()) {
                    imagePreview.setImage(image);
                }
            });
        } catch (Exception e) {
            imagePreview.setImage(null);
        }
    }

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("Disponible", "Réservé", "Vendu", "En maintenance");
        setupNumericValidation(surfaceField);
        setupNumericValidation(prixField);
    }

    private void setupNumericValidation(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                field.setText(oldValue);
            }
        });
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(imagePreview.getScene().getWindow());
        if (file != null) {
            photoPath = file.getAbsolutePath();
            imagePreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSave() {
        try {
            updateTerrainData();
            serviceTerrain.modifier(terrain);
            listeTerrainController.refreshTable();
            closeWindow();
        } catch (SQLException | NumberFormatException e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateTerrainData() {
        terrain.setAdresse(adresseField.getText());
        terrain.setSurface(Double.parseDouble(surfaceField.getText()));
        terrain.setPrix(Double.parseDouble(prixField.getText()));
        terrain.setStatus(statusCombo.getValue());
        terrain.setDescription(descriptionArea.getText());
        terrain.setLatitude(latitude);
        terrain.setLongitude(longitude);
        terrain.setTypeSol(typeSolField.getText());
        terrain.setPhotos(photoPath);
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) imagePreview.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait(); // Méthode appelée correctement
    }

    public void setListeTerrainController(ListeTerrain controller) {
        this.listeTerrainController = controller;
    }

    public class JavaBridge {
        public void updateCoordinates(double lat, double lng) {
            latitude = lat;
            longitude = lng;
        }
    }
}