package utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

public class CaptchaGenerator {
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int WIDTH = 150;
    private static final int HEIGHT = 50;

    public static CaptchaResult generateCaptcha() {
        String captchaText = generateRandomText(6);
        BufferedImage image = createImage(captchaText);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return new CaptchaResult(captchaText, new ByteArrayInputStream(baos.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CAPTCHA image", e);
        }
    }

    private static String generateRandomText(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private static BufferedImage createImage(String text) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Fond
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Texte
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(text, 20, 30);

        // Bruit
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }

        g.dispose();
        return image;
    }

    public static class CaptchaResult {
        private final String text;
        private final ByteArrayInputStream imageStream;

        public CaptchaResult(String text, ByteArrayInputStream imageStream) {
            this.text = text;
            this.imageStream = imageStream;
        }

        public String getText() { return text; }
        public ByteArrayInputStream getImageStream() { return imageStream; }
    }
}