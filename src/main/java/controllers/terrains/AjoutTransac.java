package controllers.terrains;

import entities.Terrain;
import entities.Transaction;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceTransaction;
import services.UtilisateurService;
import utils.Mail;
import utils.PdfGenerator;
import utils.SessionManager;

import javax.mail.MessagingException;
import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AjoutTransac {
    private static final Logger LOGGER = Logger.getLogger(AjoutTransac.class.getName());

    @FXML private Label terrainInfoLabel;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<Utilisateur> clientCombo;
    @FXML private TextField montantField;
    @FXML private DatePicker datePicker;

    private Terrain terrain;
    private ListeTerrainFrAgr listeTerrainController;
    private final ServiceTransaction transactionService = new ServiceTransaction();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final Mail mailService = new Mail(); // Initialize Mail service
    private Utilisateur agriculteurConnecte;

    public void setAgriculteurConnecte(Utilisateur agriculteur) {
        this.agriculteurConnecte = agriculteur;
        if (agriculteur == null || !agriculteur.getRoles().contains("AGRICULTEUR")) {
            showAlert("Erreur", "Aucun agriculteur valide connecté", Alert.AlertType.ERROR);
            closeWindow();
        }
    }

    @FXML
    public void initialize() {
        try {
            // Vérification du rôle agriculteur
            if (!SessionManager.getInstance().getRoles().contains("AGRICULTEUR")) {
                showAlert("Erreur", "Accès réservé aux agriculteurs", Alert.AlertType.ERROR);
                closeWindow();
                return;
            }

            // Initialisation du ComboBox des types de transaction
            typeCombo.getItems().addAll("Achat", "Vente", "Location", "Échange");

            // Configuration de l'affichage des clients
            clientCombo.setCellFactory(param -> new ListCell<Utilisateur>() {
                @Override
                protected void updateItem(Utilisateur user, boolean empty) {
                    super.updateItem(user, empty);
                    setText(empty || user == null ? null :
                            String.format("%s %s (%s)", user.getFirstName(), user.getLastName(), user.getEmail()));
                }
            });

            clientCombo.setButtonCell(new ListCell<Utilisateur>() {
                @Override
                protected void updateItem(Utilisateur user, boolean empty) {
                    super.updateItem(user, empty);
                    setText(empty || user == null ? null : user.getFirstName() + " " + user.getLastName());
                }
            });

            datePicker.setValue(LocalDate.now());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            showAlert("Erreur", "Initialisation impossible", Alert.AlertType.ERROR);
            closeWindow();
        }
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
        terrainInfoLabel.setText("Transaction pour le terrain #" + terrain.getId());
        loadClients();
    }

    public void setListeTerrainController(ListeTerrainFrAgr controller) {
        this.listeTerrainController = controller;
    }

    private void loadClients() {
        try {
            List<Utilisateur> clients = utilisateurService.getAllUtilisateurs()
                    .stream()
                    .filter(user -> user.getRoles().contains("ROLE_CLIENT") ||
                            user.getRoles().contains("ROLE_USER"))
                    .collect(Collectors.toList());

            clientCombo.getItems().setAll(clients);

            if (clients.isEmpty()) {
                showAlert("Information", "Aucun client trouvé", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur de chargement des clients", e);
            showAlert("Erreur", "Chargement des clients impossible", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleValider() {
        try {
            // Validation des champs
            if (typeCombo.getValue() == null || clientCombo.getValue() == null ||
                    montantField.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.ERROR);
                return;
            }

            // Validation du montant
            double montant;
            try {
                montant = Double.parseDouble(montantField.getText());
                if (montant <= 0) {
                    showAlert("Erreur", "Le montant doit être positif", Alert.AlertType.ERROR);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Montant invalide", Alert.AlertType.ERROR);
                return;
            }

            // Création de la transaction
            Transaction transaction = new Transaction();
            transaction.setTerrainId(terrain.getId());
            transaction.setAgriculteurId(SessionManager.getInstance().getId());
            transaction.setClientId(clientCombo.getValue().getId());
            transaction.setType(typeCombo.getValue());
            transaction.setMontant(montant);
            transaction.setDateTransaction(Date.valueOf(datePicker.getValue()));

            // Enregistrement
            transactionService.ajouter(transaction);

            // Génération du PDF
            File pdfFile = null;
            try {
                pdfFile = PdfGenerator.generateContract(
                        transaction,
                        terrain,
                        agriculteurConnecte,
                        clientCombo.getValue()
                );

                // Ouvrir le PDF après génération
                if (pdfFile.exists()) {
                    java.awt.Desktop.getDesktop().open(pdfFile);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la génération du PDF", e);
                showAlert("Avertissement", "Le contrat a été enregistré mais le PDF n'a pas pu être généré", Alert.AlertType.WARNING);
            }

            // Envoi de l'email de confirmation au client
            try {
                String recipientEmail = clientCombo.getValue().getEmail();
                String subject = "Confirmation de votre transaction - WeFarm";
                String emailContent = String.format(
                        "Bonjour %s %s,\n\n" +
                                "Nous vous confirmons que votre transaction a été enregistrée avec succès.\n\n" +
                                "Détails de la transaction :\n" +
                                "- Terrain : #%d\n" +
                                "- Type : %s\n" +
                                "- Montant : %.2f TND\n" +
                                "- Date : %s\n\n" +
                                "Un contrat PDF a été généré et est disponible pour consultation.\n\n" +
                                "Cordialement,\nL'équipe WeFarm.",
                        clientCombo.getValue().getFirstName(),
                        clientCombo.getValue().getLastName(),
                        terrain.getId(),
                        transaction.getType(),
                        transaction.getMontant(),
                        transaction.getDateTransaction().toString()
                );

                mailService.sendEmail(recipientEmail, subject, emailContent);
            } catch (MessagingException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de l'envoi de l'email au client", e);
                showAlert("Avertissement", "Transaction enregistrée, mais l'email de confirmation n'a pas pu être envoyé.", Alert.AlertType.WARNING);
            }

            // Confirmation
            showAlert("Succès", "Transaction enregistrée avec succès. Le contrat a été généré.", Alert.AlertType.INFORMATION);

            // Rafraîchissement
            if (listeTerrainController != null) {
                listeTerrainController.refreshTerrains();
            }

            closeWindow();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL", e);
            showAlert("Erreur BD", "Erreur lors de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur inattendue", e);
            showAlert("Erreur", "Une erreur inattendue est survenue", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) terrainInfoLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}