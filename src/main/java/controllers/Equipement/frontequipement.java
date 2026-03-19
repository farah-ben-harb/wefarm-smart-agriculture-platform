package controllers.Equipement;

import entities.Achat;
import entities.Equipement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import services.AchatService;
import services.AchatServiceImpl;
import services.EquipementService;
import services.EquipementServiceImpl;
import utils.EmailService;

import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class frontequipement {

    @FXML
    private FlowPane equipementContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnTous;

    @FXML
    private Button btnMachines;

    @FXML
    private Button btnOutils;

    @FXML
    private Button backButton;

    @FXML
    private Button btnAccessoires;

    @FXML
    public ImageView logoutButton;
    @FXML
    public Label profileLabel;
    @FXML
    private StackPane imageContainer;
    @FXML
    private ImageView mainImageView;
    @FXML
    MenuButton servicesMenu;


    private EquipementService equipementService = new EquipementServiceImpl();
    private ObservableList<Equipement> equipementList = FXCollections.observableArrayList();
    private FilteredList<Equipement> filteredEquipements;
    
    // Panier d'achat (Map avec l'ID de l'équipement et la quantité)
    private Map<Integer, Integer> panier = new HashMap<>();

    @FXML
    private ComboBox<String> equipementComboBox;

    @FXML
    private Label equipementLabel;

    private String currentCategorie = "Tous";

    @FXML
    public void initialize() {
        // Charger tous les équipements
        loadEquipements();
        
        // Configurer la recherche
        setupSearch();
        
        // Afficher les équipements
        afficherEquipements(equipementList);
        
        // Démarrer le serveur d'approbation d'achat
        utils.PurchaseApprovalServer.startServer(this);
        
        // Initialiser le ComboBox des équipements
        equipementComboBox.setOnAction(event -> filtrerParNomEquipement());
    }

    private void loadEquipements() {
        equipementList.clear();
        equipementList.addAll(equipementService.getAllEquipements());
        filteredEquipements = new FilteredList<>(equipementList, p -> true);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredEquipements.setPredicate(equipement -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Vérifier si la recherche contient des mots-clés de catégorie
                boolean containsCategoryKeyword = false;
                String categoryToMatch = null;
                
                // Vérifier si la recherche contient des informations de prix
                boolean containsPriceInfo = false;
                double maxPrice = Double.MAX_VALUE;
                
                // Liste des mots-clés de catégorie à rechercher
                if (lowerCaseFilter.contains("machine")) {
                    containsCategoryKeyword = true;
                    categoryToMatch = "machines";
                } else if (lowerCaseFilter.contains("outil")) {
                    containsCategoryKeyword = true;
                    categoryToMatch = "outils";
                } else if (lowerCaseFilter.contains("accessoire")) {
                    containsCategoryKeyword = true;
                    categoryToMatch = "accessoires";
                }
                
                // Recherche d'informations de prix (format: "prix < X" ou "moins de X")
                if (lowerCaseFilter.contains("prix") || lowerCaseFilter.contains("dt") || 
                    lowerCaseFilter.contains("moins") || lowerCaseFilter.contains("max")) {
                    
                    // Extraire les nombres de la chaîne de recherche
                    String[] words = lowerCaseFilter.split("\\s+");
                    for (int i = 0; i < words.length; i++) {
                        try {
                            // Essayer de convertir le mot en nombre
                            double price = Double.parseDouble(words[i].replaceAll("[^0-9.]", ""));
                            if (price > 0) {
                                containsPriceInfo = true;
                                maxPrice = price;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            // Ignorer si ce n'est pas un nombre
                            continue;
                        }
                    }
                }
                
                // Extraire le nom à rechercher (en supprimant les mots-clés)
                String searchName = lowerCaseFilter
                    .replace("machine", "")
                    .replace("machines", "")
                    .replace("outil", "")
                    .replace("outils", "")
                    .replace("accessoire", "")
                    .replace("accessoires", "")
                    .replace("de", "")
                    .replace("categorie", "")
                    .replace("category", "")
                    .replace("prix", "")
                    .replace("moins", "")
                    .replace("max", "")
                    .replace("dt", "")
                    .replace("<", "")
                    .replace(">", "")
                    .replace("=", "")
                    .replaceAll("\\d+", "")
                    .replaceAll("\\.", "")
                    .replace("  ", " ")
                    .trim();
                
                // Appliquer les filtres combinés
                boolean matchesName = searchName.isEmpty() || 
                                      equipement.getNomEquipement().toLowerCase().contains(searchName);
                boolean matchesCategory = !containsCategoryKeyword || 
                                         equipement.getCategory().toLowerCase().equals(categoryToMatch);
                boolean matchesPrice = !containsPriceInfo || equipement.getPrix() <= maxPrice;
                
                return matchesName && matchesCategory && matchesPrice;
            });
            
            afficherEquipements(filteredEquipements);
        });
    }

    @FXML
    private void filtrerParCategorie(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        
        // Reset all button styles
        btnTous.getStyleClass().remove("category-button-active");
        btnMachines.getStyleClass().remove("category-button-active");
        btnOutils.getStyleClass().remove("category-button-active");
        btnAccessoires.getStyleClass().remove("category-button-active");
        
        // Apply active style to selected button
        sourceButton.getStyleClass().add("category-button-active");
        
        String categorie = sourceButton.getText();
        currentCategorie = categorie;
        
        // Filter equipment by category
        filteredEquipements.setPredicate(equipement -> {
            if (categorie.equals("Tous")) {
                return true;
            }
            return equipement.getCategory().equalsIgnoreCase(categorie);
        });
        
        // Update ComboBox with equipment names for this category
        updateEquipementComboBox(categorie);
        
        // Display filtered equipment
        afficherEquipements(filteredEquipements);
    }
    
    /**
     * Met à jour le ComboBox avec les noms d'équipements de la catégorie sélectionnée
     */
    private void updateEquipementComboBox(String categorie) {
        // Vider le ComboBox
        equipementComboBox.getItems().clear();
        
        // Si "Tous" est sélectionné, masquer le ComboBox
        if (categorie.equals("Tous")) {
            equipementComboBox.setVisible(false);
            equipementLabel.setVisible(false);
            return;
        }
        
        // Ajouter une option "Tous les [catégorie]"
        equipementComboBox.getItems().add("Tous les " + categorie.toLowerCase());
        
        // Récupérer tous les noms d'équipements de cette catégorie
        List<String> nomEquipements = new ArrayList<>();
        for (Equipement equipement : equipementList) {
            if (equipement.getCategory().equalsIgnoreCase(categorie)) {
                nomEquipements.add(equipement.getNomEquipement());
            }
        }
        
        // Ajouter les noms au ComboBox
        equipementComboBox.getItems().addAll(nomEquipements);
        
        // Sélectionner la première option
        equipementComboBox.getSelectionModel().selectFirst();
        
        // Rendre le ComboBox visible
        equipementComboBox.setVisible(true);
        equipementLabel.setVisible(true);
    }
    
    /**
     * Filtre les équipements par nom après sélection dans le ComboBox
     */
    @FXML
    private void filtrerParNomEquipement() {
        String selectedEquipement = equipementComboBox.getValue();
        
        if (selectedEquipement == null) {
            return;
        }
        
        // Si "Tous les [catégorie]" est sélectionné, afficher tous les équipements de cette catégorie
        if (selectedEquipement.startsWith("Tous les ")) {
            filteredEquipements.setPredicate(equipement -> {
                if (currentCategorie.equals("Tous")) {
                    return true;
                }
                return equipement.getCategory().equalsIgnoreCase(currentCategorie);
            });
        } else {
            // Sinon, filtrer par nom d'équipement spécifique
            filteredEquipements.setPredicate(equipement -> {
                return equipement.getNomEquipement().equals(selectedEquipement) &&
                       equipement.getCategory().equalsIgnoreCase(currentCategorie);
            });
        }
        
        // Afficher les équipements filtrés
        afficherEquipements(filteredEquipements);
    }

    private void afficherEquipements(List<Equipement> equipements) {
        equipementContainer.getChildren().clear();
        
        for (Equipement equipement : equipements) {
            // Créer une carte pour chaque équipement
            VBox equipementCard = createEquipementCard(equipement);
            equipementContainer.getChildren().add(equipementCard);
        }
    }

    private VBox createEquipementCard(Equipement equipement) {
        VBox card = new VBox(10);
        card.getStyleClass().add("equipment-card");

        ImageView imageView = new ImageView();
        try {
            // Calcul correct pour obtenir une alternance entre 1 et 4
            int imageNumber = (equipement.getId() % 4) + 1;  // Garantit 1-4

            System.out.println("ID: " + equipement.getId() + " → Image: " + imageNumber); // Debug

            Image image = new Image(getClass().getResourceAsStream(
                    "/img/" + imageNumber + ".jpg"
            ));
            imageView.setImage(image);
            imageView.setFitWidth(250);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(false);
        } catch (Exception e) {
            // Default image on error
            Image defaultImage = new Image(getClass().getResourceAsStream("/icons/equipment_default.png"));
            imageView.setImage(defaultImage);
        }
        
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        
        // Equipment name
        Label nameLabel = new Label(equipement.getNomEquipement());
        nameLabel.getStyleClass().add("equipment-name");
        
        // Price
        Label priceLabel = new Label(String.format("%.2f DT", equipement.getPrix()));
        priceLabel.getStyleClass().add("equipment-price");
        
        // Category
        Label categoryLabel = new Label(equipement.getCategory());
        categoryLabel.getStyleClass().add("equipment-category");
        
        // Quantity
        Label quantityLabel = new Label("Disponible: " + equipement.getQuantiteDisponible());
        quantityLabel.getStyleClass().add("equipment-quantity");
        if (equipement.getQuantiteDisponible() < 5) {
            quantityLabel.getStyleClass().add("low");
        }
        
        // Buy button
        Button buyButton = new Button("Acheter");
        buyButton.getStyleClass().add("buy-button");
        buyButton.setDisable(equipement.getQuantiteDisponible() <= 0);
        buyButton.setOnAction(e -> ajouterAuPanier(equipement));
        
        card.getChildren().addAll(imageView, nameLabel, priceLabel, categoryLabel, quantityLabel, buyButton);
        return card;
    }

    private void ajouterAuPanier(Equipement equipement) {
        if (equipement.getQuantiteDisponible() <= 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Cet équipement n'est pas disponible.");
            return;
        }
        
        // Demander l'email de l'utilisateur pour l'envoi de la confirmation
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Email pour confirmation");
        emailDialog.setHeaderText("Veuillez entrer votre adresse email");
        emailDialog.setContentText("Email:");
        
        emailDialog.showAndWait().ifPresent(email -> {
            // Demander le nom de l'utilisateur
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Nom d'utilisateur");
            nameDialog.setHeaderText("Veuillez entrer votre nom");
            nameDialog.setContentText("Nom:");
            
            nameDialog.showAndWait().ifPresent(userName -> {
                int quantite = 1;
                double prixTotal = equipement.getPrix() * quantite;
                
                // Vérifier si l'équipement est déjà dans le panier
                if (panier.containsKey(equipement.getId())) {
                    int quantiteActuelle = panier.get(equipement.getId());
                    
                    // Vérifier si on peut ajouter plus
                    if (quantiteActuelle < equipement.getQuantiteDisponible()) {
                        quantite = quantiteActuelle + 1;
                        panier.put(equipement.getId(), quantite);
                        prixTotal = equipement.getPrix() * quantite;
                        showAlert(Alert.AlertType.INFORMATION, "Panier", "Quantité mise à jour dans le panier.");
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Panier", "Vous avez atteint la quantité maximale disponible.");
                        return;
                    }
                } else {
                    panier.put(equipement.getId(), quantite);
                    showAlert(Alert.AlertType.INFORMATION, "Panier", "Équipement ajouté au panier.");
                }
                try {
                    EmailService.sendPurchaseApprovalEmail(email, userName, equipement, quantite, prixTotal);
                    showAlert(Alert.AlertType.INFORMATION, "Email envoyé", 
                            "Un email de confirmation a été envoyé à " + email + ".\n" +
                            "Veuillez vérifier votre boîte de réception et approuver l'achat.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'envoyer l'email: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });
    }

    @FXML
    private void afficherPanier() {
        if (panier.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Panier", "Votre panier est vide.");
            return;
        }
        
        // Ici, vous pourriez ouvrir une nouvelle fenêtre pour afficher le panier
        // ou implémenter une logique pour finaliser l'achat
        StringBuilder panierDetails = new StringBuilder("Contenu du panier:\n");
        double total = 0;
        
        for (Map.Entry<Integer, Integer> entry : panier.entrySet()) {
            Equipement equipement = equipementService.getEquipementById(entry.getKey());
            int quantite = entry.getValue();
            double sousTotal = equipement.getPrix() * quantite;
            
            panierDetails.append(String.format("%s - %d x %.2f DT = %.2f DT\n", 
                    equipement.getNomEquipement(), quantite, equipement.getPrix(), sousTotal));
            
            total += sousTotal;
        }
        
        panierDetails.append(String.format("\nTotal: %.2f DT", total));
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Panier");
        alert.setHeaderText("Détails de votre panier");
        alert.setContentText(panierDetails.toString());
        alert.showAndWait();
    }
    
    /**
     * Méthode pour traiter l'approbation d'un achat après confirmation par email
     * Cette méthode serait appelée par un endpoint web qui traite les clics sur les boutons d'approbation
     */
    public void processApprovedPurchase(int equipementId, int quantity, double total) {
        try {
            // Créer un nouvel achat
            Achat achat = new Achat();
            achat.setEquipementId(equipementId);
            achat.setDate(new Date()); // Date actuelle
            achat.setTotal((int)total); // Conversion en int selon le modèle Achat
            
            // Ajouter l'achat à la base de données
            AchatService achatService = new AchatServiceImpl();
            achatService.addAchat(achat);
            
            // Mettre à jour la quantité disponible de l'équipement
            Equipement equipement = equipementService.getEquipementById(equipementId);
            if (equipement != null) {
                equipement.setQuantiteDisponible(equipement.getQuantiteDisponible() - quantity);
                equipementService.updateEquipement(equipement);
                
                // Conserver l'achat dans le panier au lieu de le supprimer
                // Si l'équipement n'est pas déjà dans le panier, l'ajouter
                if (!panier.containsKey(equipementId)) {
                    panier.put(equipementId, quantity);
                } else {
                    // Si l'équipement est déjà dans le panier, mettre à jour la quantité
                    int currentQuantity = panier.get(equipementId);
                    panier.put(equipementId, currentQuantity + quantity);
                }
                
                // Afficher un message de confirmation avec le total du panier
                double panierTotal = 0;
                StringBuilder message = new StringBuilder("Achat approuvé avec succès!\n\nContenu actuel du panier:\n");
                
                for (Map.Entry<Integer, Integer> entry : panier.entrySet()) {
                    Equipement equip = equipementService.getEquipementById(entry.getKey());
                    int quant = entry.getValue();
                    double sousTotal = equip.getPrix() * quant;
                    
                    message.append(String.format("%s - %d x %.2f DT = %.2f DT\n", 
                            equip.getNomEquipement(), quant, equip.getPrix(), sousTotal));
                    
                    panierTotal += sousTotal;
                }
                
                message.append(String.format("\nTotal du panier: %.2f DT", panierTotal));
                
                showAlert(Alert.AlertType.INFORMATION, "Achat approuvé", message.toString());
            }
            
            // Rafraîchir l'affichage
            loadEquipements();
            afficherEquipements(filteredEquipements);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement de l'achat approuvé: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Méthode pour arrêter le serveur d'approbation lorsque l'application se ferme
     */
    public void shutdown() {
        utils.PurchaseApprovalServer.stopServer();
    }

    /**
     * Configure l'événement de fermeture de la fenêtre pour arrêter le serveur
     * Cette méthode doit être appelée après l'initialisation de la scène
     */
    public void setupCloseHandler(Stage stage) {
        stage.setOnCloseRequest((WindowEvent event) -> {
            shutdown();
        });
    }

    /**
     * Méthode pour naviguer vers le système de recommandation IA
     */
    @FXML
    private void navigateToRecommendation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Equipement/ai_recommendation.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers le système de recommandation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/Home.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    /**
     * Méthode pour naviguer vers les statistiques d'achat
     */
    @FXML
    private Button btnStatistics;

    @FXML
    private void navigateToStatistics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Equipement/achat_statistique.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers les statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }




}
