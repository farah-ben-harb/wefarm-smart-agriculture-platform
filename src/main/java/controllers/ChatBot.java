package controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatBot {

    @FXML
    private TextArea queryField;

    @FXML
    private Button generateButton;

    @FXML
    private Label loadingLabel;

    @FXML
    private TextArea recommendationResult;

    @FXML
    private Button backButton;

    @FXML
    private ComboBox<String> languageSelector;

    private static final String API_KEY = "AIzaSyAd1Cf8JVxfBI5eMmZAiiaVvy0_TmKXtL0";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    @FXML
    void initialize() {
        loadingLabel.setVisible(false);

        // Initialize language selector
        languageSelector.getItems().addAll("Français", "العربية");
        languageSelector.setValue("Français"); // Default language
    }

    @FXML
    void handleGenerateRecommendation(ActionEvent event) {
        String query = queryField.getText().trim();
        String selectedLanguage = languageSelector.getValue();

        if (query.isEmpty()) {
            showAlert("خطأ", "الرجاء وصف المعدات أو احتياجاتك.", "Erreur", "Veuillez décrire votre équipement ou votre besoin.");
            return;
        }

        loadingLabel.setVisible(true);
        generateButton.setDisable(true);
        recommendationResult.clear();

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return generateRecommendationFromAPI(query, selectedLanguage);
            }
        };

        task.setOnSucceeded(e -> {
            String apiResponse = task.getValue();
            recommendationResult.setText(apiResponse);
            loadingLabel.setVisible(false);
            generateButton.setDisable(false);
        });

        task.setOnFailed(e -> {
            loadingLabel.setVisible(false);
            generateButton.setDisable(false);
            showAlert("خطأ", "حدث خطأ أثناء توليد التوصيات.", "Erreur", "Erreur lors de la génération des recommandations: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private String generateRecommendationFromAPI(String query, String selectedLanguage) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Multilingual system context
            String systemContextFr = "Vous êtes un assistant IA spécialisé dans les recommandations d'achat d'équipements agricoles. Lorsqu'un utilisateur fournit le nom d'un équipement agricole spécifique (par exemple, tracteur, système d'irrigation, semoir), fournissez une analyse détaillée comprenant : \n\n" +
                    "La fourchette de prix moyenne actuelle pour cet équipement (basée sur des annonces en ligne, des prix de vendeurs et des comparaisons de marques) \n\n" +
                    "Une évaluation des prix pour déterminer si le prix proposé dans un magasin donné est acceptable, surévalué ou sous-évalué \n\n" +
                    "Les principales catégories ou types de cet équipement \n\n" +
                    "Une liste des fonctionnalités essentielles et des spécifications techniques basées sur la classe d'équipement et son utilisation \n\n" +
                    "Recommandations d'utilisation et compatibilité \n\n" +
                    "Rendez la sortie facile à comprendre pour les utilisateurs non techniques et indiquez si un achat est recommandé ou non. Suggérez des vendeurs ou des plateformes de confiance si possible.";

            String systemContextAr = "أنت مساعد ذكاء اصطناعي متخصص في توصيات شراء المعدات الزراعية. عندما يقدم المستخدم اسم معدة زراعية محددة (مثل الجرار الزراعي، نظام الري، ماكينة بذر البذور)، قدم تحليلاً مفصلاً يتضمن: \n\n" +
                    "نطاق السعر السوقي الحالي المتوسط لهذه المعدة (بناءً على الإعلانات عبر الإنترنت، وأسعار البائعين، ومقارنات العلامات التجارية) \n\n" +
                    "تقييم للأسعار لتحديد ما إذا كان السعر المعروض في متجر معين مقبولاً أو مبالغ فيه أو منخفضاً \n\n" +
                    "الفئات الرئيسية أو الأنواع الخاصة بهذه المعدة \n\n" +
                    "قائمة بالوظائف الأساسية والمواصفات التقنية بناءً على فئة المعدة واستخدامها \n\n" +
                    "توصيات الاستخدام والتوافق \n\n" +
                    "اجعل المخرجات سهلة الفهم للمستخدمين غير التقنيين وحدد ما إذا كان الشراء موصى به أم لا. اقترح بائعين أو منصات موثوقة إن أمكن.";

            String systemContext = selectedLanguage.equals("Français") ? systemContextFr : systemContextAr;
            String promptPrefix = selectedLanguage.equals("Français")
                    ? "Fournissez des recommandations d'optimisation des ventes pour l'équipement ou le besoin suivant : "
                    : "قدم توصيات لتحسين المبيعات للمعدات أو الحاجة التالية: ";

            String requestBody = "{"
                    + "  \"contents\": [{"
                    + "    \"role\": \"user\","
                    + "    \"parts\": [{"
                    + "      \"text\": \"" + systemContext + "\\n\\n" + promptPrefix + query + "\""
                    + "    }]"
                    + "  }]"
                    + "}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return extractTextFromResponse(response.toString());
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    throw new IOException("API Error: " + response.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return selectedLanguage.equals("Français")
                    ? "Erreur: " + e.getMessage()
                    : "خطأ: " + e.getMessage();
        }
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            System.out.println("Réponse JSON brute: " + jsonResponse);

            // Approche utilisant Jackson
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(jsonResponse);

                // Navigation dans la structure JSON pour trouver le texte
                if (rootNode.has("candidates") && rootNode.get("candidates").isArray() && rootNode.get("candidates").size() > 0) {
                    com.fasterxml.jackson.databind.JsonNode firstCandidate = rootNode.get("candidates").get(0);
                    if (firstCandidate.has("content") && firstCandidate.get("content").has("parts") &&
                            firstCandidate.get("content").get("parts").isArray() &&
                            firstCandidate.get("content").get("parts").size() > 0) {

                        com.fasterxml.jackson.databind.JsonNode firstPart = firstCandidate.get("content").get("parts").get(0);
                        if (firstPart.has("text")) {
                            return firstPart.get("text").asText();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Erreur lors du parsing JSON avec Jackson: " + e.getMessage());
            }

            // Méthodes alternatives de parsing JSON (identiques à l'implémentation précédente)
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"text\"\\s*:\\s*\"([^\"]*(?:\\\\\"[^\"]*)*)\"", java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher matcher = pattern.matcher(jsonResponse);

            if (matcher.find()) {
                String text = matcher.group(1);
                String processedText = text
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\t", "\t")
                        .replace("\\r", "");

                return processedText;
            }

            // Recherche alternative
            if (jsonResponse.contains("\"text\":")) {
                int startIndex = jsonResponse.indexOf("\"text\":") + 7;
                int endIndex = -1;
                boolean inQuote = false;
                boolean escaped = false;

                for (int i = startIndex + 1; i < jsonResponse.length(); i++) {
                    char c = jsonResponse.charAt(i);

                    if (escaped) {
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == '"') {
                        if (!inQuote) {
                            inQuote = true;
                        } else {
                            endIndex = i;
                            break;
                        }
                    }
                }

                if (endIndex > startIndex) {
                    String text = jsonResponse.substring(startIndex + 1, endIndex);
                    String processedText = text
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\t", "\t")
                            .replace("\\r", "");

                    return processedText;
                }
            }

            return "Aucune recommandation trouvée dans la réponse. Veuillez réessayer.";
        } catch (Exception e) {
            return "Erreur lors du traitement de la réponse: " + e.getMessage();
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Equipement/equipementfront.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("خطأ", "حدث خطأ أثناء العودة إلى الصفحة السابقة.", "Erreur", "Erreur lors du retour à la page précédente: " + e.getMessage());
        }
    }

    // Overloaded method to support multilingual alerts
    private void showAlert(String titleAr, String messageAr, String titleFr, String messageFr) {
        String title, message;
        if (languageSelector.getValue().equals("العربية")) {
            title = titleAr;
            message = messageAr;
        } else {
            title = titleFr;
            message = messageFr;
        }

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}