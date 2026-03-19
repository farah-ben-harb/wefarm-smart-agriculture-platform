package services;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PredictService {
    private final String flaskApiUrl;
    private final HttpClient httpClient;

    public PredictService(String flaskApiUrl) {
        this.flaskApiUrl = flaskApiUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    public double predict(double surface, String typeSol, String adresse) throws Exception {
        // Create nested JSON payload
        JSONObject terrainData = new JSONObject();
        terrainData.put("surface", surface);
        terrainData.put("typeSol", typeSol);
        terrainData.put("adresse", adresse);

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("terrain", terrainData);

        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(flaskApiUrl + "/predict"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload.toString()))
                .build();

        try {
            // Send request and get response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Log the status code aand raw response for debugging
            System.out.println("API Response Status Code: " + response.statusCode());
            System.out.println("API Response Body: " + response.body());

            // Check if the response is successful (status code 2xx)
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                // Parse response
                JSONObject responseJson = new JSONObject(response.body());
                return responseJson.getDouble("predicted_price");
            } else {
                throw new RuntimeException("API returned an error: Status " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la prédiction : " + e.getMessage(), e);
        }
    }
}