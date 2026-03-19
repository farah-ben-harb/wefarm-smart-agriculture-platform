package controllers.marketplace;
import entities.Commande;
import entities.Produit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.CommandeService;
import services.CommandeServiceImpl;
import services.ProduitServiceImpl;

import java.io.IOException;
import java.util.Date;

public class OrderDialogController {
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private ComboBox<String> paymentModeCombo;
    @FXML private TextArea deliveryAddressArea;
    @FXML private Label totalPriceLabel;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private Produit produit;
    private final CommandeService commandeService;
    private Stage dialogStage;

    public OrderDialogController() {
        this.commandeService = new CommandeServiceImpl(new ProduitServiceImpl());
    }

    @FXML
    public void initialize() {
        // Initialize payment modes
        paymentModeCombo.getItems().addAll(
            "Carte bancaire",
            "PayPal",
            "Virement bancaire",
            "Espèces à la livraison"
        );
        paymentModeCombo.setValue("Carte bancaire");

        // Initialize quantity spinner with value factory
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        quantitySpinner.setValueFactory(valueFactory);

        // Update total price when quantity changes
        quantitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> 
            updateTotalPrice());
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        updateTotalPrice();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
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
            commande.setStatus("Pending");
            commande.setModePaiement(paymentModeCombo.getValue());
            commande.setAdresseLivraison(deliveryAddressArea.getText());
            commande.setPrixTotal(produit.getPrixTotal() * quantitySpinner.getValue());
            
            // Set the product object
            commande.setProduit(produit);

            // Save the order
            commandeService.createCommande(commande);

            // If payment mode is "Carte bancaire", show Stripe payment form
            if ("Carte bancaire".equals(paymentModeCombo.getValue())) {
                showStripePaymentForm(commande);
            } else {
                showSuccessAlert();
                closeDialog();
            }
        } catch (Exception e) {
            showErrorAlert("Erreur lors de la création de la commande: " + e.getMessage());
        }
    }

    private void showStripePaymentForm(Commande commande) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MarketPlace/StripePayment.fxml"));
            Parent root = loader.load();

            StripePaymentController paymentController = loader.getController();
            paymentController.setCommande(commande);

            Stage paymentStage = new Stage();
            paymentStage.setTitle("Paiement");
            paymentStage.setScene(new Scene(root));
            paymentStage.initModality(Modality.APPLICATION_MODAL);
            paymentStage.initOwner(dialogStage);

            // Close the order dialog
            closeDialog();

            // Show the payment form
            paymentStage.showAndWait();
        } catch (IOException e) {
            showErrorAlert("Erreur lors de l'ouverture du formulaire de paiement: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateInputs() {
        if (deliveryAddressArea.getText().trim().isEmpty()) {
            showErrorAlert("Veuillez entrer une adresse de livraison");
            return false;
        }

        if (paymentModeCombo.getValue() == null) {
            showErrorAlert("Veuillez sélectionner un mode de paiement");
            return false;
        }

        return true;
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Commande créée avec succès!");
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
} 