package controllers.terrains;

import entities.Terrain;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ConversationService;
import services.MessageService;
import services.ServiceTerrain;
import services.UtilisateurService;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ListeTerrainFr implements Initializable {

    @FXML
    private GridPane terrainsGrid;

    private final ServiceTerrain terrainService = new ServiceTerrain();
    private final MessageService messageService = new MessageService();
    private final ConversationService conversationService = new ConversationService();
    private final int COLUMNS = 3;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadTerrains();
    }

    private void loadTerrains() {
        try {
            List<Terrain> terrains = terrainService.afficher();
            displayTerrains(terrains);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des terrains: " + e.getMessage());
        }
    }

    private void displayTerrains(List<Terrain> terrains) {
        terrainsGrid.getChildren().clear();

        int row = 0;
        int column = 0;

        for (Terrain terrain : terrains) {
            VBox card = createTerrainCard(terrain);
            terrainsGrid.add(card, column, row);

            column++;
            if (column == COLUMNS) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createTerrainCard(Terrain terrain) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-background-color: white; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 5, 0, 0);");
        card.setPrefWidth(280);
        card.setMaxWidth(280);

        // Image
        ImageView imageView = new ImageView();
        try {
            int imageNumber = 2 + (terrain.getId() % 5);
            Image image = new Image(getClass().getResourceAsStream(
                    "/img/t" + imageNumber + ".jpg"
            ));

            imageView.setImage(image);
            imageView.setFitWidth(250);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(false);
            imageView.setStyle("-fx-border-radius: 5;");
        } catch (Exception e) {
            imageView.setImage(new Image("/img/default_terrain.jpg"));
        }

        // Détails
        Label adresseLabel = new Label("📍 " + terrain.getAdresse());
        Label surfaceLabel = new Label("📐 " + terrain.getSurface() + " m²");
        Label prixLabel = new Label("💰 " + terrain.getPrix() + " DT");
        Label typeLabel = new Label("🌱 " + terrain.getTypeSol());

        // Style des labels
        String labelStyle = "-fx-font-size: 13; -fx-text-fill: #555;";
        adresseLabel.setStyle(labelStyle);
        surfaceLabel.setStyle(labelStyle);
        prixLabel.setStyle(labelStyle);
        typeLabel.setStyle(labelStyle);

        // Bouton de messagerie
        ImageView messageIcon = new ImageView(new Image("/icons/message-icon.jpg"));
        messageIcon.setFitWidth(20);
        messageIcon.setFitHeight(20);
        messageIcon.setStyle("-fx-cursor: hand;");
        messageIcon.setOnMouseClicked(event -> startConversation(terrain));

        // Conteneur pour le bouton (aligné à droite)
        HBox messageContainer = new HBox(messageIcon);
        messageContainer.setStyle("-fx-alignment: center-right;");

        card.getChildren().addAll(imageView, adresseLabel, surfaceLabel, prixLabel, typeLabel, messageContainer);
        return card;
    }

    private void startConversation(Terrain terrain) {
        try {
            Utilisateur client = SessionManager.getCurrentUser();
            if (client == null || client.getId() == 0) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Veuillez vous connecter pour envoyer un message");
                return;
            }

            int clientId = client.getId();
            int ownerId = terrain.getOwnerId();

            if (clientId == ownerId) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Vous ne pouvez pas vous envoyer un message à vous-même");
                return;
            }

            openMessagerieWindow(terrain, client);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la messagerie: " + e.getMessage());
        }
    }

    private void openMessagerieWindow(Terrain terrain, Utilisateur client) {
        try {
            // Récupérer l'owner (agriculteur) via ownerId
            Utilisateur owner = new UtilisateurService().getUtilisateurById(terrain.getOwnerId()); // Suppose un UtilisateurService
            if (owner == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Propriétaire non trouvé pour ce terrain.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/terrains/Conversation.fxml"));
            Parent root = loader.load();

            ConversationController controller = loader.getController();
            controller.setConversation(conversationService, client, owner, terrain);

            // Ouvrir la nouvelle fenêtre
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Messagerie - Terrain #" + terrain.getId());
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la fenêtre: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}