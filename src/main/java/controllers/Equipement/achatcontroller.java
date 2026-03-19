package controllers.Equipement;

import entities.Achat;
import entities.Equipement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import services.AchatService;
import services.AchatServiceImpl;
import services.EquipementService;
import services.EquipementServiceImpl;

import java.io.IOException;
import java.util.Date;

public class achatcontroller {


    @FXML 
    private Button exporter;
    
    @FXML 
    private Button add_achat;
    
    @FXML 
    private Button retour;
    
    @FXML 
    private TableColumn<Achat, Integer> id_achat;
    
    @FXML 
    private TableColumn<Achat, Integer> equipement_id;
    
    @FXML 
    private TableColumn<Achat, Date> date;
    
    @FXML 
    private TableColumn<Achat, Integer> total;

    @FXML 
    private TableView<Achat> achattable;
    
    @FXML 
    private TableColumn<Achat, Void> actionColumn;

    private ObservableList<Achat> achatList = FXCollections.observableArrayList();
    
    private AchatService achatService = new AchatServiceImpl();
    private EquipementService equipementService = new EquipementServiceImpl();
    
    @FXML
    void initialize() {

        
        // Configure cell value factories
        id_achat.setCellValueFactory(new PropertyValueFactory<>("id"));
        equipement_id.setCellValueFactory(new PropertyValueFactory<>("equipementId"));
        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        total.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Load data
        loadAchatsFromDB();

        if (actionColumn == null) {
            actionColumn = new TableColumn<>("Actions");
            actionColumn.setPrefWidth(150); // Set your desired width here
            achattable.getColumns().add(actionColumn);
        }

    

        // Configure action buttons
        addActionButtonsToTable();
        
        // Configure custom cell factories
        setupCustomCellFactories();
    }
    
    private void setupCustomCellFactories() {
        // Add custom cell factory for the equipement_id column to display equipment name instead of ID
        equipement_id.setCellFactory(column -> {
            return new TableCell<Achat, Integer>() {
                @Override
                protected void updateItem(Integer equipementId, boolean empty) {
                    super.updateItem(equipementId, empty);
                    
                    if (empty || equipementId == null) {
                        setText(null);
                    } else {
                        try {
                            Equipement equipement = equipementService.getEquipementById(equipementId);
                            if (equipement != null) {
                                setText(equipement.getNomEquipement());
                            } else {
                                setText(String.valueOf(equipementId));
                            }
                        } catch (Exception e) {
                            setText(String.valueOf(equipementId));
                        }
                    }
                }
            };
        });
        
        // Format the date column
        date.setCellFactory(column -> {
            return new TableCell<Achat, Date>() {
                private final java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd/MM/yyyy");
                
                @Override
                protected void updateItem(Date date, boolean empty) {
                    super.updateItem(date, empty);
                    
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(format.format(date));
                    }
                }
            };
        });
    }

    private void loadAchatsFromDB() {
        try {
            achatList.clear();
            achatList.addAll(achatService.getAllAchats());
            achattable.setItems(achatList);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des achats: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors du chargement des achats: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void deleteAchat(int id) {
        try {
            achatService.deleteAchat(id);
            System.out.println("Achat supprimé avec succès.");
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de la suppression: " + e.getMessage());
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
                    Achat selectedAchat = getTableView().getItems().get(getIndex());

                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Equipement/ajouterachat.fxml"));
                        Scene scene = new Scene(loader.load());

                        AddAchatController controller = loader.getController();
                        controller.initData(selectedAchat);
                        controller.setOnSaveCallback(() -> loadAchatsFromDB());
                        
                        Stage stage = new Stage();
                        stage.setTitle("Modifier un Achat");
                        stage.setScene(scene);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                btnDelete.setOnAction(event -> {
                    Achat selectedAchat = getTableView().getItems().get(getIndex());
                    
                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Confirmation de suppression");
                    confirmDialog.setHeaderText("Supprimer l'achat");
                    confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cet achat?");
                    
                    confirmDialog.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            deleteAchat(selectedAchat.getId());
                            loadAchatsFromDB();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    @FXML
    void ajouterachat(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Equipement/ajouterachat.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Achat");
            stage.setScene(new Scene(loader.load()));

            AddAchatController controller = loader.getController();
            controller.setOnSaveCallback(() -> loadAchatsFromDB());

            stage.show();
            stage.setOnHidden(e -> loadAchatsFromDB());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void retourEquipement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/equipement/equipementpage.fxml"));
            Scene scene = new Scene(loader.load());

            Stage newStage = new Stage();
            newStage.setTitle("Equipement");
            newStage.setScene(scene);
            newStage.show();

            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Navigation");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de naviguer vers la page Equipement: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void exportertable(ActionEvent event) {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save PDF File");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            java.io.File file = fileChooser.showSaveDialog(source.getScene().getWindow());

            if (file != null) {
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
                document.open();

                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Liste des Achats", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
                document.add(new com.itextpdf.text.Paragraph(" ")); // Add space

                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(4); // 4 columns
                pdfTable.setWidthPercentage(100);

                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
                String[] headers = {"ID", "Équipement", "Date", "Total"};
                for (String header : headers) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
                    pdfTable.addCell(cell);
                }

                for (Achat a : achatList) {
                    pdfTable.addCell(String.valueOf(a.getId()));
                    
                    // Get equipment name
                    String equipementName = "N/A";
                    try {
                        Equipement equipement = equipementService.getEquipementById(a.getEquipementId());
                        if (equipement != null) {
                            equipementName = equipement.getNomEquipement();
                        }
                    } catch (Exception e) {
                        // Use default if error
                    }
                    pdfTable.addCell(equipementName);
                    
                    // Format date
                    pdfTable.addCell(new java.text.SimpleDateFormat("dd/MM/yyyy").format(a.getDate()));
                    
                    // Add total
                    pdfTable.addCell(String.valueOf(a.getTotal()));
                }

                document.add(pdfTable);
                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Les données d'achat ont été exportées en PDF avec succès!");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de l'exportation en PDF: " + e.getMessage());
            alert.showAndWait();
        }
    }
}