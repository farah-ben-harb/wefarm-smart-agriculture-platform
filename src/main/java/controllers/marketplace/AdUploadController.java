package controllers.marketplace;

import entities.Ad;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import services.AdService;
import utils.CloudinaryUtil;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AdUploadController {
    @FXML private Label imagePathLabel;
    @FXML private Button selectImageBtn;
    @FXML private ProgressIndicator uploadProgress;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private CheckBox isActiveCheckbox;
    @FXML private Button saveAdBtn;
    @FXML private Button clearBtn;
    @FXML private TableView<Ad> adsTable;
    @FXML private TableColumn<Ad, ImageView> imageColumn;
    @FXML private TableColumn<Ad, Date> startDateColumn;
    @FXML private TableColumn<Ad, Date> endDateColumn;
    @FXML private TableColumn<Ad, Boolean> isActiveColumn;
    @FXML private TableColumn<Ad, Void> actionsColumn;

    private File selectedImageFile;
    private String cloudinaryUrl;
    private final AdService adService;

    public AdUploadController() {
        this.adService = new AdService();
    }

    @FXML
    public void initialize() {
        setupButtons();
        setupTable();
        loadAds();
    }

    private void setupButtons() {
        selectImageBtn.setOnAction(e -> handleSelectImage());
        saveAdBtn.setOnAction(e -> handleSave());
        clearBtn.setOnAction(e -> handleClear());
    }

    private void setupTable() {
        // Image column
        imageColumn.setCellValueFactory(param -> {
            ImageView imageView = new ImageView(new Image(param.getValue().getImageUrl()));
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            return new SimpleObjectProperty<>(imageView);
        });

        // Date columns
        startDateColumn.setCellValueFactory(param -> 
            new SimpleObjectProperty<>(param.getValue().getStartDate()));
        endDateColumn.setCellValueFactory(param -> 
            new SimpleObjectProperty<>(param.getValue().getEndDate()));

        // Active status column
        isActiveColumn.setCellValueFactory(param -> 
            new SimpleObjectProperty<>(param.getValue().isActive()));

        // Actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");
            private final Button toggleButton = new Button("Activer/Désactiver");

            {
                deleteButton.setOnAction(event -> {
                    Ad ad = getTableView().getItems().get(getIndex());
                    handleDelete(ad);
                });

                toggleButton.setOnAction(event -> {
                    Ad ad = getTableView().getItems().get(getIndex());
                    handleToggleActive(ad);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, deleteButton, toggleButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(selectImageBtn.getScene().getWindow());
        if (selectedFile != null) {
            selectedImageFile = selectedFile;
            imagePathLabel.setText(selectedFile.getName());
        }
    }

    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        saveAdBtn.setDisable(true);
        uploadProgress.setVisible(true);

        // Upload image to Cloudinary
        new Thread(() -> {
            try {
                cloudinaryUrl = CloudinaryUtil.getInstance().uploadImage(selectedImageFile);
                
                javafx.application.Platform.runLater(() -> {
                    try {
                        saveAd();
                        loadAds();
                        handleClear();
                    } catch (Exception e) {
                        showError("Erreur", "Erreur lors de la sauvegarde de la publicité: " + e.getMessage());
                    } finally {
                        saveAdBtn.setDisable(false);
                        uploadProgress.setVisible(false);
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Erreur", "Erreur lors de l'upload de l'image: " + e.getMessage());
                    saveAdBtn.setDisable(false);
                    uploadProgress.setVisible(false);
                });
            }
        }).start();
    }

    private void saveAd() {
        Ad ad = new Ad();
        ad.setImageUrl(cloudinaryUrl);
        ad.setStartDate(Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        ad.setEndDate(Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        ad.setActive(isActiveCheckbox.isSelected());
        
        adService.createAd(ad);
    }

    private void handleDelete(Ad ad) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer cette publicité ?");

        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                // Delete from Cloudinary first
                String publicId = CloudinaryUtil.getInstance().getPublicIdFromUrl(ad.getImageUrl());
                if (publicId != null) {
                    CloudinaryUtil.getInstance().deleteImage(publicId);
                }
                
                // Then delete from database
                adService.deleteAd(ad.getId());
                loadAds();
            } catch (Exception e) {
                showError("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void handleToggleActive(Ad ad) {
        try {
            ad.setActive(!ad.isActive());
            adService.updateAd(ad);
            loadAds();
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    private void handleClear() {
        selectedImageFile = null;
        cloudinaryUrl = null;
        imagePathLabel.setText("Aucune image sélectionnée");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        isActiveCheckbox.setSelected(true);
    }

    private void loadAds() {
        try {
            List<Ad> ads = adService.getAllAds();
            adsTable.setItems(FXCollections.observableArrayList(ads));
        } catch (Exception e) {
            showError("Erreur", "Erreur lors du chargement des publicités: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (selectedImageFile == null) {
            showError("Erreur", "Une image est requise");
            return false;
        }

        if (startDatePicker.getValue() == null) {
            showError("Erreur", "La date de début est requise");
            return false;
        }

        if (endDatePicker.getValue() == null) {
            showError("Erreur", "La date de fin est requise");
            return false;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(today)) {
            showError("Erreur", "La date de début doit être aujourd'hui ou après");
            return false;
        }

        if (endDate.isBefore(startDate)) {
            showError("Erreur", "La date de fin doit être après la date de début");
            return false;
        }

        return true;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 