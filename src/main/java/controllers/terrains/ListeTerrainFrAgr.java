package controllers.terrains;

import entities.Terrain;
import entities.Utilisateur;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ServiceTerrain;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ListeTerrainFrAgr implements Initializable {

    @FXML
    private FlowPane terrainFlowPane;
    @FXML
    private StackPane chatbotContainer; // Remplace WebView
    @FXML
    private Button chatbotButton;
    @FXML
    private Button messagesButton;
    @FXML
    private Button statButton;

    private WebView chatbotWebView;
    private ServiceTerrain terrainService = new ServiceTerrain();
    private static final String CHATBOT_ID = "idzJ3D2TRHr3OiLT_f96i";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation différée du WebView
        Platform.runLater(this::initializeWebView);

        try {
            // Charger la liste des terrains
            loadTerrains();
            setupMessagesButton();
        } catch (SQLException e) {
            handleError("Erreur lors du chargement des terrains", e);
        }
    }

    private void initializeWebView() {
        try {
            chatbotWebView = new WebView();
            chatbotWebView.setPrefSize(350, 700);

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body, html { margin:0; padding:0; width:100%; height:100%; overflow:hidden; }
                        iframe { width:100%; height:100%; border:none; }
                    </style>
                </head>
                <body>
                    <iframe src="https://www.chatbase.co/chatbot-iframe/%s" frameborder="0"></iframe>
                </body>
                </html>
                """.formatted(CHATBOT_ID);

            chatbotWebView.getEngine().loadContent(htmlContent);
            chatbotContainer.getChildren().add(chatbotWebView);

            // Configurer le bouton après l'initialisation
            setupChatbotButton();
        } catch (Exception e) {
            System.err.println("Erreur d'initialisation du WebView: " + e.getMessage());
            Label fallbackLabel = new Label("Chatbot indisponible");
            chatbotContainer.getChildren().add(fallbackLabel);
        }
    }

    private void loadTerrains() throws SQLException {
        List<Terrain> terrains = terrainService.afficher();
        for (Terrain terrain : terrains) {
            VBox card = createTerrainCard(terrain);
            terrainFlowPane.getChildren().add(card);
        }
    }

    private void setupChatbotButton() {
        chatbotButton.setOnAction(e -> toggleChatbotVisibility());

        chatbotButton.setOnMouseEntered(event -> {
            chatbotButton.setStyle("-fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; " +
                    "-fx-background-color: #3AA789; -fx-text-fill: white; -fx-font-weight: bold;");
        });

        chatbotButton.setOnMouseExited(event -> {
            chatbotButton.setStyle("-fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; " +
                    "-fx-background-color: #2E8B75; -fx-text-fill: white; -fx-font-weight: bold;");
        });
    }

    private void setupMessagesButton() {
        messagesButton.setOnMouseEntered(event -> {
            messagesButton.setStyle("-fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; " +
                    "-fx-background-color: #3399FF; -fx-text-fill: white; -fx-font-size: 16px;");
        });

        messagesButton.setOnMouseExited(event -> {
            messagesButton.setStyle("-fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; " +
                    "-fx-background-color: #007ACC; -fx-text-fill: white; -fx-font-size: 16px;");
        });
    }

    private void toggleChatbotVisibility() {
        boolean isVisible = chatbotContainer.isVisible();
        chatbotContainer.setVisible(!isVisible);

        FadeTransition ft = new FadeTransition(Duration.millis(300), chatbotContainer);
        ft.setFromValue(isVisible ? 1.0 : 0.0);
        ft.setToValue(isVisible ? 0.0 : 1.0);
        ft.play();
    }
    private void handleError(String message, Exception e) {
        e.printStackTrace();
        showAlert("Erreur", message, Alert.AlertType.ERROR);
    }

    private VBox createTerrainCard(Terrain terrain) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-background-color: white; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 5, 0, 0);");
        card.setPrefWidth(280);
        card.setMaxWidth(280);

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

        Label adresseLabel = new Label("Adresse : " + terrain.getAdresse());
        Label surfaceLabel = new Label("Surface : " + terrain.getSurface() + " m²");
        Label prixLabel = new Label("Prix : " + terrain.getPrix() + " DT");
        Label typeSolLabel = new Label("Type de sol : " + terrain.getTypeSol());

        adresseLabel.setFont(new Font(14));
        surfaceLabel.setFont(new Font(14));
        prixLabel.setFont(new Font(14));
        typeSolLabel.setFont(new Font(14));

        Button transactionBtn = new Button("Faire Transaction");
        transactionBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        transactionBtn.setOnAction(event -> openTransactionForm(terrain));

        card.getChildren().addAll(imageView, adresseLabel, surfaceLabel, prixLabel, typeSolLabel, transactionBtn);
        return card;
    }

    @FXML
    private void openTransactionForm(Terrain terrain) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Terrains/AjoutTransac.fxml"));
            Parent root = loader.load();

            AjoutTransac controller = loader.getController();
            controller.setTerrain(terrain);
            controller.setListeTerrainController(this);

            Utilisateur agriculteur = new Utilisateur();
            agriculteur.setId(SessionManager.getInstance().getId());
            agriculteur.setEmail(SessionManager.getInstance().getEmail());
            agriculteur.setRoles(SessionManager.getInstance().getRoles());

            controller.setAgriculteurConnecte(agriculteur);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire", Alert.AlertType.ERROR);
        }
    }

    public void refreshTerrains() {
        try {
            terrainFlowPane.getChildren().clear();
            List<Terrain> terrains = terrainService.afficher();

            for (Terrain terrain : terrains) {
                VBox card = createTerrainCard(terrain);
                terrainFlowPane.getChildren().add(card);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du rafraîchissement des terrains");
            e.printStackTrace();
            showAlert("Erreur", "Impossible de rafraîchir la liste des terrains", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void openAjoutTerrain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Terrains/AjoutTerrain.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un nouveau terrain");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'interface d'ajout de terrain", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void openStatView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Terrains/PowerBIView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques des Terrains");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la vue des statistiques", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void openMessagesInterface(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Terrains/AgriculteurMessages.fxml"));
            Parent root = loader.load();

            AgriculteurMessagesController controller = loader.getController();
            Utilisateur agriculteur = new Utilisateur();
            agriculteur.setId(SessionManager.getInstance().getId());
            agriculteur.setEmail(SessionManager.getInstance().getEmail());
            agriculteur.setRoles(SessionManager.getInstance().getRoles());
            controller.setCurrentUser(agriculteur);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Messagerie de l'agriculteur");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'interface de messagerie", Alert.AlertType.ERROR);
        }
    }
}