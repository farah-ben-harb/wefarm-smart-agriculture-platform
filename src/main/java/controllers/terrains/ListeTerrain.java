package controllers.terrains;

import entities.Terrain;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ServiceTerrain;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ListeTerrain {
    @FXML
    private TableView<Terrain> terrainTableView;

    @FXML
    private TableColumn<Terrain, Integer> idCol;
    @FXML
    private TableColumn<Terrain, String> adresseCol;
    @FXML
    private TableColumn<Terrain, Double> surfaceCol;
    @FXML
    private TableColumn<Terrain, Double> prixCol;
    @FXML
    private TableColumn<Terrain, String> statusCol;
    @FXML
    private TableColumn<Terrain, Void> actionsCol;
    @FXML
    private TableColumn<Terrain, String> photoCol;
    @FXML
    private TableColumn<Terrain, String> typeSolCol;
    @FXML
    private TableColumn<Terrain, Double> latitudeCol;
    @FXML
    private TableColumn<Terrain, Double> longitudeCol;

    private final ServiceTerrain serviceTerrain = new ServiceTerrain();
    private final ObservableList<Terrain> terrainsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTableColumns();
        loadTerrainsData();
    }

    private void configureTableColumns() {
        // Configuration des colonnes standard
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        adresseCol.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        surfaceCol.setCellValueFactory(new PropertyValueFactory<>("surface"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        typeSolCol.setCellValueFactory(new PropertyValueFactory<>("typeSol"));
        latitudeCol.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        longitudeCol.setCellValueFactory(new PropertyValueFactory<>("longitude"));

        // Configuration spéciale pour la colonne photo
        photoCol.setCellValueFactory(new PropertyValueFactory<>("photosPath"));
        photoCol.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String photoPath, boolean empty) {
                super.updateItem(photoPath, empty);

                if (empty || photoPath == null || photoPath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        // Charge l'image depuis le système de fichiers
                        File file = new File(photoPath);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString(), true);
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } else {
                            setGraphic(new Label("❌"));
                        }
                    } catch (Exception e) {
                        setGraphic(new Label("📷"));
                    }
                }
            }
        });

        // Configuration de la colonne d'actions
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                // Style des boutons
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                // Actions des boutons
                editBtn.setOnAction(event -> {
                    Terrain terrain = getTableView().getItems().get(getIndex());
                    handleEditTerrain(terrain);
                });

                deleteBtn.setOnAction(event -> {
                    Terrain terrain = getTableView().getItems().get(getIndex());
                    handleDeleteTerrain(terrain);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void loadTerrainsData() {
        try {
            terrainsList.setAll(serviceTerrain.afficher());
            terrainTableView.setItems(terrainsList);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des terrains: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddTerrain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Terrains/AjoutTerrain.fxml"));
            Parent root = loader.load();

            AjoutTerrain controller = loader.getController();
            controller.setListeTerrainController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);

            stage.setTitle("Ajouter un Terrain");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir après fermeture
            refreshTable();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleEditTerrain(Terrain terrain) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Terrains/ModifierTerrain.fxml"));
            Parent root = loader.load();

            ModifierTerrain controller = loader.getController();
            controller.initData(terrain, this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);

            stage.setTitle("Modifier Terrain");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDeleteTerrain(Terrain terrain) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer ce terrain ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer le terrain #" + terrain.getId() + " ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceTerrain.supprimer(terrain.getId());
                terrainsList.remove(terrain);
                showAlert("Succès", "Terrain supprimé avec succès.", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    public void refreshTable() {
        loadTerrainsData();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Add this new method to your ListeTerrain controller class
    @FXML
    private void handleViewTransactions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Terrains/ListeTransac.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage currentStage = (Stage) terrainTableView.getScene().getWindow();

            // Create new scene and stage
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Transactions");

            // Set modality if needed (optional)
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(currentStage);

            stage.show();

            // Optional: Close current window if you want
            // currentStage.close();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la liste des transactions: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}