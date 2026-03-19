package controllers.Equipement;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AIRecommendationController {

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
    private Button equipmentButton;
    
    private static final String API_KEY = "AIzaSyAd1Cf8JVxfBI5eMmZAiiaVvy0_TmKXtL0";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    @FXML
    void initialize() {
        loadingLabel.setVisible(false);
    }

    @FXML
    void handleGenerateRecommendation(ActionEvent event) {
        String query = queryField.getText().trim();
        
        if (query.isEmpty()) {
            showAlert("Erreur", "Veuillez décrire votre équipement ou votre besoin.");
            return;
        }
        
        loadingLabel.setVisible(true);
        generateButton.setDisable(true);
        recommendationResult.clear();
        
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return generateRecommendationFromAPI(query);
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
            showAlert("Erreur", "Erreur lors de la génération des recommandations: " + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    private String generateRecommendationFromAPI(String query) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String systemContext = "You are an AI assistant specialized in agricultural equipment purchasing recommendations. When a user provides the name of a specific agricultural equipment (e.g., tractor, irrigation system, seed spreader), provide a detailed analysis including: \n\n" +
                    "The average current market price range for that equipment (based on online listings, vendor prices, and brand comparisons) \n\n" +
                    "A pricing evaluation to determine whether the offered price in a given shop is acceptable, overpriced, or underpriced \n\n" +
                    "The main categories or types of that equipment (e.g., compact tractor, utility tractor, row crop tractor) \n\n" +
                    "A list of core functionalities and technical specifications based on the equipment class and usage \n\n" +
                    "Best-use recommendations and compatibility (e.g., suitable for small farms, hilly terrain, specific crops) \n\n" +
                    "Make the output easy to understand for non-technical users and highlight if a purchase is recommended or not. Suggest trusted vendors or platforms if possible.";

            String requestBody = "{"
                    + "  \"contents\": [{"
                    + "    \"role\": \"user\","
                    + "    \"parts\": [{"
                    + "      \"text\": \"" + systemContext + "\\n\\nProvide sales optimization recommendations for the following equipment or need: " + query + "\""
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
            return "Erreur: " + e.getMessage();
        }
    }
    
    private String extractTextFromResponse(String jsonResponse) {
        try {
            System.out.println("Réponse JSON brute: " + jsonResponse);
            
            // Approche utilisant Jackson (solution recommandée)
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
                // Continuer avec les méthodes alternatives si Jackson échoue
            }
            
            // Méthode alternative 1: Utiliser une expression régulière simplifiée
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
            
            // Méthode alternative 2: Recherche d'indices
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
        showAlert("Erreur", "Erreur lors du retour à la page précédente: " + e.getMessage());
    }
}


    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}