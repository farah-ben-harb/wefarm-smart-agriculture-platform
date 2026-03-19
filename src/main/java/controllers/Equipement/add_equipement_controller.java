package controllers.Equipement;

import entities.Equipement;
import services.EquipementService;
import services.EquipementServiceImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class add_equipement_controller {

    @FXML private Button ajouter;
    @FXML private Button effacer;
    @FXML private Button exit;
    @FXML private TextField nom_equipement;
    @FXML private TextField quantite_disponible;
    @FXML private TextField prix;
    @FXML private TextField image;
    @FXML private TextField category;
    @FXML private ImageView imagePreview;
    @FXML private Text nom_error;
    @FXML private Text quantite_error;
    @FXML private Text prix_error;
    @FXML private Text image_error;
    @FXML private Text category_error;

    private boolean isEditMode = false;
    private Equipement equipementToEdit;
    private Runnable onSaveCallback; 
    
    private EquipementService equipementService = new EquipementServiceImpl();

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void initData(Equipement e) {
        if (e != null) {
            this.equipementToEdit = e;
            this.isEditMode = true;
            ajouter.setText("Modifier");

            nom_equipement.setText(e.getNomEquipement());
            quantite_disponible.setText(String.valueOf(e.getQuantiteDisponible()));
            prix.setText(String.valueOf(e.getPrix()));
            image.setText(e.getImage());
            category.setText(e.getCategory());
        }
    }

    @FXML
    void initialize() {
        // Ajouter des écouteurs pour valider les champs en temps réel
        nom_equipement.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNomEquipement();
        });
        
        quantite_disponible.textProperty().addListener((observable, oldValue, newValue) -> {
            validateQuantite();
        });
        
        prix.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePrix();
        });
        
        image.textProperty().addListener((observable, oldValue, newValue) -> {
            validateImage();
        });
        
        category.textProperty().addListener((observable, oldValue, newValue) -> {
            validateCategory();
        });
        
        image.setOnMouseClicked(event -> {
            openFileChooser();
        });
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                File imagesDir = new File("images");
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs(); // Create directory if it doesn't exist
                }

                File destinationFile = new File(imagesDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                image.setText(destinationFile.getAbsolutePath());
            } catch (Exception e) {
                showError("Error saving image: " + e.getMessage());
                System.out.println(e.getMessage());
            }
        } else {
            showError("No file selected.");
        }
    }

    @FXML
    void ajouterequipement(ActionEvent event) {
        // Valider tous les champs avant de procéder
        if (!validateAllFields()) {
            return; // Ne pas continuer si la validation échoue
        }
        
        String nom = nom_equipement.getText().trim();
        int quantite = Integer.parseInt(quantite_disponible.getText().trim());
        double prixValue = Double.parseDouble(prix.getText().trim());
        String imageValue = image.getText().trim();
        String categoryValue = category.getText().trim();

        try {
            if (isEditMode && equipementToEdit != null) {
                // Update existing equipement using the service
                Equipement updatedEquipement = new Equipement(
                    equipementToEdit.getId(),
                    equipementToEdit.getFournisseurId(), // Utiliser le fournisseurId existant
                    nom,
                    quantite,
                    prixValue,
                    imageValue,
                    categoryValue
                );
                equipementService.updateEquipement(updatedEquipement);
                showAlert("Équipement modifié avec succès !");
            } else {
                // Create new equipement using the service
                Equipement newEquipement = new Equipement(
                    null, // Utiliser null pour le fournisseurId ou une valeur par défaut
                    nom,
                    quantite,
                    prixValue,
                    imageValue,
                    categoryValue
                );
                equipementService.addEquipement(newEquipement);
                showAlert("Équipement ajouté avec succès !");
            }

            if (onSaveCallback != null) onSaveCallback.run();

            // Fermer la fenêtre
            Stage stage = (Stage) ajouter.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur : " + e.getMessage());
        }
    }

    private boolean validateNomEquipement() {
        String nom = nom_equipement.getText().trim();
        if (nom.isEmpty()) {
            nom_error.setText("Le nom ne peut pas être vide");
            nom_error.setVisible(true);
            return false;
        }
        nom_error.setVisible(false);
        return true;
    }
    
    private boolean validateQuantite() {
        String quantiteStr = quantite_disponible.getText().trim();
        if (quantiteStr.isEmpty()) {
            quantite_error.setText("La quantité ne peut pas être vide");
            quantite_error.setVisible(true);
            return false;
        }
        try {
            int quantite = Integer.parseInt(quantiteStr);
            if (quantite < 0) {
                quantite_error.setText("La quantité doit être positive");
                quantite_error.setVisible(true);
                return false;
            }
            quantite_error.setVisible(false);
            return true;
        } catch (NumberFormatException e) {
            quantite_error.setText("La quantité doit être un nombre entier");
            quantite_error.setVisible(true);
            return false;
        }
    }
    
    private boolean validatePrix() {
        String prixStr = prix.getText().trim();
        if (prixStr.isEmpty()) {
            prix_error.setText("Le prix ne peut pas être vide");
            prix_error.setVisible(true);
            return false;
        }
        try {
            double prixValue = Double.parseDouble(prixStr);
            if (prixValue < 0) {
                prix_error.setText("Le prix doit être positif");
                prix_error.setVisible(true);
                return false;
            }
            prix_error.setVisible(false);
            return true;
        } catch (NumberFormatException e) {
            prix_error.setText("Le prix doit être un nombre");
            prix_error.setVisible(true);
            return false;
        }
    }
    
    private boolean validateImage() {
        String imageStr = image.getText().trim();
        if (imageStr.isEmpty()) {
            image_error.setText("L'image ne peut pas être vide");
            image_error.setVisible(true);
            return false;
        }
        image_error.setVisible(false);
        return true;
    }
    
    private boolean validateCategory() {
        String categoryStr = category.getText().trim();
        if (categoryStr.isEmpty()) {
            category_error.setText("La catégorie ne peut pas être vide");
            category_error.setVisible(true);
            return false;
        }
        category_error.setVisible(false);
        return true;
    }
    
    private boolean validateAllFields() {
        boolean isNomValid = validateNomEquipement();
        boolean isQuantiteValid = validateQuantite();
        boolean isPrixValid = validatePrix();
        boolean isImageValid = validateImage();
        boolean isCategoryValid = validateCategory();
        
        return isNomValid && isQuantiteValid && isPrixValid && isImageValid && isCategoryValid;
    }
    
    private void showAlert(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    void effacerequipement(ActionEvent event) {
        nom_equipement.clear();
        quantite_disponible.clear();
        prix.clear();
        image.clear();
        category.clear();
        
        // Masquer tous les messages d'erreur
        nom_error.setVisible(false);
        quantite_error.setVisible(false);
        prix_error.setVisible(false);
        image_error.setVisible(false);
        category_error.setVisible(false);
    }
}