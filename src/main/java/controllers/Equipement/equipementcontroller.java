package controllers.Equipement;

import entities.Equipement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.EquipementService;
import services.EquipementServiceImpl;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class equipementcontroller {

    @FXML
    private Button exporter;

    @FXML
    private TableColumn<Equipement, Integer> id_equipement;

    @FXML
    private TableColumn<Equipement, String> nom_equipement;
    
    @FXML
    private TableColumn<Equipement, Integer> quantite_disponible;
    
    @FXML
    private TableColumn<Equipement, Double> prix;
    
    @FXML
    private TableColumn<Equipement, String> image;
    
    @FXML
    private TableColumn<Equipement, String> category;

    @FXML
    private TableView<Equipement> equipementtable;

    @FXML
    private TableColumn<Equipement, Void> actionColumn;

    private ObservableList<Equipement> equipementList = FXCollections.observableArrayList();
    
    private EquipementService equipementService = new EquipementServiceImpl();
    
    @FXML
    void initialize() {
        id_equipement.setCellValueFactory(new PropertyValueFactory<>("id"));
        nom_equipement.setCellValueFactory(new PropertyValueFactory<>("nomEquipement"));
        quantite_disponible.setCellValueFactory(new PropertyValueFactory<>("quantiteDisponible"));
        prix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        image.setCellValueFactory(new PropertyValueFactory<>("image"));
        category.setCellValueFactory(new PropertyValueFactory<>("category"));

        loadEquipementsFromDB();

        if (actionColumn == null) {
            actionColumn = new TableColumn<>("Actions");
            actionColumn.setMinWidth(200);
            actionColumn.setMaxWidth(300);
            equipementtable.getColumns().add(actionColumn);

        }

        addActionButtonsToTable();
    }
    
    @FXML
    void navigateToAchat(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Equipement/achatpage.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadEquipementsFromDB() {
        equipementList.clear();
        equipementList.addAll(equipementService.getAllEquipements());
        equipementtable.setItems(equipementList);
    }

    private void deleteEquipement(int id) {
        try {
            System.out.println("Tentative de suppression de l'équipement avec ID: " + id);
            
            // Appel direct à la méthode de suppression
            equipementService.deleteEquipement(id);
            
            // Si on arrive ici, c'est que la suppression a réussi
            System.out.println("Équipement supprimé avec succès.");
            
            // Rafraîchir la liste des équipements
            loadEquipementsFromDB();
            
            // Notification de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Suppression réussie");
            alert.setHeaderText(null);
            alert.setContentText("L'équipement a été supprimé avec succès.");
            alert.showAndWait();
            
        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression: " + e.getMessage());
            e.printStackTrace();
            
            // Afficher un message d'erreur à l'utilisateur avec un message plus clair
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de suppression");
            alert.setHeaderText(null);
            
            String errorMessage = e.getMessage();
            
            // Vérifier si l'erreur est liée à une contrainte de clé étrangère
            if (errorMessage != null && (
                errorMessage.contains("clé étrangère") || 
                errorMessage.contains("foreign key constraint") ||
                errorMessage.contains("référencé dans des achats") ||
                errorMessage.contains("constraint violation") ||
                errorMessage.contains("integrity constraint"))) {
                alert.setContentText("Impossible de supprimer cet équipement car il est utilisé dans des achats.\n" +
                                    "Veuillez d'abord supprimer les achats associés à cet équipement.");
            } else {
                alert.setContentText("Une erreur est survenue lors de la suppression de l'équipement: " + errorMessage);
            }
            
            alert.showAndWait();
        }
    }

    private void addActionButtonsToTable() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox pane = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setOnAction(event -> {
                    Equipement selectedEquipement = getTableView().getItems().get(getIndex());
                    editEquipement(selectedEquipement);
                });

                btnDelete.setOnAction(event -> {
                    Equipement selectedEquipement = getTableView().getItems().get(getIndex());

                    if (selectedEquipement != null) {

                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation de suppression");
                        alert.setHeaderText("Supprimer l'équipement");
                        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet équipement?\nID: " + selectedEquipement.getId() + "\nNom: " + selectedEquipement.getNomEquipement());
                        
                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                // Supprimer l'équipement et rafraîchir la table
                                // Stocker l'ID avant la suppression
                                int idToDelete = selectedEquipement.getId();
                                System.out.println("Suppression de l'équipement ID: " + idToDelete);
                                deleteEquipement(idToDelete);
                                // Ne pas appeler loadEquipementsFromDB() ici car il est déjà appelé dans deleteEquipement()
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void editEquipement(Equipement equipement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Equipement/ajouterequipement.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);

            add_equipement_controller controller = loader.getController();
            controller.initData(equipement);
            controller.setOnSaveCallback(this::loadEquipementsFromDB);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void ajouterequipement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Equipement/ajouterequipement.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);

            add_equipement_controller controller = loader.getController();
            controller.setOnSaveCallback(this::loadEquipementsFromDB);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void exportertable(ActionEvent event) {
        try {

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            java.io.File file = fileChooser.showSaveDialog(exporter.getScene().getWindow());
            
            if (file != null) {

                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
                document.open();
                

                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Liste des Équipements", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
                document.add(new com.itextpdf.text.Paragraph(" "));
                
                // Créer le tableau
                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(6); // 6 colonnes
                pdfTable.setWidthPercentage(100);
                
                // Ajouter les en-têtes
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
                String[] headers = {"ID", "Nom", "Quantité", "Prix", "Image", "Catégorie"};
                
                for (String header : headers) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
                    pdfTable.addCell(cell);
                }
                
                // Ajouter les données
                for (Equipement equipement : equipementList) {
                    pdfTable.addCell(String.valueOf(equipement.getId()));
                    pdfTable.addCell(equipement.getNomEquipement());
                    pdfTable.addCell(String.valueOf(equipement.getQuantiteDisponible()));
                    pdfTable.addCell(String.valueOf(equipement.getPrix()));
                    pdfTable.addCell(equipement.getImage());
                    pdfTable.addCell(equipement.getCategory());
                }
                
                document.add(pdfTable);
                document.close();
                

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Exportation réussie");
                alert.setHeaderText(null);
                alert.setContentText("Le fichier PDF a été créé avec succès à l'emplacement: " + file.getAbsolutePath());
                alert.showAndWait();
                
            }
        } catch (Exception e) {
            // Gérer les erreurs
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'exportation");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur s'est produite lors de l'exportation: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}