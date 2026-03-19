package controllers.marketplace;

import entities.Ad;
import entities.Produit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MarketplaceController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML
    private GridPane productsGrid;
    @FXML
    private Button addProductBtn;
    @FXML
    private Dialog<Void> productDetailsDialog;
    @FXML
    private ImageView detailsImage;
    @FXML
    private Label detailsName;
    @FXML
    private Label detailsPrice;
    @FXML
    private Label detailsStock;
    @FXML
    private Label detailsCategory;
    @FXML
    private TextArea detailsDescription;
    @FXML
    private Button statisticsBtn;
    @FXML
    private ImageView adImageView;
    @FXML
    private HBox adIndicators;
    @FXML
    private Button prevAdBtn;
    @FXML
    private Button nextAdBtn;
    @FXML
    private Button filterBtn;
    @FXML
    private Button refreshBtn;
    @FXML
    private Label usernameLabel;

    private final ProduitService produitService;
    private final CommandeService commandeService;
    private final Image defaultImage;
    private final AdService adService = new AdService();
    private final FavoriteService favoriteService;
    private List<Ad> activeAds;
    private int currentAdIndex = 0;
    private Timeline slideTimer;
    private static final int CURRENT_USER_ID = 1; // TODO: Replace with actual user ID from session
    private int currentMaxCols = 3;

    public MarketplaceController() {
        produitService = new ProduitServiceImpl();
        commandeService = new CommandeServiceImpl(produitService);
        favoriteService = new FavoriteService();
        defaultImage = new Image(getClass().getResourceAsStream("/img/default-product.png"));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProducts();
        setupSearch();
        loadAds();
        setupAdSlider();
        // Responsive columns
        productsGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((o, oldW, newW) -> {
                    int newCols = Math.max(1, (int) (newW.doubleValue() / 300));
                    if (newCols != currentMaxCols) {
                        currentMaxCols = newCols;
                        loadProducts();
                    }
                });
            }
        });
    }

    private void loadProducts() {
        productsGrid.getChildren().clear();
        int row = 0;
        int col = 0;
        int maxCols = currentMaxCols;

        // Récupérer tous les produits approuvés
        List<Produit> produits = produitService.getApprovedProduits();
        
        // Mettre à jour les produits avec quantité 0 au lieu de les supprimer
        for (Produit produit : produits) {
            if (produit.getQuantite() <= 0) {
                try {
                    produit.setApproved(false); // Marquer comme indisponible au lieu de supprimer
                    produitService.updateProduit(produit);
                    System.out.println("Produit marqué comme indisponible (quantité = 0): " + produit.getNom());
                } catch (Exception e) {
                    System.err.println("Erreur lors de la mise à jour du produit: " + e.getMessage());
                }
            }
        }

        // Recharger la liste des produits après mise à jour
        produits = produitService.getApprovedProduits();
        
        // Afficher les produits disponibles
        for (Produit produit : produits) {
            if (produit.getQuantite() > 0) {
                VBox productCard = createProductCard(produit);
                productsGrid.add(productCard, col, row);
                
                col++;
                if (col >= maxCols) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    private VBox createProductCard(Produit produit) {
        VBox productCard = new VBox(12);
        productCard.setId("product-card");
        productCard.getStyleClass().add("product-card");

        // Product Image with Badge
        StackPane imageContainer = new StackPane();
        ImageView productImage = new ImageView();
        productImage.setFitWidth(220);
        productImage.setFitHeight(160);
        productImage.setPreserveRatio(true);

        try {
            if (produit.getImage() != null && !produit.getImage().isEmpty()) {
                Image image = new Image(produit.getImage());
                productImage.setImage(image);
            } else {
                productImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            productImage.setImage(defaultImage);
        }

        // Stock Badge
        HBox stockBadge = new HBox();
        stockBadge.setAlignment(Pos.CENTER_RIGHT);
        Label stockLabel = new Label(produit.getStockD());
        stockLabel.setId("stock-badge");
        stockBadge.getChildren().add(stockLabel);

        imageContainer.getChildren().addAll(productImage, stockBadge);

        // Product Details
        VBox details = new VBox(6);
        Label nameLabel = new Label(produit.getNom());
        nameLabel.setId("product-name");

        HBox priceBox = new HBox(5);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label(String.format("%.2f DT", produit.getPrixTotal()));
        priceLabel.setId("product-price");
        Label unitLabel = new Label("/unit");
        unitLabel.setId("unit-label");
        priceBox.getChildren().addAll(priceLabel, unitLabel);

        Text descriptionText = new Text(produit.getDescription());
        descriptionText.setId("product-description");
        descriptionText.setWrappingWidth(200);

        // Rating and Actions
        HBox actionsBox = new HBox(5);
        actionsBox.setAlignment(Pos.CENTER_LEFT);

        // Rating display
        HBox ratingBox = new HBox(2);
        Label starLabel = new Label("★");
        starLabel.setId("star-rating");
        Label ratingValue = new Label("4.5");
        ratingValue.setId("rating-value");
        ratingBox.getChildren().addAll(starLabel, ratingValue);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Favorite Toggle Button
        Button favoriteBtn = new Button();  // Changed from ToggleButton to Button for better control
        favoriteBtn.getStyleClass().add("favorite-btn");
        
        // Check if product is already in favorites and set initial state
        System.out.println("Checking favorite status for user " + CURRENT_USER_ID + " and product " + produit.getId());
        boolean isFavorite = favoriteService.isFavorite(CURRENT_USER_ID, produit.getId());
        System.out.println("Is favorite: " + isFavorite);
        favoriteBtn.setText(isFavorite ? "♥" : "♡");
        
        favoriteBtn.setOnAction(e -> {
            try {
                // Check current state
                boolean currentlyFavorite = favoriteService.isFavorite(CURRENT_USER_ID, produit.getId());
                
                if (currentlyFavorite) {
                    // If it's currently a favorite, remove it
                    System.out.println("Removing from favorites...");
                    favoriteService.removeFromFavorites(CURRENT_USER_ID, produit.getId());
                    favoriteBtn.setText("♡");
                    showAlert("Favoris", "Produit retiré des favoris", Alert.AlertType.INFORMATION);
                } else {
                    // If it's not a favorite, add it
                    System.out.println("Adding to favorites...");
                    favoriteService.addToFavorites(CURRENT_USER_ID, produit.getId());
                    favoriteBtn.setText("♥");
                    showAlert("Favoris", "Produit ajouté aux favoris", Alert.AlertType.INFORMATION);
                }
                
            } catch (Exception ex) {
                System.err.println("Error toggling favorite: " + ex.getMessage());
                System.err.println("User ID: " + CURRENT_USER_ID);
                System.err.println("Product ID: " + produit.getId());
                ex.printStackTrace();
                showError("Erreur", "Impossible de modifier les favoris: " + ex.getMessage());
            }
        });

        // Details and Add to Cart buttons
        Button detailsBtn = new Button("Détails");
        detailsBtn.setId("details-btn");
        detailsBtn.setGraphic(new Label("🔍"));
        detailsBtn.setOnAction(e -> showProductDetails(produit));

        Button addToCartBtn = new Button("Commander");
        addToCartBtn.setId("add-to-cart-btn");
        addToCartBtn.setGraphic(new Label("🛒"));
        addToCartBtn.setOnAction(e -> showOrderDialog(produit));

        actionsBox.getChildren().addAll(ratingBox, spacer, favoriteBtn, detailsBtn, addToCartBtn);

        details.getChildren().addAll(nameLabel, priceBox, descriptionText, actionsBox);
        productCard.getChildren().addAll(imageContainer, details);

        return productCard;
    }

    private void showProductDetails(Produit product) {
        try {
            // Create the dialog if it doesn't exist
            if (productDetailsDialog == null) {
                productDetailsDialog = new Dialog<>();
                productDetailsDialog.setTitle("Détails du Produit");
                
                ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType orderButton = new ButtonType("Commander", ButtonBar.ButtonData.OK_DONE);
                productDetailsDialog.getDialogPane().getButtonTypes().addAll(orderButton, closeButton);

                // Create the dialog content
                VBox content = new VBox(15);
                content.setPadding(new Insets(20));

                detailsImage = new ImageView();
                detailsImage.setFitWidth(200);
                detailsImage.setFitHeight(200);
                detailsImage.setPreserveRatio(true);

                detailsName = new Label();
                detailsName.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

                detailsPrice = new Label();
                detailsPrice.setStyle("-fx-font-size: 20px; -fx-text-fill: #28a745; -fx-font-weight: bold;");

                detailsStock = new Label();
                detailsStock.setStyle("-fx-font-size: 14px;");

                detailsCategory = new Label();
                detailsCategory.setStyle("-fx-font-size: 14px;");

                detailsDescription = new TextArea();
                detailsDescription.setWrapText(true);
                detailsDescription.setEditable(false);
                detailsDescription.setPrefRowCount(3);
                detailsDescription.setStyle("-fx-background-color: transparent;");

                content.getChildren().addAll(
                    detailsImage,
                    detailsName,
                    detailsPrice,
                    detailsStock,
                    detailsCategory,
                    new Label("Description:"),
                    detailsDescription
                );

                productDetailsDialog.getDialogPane().setContent(content);
                
                // Handle the order button
                Button orderBtn = (Button) productDetailsDialog.getDialogPane().lookupButton(orderButton);
                orderBtn.addEventFilter(ActionEvent.ACTION, event -> {
                    event.consume(); // Prevent dialog from closing
                    showOrderDialog(product);
                });
            }

            // Update dialog content with product details
            try {
                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    String imagePath = "/images/products/" + product.getImage();
                    InputStream imageStream = getClass().getResourceAsStream(imagePath);
                    
                    if (imageStream != null) {
                        detailsImage.setImage(new Image(imageStream));
                    } else {
                        File imageFile = new File(product.getImage().replace("c:", "C:"));
                        if (imageFile.exists()) {
                            String fileUrl = imageFile.toURI().toString();
                            detailsImage.setImage(new Image(fileUrl));
                        } else {
                            detailsImage.setImage(defaultImage);
                        }
                    }
                } else {
                    detailsImage.setImage(defaultImage);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                detailsImage.setImage(defaultImage);
            }
            
            detailsName.setText(product.getNom());
            detailsPrice.setText(String.format("%.2f DT", product.getPrixTotal()));
            detailsStock.setText("Stock: " + product.getQuantite() + " unités");
            detailsCategory.setText("Catégorie: " + product.getCategorie());
            detailsDescription.setText(product.getDescription());
            
            // Show the dialog
            productDetailsDialog.showAndWait();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des détails: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Impossible d'afficher les détails du produit");
        }
    }

    @FXML
    private void showOrderDialog(Produit product) {
        try {
            // Load the order dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MarketPlace/OrderDialog.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the product
            OrderDialogController controller = loader.getController();
            controller.setProduit(product);
            
            // Create and configure the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Commander " + product.getNom());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(productsGrid.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            // Set the dialog stage in the controller
            controller.setDialogStage(dialogStage);
            
            // Show the dialog
            dialogStage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Erreur lors de l'ouverture du formulaire de commande: " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            productsGrid.getChildren().clear();
            int row = 0;
            int col = 0;
            int maxCols = currentMaxCols;

            for (Produit produit : produitService.getApprovedProduits()) {
                if (produit.getNom().toLowerCase().contains(newValue.toLowerCase()) ||
                    produit.getDescription().toLowerCase().contains(newValue.toLowerCase())) {
                    VBox productCard = createProductCard(produit);
                    productsGrid.add(productCard, col, row);
                    
                    col++;
                    if (col >= maxCols) {
                        col = 0;
                        row++;
                    }
                }
            }
        });
    }

    @FXML
    private void handleShowCommandes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MarketPlace/CommandeManagement.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Gestion des Commandes");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre des commandes");
        }
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MarketPlace/AddProduct.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Produit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(addProductBtn.getScene().getWindow());
            
            // Add event handler for when the window is closed
            stage.setOnHidden(e -> {
                loadProducts(); // Refresh the products list when returning to marketplace
            });
            
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre d'ajout de produit: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        showAlert(title, content, Alert.AlertType.ERROR);
    }

    @FXML
    private void handleRefresh() {
        loadProducts();
        loadAds();
    }

    // Méthode pour mettre à jour un produit après une commande
    private void updateProductQuantity(Produit produit, int quantiteCommandee) {
        try {
            int nouvelleQuantite = produit.getQuantite() - quantiteCommandee;
            produit.setQuantite(nouvelleQuantite);
            
            if (nouvelleQuantite <= 0) {
                // Marquer le produit comme indisponible au lieu de le supprimer
                produit.setApproved(false);
                produitService.updateProduit(produit);
                System.out.println("Produit marqué comme indisponible après commande: " + produit.getNom());
            } else {
                produitService.updateProduit(produit);
            }
            
            // Rafraîchir l'affichage
            loadProducts();
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de la quantité: " + e.getMessage());
            showError("Erreur", "Impossible de mettre à jour la quantité du produit");
        }
    }

    private void loadAds() {
        try {
            activeAds = adService.getActiveAds();
            if (!activeAds.isEmpty()) {
                updateAdDisplay();
                createAdIndicators();
            }
        } catch (Exception e) {
            showError("Error", "Failed to load advertisements: " + e.getMessage());
        }
    }

    private void setupAdSlider() {
        // Set up auto-sliding timer
        slideTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> nextAd()));
        slideTimer.setCycleCount(Timeline.INDEFINITE);
        slideTimer.play();

        // Configure navigation buttons
        prevAdBtn.setOnAction(e -> {
            resetTimer();
            previousAd();
        });

        nextAdBtn.setOnAction(e -> {
            resetTimer();
            nextAd();
        });
    }

    private void updateAdDisplay() {
        if (activeAds != null && !activeAds.isEmpty()) {
            Ad currentAd = activeAds.get(currentAdIndex);
            adImageView.setImage(new Image(currentAd.getImageUrl()));
            updateIndicators();
        }
    }

    private void createAdIndicators() {
        adIndicators.getChildren().clear();
        for (int i = 0; i < activeAds.size(); i++) {
            Circle indicator = new Circle(4);
            indicator.getStyleClass().add("ad-indicator");
            final int index = i;
            indicator.setOnMouseClicked(e -> {
                currentAdIndex = index;
                updateAdDisplay();
                resetTimer();
            });
            adIndicators.getChildren().add(indicator);
        }
        updateIndicators();
    }

    private void updateIndicators() {
        for (int i = 0; i < adIndicators.getChildren().size(); i++) {
            Circle indicator = (Circle) adIndicators.getChildren().get(i);
            indicator.getStyleClass().removeAll("active");
            if (i == currentAdIndex) {
                indicator.getStyleClass().add("active");
            }
        }
    }

    private void nextAd() {
        if (activeAds != null && !activeAds.isEmpty()) {
            currentAdIndex = (currentAdIndex + 1) % activeAds.size();
            updateAdDisplay();
        }
    }

    private void previousAd() {
        if (activeAds != null && !activeAds.isEmpty()) {
            currentAdIndex = (currentAdIndex - 1 + activeAds.size()) % activeAds.size();
            updateAdDisplay();
        }
    }

    private void resetTimer() {
        slideTimer.stop();
        slideTimer.play();
    }

    @FXML
    private void handleShowFavorites() {
        try {
            // Get the resource URL directly from the class loader
            URL resourceUrl = getClass().getResource("/views/MarketPlace/Favorites.fxml");
            if (resourceUrl == null) {
                throw new IOException("Could not find Favorites.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Mes Favoris");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(productsGrid.getScene().getWindow());
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading Favorites.fxml: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la fenêtre des favoris: " + e.getMessage());
        }
    }
} 