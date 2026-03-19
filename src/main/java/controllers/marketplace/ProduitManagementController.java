package controllers.marketplace;

import entities.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ProduitService;
import services.ProduitServiceImpl;
import utils.CloudinaryUtil;
import utils.DataBase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProduitManagementController implements Initializable {
    @FXML private TextField nomField;
    @FXML private TextField quantiteField;
    @FXML private TextField prixTotalField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField imageField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private ComboBox<String> stockDCombo;
    @FXML private Button imageChooserBtn;
    @FXML private ImageView imagePreview;
    @FXML private Button addBtn;
    @FXML private Button updateBtn;
    @FXML private Button deleteBtn;
    @FXML private Button clearBtn;
    @FXML private Button approveBtn;
    @FXML private Button disapproveBtn;
    @FXML private Button commandesBtn;
    @FXML private Button statisticsBtn;
    @FXML private Button adsBtn;
    @FXML private TableView<Produit> produitTable;
    @FXML private TableColumn<Produit, Integer> idCol;
    @FXML private TableColumn<Produit, String> nomCol;
    @FXML private TableColumn<Produit, Integer> quantiteCol;
    @FXML private TableColumn<Produit, Double> prixTotalCol;
    @FXML private TableColumn<Produit, String> descriptionCol;
    @FXML private TableColumn<Produit, String> imageCol;
    @FXML private TableColumn<Produit, String> categorieCol;
    @FXML private TableColumn<Produit, String> stockDCol;
    @FXML private TableColumn<Produit, Boolean> approvedCol;
    @FXML private TableColumn<Produit, Date> updatedAtCol;
    @FXML private TextField searchField;
    @FXML private Button refreshBtn;

    private final ProduitService produitService;
    private final ObservableList<Produit> produitList = FXCollections.observableArrayList();
    private File selectedImageFile;

    public ProduitManagementController() {
        produitService = new ProduitServiceImpl();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Test database connection
            Connection conn = DataBase.getInstance().getConnection();
            if (conn != null) {
                System.out.println("Database connection successful");
                conn.close();
            }
            
            setupTableColumns();
            setupComboBoxes();
            loadProduits();
            setupSearch();
            setupTableSelection();
            setupImageChooser();
            
            // Initially disable buttons
            updateBtn.setDisable(true);
            deleteBtn.setDisable(true);
            approveBtn.setDisable(true);
            disapproveBtn.setDisable(true);
        } catch (Exception e) {
            showAlert("Database Error", 
                     "Unable to connect to database: " + e.getMessage() + 
                     "\nPlease check your database connection settings.", 
                     Alert.AlertType.ERROR);
        }
    }

    private void setupTableColumns() {
        // Set up cell value factories using lambda expressions
        idCol.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        nomCol.setCellValueFactory(data -> data.getValue().nomProperty());
        quantiteCol.setCellValueFactory(data -> data.getValue().quantiteProperty().asObject());
        prixTotalCol.setCellValueFactory(data -> data.getValue().prixTotalProperty().asObject());
        descriptionCol.setCellValueFactory(data -> data.getValue().descriptionProperty());
        imageCol.setCellValueFactory(data -> data.getValue().imageProperty());
        categorieCol.setCellValueFactory(data -> data.getValue().categorieProperty());
        stockDCol.setCellValueFactory(data -> data.getValue().stockDProperty());
        approvedCol.setCellValueFactory(data -> data.getValue().approvedProperty());
        updatedAtCol.setCellValueFactory(data -> data.getValue().updatedAtProperty());
        
        // Format the price column to show currency
        prixTotalCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", price));
                }
            }
        });

        // Format the approved column
        approvedCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Approuvé" : "Non approuvé");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
    }

    private void setupComboBoxes() {
        // Setup categories
        categorieCombo.getItems().addAll(
            "Fruits", "Légumes", "Céréales", "Viandes", "Produits laitiers", "Autres"
        );
        
        // Setup stock status
        stockDCombo.getItems().addAll(
            "En stock", "Stock faible", "Rupture de stock"
        );
    }

    private void loadProduits() {
        produitList.clear();
        produitList.addAll(produitService.getAllProduits());
        produitTable.setItems(produitList);
    }

    private void setupSearch() {
        FilteredList<Produit> filteredData = new FilteredList<>(produitList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(produit -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return produit.getNom().toLowerCase().contains(lowerCaseFilter) ||
                       produit.getCategorie().toLowerCase().contains(lowerCaseFilter) ||
                       produit.getDescription().toLowerCase().contains(lowerCaseFilter);
            });
        });
        SortedList<Produit> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(produitTable.comparatorProperty());
        produitTable.setItems(sortedData);
    }

    private void setupTableSelection() {
        produitTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFieldsWithProduit(newSelection);
                updateBtn.setDisable(false);
                deleteBtn.setDisable(false);
                approveBtn.setDisable(newSelection.isApproved());
                disapproveBtn.setDisable(!newSelection.isApproved());
            } else {
                clearFields();
                updateBtn.setDisable(true);
                deleteBtn.setDisable(true);
                approveBtn.setDisable(true);
                disapproveBtn.setDisable(true);
            }
        });
    }

    private void fillFieldsWithProduit(Produit produit) {
        nomField.setText(produit.getNom());
        quantiteField.setText(String.valueOf(produit.getQuantite()));
        prixTotalField.setText(String.valueOf(produit.getPrixTotal()));
        descriptionArea.setText(produit.getDescription());
        imageField.setText(produit.getImage());
        categorieCombo.setValue(produit.getCategorie());
        stockDCombo.setValue(produit.getStockD());
        
        // Load and display the image from Cloudinary URL
        if (produit.getImage() != null && !produit.getImage().isEmpty()) {
            try {
                Image image = new Image(produit.getImage());
                imagePreview.setImage(image);
            } catch (Exception e) {
                System.err.println("Failed to load image preview: " + e.getMessage());
                imagePreview.setImage(null);
            }
        } else {
            imagePreview.setImage(null);
        }
    }

    private void setupImageChooser() {
        imageChooserBtn.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            selectedImageFile = fileChooser.showOpenDialog(imageChooserBtn.getScene().getWindow());
            if (selectedImageFile != null) {
                imageField.setText(selectedImageFile.getPath());
                try {
                    Image image = new Image(selectedImageFile.toURI().toString());
                    imagePreview.setImage(image);
                } catch (Exception e) {
                    showAlert("Erreur", "Impossible de charger l'image", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        try {
            loadProduits();
            showAlert("Success", "Product list refreshed successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to refresh products: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAdd() {
        try {
            if (!validateInput()) {
                return;
            }

            Produit newProduit = createProduitFromInput();
            produitService.createProduit(newProduit);
            clearFields();
            loadProduits();
            showAlert("Success", "Product added successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to add product: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            if (!validateInput()) {
                return;
            }

            Produit selectedProduit = produitTable.getSelectionModel().getSelectedItem();
            if (selectedProduit == null) {
                showAlert("Error", "Please select a product to update", Alert.AlertType.WARNING);
                return;
            }

            // Handle image update first
            handleImageUpdate(selectedProduit);

            // Update other fields
            selectedProduit.setNom(nomField.getText().trim());
            selectedProduit.setQuantite(Integer.parseInt(quantiteField.getText().trim()));
            selectedProduit.setPrixTotal(Double.parseDouble(prixTotalField.getText().trim()));
            selectedProduit.setDescription(descriptionArea.getText().trim());
            selectedProduit.setCategorie(categorieCombo.getValue());
            selectedProduit.setStockD(stockDCombo.getValue());
            selectedProduit.setUpdatedAt(new Date());

            produitService.updateProduit(selectedProduit);
            
            clearFields();
            loadProduits();
            showAlert("Success", "Product updated successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to update product: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        try {
            Produit selectedProduit = produitTable.getSelectionModel().getSelectedItem();
            if (selectedProduit == null) {
                showAlert("Error", "Please select a product to delete", Alert.AlertType.WARNING);
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Are you sure you want to delete this product?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Delete image from Cloudinary first
                String publicId = CloudinaryUtil.getInstance().getPublicIdFromUrl(selectedProduit.getImage());
                if (publicId != null) {
                    try {
                        CloudinaryUtil.getInstance().deleteImage(publicId);
                    } catch (IOException e) {
                        System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
                    }
                }

                // Then delete the product
                produitService.deleteProduit(selectedProduit.getId());
                clearFields();
                loadProduits();
                showAlert("Success", "Product deleted successfully!", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to delete product: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
    }

    @FXML
    private void handleShowCommandes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MarketPlace/CommandeManagement.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Gestion des Commandes");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(commandesBtn.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la gestion des commandes", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleApprove() {
        Produit selectedProduit = produitTable.getSelectionModel().getSelectedItem();
        if (selectedProduit == null) {
            showAlert("Erreur", "Veuillez sélectionner un produit à approuver", Alert.AlertType.ERROR);
            return;
        }

        try {
            produitService.approveProduit(selectedProduit.getId(), true);
            loadProduits();
            showAlert("Succès", "Produit approuvé avec succès", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDisapprove() {
        Produit selectedProduit = produitTable.getSelectionModel().getSelectedItem();
        if (selectedProduit == null) {
            showAlert("Erreur", "Veuillez sélectionner un produit à désapprouver", Alert.AlertType.ERROR);
            return;
        }

        try {
            produitService.approveProduit(selectedProduit.getId(), false);
            loadProduits();
            showAlert("Succès", "Produit désapprouvé avec succès", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleShowStatistics() {
        try {
            // Load FXML file
            URL fxmlUrl = getClass().getResource("/views/MarketPlace/StatisticsDialog.fxml");
            
            if (fxmlUrl == null) {
                System.err.println("Cannot find StatisticsDialog.fxml");
                showAlert("Erreur", "Le fichier StatisticsDialog.fxml est introuvable", Alert.AlertType.ERROR);
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Statistiques");
            Scene scene = new Scene(root);
            
            // Try to load CSS with correct path
            URL cssUrl = getClass().getResource("/css/statistics.css");
            if (cssUrl == null) {
                cssUrl = getClass().getClassLoader().getResource("css/statistics.css");
            }
            
            if (cssUrl != null) {
                String cssExternalForm = cssUrl.toExternalForm();
                System.out.println("Loading CSS from: " + cssExternalForm);
                scene.getStylesheets().add(cssExternalForm);
            } else {
                System.err.println("Warning: Could not load statistics.css from any location");
                System.err.println("Tried paths:");
                System.err.println("- /css/statistics.css");
                System.err.println("- css/statistics.css");
            }
            
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(statisticsBtn.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les statistiques: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleShowAds() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MarketPlace/AdUpload.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Gestion des Publicités");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(adsBtn.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de gestion des publicités: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void clearFields() {
        nomField.clear();
        quantiteField.clear();
        prixTotalField.clear();
        descriptionArea.clear();
        imageField.clear();
        categorieCombo.setValue(null);
        stockDCombo.setValue(null);
        imagePreview.setImage(null);
        selectedImageFile = null;
        produitTable.getSelectionModel().clearSelection();
        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        approveBtn.setDisable(true);
        disapproveBtn.setDisable(true);
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        
        if (nomField.getText().isEmpty()) {
            errors.append("Product name is required\n");
        }
        
        try {
            int quantite = Integer.parseInt(quantiteField.getText());
            if (quantite < 0) {
                errors.append("Quantity must be positive\n");
            }
        } catch (NumberFormatException e) {
            errors.append("Quantity must be a valid number\n");
        }
        
        try {
            double prix = Double.parseDouble(prixTotalField.getText());
            if (prix <= 0) {
                errors.append("Price must be greater than 0\n");
            }
        } catch (NumberFormatException e) {
            errors.append("Price must be a valid number\n");
        }
        
        if (descriptionArea.getText().isEmpty()) {
            errors.append("Description is required\n");
        }
        
        if (categorieCombo.getValue() == null) {
            errors.append("Category is required\n");
        }
        
        if (stockDCombo.getValue() == null) {
            errors.append("Stock status is required\n");
        }
        
        if (errors.length() > 0) {
            showAlert("Validation Error", errors.toString(), Alert.AlertType.ERROR);
            return false;
        }
        
        return true;
    }

    private Produit createProduitFromInput() throws IOException {
        String nom = nomField.getText().trim();
        int quantite = Integer.parseInt(quantiteField.getText().trim());
        double prixTotal = Double.parseDouble(prixTotalField.getText().trim());
        String description = descriptionArea.getText().trim();
        String categorie = categorieCombo.getValue();
        String stockD = stockDCombo.getValue();
        
        // Handle image upload to Cloudinary
        String imageUrl = "";
        if (selectedImageFile != null) {
            try {
                imageUrl = CloudinaryUtil.getInstance().uploadImage(selectedImageFile);
            } catch (IOException e) {
                throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage());
            }
        }

        return new Produit(nom, quantite, prixTotal, description, imageUrl, 
                          categorie, stockD, false, new Date());
    }

    private void handleImageUpdate(Produit existingProduit) throws IOException {
        // If a new image is selected, upload it and delete the old one
        if (selectedImageFile != null) {
            // Delete old image if it exists
            String oldPublicId = CloudinaryUtil.getInstance().getPublicIdFromUrl(existingProduit.getImage());
            if (oldPublicId != null) {
                try {
                    CloudinaryUtil.getInstance().deleteImage(oldPublicId);
                } catch (IOException e) {
                    System.err.println("Failed to delete old image: " + e.getMessage());
                }
            }
            
            // Upload new image
            String newImageUrl = CloudinaryUtil.getInstance().uploadImage(selectedImageFile);
            existingProduit.setImage(newImageUrl);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Make error alerts resizable for better readability of long messages
        if (type == Alert.AlertType.ERROR) {
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setResizable(true);
        }
        
        alert.showAndWait();
    }
} 