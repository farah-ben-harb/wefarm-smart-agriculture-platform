package services;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import entities.Commande;

public class StripePaymentService {
    private static final String STRIPE_SECRET_KEY = "sk_test_51Qz8co064LyKp30Px4MUaaBK1cWWmPgIRZo0EevaInr3LWcYY6WQ2jToIG78E11O7WS3LJwMCdRhsiamxzDVoFqc002fyND85N";
    private static final String STRIPE_PUBLIC_KEY = "pk_test_51Qz8co064LyKp30PvI4HN9ibEIQPmXxYgCOdmYKdHqXpodYQ61YWtEMiCp5QmpdIwJDqLhGqkSNdytFFjNHwubvr00Ji9BtLJc";

    public StripePaymentService() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    public String getPublicKey() {
        return STRIPE_PUBLIC_KEY;
    }

    public PaymentIntent createPaymentIntent(Commande commande) throws Exception {
        // Convert price to cents (Stripe uses smallest currency unit)
        long amount = Math.round(commande.getPrixTotal() * 100);
        
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amount)
            .setCurrency("eur")
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .putMetadata("order_id", String.valueOf(commande.getId()))
            .build();

        return PaymentIntent.create(params);
    }
} 