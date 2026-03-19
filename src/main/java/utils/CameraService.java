package utils;

import com.github.sarxos.webcam.Webcam;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;

public class CameraService {
    private Webcam webcam;
    private ImageView imageView;
    private static final String CAPTURES_DIR = System.getProperty("user.home") + "/WebcamCaptures/";

//    public File captureHighQualityPhoto() throws Exception {
//        // Configuration de haute qualité
//        webcam.setViewSize(new Dimension(1280, 720)); // HD 720p
//        BufferedImage image = webcam.getImage();
//
//        // Vérification de la luminosité
//        if (isImageTooDark(image)) {
//            throw new Exception("Luminosité insuffisante");
//        }
//
//        File tempFile = File.createTempFile("face_", ".jpg");
//        ImageIO.write(image, "JPEG", tempFile);
//        return tempFile;
//    }

    private boolean isImageTooDark(BufferedImage image) {
        // Analyse de la luminosité moyenne
        long sum = 0;
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                sum += (color.getRed() + color.getGreen() + color.getBlue()) / 3;
            }
        }

        double avg = sum / (width * height);
        return avg < 50; // Seuil de luminosité
    }

    public CameraService(ImageView imageView) {
        this.imageView = imageView;
        this.webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new RuntimeException("Aucune webcam détectée");
        }
        webcam.open();
    }

    public void startCamera() {
        Thread cameraThread = new Thread(() -> {
            while (webcam.isOpen()) {
                BufferedImage frame = webcam.getImage();
                if (frame != null) {
                    Image fxImage = convertToFxImage(frame);
                    javafx.application.Platform.runLater(() ->
                            imageView.setImage(fxImage)
                    );
                }
                try { Thread.sleep(30); } catch (InterruptedException e) {}
            }
        });
        cameraThread.setDaemon(true);
        cameraThread.start();
    }

//    public File capturePhoto() throws Exception {
//        BufferedImage image = webcam.getImage();
//        File tempFile = File.createTempFile("face_auth_", ".jpg");
//
//        // Afficher le chemin complet pour débogage
//        System.out.println("Fichier temporaire créé : " + tempFile.getAbsolutePath());
//        ImageIO.write(image, "JPG", tempFile);
//
//
//        return tempFile;
//    }

    private Image convertToFxImage(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", out);
            return new Image(new java.io.ByteArrayInputStream(out.toByteArray()));
        } catch (Exception e) {
            return null;
        }
    }

    public void closeCamera() {
        webcam.close();
    }
    // Chemin du dossier où sauvegarder les captures

    public File captureHighQualityPhoto() throws Exception {
        webcam.setViewSize(new Dimension(1280, 720));
        BufferedImage image = webcam.getImage();

        if (isImageTooDark(image)) {
            throw new Exception("Luminosité insuffisante");
        }

        File outputFile = getOutputFile("hq_");
        ImageIO.write(image, "JPEG", outputFile);
        return outputFile;
    }

    public File capturePhoto() throws Exception {
        BufferedImage image = webcam.getImage();
        File outputFile = getOutputFile("capture_");
        ImageIO.write(image, "JPG", outputFile);
        System.out.println("Capture sauvegardée : " + outputFile.getAbsolutePath());

        return outputFile;
    }

    private File getOutputFile(String prefix) throws Exception {
        // Créer le dossier s'il n'existe pas
        Files.createDirectories(Paths.get(CAPTURES_DIR));

        // Générer un nom de fichier avec timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = prefix + timestamp + ".jpg";

        return new File(CAPTURES_DIR + filename);
    }
}