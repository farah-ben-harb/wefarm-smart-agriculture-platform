package controllers.marketplace;

import entities.Commande;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.CommandeService;
import services.CommandeServiceImpl;
import services.ProduitService;
import services.ProduitServiceImpl;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProduitCommandeManagementController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ProduitCommandeManagementController.class.getName());
    
    // Services
    private final ProduitService produitService = new ProduitServiceImpl();
    private final CommandeService commandeService = new CommandeServiceImpl(produitService);
    
    // Observable Lists
    private final ObservableList<Produit> produitsList = FXCollections.observableArrayList();
    private final ObservableList<Commande> commandesList = FXCollections.observableArrayList();
    
    // Product UI Components
    @FXML private TabPane mainTabPane;
    @FXML private TextField productSearchField;
    @FXML private Button productFilterBtn;
    @FXML private Button refreshProductsBtn;
    @FXML private Button addProductBtn;
    @FXML private TableView<Produit> productTable;
    @FXML private TableColumn<Produit, Integer> idCol;
    @FXML private TableColumn<Produit, String> nomCol;
    @FXML private TableColumn<Produit, Integer> quantiteCol;
    @FXML private TableColumn<Produit, Double> prixTotalCol;
    @FXML private TableColumn<Produit, String> categorieCol;
    @FXML private TableColumn<Produit, String> stockDCol;
    @FXML private TableColumn<Produit, Boolean> approvedCol;
    
    @FXML private TextField nomField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TextField quantiteField;
    @FXML private TextField prixTotalField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> stockDCombo;
    @FXML private TextField imageField;
    @FXML private Button imageChooserBtn;
    @FXML private ImageView imagePreview;
    @FXML private Button addBtn;
    @FXML private Button updateBtn;
    @FXML private Button deleteBtn;
    @FXML private Button clearBtn;
    @FXML private Button approveBtn;
    @FXML private Button disapproveBtn;
    
    // Order UI Components
    @FXML private TextField orderSearchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Button refreshOrdersBtn;
    @FXML private Button addOrderBtn;
    @FXML private TableView<Commande> orderTable;
    @FXML private TableColumn<Commande, Integer> orderIdCol;
    @FXML private TableColumn<Commande, Date> orderDateCol;
    @FXML private TableColumn<Commande, Integer> orderProductCol;
    @FXML private TableColumn<Commande, Integer> orderClientCol;
    @FXML private TableColumn<Commande, Integer> orderQuantityCol;
    @FXML private TableColumn<Commande, String> orderStatusCol;
    @FXML private TableColumn<Commande, String> orderPaymentCol;
    @FXML private TableColumn<Commande, Double> orderTotalCol;
    
    @FXML private ComboBox<Produit> orderProductCombo;
    @FXML private ComboBox<String> orderClientCombo;
    @FXML private TextField orderQuantityField;
    @FXML private ComboBox<String> orderStatusCombo;
    @FXML private ComboBox<String> orderPaymentCombo;
    @FXML private TextField orderTotalField;
    @FXML private TextField orderAddressField;
    @FXML private Button addOrderBtn2;
    @FXML private Button updateOrderBtn;
    @FXML private Button deleteOrderBtn;
    @FXML private Button clearOrderBtn;
    @FXML private Button confirmOrderBtn;
    @FXML private Button cancelOrderBtn;
    @FXML private Button shipOrderBtn;
    
    // User info
    @FXML private Label usernameLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Initialize product table columns
            setupProductTable();
            
            // Initialize order table columns
            setupOrderTable();
            
            // Initialize combo boxes
            setupComboBoxes();
            
            // Load data
            loadProduits();
            loadCommandes();
            
            // Setup search functionality
            setupSearchFunctionality();
            
            // Setup selection listeners
            setupSelectionListeners();
            
            // Set username (for demo purposes)
            usernameLabel.setText("Admin");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing controller", e);
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation", "Une erreur est survenue lors de l'initialisation de l'interface.", e.getMessage());
        }
    }
    
    private void setupProductTable() {
        // Set up cell value factories
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        quantiteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixTotalCol.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        stockDCol.setCellValueFactory(new PropertyValueFactory<>("stockD"));
        approvedCol.setCellValueFactory(new PropertyValueFactory<>("approved"));
        
        // Format the approved column to show "Oui" or "Non"
        approvedCol.setCellFactory(column -> new TableCell<Produit, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Oui" : "Non");
                }
            }
        });
    }
    
    private void setupOrderTable() {
        // Set up cell value factories
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
        orderProductCol.setCellValueFactory(new PropertyValueFactory<>("produitId"));
        orderClientCol.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        orderQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        orderStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderPaymentCol.setCellValueFactory(new PropertyValueFactory<>("modePaiement"));
        orderTotalCol.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        
        // Format the date column
        orderDateCol.setCellFactory(column -> new TableCell<Commande, Date>() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });
    }
    
    private void setupComboBoxes() {
        // Product categories
        categorieCombo.setItems(FXCollections.observableArrayList(
            "Légumes", "Fruits", "Céréales", "Viandes", "Produits laitiers", "Autres"
        ));
        
        // Stock status
        stockDCombo.setItems(FXCollections.observableArrayList(
            "En stock", "Stock faible", "Rupture de stock"
        ));
        
        // Order status
        orderStatusCombo.setItems(FXCollections.observableArrayList(
            "En attente", "Confirmée", "En préparation", "Expédiée", "Livrée", "Annulée"
        ));
        
        // Payment methods
        orderPaymentCombo.setItems(FXCollections.observableArrayList(
            "Espèces", "Carte bancaire", "Virement", "PayPal"
        ));
        
        // Status filter
        statusFilterCombo.setItems(FXCollections.observableArrayList(
            "Tous", "En attente", "Confirmée", "En préparation", "Expédiée", "Livrée", "Annulée"
        ));
        statusFilterCombo.getSelectionModel().selectFirst();
    }
    
    private void loadProduits() {
        try {
            produitsList.clear();
            produitsList.addAll(produitService.getAllProduits());
            productTable.setItems(produitsList);
            
            // Update product combo box in order form
            orderProductCombo.setItems(produitsList);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading products", e);
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible de charger les produits.", e.getMessage());
        }
    }
    
    private void loadCommandes() {
        try {
            commandesList.clear();
            commandesList.addAll(commandeService.getAllCommandes());
            orderTable.setItems(commandesList);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading orders", e);
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible de charger les commandes.", e.getMessage());
        }
    }
    
    private void setupSearchFunctionality() {
        // Product search
        FilteredList<Produit> filteredProduits = new FilteredList<>(produitsList, p -> true);
        
        productSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredProduits.setPredicate(produit -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                return produit.getNom().toLowerCase().contains(lowerCaseFilter) ||
                       produit.getCategorie().toLowerCase().contains(lowerCaseFilter) ||
                       produit.getStockD().toLowerCase().contains(lowerCaseFilter);
            });
        });
        
        SortedList<Produit> sortedProduits = new SortedList<>(filteredProduits);
        sortedProduits.comparatorProperty().bind(productTable.comparatorProperty());
        productTable.setItems(sortedProduits);
        
        // Order search
        FilteredList<Commande> filteredCommandes = new FilteredList<>(commandesList, c -> true);
        
        orderSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCommandes.setPredicate(commande -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                return String.valueOf(commande.getId()).contains(lowerCaseFilter) ||
                       String.valueOf(commande.getProduitId()).contains(lowerCaseFilter) ||
                       String.valueOf(commande.getClientId()).contains(lowerCaseFilter) ||
                       commande.getStatus().toLowerCase().contains(lowerCaseFilter);
            });
        });
        
        // Status filter
        statusFilterCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filteredCommandes.setPredicate(commande -> {
                if (newValue == null || newValue.equals("Tous")) {
                    return true;
                }
                
                return commande.getStatus().equals(newValue);
            });
        });
        
        SortedList<Commande> sortedCommandes = new SortedList<>(filteredCommandes);
        sortedCommandes.comparatorProperty().bind(orderTable.comparatorProperty());
        orderTable.setItems(sortedCommandes);
    }
    
    private void setupSelectionListeners() {
        // Product selection
        productTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displayProduitDetails(newValue);
            }
        });
        
        // Order selection
        orderTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displayCommandeDetails(newValue);
            }
        });
    }
    
    private void displayProduitDetails(Produit produit) {
        nomField.setText(produit.getNom());
        categorieCombo.setValue(produit.getCategorie());
        quantiteField.setText(String.valueOf(produit.getQuantite()));
        prixTotalField.setText(String.valueOf(produit.getPrixTotal()));
        descriptionArea.setText(produit.getDescription());
        stockDCombo.setValue(produit.getStockD());
        imageField.setText(produit.getImage());
        
        // Load image preview if available
        if (produit.getImage() != null && !produit.getImage().isEmpty()) {
            try {
                File file = new File(produit.getImage());
                if (file.exists()) {
                    imagePreview.setImage(new Image(file.toURI().toString()));
                } else {
                    imagePreview.setImage(new Image(getClass().getResourceAsStream("/images/product-placeholder.png")));
                }
            } catch (Exception e) {
                imagePreview.setImage(new Image(getClass().getResourceAsStream("/images/product-placeholder.png")));
            }
        } else {
            imagePreview.setImage(new Image(getClass().getResourceAsStream("/images/product-placeholder.png")));
        }
    }
    
    private void displayCommandeDetails(Commande commande) {
        // Find the product in the combo box
        for (Produit produit : orderProductCombo.getItems()) {
            if (produit.getId() == commande.getProduitId()) {
                orderProductCombo.setValue(produit);
                break;
            }
        }
        
        // Set client ID (in a real app, you would have a client combo box)
        orderClientCombo.setValue(String.valueOf(commande.getClientId()));
        
        orderQuantityField.setText(String.valueOf(commande.getQuantite()));
        orderStatusCombo.setValue(commande.getStatus());
        orderPaymentCombo.setValue(commande.getModePaiement());
        orderTotalField.setText(String.valueOf(commande.getPrixTotal()));
        orderAddressField.setText(commande.getAdresseLivraison());
    }
    
    // Product Actions
    @FXML
    private void handleRefreshProducts() {
        loadProduits();
    }
    
    @FXML
    private void handleAddProduct() {
        try {
            // Validate inputs
            if (nomField.getText().isEmpty() || quantiteField.getText().isEmpty() || 
                prixTotalField.getText().isEmpty() || categorieCombo.getValue() == null || 
                stockDCombo.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires.");
                return;
            }
            
            // Create new product
            Produit newProduit = new Produit(
                nomField.getText(),
                Integer.parseInt(quantiteField.getText()),
                Double.parseDouble(prixTotalField.getText()),
                descriptionArea.getText(),
                imageField.getText(),
                categorieCombo.getValue(),
                stockDCombo.getValue(),
                false, // Not approved by default
                new Date() // Current date
            );
            
            // Save product
            produitService.createProduit(newProduit);
            
            // Refresh table
            loadProduits();
            
            // Clear form
            handleClearProduct();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté avec succès.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez entrer des valeurs numériques valides pour la quantité et le prix.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding product", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter le produit.", e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdateProduct() {
        try {
            Produit selectedProduit = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduit == null) {
                showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un produit à modifier.");
                return;
            }
            
            // Validate inputs
            if (nomField.getText().isEmpty() || quantiteField.getText().isEmpty() || 
                prixTotalField.getText().isEmpty() || categorieCombo.getValue() == null || 
                stockDCombo.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires.");
                return;
            }
            
            // Update product
            selectedProduit.setNom(nomField.getText());
            selectedProduit.setQuantite(Integer.parseInt(quantiteField.getText()));
            selectedProduit.setPrixTotal(Double.parseDouble(prixTotalField.getText()));
            selectedProduit.setDescription(descriptionArea.getText());
            selectedProduit.setImage(imageField.getText());
            selectedProduit.setCategorie(categorieCombo.getValue());
            selectedProduit.setStockD(stockDCombo.getValue());
            selectedProduit.setUpdatedAt(new Date());
            
            // Save changes
            produitService.updateProduit(selectedProduit);
            
            // Refresh table
            loadProduits();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit mis à jour avec succès.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez entrer des valeurs numériques valides pour la quantité et le prix.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating product", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le produit.", e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteProduct() {
        try {
            Produit selectedProduit = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduit == null) {
                showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un produit à supprimer.");
                return;
            }
            
            // Confirm deletion
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmation de suppression");
            confirmDialog.setHeaderText("Supprimer le produit");
            confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer ce produit ? Cette action est irréversible.");
            
            if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                // Delete product
                produitService.deleteProduit(selectedProduit.getId());
                
                // Refresh table
                loadProduits();
                
                // Clear form
                handleClearProduct();
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé avec succès.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting product", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le produit.", e.getMessage());
        }
    }
    
    @FXML
    private void handleClearProduct() {
        nomField.clear();
        categorieCombo.getSelectionModel().clearSelection();
        quantiteField.clear();
        prixTotalField.clear();
        descriptionArea.clear();
        stockDCombo.getSelectionModel().clearSelection();
        imageField.clear();
        imagePreview.setImage(new Image(getClass().getResourceAsStream("/images/product-placeholder.png")));
        productTable.getSelectionModel().clearSelection();
    }
    
    @FXML
    private void handleApproveProduct() {
        try {
            Produit selectedProduit = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduit == null) {
                showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un produit à approuver.");
                return;
            }
            
            selectedProduit.setApproved(true);
            produitService.updateProduit(selectedProduit);
            
            // Refresh table
            loadProduits();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit approuvé avec succès.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error approving product", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'approuver le produit.", e.getMessage());
        }
    }
    
    @FXML
    private void handleDisapproveProduct() {
        try {
            Produit selectedProduit = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduit == null) {
                showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un produit à désapprouver.");
                return;
            }
            
            selectedProduit.setApproved(false);
            produitService.updateProduit(selectedProduit);
            
            // Refresh table
            loadProduits();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit désapprouvé avec succès.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error disapproving product", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de désapprouver le produit.", e.getMessage());
        }
    }
    
    @FXML
    private void handleImageChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(imageChooserBtn.getScene().getWindow());
        if (selectedFile != null) {
            imageField.setText(selectedFile.getAbsolutePath());
            imagePreview.setImage(new Image(selectedFile.toURI().toString()));
        }
    }
    
    // Order Actions
    @FXML
    private void handleRefreshOrders() {
        loadCommandes();
    }
    
    @FXML
    private void handleAddOrder() {
        try {
            // Validate inputs
            if (orderProductCombo.getValue() == null || orderQuantityField.getText().isEmpty() || 
                orderPaymentCombo.getValue() == null || orderAddressField.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", 
                         "Veuillez remplir tous les champs obligatoires.", null);
                return;
            }
            
            // Calculate total price based on product price and quantity
            double totalPrice = orderProductCombo.getValue().getPrixTotal() * 
                              Double.parseDouble(orderQuantityField.getText());
            
            // Create new order using constructor without ID and explicitly set status to "Pending"
            Commande newCommande = new Commande(
                orderProductCombo.getValue().getId(), // produitId
                1, // clientId (default)
                new Date(), // dateCommande
                Integer.parseInt(orderQuantityField.getText()), // quantite
                "Pending", // status - explicitly set to Pending
                orderPaymentCombo.getValue(), // modePaiement
                orderAddressField.getText(), // adresseLivraison
                totalPrice // prixTotal - calculated based on product price and quantity
            );
            
            // Save order
            commandeService.createCommande(newCommande);
            
            // Refresh table
            loadCommandes();
            
            // Clear form
            handleClearOrder();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                     "Commande ajoutée avec succès.", null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding order", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors de l'ajout de la commande: " + e.getMessage(), null);
        }
    }
    
    @FXML
    private void handleUpdateOrder() {
        try {
            Commande selectedCommande = orderTable.getSelectionModel().getSelectedItem();
            if (selectedCommande == null) {
                showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une commande à modifier.");
                return;
            }
            
            // Validate inputs
            if (orderProductCombo.getValue() == null || orderQuantityField.getText().isEmpty() || 
                orderStatusCombo.getValue() == null || orderPaymentCombo.getValue() == null || 
                orderAddressField.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires.");
                return;
            }
            
            // Update order
            selectedCommande.setProduitId(orderProductCombo.getValue().getId());
            selectedCommande.setQuantite(Integer.parseInt(orderQuantityField.getText()));
            selectedCommande.setStatus(orderStatusCombo.getValue());
            selectedCommande.setModePaiement(orderPaymentCombo.getValue());
            selectedCommande.setAdresseLivraison(orderAddressField.getText());
            selectedCommande.setPrixTotal(Double.parseDouble(orderTotalField.getText()));
            
            // Save changes
            commandeService.updateCommande(selectedCommande);
            
            // Refresh table
            loadCommandes();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande mise à jour avec succès.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez entrer des valeurs numériques valides pour la quantité et le prix total.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating order", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour la commande.", e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteOrder() {
        try {
            Commande selectedCommande = orderTable.getSelectionModel().getSelectedItem();
            if (selectedCommande == null) {
                showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une commande à supprimer.");
                return;
            }
            
            // Confirm deletion
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmation de suppression");
            confirmDialog.setHeaderText("Supprimer la commande");
            confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette commande ? Cette action est irréversible.");
            
            if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                // Delete order
                commandeService.deleteCommande(selectedCommande.getId());
                
                // Refresh table
                loadCommandes();
                
                // Clear form
                handleClearOrder();
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande supprimée avec succès.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting order", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la commande.", e.getMessage());
        }
    }
    
    @FXML
    private void handleClearOrder() {
        orderProductCombo.getSelectionModel().clearSelection();
        orderClientCombo.getSelectionModel().clearSelection();
        orderQuantityField.clear();
        orderStatusCombo.getSelectionModel().clearSelection();
        orderPaymentCombo.getSelectionModel().clearSelection();
        orderTotalField.clear();
        orderAddressField.clear();
        orderTable.getSelectionModel().clearSelection();
    }
    
    @FXML
    private void handleConfirmOrder() {
        try {
            Commande selectedCommande = orderTable.getSelectionModel().getSelectedItem();
            if (selectedCommande == null) {
                showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une commande à confirmer.");
                return;
            }
            
            selectedCommande.setStatus("Confirmée");
            commandeService.updateCommande(selectedCommande);
            
            // Refresh table
            loadCommandes();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande confirmée avec succès.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error confirming order", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de confirmer la commande.", e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelOrder() {
        try {
            Commande selectedCommande = orderTable.getSelectionModel().getSelectedItem();
            if (selectedCommande == null) {
                showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une commande à annuler.");
                return;
            }
            
            selectedCommande.setStatus("Annulée");
            commandeService.updateCommande(selectedCommande);
            
            // Refresh table
            loadCommandes();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande annulée avec succès.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error canceling order", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'annuler la commande.", e.getMessage());
        }
    }
    
    @FXML
    private void handleShipOrder() {
        try {
            Commande selectedCommande = orderTable.getSelectionModel().getSelectedItem();
            if (selectedCommande == null) {
                showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une commande à expédier.");
                return;
            }
            
            selectedCommande.setStatus("Expédiée");
            commandeService.updateCommande(selectedCommande);
            
            // Refresh table
            loadCommandes();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande expédiée avec succès.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error shipping order", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'expédier la commande.", e.getMessage());
        }
    }
    
    @FXML
    private void handleReturnToMarketplace() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MarketPlace/Marketplace.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Marketplace");
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error returning to marketplace", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner au marketplace.", e.getMessage());
        }
    }
    
    // Utility methods
    private void showAlert(Alert.AlertType type, String title, String content) {
        showAlert(type, title, content, null);
    }
    
    private void showAlert(Alert.AlertType type, String title, String content, String details) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        if (details != null && !details.isEmpty()) {
            TextArea textArea = new TextArea(details);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(5);
            alert.getDialogPane().setExpandableContent(textArea);
        }
        
        alert.showAndWait();
    }
} 