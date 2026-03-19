package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class Quotes {

    public static String fetchAgricultureQuote() throws IOException, InterruptedException {
        try {
            // Charger les citations depuis le fichier JSON dans les ressources
            InputStream inputStream = Quotes.class.getResourceAsStream("/citation.json");
            if (inputStream == null) {
                throw new IOException("Fichier citation.json introuvable dans les ressources");
            }

            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader(inputStream);

            List<Citation> quotes = gson.fromJson(reader, new TypeToken<List<Citation>>(){}.getType());

            if (quotes == null || quotes.isEmpty()) {
                throw new IOException("Aucune citation trouvée dans le fichier");
            }

            // Sélectionner une citation au hasard
            int randomIndex = (int) (Math.random() * quotes.size());
            Citation randomQuote = quotes.get(randomIndex);

            return randomQuote.getContent() + " — " + randomQuote.getAuthor();
        } catch (Exception e) {
            // Si échec, essayer l'API en ligne comme fallback
            try {
                return fetchZenQuote();
            } catch (Exception ex) {
                throw new IOException("Impossible de récupérer une citation: " + e.getMessage());
            }
        }
    }

    // Classe statique pour la désérialisation
    public static class Citation {
        private String content;
        private String author;

        public String getContent() {
            return content;
        }

        public String getAuthor() {
            return author;
        }
    }

    public static String fetchZenQuote() throws IOException, InterruptedException {
        String url = "https://zenquotes.io/api/random";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonResponse = response.body();

        // Meilleure gestion du parsing JSON
        return jsonResponse.split("\"q\":\"")[1].split("\",")[0];
    }
}