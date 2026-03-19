package utils;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.messages.TextMessage;

public class SMSCompte {

    public void envoyerSMS(String numero, String contenuMessage) {
        // Initialisation du client Vonage
        VonageClient client = VonageClient.builder()
                .apiKey("1f6be1e5")
                .apiSecret("5ZTJ11aa6YVnhy3X")
                .build();

        // Création du message SMS
        TextMessage message = new TextMessage(
                "WeFarm",      // Expéditeur
                numero,           // Destinataire (ex: "216XXXXXXXX")
                contenuMessage    // Contenu du SMS
        );

        try {
            SmsSubmissionResponse response = client.getSmsClient().submitMessage(message);
            if (response.getMessages().get(0).getStatus() == MessageStatus.OK) {
                System.out.println("✅ Message envoyé avec succès !");
            } else {
                System.out.println("❌ Échec de l'envoi : " + response.getMessages().get(0).getErrorText());
            }
        } catch (Exception e) {
            System.out.println("🚫 Erreur d’envoi : " + e.getMessage());
        }
    }
}
