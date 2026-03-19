package utils;

import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import entities.Equipement;

import java.util.*;

public class EmailService {

    private static final String BREVO_API_KEY = "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";
    private static final String FROM_EMAIL = "douraid7d@gmail.com";
    private static final String FROM_NAME = "WeFarm";

    private static ApiClient getApiClient() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(BREVO_API_KEY);
        return defaultClient;
    }
    /**
     * Sends an email with equipment purchase details and approval/rejection buttons
     * @param toEmail The recipient's email address
     * @param userName The recipient's name
     * @param equipement The equipment to be purchased
     * @param quantity The quantity to be purchased
     * @param totalPrice The total price of the purchase
     */
    public static void sendPurchaseApprovalEmail(String toEmail, String userName, Equipement equipement, int quantity, double totalPrice) {
        try {

            
        
            String purchaseId = UUID.randomUUID().toString();
            
            String htmlContent = String.format(
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f2f4f8; padding: 20px; border-radius: 12px;'>"
                            + "<div style='text-align: center; margin-bottom: 20px;'>"
                            + "</div>"
                            + "<div style='background-color: #ffffff; padding: 30px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>"
                            + "<h2 style='color: #3f51b5;'>Bonjour %s,</h2>"
                            + "<p style='color: #555;'>Nous avons reçu votre demande d'achat. Voici les détails :</p>"

                            + "<div style='margin: 20px 0; border: 1px solid #eee; border-radius: 8px; padding: 15px;'>"
                            + "<h3 style='color: #333; margin-top: 0;'>Détails de l'équipement</h3>"
                            + "<p><strong>Nom:</strong> %s</p>"
                            + "<p><strong>Catégorie:</strong> %s</p>"
                            + "<p><strong>Prix unitaire:</strong> %.2f DT</p>"
                            + "<p><strong>Quantité:</strong> %d</p>"
                            + "<p><strong>Prix total:</strong> <span style='color: #3f51b5; font-weight: bold; font-size: 18px;'>%.2f DT</span></p>"
                            + "</div>"

                            + "<p style='color: #555;'>Veuillez confirmer ou rejeter cet achat en cliquant sur l'un des boutons ci-dessous :</p>"

                            + "<div style='text-align: center; margin: 30px 0;'>"
                            + "<a href='http://localhost:8080/approve-purchase?id=%s&equipId=%d&quantity=%d&total=%.2f' "
                            + "style='display: inline-block; padding: 12px 24px; background-color: #4CAF50; color: #ffffff; text-decoration: none; border-radius: 25px; font-weight: bold; margin-right: 15px;'>Approuver l'achat</a>"
                            + "<a href='http://localhost:8080/reject-purchase?id=%s' "
                            + "style='display: inline-block; padding: 12px 24px; background-color: #F44336; color: #ffffff; text-decoration: none; border-radius: 25px; font-weight: bold;'>Rejeter l'achat</a>"
                            + "</div>"

                            + "<p style='color: #777; font-style: italic; text-align: center;'>Si vous n'avez pas demandé cet achat, veuillez ignorer cet email.</p>"
                            + "</div>"
                            + "<div style='text-align: center; font-size: 12px; color: #aaa; margin-top: 20px;'>"
                            + "<p>© 2025 - Système de gestion d'équipement</p>"
                            + "</div>"
                            + "</div>", 
                    userName,
                    equipement.getNomEquipement(),
                    equipement.getCategory(),
                    equipement.getPrix(),
                    quantity,
                    totalPrice,
                    purchaseId, equipement.getId(), quantity, totalPrice,
                    purchaseId
            );
        
            // Configure the email API
            TransactionalEmailsApi api = new TransactionalEmailsApi(getApiClient());

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(FROM_EMAIL);
            sender.setName(FROM_NAME);

            List<SendSmtpEmailTo> toList = new ArrayList<>();
            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(toEmail);
            to.setName(userName);
            toList.add(to);

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(toList);
            sendSmtpEmail.setHtmlContent(htmlContent);
            sendSmtpEmail.setSubject("Confirmation d'achat d'équipement - Action requise");

            CreateSmtpEmail response = api.sendTransacEmail(sendSmtpEmail);
            System.out.println("✅ Email de confirmation d'achat envoyé avec succès. Message ID: " + response.getMessageId());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email de confirmation d'achat: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
