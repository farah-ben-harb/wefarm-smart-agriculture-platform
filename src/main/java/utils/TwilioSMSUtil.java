package utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioSMSUtil {

    public static final String ACCOUNT_SID = "AC5f4156e59a209f58cd00c361662f6123";
    public static final String AUTH_TOKEN = "74fd061b07450694c1949094e4ed0a9c";
    public static final String TWILIO_PHONE_NUMBER = "++13365379134";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static void sendSMS(String toPhoneNumber, String messageBody) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    messageBody
            ).create();

            System.out.println("SMS envoyé avec SID: " + message.getSid());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du SMS: " + e.getMessage());
        }
    }
}