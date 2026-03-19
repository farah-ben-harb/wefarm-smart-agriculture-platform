package controllers.terrains;

import entities.Transaction;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import services.ServiceTransaction;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListeTransac implements Initializable {

    @FXML private TableView<Transaction> transacTable;
    @FXML private TableColumn<Transaction, Integer> idCol;
    @FXML private TableColumn<Transaction, Double> montantCol;
    @FXML private TableColumn<Transaction, String> dateCol;
    @FXML private TableColumn<Transaction, String> typeCol;
    @FXML private TableColumn<Transaction, Integer> terrainIdCol;
    @FXML private TableColumn<Transaction, Integer> clientIdCol;
    @FXML private TableColumn<Transaction, Void> actionsCol;
    @FXML private Button ajouterBtn;

    private final ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();
    private final ServiceTransaction serviceTransaction = new ServiceTransaction();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupActionButtons();
        loadTransactions();
        setupButtons();
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        montantCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getMontant()).asObject());
        typeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        terrainIdCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTerrainId()).asObject());
        clientIdCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getClientId()).asObject());

        dateCol.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDateTransaction();
            String dateStr = (date != null) ? new SimpleDateFormat("dd/MM/yyyy").format(date) : "";
            return new SimpleStringProperty(dateStr);
        });
    }

    private void setupActionButtons() {
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttons = new HBox(5, deleteBtn);

            {
                // Style des boutons
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                // Action modification


                // Action suppression
                deleteBtn.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    supprimerTransaction(transaction);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }


    private void supprimerTransaction(Transaction transaction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer cette transaction ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la transaction #" + transaction.getId() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceTransaction.supprimer(transaction.getId());
            transactionsList.remove(transaction);
            showAlert("Succès", "Transaction supprimée avec succès", Alert.AlertType.INFORMATION);
        }
    }

    private void setupButtons() {
        ajouterBtn.setOnAction(event -> {
            // Ajoutez ici la logique pour ouvrir le formulaire d'ajout
            showAlert("Information", "Fonctionnalité d'ajout à implémenter", Alert.AlertType.INFORMATION);
        });
    }

    private void loadTransactions() {
        try {
            transactionsList.setAll(serviceTransaction.afficher());
            transacTable.setItems(transactionsList);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void refreshTable() {
        loadTransactions();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}