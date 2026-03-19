package controllers.marketplace;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import entities.Commande;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import services.CommandeService;
import services.CommandeServiceImpl;
import services.ProduitServiceImpl;

public class StripePaymentController {
    @FXML private WebView stripeWebView;
    @FXML private Label productNameLabel;
    @FXML private Label quantityLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Button payButton;
    @FXML private Button cancelButton;

    private Commande commande;
    private final CommandeService commandeService;
    private static final String STRIPE_PUBLIC_KEY = "pk_test_51Qz8co064LyKp30PvI4HN9ibEIQPmXxYgCOdmYKdHqXpodYQ61YWtEMiCp5QmpdIwJDqLhGqkSNdytFFjNHwubvr00Ji9BtLJc";
    private static final String STRIPE_SECRET_KEY = "sk_test_51Qz8co064LyKp30Px4MUaaBK1cWWmPgIRZo0EevaInr3LWcYY6WQ2jToIG78E11O7WS3LJwMCdRhsiamxzDVoFqc002fyND85N";

    public StripePaymentController() {
        this.commandeService = new CommandeServiceImpl(new ProduitServiceImpl());
    }

    @FXML
    public void initialize() {
        // Load the Stripe Elements form
        stripeWebView.getEngine().load(getClass().getResource("/html/stripe-form.html").toExternalForm());
        
        // Add JavaScript bridge
        stripeWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) stripeWebView.getEngine().executeScript("window");
                    window.setMember("javaController", this);
                    
                    // Set Stripe key
                    stripeWebView.getEngine().executeScript(
                        String.format("window.stripeKey = '%s';", STRIPE_PUBLIC_KEY)
                    );
                    
                    System.out.println("JavaScript bridge initialized successfully");
                    
                    // If we already have a commande, create the payment intent
                    if (commande != null) {
                        createPaymentIntent();
                    }
                } catch (Exception e) {
                    System.err.println("Error initializing JavaScript bridge: " + e.getMessage());
                    e.printStackTrace();
                    showError("Erreur de paiement", "Impossible d'initialiser le formulaire de paiement: " + e.getMessage());
                }
            }
        });
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
        updateOrderSummary();
        if (stripeWebView.getEngine().getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
            createPaymentIntent();
        }
    }

    private void updateOrderSummary() {
        if (commande.getProduit() != null) {
            productNameLabel.setText(commande.getProduit().getNom());
        } else {
            // If product is not available, show the product ID instead
            productNameLabel.setText("Produit #" + commande.getProduitId());
        }
        quantityLabel.setText(String.valueOf(commande.getQuantite()));
        totalAmountLabel.setText(String.format("%.2f €", commande.getPrixTotal()));
    }

    private void createPaymentIntent() {
        try {
            // Initialize Stripe with secret key
            Stripe.apiKey = STRIPE_SECRET_KEY;
            
            // Create payment intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (commande.getPrixTotal() * 100)) // Convert to cents
                .setCurrency("eur")
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            String clientSecret = paymentIntent.getClientSecret();
            
            if (clientSecret == null) {
                throw new Exception("Client secret is null");
            }
            
            System.out.println("Payment intent created successfully");
            
            // Pass the client secret to the frontend
            stripeWebView.getEngine().executeScript(
                String.format("setPaymentIntent('%s')", clientSecret)
            );
        } catch (Exception e) {
            System.err.println("Error creating payment intent: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de paiement", "Impossible d'initialiser le paiement: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayment() {
        stripeWebView.getEngine().executeScript("submitPayment()");
    }

    // Called from JavaScript
    public void onPaymentSuccess() {
        try {
            // Update order status
            commande.setStatus("Paid");
            commandeService.updateCommande(commande);
            
            // Show success message
            showSuccess();
            
            // Close the payment window
            closeWindow();
        } catch (Exception e) {
            showError("Erreur", "Impossible de mettre à jour la commande: " + e.getMessage());
        }
    }

    // Called from JavaScript
    public void onPaymentError(String error) {
        showError("Erreur de paiement", error);
    }

    @FXML
    private void handleCancel() {
        try {
            // Update order status to cancelled
            commande.setStatus("Cancelled");
            commandeService.updateCommande(commande);
            closeWindow();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'annuler la commande: " + e.getMessage());
        }
    }

    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Paiement effectué avec succès!");
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) payButton.getScene().getWindow();
        stage.close();
    }
} 