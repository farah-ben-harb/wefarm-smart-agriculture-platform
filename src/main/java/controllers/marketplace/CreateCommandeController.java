package controllers.marketplace;
import entities.Produit;
import entities.Commande;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.CommandeService;
import services.CommandeServiceImpl;
import services.ProduitService;
import services.ProduitServiceImpl;
import java.util.Date;

public class CreateCommandeController {
    @FXML private Label productNameLabel;
    @FXML private Label productPriceLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private ComboBox<String> paymentModeCombo;
    @FXML private TextArea deliveryAddressArea;
    @FXML private Label totalPriceLabel;

    private Produit produit;
    private CommandeService commandeService;

    @FXML
    public void initialize() {
        ProduitService produitService = new ProduitServiceImpl();
        commandeService = new CommandeServiceImpl(produitService);
        
        // Initialize payment modes
        paymentModeCombo.getItems().addAll(
            "Carte bancaire",
            "PayPal",
            "Virement bancaire",
            "Espèces à la livraison"
        );

        // Set up quantity spinner
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        quantitySpinner.setValueFactory(valueFactory);

        // Update total price when quantity changes
        quantitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> 
            updateTotalPrice());
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        productNameLabel.setText(produit.getNom());
        productPriceLabel.setText(String.format("%.2f €", produit.getPrixTotal()));
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        if (produit != null && quantitySpinner.getValue() != null) {
            double total = produit.getPrixTotal() * quantitySpinner.getValue();
            totalPriceLabel.setText(String.format("%.2f €", total));
        }
    }

    @FXML
    private void handleConfirm() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Create new order
            Commande commande = new Commande();
            commande.setProduitId(produit.getId());
            commande.setClientId(1); // Set default client ID since we don't have user authentication yet
            commande.setDateCommande(new Date());
            commande.setQuantite(quantitySpinner.getValue());
            commande.setStatus("Pending"); // Explicitly set status to Pending
            commande.setModePaiement(paymentModeCombo.getValue());
            commande.setAdresseLivraison(deliveryAddressArea.getText());
            commande.setPrixTotal(produit.getPrixTotal() * quantitySpinner.getValue());

            // Save the order
            commandeService.createCommande(commande);
            
            // Show success message
            showAlert("Success", "Order created successfully!", Alert.AlertType.INFORMATION);
            
            // Clear fields
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to create order: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        if (paymentModeCombo.getValue() == null || paymentModeCombo.getValue().isEmpty()) {
            showError("Erreur", "Veuillez sélectionner un mode de paiement");
            return false;
        }

        if (deliveryAddressArea.getText() == null || deliveryAddressArea.getText().trim().isEmpty()) {
            showError("Erreur", "Veuillez saisir une adresse de livraison");
            return false;
        }

        return true;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) productNameLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        quantitySpinner.getValueFactory().setValue(1);
        paymentModeCombo.setValue(null);
        deliveryAddressArea.setText("");
        updateTotalPrice();
    }
} 