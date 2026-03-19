package controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.SessionManager;


import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class navbarFrontController {
    @FXML
    public Label profileLabel;
    public Label homeLabel;
    @FXML
    MenuButton servicesMenu;
    @FXML private AnchorPane rootPane;

    // Variables pour la synthèse vocale
    private boolean ttsEnabled = false;
    private static final String VOICERSS_API_KEY = "75641d79125e4f879811f65e79f5f815";



    @FXML
    public void initialize() {
        // Texte initial en français
        updateTexts(false);

        // S'inscrire au gestionnaire de langue
        LanguageManager.getInstance().addTranslationListener("navbar", this::updateTexts);

        // Obtenir l'état TTS depuis le HomeController si disponible
        checkTTSStatus();
    }
    // Méthode pour vérifier l'état TTS actuel
    private void checkTTSStatus() {
        Platform.runLater(() -> {
            try {
                if (rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
                    Object ttsProperty = ((Stage) rootPane.getScene().getWindow()).getProperties().get("ttsEnabled");
                    if (ttsProperty instanceof Boolean) {
                        this.ttsEnabled = (Boolean) ttsProperty;
                    }
                }
            } catch (Exception e) {
                // Si une erreur survient, on laisse l'état par défaut (false)
                System.err.println("Impossible de récupérer l'état TTS: " + e.getMessage());
            }
        });
    }

    // Méthode pour permettre au HomeController de mettre à jour l'état TTS
    public void setTTSEnabled(boolean enabled) {
        this.ttsEnabled = enabled;
    }

    private void updateTexts(boolean isArabic) {
        homeLabel.setText(isArabic ? "الرئيسية" : "Accueil");
        servicesMenu.setText(isArabic ? "الخدمات" : "Services");
        profileLabel.setText(isArabic ? "ملفي الشخصي" : "Mon profil");

        // Mise à jour des options du menu
        // Si vous avez des Labels dans les CustomMenuItem, vous pouvez les mettre à jour ici aussi

        // Changer l'orientation du texte
        NodeOrientation orientation = isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT;
        rootPane.setNodeOrientation(orientation);
    }

    @FXML
    private void handleHover(MouseEvent event) {
        // Vérifier si TTS est activé
        if (!ttsEnabled) return;

        // Récupérer le texte à lire selon la source de l'événement
        String text = "";
        Object source = event.getSource();

        if (source instanceof Label) {
            text = ((Label) source).getText();
        } else if (source instanceof MenuButton) {
            text = ((MenuButton) source).getText();
        } else if (source instanceof HBox) {
            // Pour les HBox contenant des labels, chercher le label à l'intérieur
            for (Node child : ((HBox) source).getChildren()) {
                if (child instanceof Label) {
                    text = ((Label) child).getText();
                    break;
                }
            }
        }

        // Si aucun texte n'a été trouvé ou est vide, ne rien faire
        if (text == null || text.isEmpty()) return;

        // Vérifier dans quelle langue nous sommes pour utiliser la bonne voix
        boolean isArabic = LanguageManager.getInstance().isArabic();

        // Lecture du texte
        speakText(text, isArabic);
    }


    @FXML
    private void handleExit(MouseEvent event) {
        Label source = (Label) event.getSource();
        source.setStyle(source.getStyle().replace("; -fx-background-color: #e6e6e6;", ""));
    }
    // Méthode pour la synthèse vocale - reprise du HomeController
    private void speakText(String text, boolean isArabic) {
        if (!ttsEnabled || text == null || text.isEmpty()) return;

        // Utiliser un Service pour la tâche asynchrone
        Service<Void> ttsService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        playAudioDirectly(text, isArabic);
                        return null;
                    }
                };
            }
        };
        ttsService.start();
    }
    private void playAudioDirectly(String text, boolean isArabic) throws Exception {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        // Choisir la langue selon l'état actuel
        String lang = isArabic ? "ar-sa" : "fr-fr";

        String urlStr = "http://api.voicerss.org/?"
                + "key=" + VOICERSS_API_KEY
                + "&hl=" + lang
                + "&src=" + encodedText
                + "&c=MP3"
                + "&f=16khz_16bit_mono";

        try (InputStream audioStream = new URL(urlStr).openStream()) {
            AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

            if (!AudioSystem.isLineSupported(info)) {
                throw new LineUnavailableException("Audio line not supported");
            }

            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(audioFormat);
                line.start();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = audioStream.read(buffer)) != -1) {
                    line.write(buffer, 0, bytesRead);
                }

                line.drain();
                line.stop();
            }
        } catch (Exception e) {
            Platform.runLater(() -> fallbackToGoogleTTS(text, isArabic));
        }
    }

    private void fallbackToGoogleTTS(String text, boolean isArabic) {
        try {
            String lang = isArabic ? "ar" : "fr";
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String urlStr = "https://translate.google.com/translate_tts?ie=UTF-8&tl=" + lang + "&client=tw-ob&q=" + encodedText;

            // Télécharger l'audio temporairement
            InputStream input = new URL(urlStr).openStream();
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("tts", ".mp3");
            java.nio.file.Files.copy(input, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            input.close();

            // Lire l'audio dans l'application (MediaPlayer nécessite une URL locale)
            Media media = new Media(tempFile.toUri().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnError(() -> System.err.println("Erreur audio: " + mediaPlayer.getError().getMessage()));
            mediaPlayer.play();

        } catch (Exception e) {
            System.err.println("Google TTS playback failed: " + e.getMessage());
        }
    }

    //Traduction
    public void translateNavbar(boolean isArabic) {
        System.out.println("Exécution de translateNavbar (arabe: " + isArabic + ")");

        // Traduction des éléments principaux
        if (homeLabel != null) {
            homeLabel.setText(isArabic ? "الصفحة الرئيسية" : "Acceuil");
            System.out.println("homeLabel traduit: " + homeLabel.getText());
        } else {
            System.err.println("Erreur: homeLabel est null !");
        }

        if (servicesMenu != null) {
            servicesMenu.setText(isArabic ? "الخدمات" : "Services");
            System.out.println("servicesMenu traduit: " + servicesMenu.getText());
        }

        if (profileLabel != null) {
            profileLabel.setText(isArabic ? "ملفي الشخصي" : "Mon profile");
        }

        // Traduction des éléments du menu déroulant
        if (servicesMenu != null && servicesMenu.getItems() != null) {
            for (javafx.scene.control.MenuItem item : servicesMenu.getItems()) {
                if (item instanceof CustomMenuItem) {
                    Node content = ((CustomMenuItem) item).getContent();
                    if (content instanceof Label) {
                        Label label = (Label) content;
                        String originalText = label.getText();
                        String translatedText = originalText; // Par défaut

                        switch (originalText) {
                            case "MarketPlace": translatedText = isArabic ? "السوق" : "MarketPlace"; break;
                            case "Equipements": translatedText = isArabic ? "المعدات" : "Equipements"; break;
                            case "Offres d'emploi": translatedText = isArabic ? "عروض العمل" : "Offres d'emploi"; break;
                            case "Evénements": translatedText = isArabic ? "الأحداث" : "Evénements"; break;
                            case "Terrains": translatedText = isArabic ? "الأراضي" : "Terrains"; break;
                        }

                        label.setText(translatedText);
                        System.out.println("Menu item '" + originalText + "' traduit: " + translatedText);
                    }
                }
            }
        }
    }

    //**********************METHODE F4AFFICHAGE DE PROFIL , LOGOUT , ACCEUIL ET SERVICES*********************
    @FXML
    private void handleProfileClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/profile.fxml"));
            Parent root = loader.load();

            // 1. Récupérer la fenêtre actuelle et ses dimensions
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double currentWidth = currentStage.getWidth();
            double currentHeight = currentStage.getHeight();

            // 2. Créer la nouvelle scène avec la même taille
            Scene scene = new Scene(root, currentWidth, currentHeight); // <-- Taille forcée

            // 3. Appliquer la scène et désactiver le redimensionnement
            currentStage.setResizable(false); // Empêche le zoom
            currentStage.setScene(scene);
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            currentStage.getIcons().add(icon);

            // (Optionnel) Recentrer la fenêtre si nécessaire
            currentStage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



        public void handlePress(MouseEvent event) {
        Node source = (Node) event.getSource();
        if (source instanceof Label) {
            source.setStyle("-fx-font-size: 16px; -fx-text-fill: #2E8B57; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void goToHome(MouseEvent event) {
        try {
            Parent homePage = FXMLLoader.load(getClass().getResource("/views/user/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double width = stage.getWidth();
            double height = stage.getHeight();

            Scene scene = new Scene(homePage, width, height);
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            stage.getIcons().add(icon);
            stage.setScene(scene);
            stage.setTitle("Home");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleLogOut(MouseEvent event) {
        SessionManager.logout();

        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(e -> {
            try {
                Parent loginPage = FXMLLoader.load(getClass().getResource("/views/user/login.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                double width = stage.getWidth();
                double height = stage.getHeight();

                Scene scene = new Scene(loginPage, width, height);
                Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
                stage.getIcons().add(icon);
                stage.setScene(scene);
                stage.setTitle("Login");
                stage.setMaximized(true);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        delay.play();
    }
    //Interfaces offre d'emploi
    @FXML
    private void openOffresEmploiInterface(MouseEvent event) {
        String rawRole = SessionManager.getInstance().getRoles();
        String role = rawRole.replaceAll("[\\[\\]\"]", "");

        try {
            FXMLLoader loader;
            if ("ROLE_AGRICULTEUR".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffres.fxml"));
            } else if ("ROLE_EMPLOYE".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffresEmploye.fxml"));
            } else {
                System.out.println("Rôle non reconnu !");
                return;
            }

            Parent root = loader.load();

            // Récupérer la fenêtre actuelle et changer la scène
            Stage currentStage = (Stage) servicesMenu.getScene().getWindow();
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            currentStage.getIcons().add(icon);
            currentStage.setScene(new Scene(root));
            currentStage.setMaximized(true);

            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Interfaces événements
    @FXML
    private void openEventInterface(MouseEvent event) {
        String rawRole = SessionManager.getInstance().getRoles();
        String role = rawRole.replaceAll("[\\[\\]\"]", "");

        try {
            FXMLLoader loader;
            if ("ROLE_AGRICULTEUR".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/evenement/afficherEvenements.fxml"));
            } else if ("ROLE_CLIENT".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/evenement/afficherEvenementsClient.fxml"));
            } else {
                System.out.println("Rôle non reconnu !");
                return;
            }

            Parent root = loader.load();

            // Récupérer la fenêtre actuelle et changer la scène
            Stage currentStage = (Stage) servicesMenu.getScene().getWindow();
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            currentStage.getIcons().add(icon);
            currentStage.setScene(new Scene(root));
            currentStage.setMaximized(true);
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Interfaces équipements
    @FXML
    private void openEquipementInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Equipement/equipementfront.fxml"));
            Parent root = loader.load();

            // Obtenir la fenêtre actuelle
            Stage currentStage = (Stage) servicesMenu.getScene().getWindow();

            // Créer une nouvelle scène
            Scene scene = new Scene(root);
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            currentStage.getIcons().add(icon);
            currentStage.setScene(scene);
            currentStage.setTitle("Boutique Équipement");
            currentStage.setMaximized(true);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Afficher une alerte en cas d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Navigation");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de naviguer vers la boutique d'équipement: " + e.getMessage());
            alert.showAndWait();
        }
    }

    //INTERFACES TERRAINS
    @FXML
    private void goToListeTerrains(MouseEvent event) {
        try {
            // D'abord, fermer le menu contextuel si ouvert
            if (servicesMenu.isShowing()) {
                servicesMenu.hide();
            }

            String rawRole = SessionManager.getInstance().getRoles();
            String role = rawRole.replaceAll("[\\[\\]\"]", "");

            FXMLLoader loader;
            if ("ROLE_AGRICULTEUR".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/Terrains/ListeTerrainFrAgr.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/views/Terrains/ListeTerrainFr.fxml"));
            }

            Parent root = loader.load();

            // Obtenir la scène actuelle à partir d'un élément stable (comme le MenuButton lui-même)
            Scene currentScene = servicesMenu.getScene();
            if (currentScene == null) {
                currentScene = ((Node) event.getSource()).getScene();
            }

            Stage stage = (Stage) currentScene.getWindow();
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            stage.getIcons().add(icon);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible de charger la page des terrains: " + e.getMessage());
            alert.showAndWait();
        }
    }
    //Interfaces MarketPlace
    @FXML
    private void goToMarketplace(MouseEvent event) {
        try {
            // D'abord, fermer le menu contextuel si ouvert
            if (servicesMenu.isShowing()) {
                servicesMenu.hide();
            }

            String rawRole = SessionManager.getInstance().getRoles();
            String role = rawRole.replaceAll("[\\[\\]\"]", "");

            // Initialiser loader avec une valeur par défaut
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MarketPlace/DefaultPage.fxml"));

            if ("ROLE_AGRICULTEUR".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/views/MarketPlace/Marketplace.fxml"));
            } else if ("ROLE_ADMIN".equalsIgnoreCase(role)){
                loader = new FXMLLoader(getClass().getResource("/views/MarketPlace/ProduitManagement.fxml"));
            }

            Parent root = loader.load();

            // Obtenir la scène actuelle à partir d'un élément stable (comme le MenuButton lui-même)
            Scene currentScene = servicesMenu.getScene();
            if (currentScene == null) {
                currentScene = ((Node) event.getSource()).getScene();
            }

            Stage stage = (Stage) currentScene.getWindow();
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            stage.getIcons().add(icon);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible de charger la page des produits: " + e.getMessage());
            alert.showAndWait();
        }
    }





}
