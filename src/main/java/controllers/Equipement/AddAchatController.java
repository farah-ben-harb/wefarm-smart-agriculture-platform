package controllers.Equipement;

import entities.Achat;
import entities.Equipement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.AchatService;
import services.AchatServiceImpl;
import services.EquipementService;
import services.EquipementServiceImpl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AddAchatController {

    @FXML
    private ComboBox<Equipement> equipementComboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField totalField;

    @FXML
    private Button saveButton;

    @FXML
    private Button resetButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label equipementErrorLabel;

    @FXML
    private Label dateErrorLabel;

    @FXML
    private Label totalErrorLabel;

    private AchatService achatService = new AchatServiceImpl();
    private EquipementService equipementService = new EquipementServiceImpl();

    private Achat currentAchat;
    private boolean isEditMode = false;
    private Runnable onSaveCallback;

    @FXML
    void initialize() {
        loadEquipements();

        // Add validation listeners
        totalField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                totalField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            validateTotalField();
        });

        // Add validation for equipement selection
        equipementComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateEquipementSelection();
            // Auto-fill total with the price of the selected equipment if not in edit mode
            if (newValue != null && !isEditMode) {
                totalField.setText(String.valueOf((int)newValue.getPrix()));
            }
        });

        // Add validation for date selection
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDateSelection();
        });

        // Set default date to today
        datePicker.setValue(LocalDate.now());
    }

    private boolean validateTotalField() {
        if (totalField.getText().isEmpty()) {
            totalErrorLabel.setText("Le montant total est obligatoire");
            totalErrorLabel.setVisible(true);
            totalErrorLabel.setManaged(true);
            return false;
        }

        try {
            int total = Integer.parseInt(totalField.getText());
            if (total <= 0) {
                totalErrorLabel.setText("Le montant doit être supérieur à 0");
                totalErrorLabel.setVisible(true);
                totalErrorLabel.setManaged(true);
                return false;
            }
        } catch (NumberFormatException e) {
            totalErrorLabel.setText("Veuillez entrer un nombre valide");
            totalErrorLabel.setVisible(true);
            totalErrorLabel.setManaged(true);
            return false;
        }

        totalErrorLabel.setVisible(false);
        totalErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateEquipementSelection() {
        if (equipementComboBox.getValue() == null) {
            equipementErrorLabel.setText("Veuillez sélectionner un équipement");
            equipementErrorLabel.setVisible(true);
            equipementErrorLabel.setManaged(true);
            return false;
        }
        
        // For new purchases, check if the equipment has available quantity
        if (!isEditMode && equipementComboBox.getValue().getQuantiteDisponible() <= 0) {
            equipementErrorLabel.setText("Cet équipement n'est plus en stock");
            equipementErrorLabel.setVisible(true);
            equipementErrorLabel.setManaged(true);
            return false;
        }
        
        equipementErrorLabel.setVisible(false);
        equipementErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateDateSelection() {
        if (datePicker.getValue() == null) {
            dateErrorLabel.setText("Veuillez sélectionner une date");
            dateErrorLabel.setVisible(true);
            dateErrorLabel.setManaged(true);
            return false;
        }
        
        // Check if date is not in the future
        if (datePicker.getValue().isAfter(LocalDate.now())) {
            dateErrorLabel.setText("La date ne peut pas être dans le futur");
            dateErrorLabel.setVisible(true);
            dateErrorLabel.setManaged(true);
            return false;
        }
        
        dateErrorLabel.setVisible(false);
        dateErrorLabel.setManaged(false);
        return true;
    }

    private void loadEquipements() {
        try {
            // Get all equipments from database
            List<Equipement> equipements = equipementService.getAllEquipements();
            
            if (equipements == null || equipements.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Aucun équipement");
                alert.setHeaderText(null);
                alert.setContentText("Aucun équipement disponible. Veuillez d'abord ajouter des équipements.");
                alert.showAndWait();
                return;
            }
            
            // Create observable list for the ComboBox
            ObservableList<Equipement> equipementList = FXCollections.observableArrayList(equipements);
            equipementComboBox.setItems(equipementList);
            
            // Configure how items are displayed in the ComboBox
            equipementComboBox.setCellFactory(param -> new ListCell<Equipement>() {
                @Override
                protected void updateItem(Equipement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNomEquipement() + " - " + item.getPrix());
                    }
                }
            });
            

            equipementComboBox.setButtonCell(new ListCell<Equipement>() {
                @Override
                protected void updateItem(Equipement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNomEquipement() + " - " + item.getPrix());
                    }
                }
            });
            
            // Auto-select first equipment in the list if not in edit mode
            if (!isEditMode && !equipementList.isEmpty()) {
                equipementComboBox.setValue(equipementList.get(0));
                
                // Auto-fill total with the price of the selected equipment
                if (equipementComboBox.getValue() != null) {
                    totalField.setText(String.valueOf((int)equipementComboBox.getValue().getPrix()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors du chargement des équipements: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void initData(Achat achat) {
        this.currentAchat = achat;
        this.isEditMode = (achat != null);

        if (isEditMode) {
            // First load equipment list if not already loaded
            if (equipementComboBox.getItems() == null || equipementComboBox.getItems().isEmpty()) {
                loadEquipements();
            }
            
            // Select the corresponding equipment in the ComboBox
            final int equipementIdToSelect = achat.getEquipementId();
            
            for (Equipement e : equipementComboBox.getItems()) {
                if (e.getId() == equipementIdToSelect) {
                    equipementComboBox.setValue(e);
                    break;
                }
            }
            
            // Convert Date to LocalDate for the DatePicker
            if (achat.getDate() != null) {
                // Fix for java.sql.Date conversion issue
                try {
                    // Convert using time in milliseconds
                    LocalDate localDate = new java.util.Date(achat.getDate().getTime())
                            .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    datePicker.setValue(localDate);
                } catch (Exception e) {
                    // Fallback method if conversion fails
                    try {
                        // Direct conversion for java.sql.Date
                        if (achat.getDate() instanceof java.sql.Date) {

                            long epochDay = achat.getDate().getTime() / (1000 * 60 * 60 * 24);
                            LocalDate localDate = LocalDate.ofEpochDay(epochDay);
                            datePicker.setValue(localDate);
                        } else {
                            // Default to current date
                            datePicker.setValue(LocalDate.now());
                        }
                    } catch (Exception ex) {
                        // Last resort - use current date
                        System.err.println("Error converting date: " + ex.getMessage());
                        datePicker.setValue(LocalDate.now());
                    }
                }
            }
            
            // Set total
            totalField.setText(String.valueOf(achat.getTotal()));
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeStage();
    }

    @FXML
    void handleReset(ActionEvent event) {
        // If in edit mode, keep the selected equipment
        if (!isEditMode) {
            equipementComboBox.setValue(null);
        }
        
        // Reset date to today
        datePicker.setValue(LocalDate.now());
        
        // Clear total
        totalField.setText("");
        
        // Hide all error messages
        equipementErrorLabel.setVisible(false);
        equipementErrorLabel.setManaged(false);
        dateErrorLabel.setVisible(false);
        dateErrorLabel.setManaged(false);
        totalErrorLabel.setVisible(false);
        totalErrorLabel.setManaged(false);
        
        // If not in edit mode and we have equipment items, select the first one
        if (!isEditMode && equipementComboBox.getItems() != null && 
            !equipementComboBox.getItems().isEmpty()) {
            equipementComboBox.setValue(equipementComboBox.getItems().get(0));
            
            // Auto-fill total with the price of the selected equipment
            if (equipementComboBox.getValue() != null) {
                totalField.setText(String.valueOf((int)equipementComboBox.getValue().getPrix()));
            }
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        try {
            Equipement selectedEquipement = equipementComboBox.getValue();
            LocalDate selectedDate = datePicker.getValue();
            int totalValue = Integer.parseInt(totalField.getText().trim());
            
            // Convert LocalDate to Date
            Date achatDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            if (isEditMode && currentAchat != null) {
                // Update existing achat
                Achat updatedAchat = new Achat(
                    currentAchat.getId(),
                    selectedEquipement.getId(),
                    achatDate,
                    totalValue
                );
                
                try {
                    achatService.updateAchat(updatedAchat);
                    
                    // Show success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Achat modifié avec succès!");
                    successAlert.showAndWait();
                    
                    // Run callback to refresh parent view
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    
                    // Close the window
                    closeStage();
                } catch (RuntimeException ex) {
                    // Handle specific error for unique constraint
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    
                    if (ex.getMessage().contains("Duplicate entry") || ex.getMessage().contains("UNIQ_26A98456806F0F5C")) {
                        alert.setContentText("Erreur de base de données: Contrainte unique sur l'équipement.\n\n" +
                                "Pour permettre l'utilisation du même équipement dans plusieurs achats, " +
                                "vous devez modifier la structure de votre base de données pour supprimer " +
                                "la contrainte d'unicité sur la colonne equipement_id dans la table achat.");
                    } else {
                        alert.setContentText("Erreur lors de la modification: " + ex.getMessage());
                    }
                    
                    alert.showAndWait();
                }
            } else {

                if (selectedEquipement.getQuantiteDisponible() <= 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Cet équipement n'est plus en stock");
                    alert.showAndWait();
                    return;
                }
                
                // Create new achat
                Achat newAchat = new Achat(
                    selectedEquipement.getId(),
                    achatDate,
                    totalValue
                );
                
                try {
                    achatService.addAchat(newAchat);
                    
                    // Update equipment quantity
                    updateEquipmentQuantity(selectedEquipement);
                    
                    // Show success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Achat ajouté avec succès!");
                    successAlert.showAndWait();
                    

                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    

                    closeStage();
                } catch (RuntimeException ex) {

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    
                    if (ex.getMessage().contains("Duplicate entry") || ex.getMessage().contains("UNIQ_26A98456806F0F5C")) {
                        alert.setContentText("Erreur de base de données: Contrainte unique sur l'équipement.\n\n" +
                                "Pour permettre l'utilisation du même équipement dans plusieurs achats, " +
                                "vous devez modifier la structure de votre base de données pour supprimer " +
                                "la contrainte d'unicité sur la colonne equipement_id dans la table achat.");
                    } else {
                        alert.setContentText("Erreur lors de l'ajout: " + ex.getMessage());
                    }
                    
                    alert.showAndWait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'enregistrement: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate total field
        if (!validateTotalField()) {
            isValid = false;
        }

        // Validate equipement selection
        if (!validateEquipementSelection()) {
            isValid = false;
        }

        // Validate date selection
        if (!validateDateSelection()) {
            isValid = false;
        }

        return isValid;
    }
    
    private void updateEquipmentQuantity(Equipement equipement) {
        try {
            if (equipement == null) {
                return;
            }
            
            // Reduce equipment quantity by 1 (or more if needed)
            int newQuantity = equipement.getQuantiteDisponible() - 1;
            if (newQuantity >= 0) {
                equipement.setQuantiteDisponible(newQuantity);
                equipementService.updateEquipement(equipement);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Impossible de mettre à jour la quantité: stock insuffisant");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de la mise à jour de la quantité: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void closeStage() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
