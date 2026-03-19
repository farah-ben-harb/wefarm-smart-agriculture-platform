package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import utils.ArabicTranslator;


import javafx.scene.image.ImageView;

import java.awt.*;
import javafx.scene.control.TextArea; // JavaFX TextArea
import javafx.scene.control.TextField;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


import javax.sound.sampled.*;


public class HomeController {

    @FXML
    public ImageView logoutButton;
    @FXML
    public Label profileLabel;
    public ImageView logoutIcon;
    public StackPane slideshowContainer;
    public Label servicesText;
    public Button traduirBtn;
    public Button scannerBtn;
    public Button chatBotBtn;

    @FXML private MenuButton servicesMenu;
    @FXML private Label service1Title;
    @FXML private Label service1Desc;
    @FXML private Label service2Title;
    @FXML private Label service2Desc;
    @FXML private Label service3Title;
    @FXML private Label service3Desc;
    @FXML private Label service4Title;
    @FXML private Label service4Desc;
    @FXML private Label service5Title;
    @FXML private Label service5Desc;
    @FXML private Label visitLabel;

    @FXML private AnchorPane navbar; // correspond à fx:id="navbar"

    private navbarFrontController navbarController;
    @FXML private ImageView imageView1;
    @FXML private ImageView imageView2;
    @FXML private ImageView imageView3;

    @FXML private VBox chatBotPanel;
    @FXML private Button closeChatBotBtn;
    @FXML private TextArea chatArea;
    @FXML private TextField userInput;
    @FXML private ToggleButton voiceInputBtn;
    @FXML private Button sendMessageBtn;

    private Timeline slideshowTimeline;
    private int currentImageIndex = 0;



    // Variable pour suivre l'état
    private boolean ttsEnabled = false;

    // Référence au bouton
    @FXML
    private ImageView ttsButton;

    private static final String VOICERSS_API_KEY = "75641d79125e4f879811f65e79f5f815"; // Remplacez par votre clé API VoiceRSS

    // Traduction
    private boolean isArabic = false;
    private ArabicTranslator translator = new ArabicTranslator(); // Assurez-vous d'avoir cette classe

    // Méthode pour activer/désactiver
    @FXML
    private void toggleTextToSpeech(MouseEvent event) {
        // Inverser l'état actuel de la synthèse vocale
        ttsEnabled = !ttsEnabled;

        // Mettre à jour l'apparence du bouton TTS
        updateTTSButton();

        // Déterminer la langue actuelle
        boolean isArabic = LanguageManager.getInstance().isArabic();

        // Préparer le message selon la langue et l'état
        String message;
        if (isArabic) {
            // Message en arabe
            message = ttsEnabled ? "تم تفعيل المساعدة الصوتية" : "تم إيقاف المساعدة الصوتية";
        } else {
            // Message en français
            message = ttsEnabled ? "Synthèse vocale activée" : "Synthèse vocale désactivée";
        }

        // Annoncer le changement d'état (utiliser la bonne langue pour la voix)
        speakText(message);

        // Propager l'état TTS au navbarController
        updateNavbarTTSState();

        // Stocker l'état dans les propriétés de la scène pour les autres contrôleurs
        if (navbar.getScene() != null && navbar.getScene().getWindow() != null) {
            ((Stage) navbar.getScene().getWindow()).getProperties().put("ttsEnabled", ttsEnabled);
        }

        event.consume(); // Empêche la propagation de l'événement
    }
    // Méthode pour partager l'état TTS avec le navbarController
    private void updateNavbarTTSState() {
        if (navbarController != null) {
            navbarController.setTTSEnabled(ttsEnabled);
        } else {
            // Si nous n'avons pas encore le contrôleur, essayons de le récupérer
            initializeNavbarController();
            if (navbarController != null) {
                navbarController.setTTSEnabled(ttsEnabled);
            }
        }
    }

    // Met à jour l'icône du bouton
    private void updateTTSButton() {
        String imagePath = ttsEnabled ? "/img/muet.png" : "/img/sonOn.png";
        ttsButton.setImage(new javafx.scene.image.Image(imagePath));
    }


    // Méthode pour lire du texte
    private void speakText(String text) {
        if (!ttsEnabled || text == null || text.isEmpty()) return;

        // Utiliser un Service pour la tâche asynchrone
        Service<Void> ttsService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        playAudioDirectly(text);
                        return null;
                    }
                };
            }
        };
        ttsService.start();
    }

    private void playAudioDirectly(String text) throws Exception {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String urlStr = "http://api.voicerss.org/?"
                + "key=" + VOICERSS_API_KEY
                + "&hl=ar-sa"
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
            Platform.runLater(() -> fallbackToGoogleTTS(text));
        }
    }

    private void fallbackToGoogleTTS(String text) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String urlStr = "https://translate.google.com/translate_tts?ie=UTF-8&tl=ar&client=tw-ob&q=" + encodedText;

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


    @FXML
    public void initialize() {
        setupSlideshow();
        setupResponsiveBehavior();
        updateTTSButton();

        // Initialiser le contrôleur de navbar
        initializeNavbarController();

        // S'inscrire au gestionnaire de langue et traduire l'interface initiale
        LanguageManager.getInstance().addTranslationListener("home", this::translateUI);

        // Initialiser la traduction initiale
        translateUI(LanguageManager.getInstance().isArabic());

        // Vérifier si un état TTS existe déjà dans les propriétés de la scène
        Platform.runLater(() -> {
            if (navbar.getScene() != null && navbar.getScene().getWindow() != null) {
                Object ttsProperty = ((Stage) navbar.getScene().getWindow()).getProperties().get("ttsEnabled");
                if (ttsProperty instanceof Boolean) {
                    ttsEnabled = (Boolean) ttsProperty;
                    updateTTSButton();
                    updateNavbarTTSState();
                } else {
                    // Si l'état n'existe pas encore, définissez-le
                    ((Stage) navbar.getScene().getWindow()).getProperties().put("ttsEnabled", ttsEnabled);
                }
            }
        });
        // Gestion du chatbot
        chatBotBtn.setOnAction(e -> toggleChatBot());
        closeChatBotBtn.setOnAction(e -> chatBotPanel.setVisible(false));

        // Envoi de message avec Entrée
        //userInput.setOnAction(e -> sendMessage());
        sendMessageBtn.setOnAction(e -> sendMessage());

        // Gestion de l'entrée vocale
        voiceInputBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                startVoiceRecognition();
            } else {
                stopVoiceRecognition();
            }
        });


    }

    //Méthode pour écouter après click sur texte
    @FXML
    private void handleTextClick(MouseEvent event) {
        if (!ttsEnabled) {
            showAlert("TTS désactivé", "Activez d'abord la synthèse vocale en cliquant sur l'icône d'œil.");
            return;
        }

        Node source = (Node) event.getSource();
        String text = "";

        if (source instanceof Label) {
            text = ((Label) source).getText();
        } else {
            text = findFirstLabelText(source);
        }

        if (!text.isEmpty()) {
            speakText(text);
        }
    }


    //Méthode d'animation d'images
    private void setupSlideshow() {
        // Afficher la première image
        imageView1.setOpacity(1);

        slideshowTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> {
                    switchImage();
                })
        );
        slideshowTimeline.setCycleCount(Timeline.INDEFINITE);
        slideshowTimeline.play();
    }
    private void setupResponsiveBehavior() {
        // Écouteur pour le redimensionnement de la fenêtre
        slideshowContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((obs2, oldWidth, newWidth) -> {
                    updateImageSizes();
                });
            }
        });
    }

    private void updateImageSizes() {
        // Calculer la largeur disponible (largeur de la scène moins les marges)
        double sceneWidth = slideshowContainer.getScene().getWidth();
        double containerWidth = sceneWidth - 10; // 20px de marge de chaque côté

        // Limiter la largeur maximale si nécessaire
        containerWidth = Math.min(containerWidth, 1800);

        // Appliquer la largeur calculée
        slideshowContainer.setMaxWidth(containerWidth);

        // Ajuster la hauteur proportionnellement si nécessaire
        double aspectRatio = 16.0/9.0; // Ratio d'aspect (modifiable selon vos besoins)
        double newHeight = containerWidth / aspectRatio;
        slideshowContainer.setPrefHeight(newHeight);

        // Mettre à jour les ImageView
        imageView1.setFitHeight(newHeight);
        imageView2.setFitHeight(newHeight);
        imageView3.setFitHeight(newHeight);
    }

    private void switchImage() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), getCurrentImageView());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            currentImageIndex = (currentImageIndex + 1) % 3;
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), getCurrentImageView());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private ImageView getCurrentImageView() {
        switch(currentImageIndex) {
            case 0: return imageView1;
            case 1: return imageView2;
            case 2: return imageView3;
            default: return imageView1;
        }
    }

    private String findFirstLabelText(Node parent) {
        if (parent instanceof Parent) {
            for (Node child : ((Parent) parent).getChildrenUnmodifiable()) {
                if (child instanceof Label) {
                    return ((Label) child).getText();
                }
                if (child instanceof Parent) {
                    String text = findFirstLabelText(child);
                    if (!text.isEmpty()) return text;
                }
            }
        }
        return "";
    }
    //Méthodes d'alertes
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    //Méthode pour aller au site Web
    public void handleWebsiteClick(javafx.event.ActionEvent actionEvent) {
        try {
            // Option 1: Utilisation de HostServices
            HostServices hostServices = getHostServices();
            if (hostServices != null) {
                hostServices.showDocument("https://127.0.0.1:8000/front/pages");
            } else {
                // Option 2: Fallback avec Desktop
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("https://127.0.0.1:8000/front/pages"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le site web: " + e.getMessage());
        }
    }
    private HostServices getHostServices() {
        if (navbar != null && navbar.getScene() != null && navbar.getScene().getWindow() instanceof Stage) {
            Object hostServices = ((Stage) navbar.getScene().getWindow()).getProperties().get("hostServices");
            if (hostServices instanceof HostServices) {
                return (HostServices) hostServices;
            }
        }
        return null;
    }
    //Traduction
    @FXML
    private void toggleTranslation() {
        // Utilisez le gestionnaire centralisé
        LanguageManager.getInstance().toggleLanguage();

        // Mise à jour du texte du bouton de traduction (fait normalement dans translateUI)
        boolean isArabic = LanguageManager.getInstance().isArabic();
        traduirBtn.setText(isArabic ? "Traduire en français" : "ترجمة للعربية");
    }
    private void initializeNavbarController() {
        try {
            Node navbarNode = navbar; // Récupère le nœud inclus
            FXMLLoader loader = (FXMLLoader) navbarNode.getProperties().get("fxmlloader");
            if (loader != null) {
                navbarController = loader.getController();
                System.out.println("Navbar controller rechargé avec succès !");
            }
        } catch (Exception e) {
            System.err.println("Échec critique de chargement du contrôleur navbar");
            e.printStackTrace();
        }
    }

    private void translateUI(boolean isArabic) {
        servicesText.setText(isArabic ? "الخدمات": "Services");

        service1Title.setText(isArabic ? "إدارة عروض العمل" : "Gestion des offres d'emploi");
        service1Desc.setText(isArabic ? "ابحث عن أو قدم وظائف في القطاع الزراعي." : "Trouvez ou proposez des emplois dans le secteur agricole.");

        service2Title.setText(isArabic ? "إدارة المعدات الزراعية" : "Gestion des équipements agricoles");
        service2Desc.setText(isArabic ? "استأجر أو شارك المعدات الزراعية الحديثة." : "Louez ou partagez des équipements agricoles modernes.");

        service3Title.setText(isArabic ? "إدارة الأراضي الزراعية" : "Gestion des terrains agricoles");
        service3Desc.setText(isArabic ? "حسن استخدام الأراضي الزراعية الخاصة بك." : "Optimisez l'utilisation de vos terres agricoles.");

        service4Title.setText(isArabic ? "السوق عبر الإنترنت" : "Marché en ligne");
        service4Desc.setText(isArabic ? "بيع المنتجات الزراعية مباشرة للمستهلكين." : "Vendez vos produits agricoles directement aux consommateurs.");

        service5Title.setText(isArabic ? "إدارة الأحداث الزراعية" : "Gestion des événements agricoles");
        service5Desc.setText(isArabic ? "نظم أو شارك في أحداث القطاع الزراعي." : "Organisez ou participez à des événements du secteur agricole.");

        // Pied de page
        visitLabel.setText(isArabic ? "قم بزيارة منصتنا: " : "Visitez notre plateforme : ");

        // Bouton de traduction
        traduirBtn.setText(isArabic ? "Traduire en français" : "ترجمة للعربية");

        // Changer la direction du texte si arabe
        NodeOrientation orientation = isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT;
        // Appliquer à tous les conteneurs qui nécessitent une orientation RTL/LTR
        service1Title.getParent().setNodeOrientation(orientation);
        service2Title.getParent().setNodeOrientation(orientation);
        service3Title.getParent().setNodeOrientation(orientation);
        service4Title.getParent().setNodeOrientation(orientation);
        service5Title.getParent().setNodeOrientation(orientation);
        visitLabel.getParent().setNodeOrientation(orientation);
    }
    //CHATBOT
    private void toggleChatBot() {
        chatBotPanel.setVisible(!chatBotPanel.isVisible());
        if (chatBotPanel.isVisible()) {
            chatArea.appendText("Assistant: Bonjour ! Comment puis-je vous aider ?\n");
        }
    }

    private void sendMessage() {
        String message = userInput.getText().trim();
        if (!message.isEmpty()) {
            chatArea.appendText("Vous: " + message + "\n");
            userInput.setText("");
            // Ici vous pouvez ajouter la logique pour obtenir une réponse du chatbot
            processUserMessage(message);
        }
    }

    private void processUserMessage(String message) {
        // Exemple simple de réponse
        String response = "Je suis un chatbot basique. Pour une version plus avancée, vous devrez intégrer une API comme OpenAI.";
        chatArea.appendText("Assistant: " + response + "\n");
    }

    private void startVoiceRecognition() {
        // Implémentez la reconnaissance vocale ici
        // Vous pouvez utiliser des bibliothèques comme CMU Sphinx ou Google Cloud Speech
        chatArea.appendText("Assistant: Écoute en cours... (fonctionnalité à implémenter)\n");
    }

    private void stopVoiceRecognition() {
        // Arrêter la reconnaissance vocale
        chatArea.appendText("Assistant: Écoute arrêtée\n");
    }



}
