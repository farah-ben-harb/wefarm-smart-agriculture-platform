package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import utils.SessionManager;

public class NavbarBackController {
    public Label logoutLabel;
    public Label profileLabel;
    public HBox offreEmploiHBox;
    public HBox eventsHBox;
    public HBox TerrainsHBox;
    @FXML
    public HBox equipementsHBox;
    public Button logoutButton;
    public HBox venteHBox;
    @FXML
    private HBox usersHBox;

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleExit(MouseEvent event) {
        Label source = (Label) event.getSource();
        source.setStyle(source.getStyle().replace("; -fx-background-color: #e6e6e6;", ""));
    }

    @FXML
    private void handleLogOut(ActionEvent event) {
        SessionManager.clearSession();

        try {
            Parent loginPage = FXMLLoader.load(getClass().getResource("/views/user/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(loginPage);
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            stage.getIcons().add(icon);
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de login");
        }
    }


    @FXML
    private void handleProfileClick(MouseEvent event) {
        try {
            // Charger le fichier FXML de la page Profile
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/profile.fxml"));
            Parent profileRoot = loader.load();

            // Récupérer la scène actuelle depuis n'importe quel composant
            Scene currentScene = profileLabel.getScene();
            Stage currentStage = (Stage) currentScene.getWindow();
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            currentStage.getIcons().add(icon);

            // Créer et afficher la nouvelle scène
            currentStage.setScene(new Scene(profileRoot));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void openAdminPage(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/user/admin.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
        stage.getIcons().add(icon);
        stage.setScene(new Scene(root));

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goToDashboard(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/back.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
        stage.getIcons().add(icon);
        stage.setScene(new Scene(root));

    }
//    @FXML
//    public void handleLogOut(ActionEvent actionEvent) throws IOException {
//        SessionManager.logout();
//
//        PauseTransition delay = new PauseTransition(Duration.seconds(1));
//        delay.setOnFinished(e -> {
//            redirectToLogin();
//        });
//        delay.play();
//    }
//
//    public void redirectToLogin() {
//        try {
//            // Charger le fichier FXML de la page de login
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
//            Parent root = loader.load();
//
//            // Créer une nouvelle scène
//            Scene scene = new Scene(root);
//
//            // Récupérer la Stage actuelle à partir du bouton ou d'un autre nœud de la scène actuelle
//            Stage stage = (Stage) logoutButton.getScene().getWindow(); // Supposons que logoutButton est un bouton dans votre interface
//
//            // Changer la scène
//            stage.setScene(scene);
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//@FXML
//public void handleLogOut(ActionEvent actionEvent) throws IOException {
//    SessionManager.logout();
//
//    PauseTransition delay = new PauseTransition(Duration.seconds(1));
//    delay.setOnFinished(e -> {
//        try {
//            redirectToLogin(actionEvent);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    });
//    delay.play();
//}
//
//    private void redirectToLogin(ActionEvent event) throws IOException {
//        Parent root = FXMLLoader.load(getClass().getResource("/views/user/login.fxml"));
//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.setScene(new Scene(root));
//        Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
//        stage.getIcons().add(icon);
//    }
    //Intrface offre d'emploi Admin
    @FXML
    private void openOffreEmploiAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffresAdmin.fxml"));
            Parent root = loader.load();

            // Récupérer la fenêtre actuelle
            Stage stage = (Stage) offreEmploiHBox.getScene().getWindow();

            // Sauvegarder la taille actuelle
            double width = stage.getWidth();
            double height = stage.getHeight();

            // Appliquer la nouvelle scène
            Scene scene = new Scene(root);
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            stage.getIcons().add(icon);
            stage.setScene(scene);
            stage.setTitle("Administration");


            // Restaurer la taille précédente
            stage.setWidth(width);
            stage.setHeight(height);


            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page Admin !");
        }
    }
    //Intrface événements Admin
    @FXML
    private void openEventsAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/evenement/afficherEvenementsAdmin.fxml"));
            Parent root = loader.load();

            // Récupérer la fenêtre actuelle
            Stage stage = (Stage) eventsHBox.getScene().getWindow();

            // Sauvegarder la taille actuelle
            double width = stage.getWidth();
            double height = stage.getHeight();

            // Appliquer la nouvelle scène
            Scene scene = new Scene(root);
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            stage.getIcons().add(icon);
            stage.setScene(scene);
            stage.setTitle("Administration");

            // Restaurer la taille précédente
            stage.setWidth(width);
            stage.setHeight(height);


            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page Admin !");
        }
    }


    @FXML
    private void openTerrainAdmin(MouseEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Terrains/ListeTerrain.fxml"));
            Parent root = loader.load();

            // Récupérer la fenêtre actuelle
            Stage stage = (Stage) TerrainsHBox.getScene().getWindow();

            // Sauvegarder la taille actuelle
            double width = stage.getWidth();
            double height = stage.getHeight();

            // Appliquer la nouvelle scène
            Scene scene = new Scene(root);
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            stage.getIcons().add(icon);
            stage.setScene(scene);
            stage.setTitle("Administration");

            // Restaurer la taille précédente
            stage.setWidth(width);
            stage.setHeight(height);


            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page Admin !");
        }
    }

    @FXML
    private void openEquipementPage(MouseEvent event) throws IOException {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Equipement/equipementpage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            stage.getIcons().add(icon);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page des équipements");
        }
    }
    @FXML
    private void openVentesPage(MouseEvent event) throws IOException {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/MarketPlace/ProduitManagement.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            stage.getIcons().add(icon);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page des équipements");
        }
    }
}
