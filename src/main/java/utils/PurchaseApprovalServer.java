package utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import controllers.Equipement.frontequipement;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Simple HTTP server to handle purchase approval/rejection links from emails
 */
public class PurchaseApprovalServer {
    private static HttpServer server;
    private static frontequipement controller;
    

    public static void startServer(frontequipement controllerInstance) {
        if (server != null) {
            return; // Server already running
        }
        
        controller = controllerInstance;
        
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/approve-purchase", new ApprovePurchaseHandler());
            server.createContext("/reject-purchase", new RejectPurchaseHandler());
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("✅ Serveur d'approbation d'achat démarré sur le port 8080");
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du démarrage du serveur d'approbation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Stop the approval server
     */
    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("Serveur d'approbation d'achat arrêté");
        }
    }
    
    /**
     * Parse query parameters from a URI
     */
    private static Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
    
    /**
     * Handler for purchase approval requests
     */
    static class ApprovePurchaseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI());
            String response;
            
            try {
                // Extract parameters
                String purchaseId = params.get("id");
                int equipId = Integer.parseInt(params.get("equipId"));
                int quantity = Integer.parseInt(params.get("quantity"));
                
                // Correction ici: remplacer la virgule par un point avant de parser
                String totalStr = params.get("total").replace(",", ".");
                double total = Double.parseDouble(totalStr);
                
                // Process the approved purchase
                if (controller != null) {
                    controller.processApprovedPurchase(equipId, quantity, total);
                    response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>"
                            + "<h1 style='color: #4CAF50;'>Achat approuvé avec succès!</h1>"
                            + "<p>Votre achat a été traité et enregistré dans notre système.</p>"
                            + "<p>Vous pouvez fermer cette fenêtre.</p>"
                            + "</body></html>";
                } else {
                    response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>"
                            + "<h1 style='color: #F44336;'>Erreur de traitement</h1>"
                            + "<p>Le contrôleur n'est pas disponible pour traiter votre demande.</p>"
                            + "<p>Veuillez réessayer plus tard ou contacter le support.</p>"
                            + "</body></html>";
                }
            } catch (Exception e) {
                response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>"
                        + "<h1 style='color: #F44336;'>Erreur de traitement</h1>"
                        + "<p>Une erreur s'est produite lors du traitement de votre demande: " + e.getMessage() + "</p>"
                        + "<p>Veuillez réessayer plus tard ou contacter le support.</p>"
                        + "</body></html>";
                e.printStackTrace();
            }
            
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes("UTF-8"));
            os.close();
        }
    }
    
    /**
     * Handler for purchase rejection requests
     */
    static class RejectPurchaseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI());
            String purchaseId = params.get("id");
            
            String response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>"
                    + "<h1 style='color: #F44336;'>Achat rejeté</h1>"
                    + "<p>Vous avez choisi de ne pas procéder à cet achat.</p>"
                    + "<p>Aucune action n'a été effectuée sur votre compte.</p>"
                    + "<p>Vous pouvez fermer cette fenêtre.</p>"
                    + "</body></html>";
            
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes("UTF-8"));
            os.close();
        }
    }
}