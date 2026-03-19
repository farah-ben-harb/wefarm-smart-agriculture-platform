package controllers.marketplace;
import entities.Commande;
import entities.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.CommandeService;
import services.CommandeServiceImpl;
import services.ProduitService;
import services.ProduitServiceImpl;

import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class CommandeManagementController implements Initializable {
    @FXML private TextField clientIdField;
    @FXML private ComboBox<Produit> produitCombo;
    @FXML private TextField quantiteField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> statutCombo;
    @FXML private ComboBox<String> modePaiementCombo;
    @FXML private TextField adresseLivraisonField;
    @FXML private Button addBtn;
    @FXML private Button updateBtn;
    @FXML private Button deleteBtn;
    @FXML private Button clearBtn;
    @FXML private TableView<Commande> commandeTable;
    @FXML private TableColumn<Commande, Integer> idCol;
    @FXML private TableColumn<Commande, Integer> clientIdCol;
    @FXML private TableColumn<Commande, String> produitCol;
    @FXML private TableColumn<Commande, Integer> quantiteCol;
    @FXML private TableColumn<Commande, Double> prixCol;
    @FXML private TableColumn<Commande, String> statutCol;
    @FXML private TableColumn<Commande, String> dateCommandeCol;
    @FXML private TableColumn<Commande, String> modePaiementCol;
    @FXML private TableColumn<Commande, String> adresseLivraisonCol;
    @FXML private TextField searchField;

    private final CommandeService commandeService;
    private final ProduitService produitService;
    private final ObservableList<Commande> commandeList = FXCollections.observableArrayList();

    public CommandeManagementController() {
        produitService = new ProduitServiceImpl();
        commandeService = new CommandeServiceImpl(produitService);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupComboBoxes();
        loadProduits();
        loadCommandes();
        setupSearch();
        setupTableSelection();
        setupProduitSelection();
        
        // Set default values
        statutCombo.setValue("Pending");
        modePaiementCombo.setValue("Carte Bancaire");
    }

    private void setupComboBoxes() {
        // Setup status combo
        statutCombo.getItems().addAll(
            "Pending", "Confirmed", "Shipped", "Delivered", "Cancelled"
        );
        
        // Setup payment mode combo
        modePaiementCombo.getItems().addAll(
            "Carte Bancaire", "PayPal", "Virement", "Espèces"
        );
        
        // Setup product combo display
        produitCombo.setConverter(new javafx.util.StringConverter<Produit>() {
            @Override
            public String toString(Produit produit) {
                return produit == null ? "" : produit.getNom() + " - " + produit.getPrixTotal() + "€";
            }

            @Override
            public Produit fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        clientIdCol.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        produitCol.setCellValueFactory(cellData -> {
            Produit produit = cellData.getValue().getProduit();
            return new javafx.beans.property.SimpleStringProperty(produit != null ? produit.getNom() : "");
        });
        quantiteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateCommandeCol.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDateCommande();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.toString() : "");
        });
        modePaiementCol.setCellValueFactory(new PropertyValueFactory<>("modePaiement"));
        adresseLivraisonCol.setCellValueFactory(new PropertyValueFactory<>("adresseLivraison"));
    }

    private void loadProduits() {
        produitCombo.getItems().clear();
        produitCombo.getItems().addAll(produitService.getApprovedProduits());
    }

    private void loadCommandes() {
        commandeList.clear();
        commandeList.addAll(commandeService.getAllCommandes());
        commandeTable.setItems(commandeList);
    }

    private void setupSearch() {
        FilteredList<Commande> filteredData = new FilteredList<>(commandeList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(commande -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return String.valueOf(commande.getClientId()).contains(lowerCaseFilter) ||
                       commande.getStatus().toLowerCase().contains(lowerCaseFilter) ||
                       commande.getModePaiement().toLowerCase().contains(lowerCaseFilter) ||
                       (commande.getProduit() != null && 
                        commande.getProduit().getNom().toLowerCase().contains(lowerCaseFilter));
            });
        });
        SortedList<Commande> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(commandeTable.comparatorProperty());
        commandeTable.setItems(sortedData);
    }

    private void setupTableSelection() {
        commandeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFieldsWithCommande(newSelection);
                updateBtn.setDisable(false);
                deleteBtn.setDisable(false);
            } else {
                updateBtn.setDisable(true);
                deleteBtn.setDisable(true);
            }
        });
    }

    private void setupProduitSelection() {
        produitCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updatePrixTotal();
            }
        });
        quantiteField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantiteField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                updatePrixTotal();
            }
        });
    }

    private void updatePrixTotal() {
        Produit selectedProduit = produitCombo.getValue();
        if (selectedProduit != null && !quantiteField.getText().isEmpty()) {
            try {
                int quantite = Integer.parseInt(quantiteField.getText());
                double prixTotal = selectedProduit.getPrixTotal() * quantite;
                prixField.setText(String.format("%.2f", prixTotal));
            } catch (NumberFormatException e) {
                prixField.clear();
            }
        }
    }

    private void fillFieldsWithCommande(Commande commande) {
        clientIdField.setText(String.valueOf(commande.getClientId()));
        produitCombo.setValue(commande.getProduit());
        quantiteField.setText(String.valueOf(commande.getQuantite()));
        prixField.setText(String.format("%.2f", commande.getPrixTotal()));
        statutCombo.setValue(commande.getStatus());
        modePaiementCombo.setValue(commande.getModePaiement());
        adresseLivraisonField.setText(commande.getAdresseLivraison());
    }

    @FXML
    private void handleAdd() {
        if (!validateInputs()) {
            return;
        }

        try {
            Commande commande = new Commande();
            commande.setClientId(Integer.parseInt(clientIdField.getText()));
            commande.setProduit(produitCombo.getValue());
            commande.setProduitId(produitCombo.getValue().getId());
            commande.setQuantite(Integer.parseInt(quantiteField.getText()));
            commande.setPrixTotal(Double.parseDouble(prixField.getText().replace(",", ".")));
            commande.setStatus("Pending");
            commande.setModePaiement(modePaiementCombo.getValue());
            commande.setAdresseLivraison(adresseLivraisonField.getText());
            commande.setDateCommande(new Date());

            commandeService.createCommande(commande);
            loadCommandes();
            clearFields();
            showAlert("Succès", "Commande ajoutée avec succès", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdate() {
        Commande selectedCommande = commandeTable.getSelectionModel().getSelectedItem();
        if (selectedCommande == null) {
            showAlert("Erreur", "Veuillez sélectionner une commande à modifier", Alert.AlertType.ERROR);
            return;
        }

        if (!validateInputs()) {
            return;
        }

        try {
            selectedCommande.setClientId(Integer.parseInt(clientIdField.getText()));
            selectedCommande.setProduit(produitCombo.getValue());
            selectedCommande.setProduitId(produitCombo.getValue().getId());
            selectedCommande.setQuantite(Integer.parseInt(quantiteField.getText()));
            selectedCommande.setPrixTotal(Double.parseDouble(prixField.getText().replace(",", ".")));
            selectedCommande.setStatus(statutCombo.getValue());
            selectedCommande.setModePaiement(modePaiementCombo.getValue());
            selectedCommande.setAdresseLivraison(adresseLivraisonField.getText());

            commandeService.updateCommande(selectedCommande);
            loadCommandes();
            clearFields();
            showAlert("Succès", "Commande modifiée avec succès", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        Commande selectedCommande = commandeTable.getSelectionModel().getSelectedItem();
        if (selectedCommande == null) {
            showAlert("Erreur", "Veuillez sélectionner une commande à supprimer", Alert.AlertType.ERROR);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette commande ?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                commandeService.deleteCommande(selectedCommande.getId());
                loadCommandes();
                clearFields();
                showAlert("Succès", "Commande supprimée avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
    }

    private void clearFields() {
        clientIdField.clear();
        produitCombo.setValue(null);
        quantiteField.clear();
        prixField.clear();
        statutCombo.setValue("Pending");
        modePaiementCombo.setValue("Carte Bancaire");
        adresseLivraisonField.clear();
        commandeTable.getSelectionModel().clearSelection();
        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (clientIdField.getText().trim().isEmpty()) {
            errorMessage.append("- Le Client ID est requis\n");
        } else {
            try {
                Integer.parseInt(clientIdField.getText().trim());
            } catch (NumberFormatException e) {
                errorMessage.append("- Le Client ID doit être un nombre\n");
            }
        }

        if (produitCombo.getValue() == null) {
            errorMessage.append("- Veuillez sélectionner un produit\n");
        }

        if (quantiteField.getText().trim().isEmpty()) {
            errorMessage.append("- La quantité est requise\n");
        } else {
            try {
                int quantite = Integer.parseInt(quantiteField.getText().trim());
                if (quantite <= 0) {
                    errorMessage.append("- La quantité doit être supérieure à 0\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("- La quantité doit être un nombre\n");
            }
        }

        if (statutCombo.getValue() == null) {
            errorMessage.append("- Le statut est requis\n");
        }

        if (modePaiementCombo.getValue() == null) {
            errorMessage.append("- Le mode de paiement est requis\n");
        }

        if (adresseLivraisonField.getText().trim().isEmpty()) {
            errorMessage.append("- L'adresse de livraison est requise\n");
        }

        if (errorMessage.length() > 0) {
            showAlert("Erreur de validation", errorMessage.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 