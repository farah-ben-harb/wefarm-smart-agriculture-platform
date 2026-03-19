package utils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.cloudinary.json.JSONObject;

public class ArabicTranslator {
    private static final Map<String, String> CACHE = new HashMap<>();

    public static String translateToArabic(String text) {
        if (text == null || text.isEmpty()) return text;
        if (CACHE.containsKey(text)) return CACHE.get(text);

        try {
            // 1. Encodage URL sécurisé
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

            // 2. Requête HTTP avec timeout
            HttpURLConnection conn = (HttpURLConnection) new URL(
                    "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=fr|ar")
                    .openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // 3. Lecture de la réponse en UTF-8
            String jsonResponse;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                jsonResponse = reader.lines().collect(Collectors.joining("\n"));
            }

            // 4. Parsing JSON robuste
            JSONObject json = new JSONObject(jsonResponse);
            String translated = json.getJSONObject("responseData")
                    .getString("translatedText");

            // 5. Nettoyage des séquences Unicode
            translated = decodeUnicodeEscapes(translated);

            CACHE.put(text, translated);
            return translated;

        } catch (Exception e) {
            e.printStackTrace();
            return text; // Retourne le texte original si échec
        }
    }

    private static String decodeUnicodeEscapes(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && i+1 < str.length() && str.charAt(i+1) == 'u') {
                String hex = str.substring(i+2, i+6);
                sb.append((char) Integer.parseInt(hex, 16));
                i += 5;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}