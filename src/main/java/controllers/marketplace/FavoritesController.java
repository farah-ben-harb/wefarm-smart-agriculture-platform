package controllers.marketplace;
import entities.Produit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.FavoriteService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FavoritesController {
    @FXML private GridPane favoritesGrid;
    @FXML private VBox emptyState;
    @FXML private Button backBtn;

    private final FavoriteService favoriteService;
    private static final int CURRENT_USER_ID = 1; // TODO: Replace with actual user ID from session

    public FavoritesController() {
        this.favoriteService = new FavoriteService();
    }

    @FXML
    public void initialize() {
        loadFavorites();
    }

    private void loadFavorites() {
        favoritesGrid.getChildren().clear();
        List<Produit> favorites = favoriteService.getFavoriteProducts(CURRENT_USER_ID);
        
        if (favorites.isEmpty()) {
            emptyState.setVisible(true);
            favoritesGrid.setVisible(false);
            return;
        }

        emptyState.setVisible(false);
        favoritesGrid.setVisible(true);

        int col = 0;
        int row = 0;
        int maxCols = 3;

        for (Produit produit : favorites) {
            VBox productCard = createProductCard(produit);
            favoritesGrid.add(productCard, col, row);

            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");

        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        try {
            if (produit.getImage() != null && !produit.getImage().isEmpty()) {
                String imagePath = "/images/products/" + produit.getImage();
                InputStream imageStream = getClass().getResourceAsStream(imagePath);
                
                if (imageStream != null) {
                    imageView.setImage(new Image(imageStream));
                } else {
                    // Try loading from file system if resource not found
                    File imageFile = new File(produit.getImage());
                    if (imageFile.exists()) {
                        imageView.setImage(new Image(imageFile.toURI().toString()));
                    } else {
                        // Load default image if both attempts fail
                        InputStream defaultImageStream = getClass().getResourceAsStream("/img/default-product.png");
                        if (defaultImageStream != null) {
                            imageView.setImage(new Image(defaultImageStream));
                        }
                    }
                }
            } else {
                // Load default image for null/empty image path
                InputStream defaultImageStream = getClass().getResourceAsStream("/img/default-product.png");
                if (defaultImageStream != null) {
                    imageView.setImage(new Image(defaultImageStream));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image for product " + produit.getNom() + ": " + e.getMessage());
            // Try to load default image
            try {
                InputStream defaultImageStream = getClass().getResourceAsStream("/img/default-product.png");
                if (defaultImageStream != null) {
                    imageView.setImage(new Image(defaultImageStream));
                }
            } catch (Exception ex) {
                System.err.println("Error loading default image: " + ex.getMessage());
            }
        }

        // Product Info
        VBox info = new VBox(5);
        Label nameLabel = new Label(produit.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label priceLabel = new Label(String.format("%.2f DT", produit.getPrixTotal()));
        priceLabel.setStyle("-fx-text-fill: #28a745;");
        Label stockLabel = new Label("Stock: " + produit.getQuantite());
        
        info.getChildren().addAll(nameLabel, priceLabel, stockLabel);

        // Remove from favorites button
        Button removeBtn = new Button("Retirer des favoris");
        removeBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        removeBtn.setOnAction(e -> {
            favoriteService.removeFromFavorites(CURRENT_USER_ID, produit.getId());
            loadFavorites(); // Reload the favorites list
        });

        card.getChildren().addAll(imageView, info, removeBtn);
        return card;
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBrowseProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MarketPlace/Marketplace.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
        }
    }
} 