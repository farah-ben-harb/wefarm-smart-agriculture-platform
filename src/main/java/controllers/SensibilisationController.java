package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import java.net.URL;

public class SensibilisationController {

    public Button playButton;
    @FXML
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;

    @FXML
    private void initialize() {
        loadVideo();
    }

    private void loadVideo() {
        try {
            URL videoUrl = getClass().getResource("/media/agriculture-durable.mp4");
            if (videoUrl != null) {
                Media media = new Media(videoUrl.toString());
                mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);
                mediaView.setPreserveRatio(true);

                // Réinitialiser le lecteur et le bouton quand la vidéo se termine
                mediaPlayer.setOnEndOfMedia(() -> {
                    mediaPlayer.stop();
                    playButton.setText("▶ Lire la vidéo"); // Réinitialiser le texte du bouton
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vidéo");
        }
    }

    @FXML
    private void playVideo(ActionEvent event) {
        if (mediaPlayer != null) {
            Button button = (Button) event.getSource();

            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                button.setText("▶ Lire la vidéo");
            } else {
                mediaPlayer.play();
                button.setText("⏸ Pause");
            }
        }
    }


    @FXML
    private void goBackToLogin(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/user/login.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Image icon = new Image(getClass().getResourceAsStream("/icons/fermier.png"));
            stage.getIcons().add(icon);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}