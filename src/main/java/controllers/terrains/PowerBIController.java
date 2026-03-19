package controllers.terrains;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import services.PowerBIService;

import java.net.URL;
import java.util.ResourceBundle;

public class PowerBIController implements Initializable {

    @FXML
    private WebView webView;

    private final PowerBIService powerBIService = new PowerBIService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        WebEngine webEngine = webView.getEngine();

        // ➤ Obtenir le lien depuis le service
        String reportUrl = powerBIService.getPublicReportUrl();

        // ➤ Charger le rapport dans le WebView
        webEngine.load(reportUrl);

        // ➤ Rafraîchir toutes les 15 secondes
        Timeline refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(15), event -> webEngine.reload())
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }
}
