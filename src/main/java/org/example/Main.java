package org.example;

import controllers.OffreEmploi.DetailsOffreEmploiController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Parent;
import utils.SessionManager;

import java.io.IOException;

public class Main extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {

                primaryStage.getProperties().put("hostServices", this.getHostServices());
                // Vérifier d'abord si une session valide existe
                if (SessionManager.tryAutoLogin()) {
                        // Charger directement la page appropriée

                        SessionManager session = SessionManager.getInstance();
                        String fxmlPath = session.getRoles().contains("ADMIN")
                                ? "/views/Back.fxml"
                                : "/views/user/Home.fxml";

                        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                        Parent root = loader.load();

                        if (session.getRoles().contains("ADMIN")) {
                                primaryStage.setTitle("Admin Dashboard");
                        } else {
                                primaryStage.setTitle("Home");
                        }

                        Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
                        primaryStage.getIcons().add(icon);
                        primaryStage.setScene(new Scene(root, 1540, 800));
                } else {
                        // Si pas de session valide, charger la page de login
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/login.fxml"));
                        Parent root = loader.load();
                        primaryStage.setTitle("Login");
                        Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
                        primaryStage.getIcons().add(icon);
                        primaryStage.setScene(new Scene(root, 1540, 800));
                }

                primaryStage.show();
        }
        @Override
        public void stop() {
                DetailsOffreEmploiController.cleanup();
        }

        public static void main(String[] args) throws InterruptedException {
                launch(args);

        }
}
