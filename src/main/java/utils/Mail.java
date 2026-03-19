package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;
import com.google.zxing.WriterException;


public class Mail {

    private final String senderEmail = "benharbfarah85@gmail.com";
    private final String senderPassword = "gsom tadz kcdg hiwy";

    private Session getSession() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
    }

    public void sendConfirmationEmail(String recipientEmail, String token) throws MessagingException {
        String subject = "Confirmation de votre compte";
        String emailContent = "Bonjour,\n\n" +
                "Merci de vous être inscrit sur Waves. Voici votre code de confirmation :\n\n" +
                "Code: " + token + "\n\n" +
                "Ce code expirera dans 24 heures.\n\n" +
                "Cordialement,\nL'équipe WeFarm.";

        sendEmail(recipientEmail, subject, emailContent);
    }

    public void sendOTPEmail(String recipientEmail, String otpCode) throws MessagingException {
        String subject = "Votre code OTP - Réinitialisation de mot de passe";
        String emailContent = "Bonjour,\n\n" +
                "Vous avez demandé une réinitialisation de votre mot de passe. Voici votre code OTP :\n\n" +
                "Code OTP : " + otpCode + "\n\n" +
                "Ce code est valide pour une durée limitée.\n\n" +
                "Cordialement,\nL'équipe WeFarm.";

        sendEmail(recipientEmail, subject, emailContent);
    }

    public void sendEmail(String recipientEmail, String subject, String content) throws MessagingException {
        Session session = getSession();
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject(subject);
        message.setText(content);

        Transport.send(message);
        System.out.println("📩 Email envoyé à " + recipientEmail);
    }
    public void sendReservationConfirmation(String recipientEmail, String prenomClient,
                                            String titreEvenement, int reservationId,
                                            String qrContent) throws MessagingException {
        String subject = "Confirmation de votre réservation";

        // Générer le QR code
        String qrCodeImage;
        try {
            qrCodeImage = QRCodeGenerator.generateQRCodeImage(qrContent, 200, 200);
        } catch (WriterException | IOException e) {
            throw new MessagingException("Erreur lors de la génération du QR code", e);
        }

        // Construire le contenu HTML de l'email
        String emailContent = "<html><body style='font-family: Arial, sans-serif;'>"
                + "<h2 style='color: #2e7d32;'>Bonjour " + prenomClient + ",</h2>"
                + "<p>Votre réservation pour l'événement <strong>" + titreEvenement + "</strong> a été confirmée.</p>"
                + "<div style='background: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;'>"
                + "<h3 style='color: #1b5e20;'>Votre QR code d'accès</h3>"
                + "<img src='" + qrCodeImage + "' alt='QR Code' style='display: block; margin: 0 auto; width: 200px;'>"
                + "<p style='text-align: center; font-size: 12px; color: #666;'>"
                + "Présentez ce code à l'entrée de l'événement</p>"
                + "</div>"
                + "<p>Détails de votre réservation :</p>"
                + "<ul>"
                + "<li>Numéro de réservation: <strong>" + reservationId + "</strong></li>"
                + "<li>" + qrContent.replace("\n", "</li><li>") + "</li>"
                + "</ul>"
                + "<p>Merci pour votre confiance et à bientôt !</p>"
                + "<p>Cordialement,<br><strong>L'équipe WeFarm</strong></p>"
                + "</body></html>";

        // Configuration du message MIME
        // Configuration du message MIME
        Session session = getSession(); // Utilise la session réelle
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject(subject);
        message.setContent(emailContent, "text/html; charset=utf-8");

        Transport.send(message);
        System.out.println("📩 Email envoyé à " + recipientEmail);

    }
}
