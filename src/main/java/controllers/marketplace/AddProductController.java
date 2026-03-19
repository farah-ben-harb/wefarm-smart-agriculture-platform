package controllers.marketplace;

import entities.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ProduitService;
import services.ProduitServiceImpl;
import utils.CloudinaryUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class AddProductController {
    @FXML
    private TextField nomField;
    @FXML
    private TextField quantiteField;
    @FXML
    private TextField prixField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label imagePathLabel;
    @FXML
    private ComboBox<String> categorieCombo;
    @FXML
    private ComboBox<String> stockDCombo;
    @FXML
    private Button selectImageBtn;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private ProgressIndicator uploadProgress;

    private final ProduitService produitService;
    private File selectedImageFile;
    private String cloudinaryUrl;

    public AddProductController() {
        this.produitService = new ProduitServiceImpl();
    }

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupValidation();
        setupButtonActions();
        uploadProgress.setVisible(false);
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

    private void setupValidation() {
        // Validation pour les champs numériques
        quantiteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantiteField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        prixField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                prixField.setText(newValue.replaceAll("[^\\d.]", ""));
            }
        });
    }

    private void setupButtonActions() {
        selectImageBtn.setOnAction(e -> handleSelectImage());
        saveBtn.setOnAction(e -> handleSave());
        cancelBtn.setOnAction(e -> handleCancel());
    }

    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(selectImageBtn.getScene().getWindow());
        if (selectedFile != null) {
            selectedImageFile = selectedFile;
            imagePathLabel.setText(selectedFile.getName());
        }
    }

    private void handleSave() {
        try {
            // Validation des champs
            if (!validateFields()) {
                return;
            }

            // Désactiver le bouton pendant le traitement
            saveBtn.setDisable(true);
            uploadProgress.setVisible(true);

            // Upload de l'image vers Cloudinary
            new Thread(() -> {
                try {
                    cloudinaryUrl = CloudinaryUtil.getInstance().uploadImage(selectedImageFile);
                    
                    // Retour au thread JavaFX pour mettre à jour l'interface
                    javafx.application.Platform.runLater(() -> {
                        try {
                            saveProduit();
                        } catch (Exception e) {
                            showError("Erreur", "Erreur lors de la sauvegarde du produit: " + e.getMessage());
                        } finally {
                            saveBtn.setDisable(false);
                            uploadProgress.setVisible(false);
                        }
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        showError("Erreur", "Erreur lors de l'upload de l'image: " + e.getMessage());
                        saveBtn.setDisable(false);
                        uploadProgress.setVisible(false);
                    });
                }
            }).start();

        } catch (Exception e) {
            showError("Erreur", "Impossible de sauvegarder le produit: " + e.getMessage());
            saveBtn.setDisable(false);
            uploadProgress.setVisible(false);
        }
    }

    private void saveProduit() {
        // Création du produit
        Produit produit = new Produit();
        produit.setNom(nomField.getText().trim());
        produit.setQuantite(Integer.parseInt(quantiteField.getText().trim()));
        produit.setPrixTotal(Double.parseDouble(prixField.getText().trim()));
        produit.setDescription(descriptionArea.getText().trim());
        produit.setImage(cloudinaryUrl);  // Using Cloudinary URL instead of local path
        produit.setCategorie(categorieCombo.getValue());
        produit.setStockD(stockDCombo.getValue());
        produit.setApproved(false);
        produit.setUpdatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        // Sauvegarde du produit
        produitService.createProduit(produit);

        // Fermeture de la fenêtre
        closeWindow();
    }

    private boolean validateFields() {
        if (nomField.getText().trim().isEmpty()) {
            showError("Erreur", "Le nom du produit est requis");
            return false;
        }

        if (quantiteField.getText().trim().isEmpty()) {
            showError("Erreur", "La quantité est requise");
            return false;
        }

        if (prixField.getText().trim().isEmpty()) {
            showError("Erreur", "Le prix est requis");
            return false;
        }

        if (categorieCombo.getValue() == null) {
            showError("Erreur", "La catégorie est requise");
            return false;
        }

        if (stockDCombo.getValue() == null) {
            showError("Erreur", "Le statut du stock est requis");
            return false;
        }

        if (selectedImageFile == null) {
            showError("Erreur", "Une image est requise");
            return false;
        }

        return true;
    }

    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 