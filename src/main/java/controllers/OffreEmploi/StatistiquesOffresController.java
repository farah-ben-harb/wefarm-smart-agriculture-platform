package controllers.OffreEmploi;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import services.ServiceOffreEmploi;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class StatistiquesOffresController implements Initializable {

    public BorderPane mainContainer;
    @FXML private BarChart<String, Number> saisonChart;
    @FXML private PieChart gouvernoratChart;

    private final ServiceOffreEmploi serviceOffre = new ServiceOffreEmploi();

    @FXML
    private FadeTransition fadeIn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurer l'animation
        fadeIn.setNode(mainContainer);

        try {
            chargerStatistiquesSaisons();
            chargerStatistiquesGouvernorats();

            // Démarrer l'animation
            fadeIn.play();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private void chargerStatistiquesSaisons() throws SQLException {
        Map<String, Double> stats = serviceOffre.getOffresParSaison();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pourcentage des offres d'emploi par saison");

        // Tableau des couleurs correspondant aux saisons
        String[] backgroundColors = {
                "rgba(45, 106, 79, 0.2)",  // Vert printemps
                "rgba(252, 163, 17, 0.2)",  // Orange été
                "rgba(255, 140, 0, 0.2)",   // Orange automne
                "rgba(0, 150, 136, 0.2)"    // Bleu hiver
        };

        String[] borderColors = {
                "rgba(45, 106, 79, 1)",
                "rgba(252, 163, 17, 1)",
                "rgba(255, 140, 0, 1)",
                "rgba(0, 150, 136, 1)"
        };

        int i = 0;
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(
                    getSaisonWithEmoji(entry.getKey()),
                    entry.getValue()
            );
            series.getData().add(data);

            // Appliquer le style après que le nœud est créé
            final int colorIndex = i;
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle(
                            "-fx-bar-fill: " + backgroundColors[colorIndex] + "; " +
                                    "-fx-stroke: " + borderColors[colorIndex] + "; " +
                                    "-fx-stroke-width: 1px;"
                    );
                }
            });
            i++;
        }

        saisonChart.getData().add(series);

        // Configuration supplémentaire du graphique
        saisonChart.setCategoryGap(20); // Espacement entre les barres
        saisonChart.setLegendVisible(false);

        // Animation des barres
        animateBarChart();
    }

    private void animateBarChart() {
        for (XYChart.Series<String, Number> series : saisonChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setScaleY(0);
                    ScaleTransition st = new ScaleTransition(Duration.millis(2000), node);
                    st.setFromY(0);
                    st.setToY(1);
                    st.play();
                }
            }
        }
    }

    private String getSaisonWithEmoji(String saison) {
        switch(saison) {
            case "Printemps": return "Printemps 🌱";
            case "Été": return "Été ☀";
            case "Automne": return "Automne 🍂";
            case "Hiver": return "Hiver ❄";
            default: return saison;
        }
    }

    private void chargerStatistiquesGouvernorats() throws SQLException {
        Map<String, Double> stats = serviceOffre.getOffresParGouvernorat();

        // Couleurs alternées
        boolean alt = true;
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            PieChart.Data data = new PieChart.Data(
                    entry.getKey(),
                    entry.getValue()
            );
            gouvernoratChart.getData().add(data);

            // Appliquer les couleurs
            if (alt) {
                data.getNode().setStyle("-fx-pie-color: #20c997;");
            } else {
                data.getNode().setStyle("-fx-pie-color: rgba(246, 202, 119, 0.7);");
            }
            alt = !alt;
        }
    }
    private void animatePieChart() {
        gouvernoratChart.setLabelsVisible(false); // Cachez les labels pendant l'animation

        Timeline timeline = new Timeline();
        final double maxTime = 2000.0; // Durée totale de l'animation (2 secondes)

        for (PieChart.Data data : gouvernoratChart.getData()) {
            // Sauvegarder la valeur originale
            final double originalValue = data.getPieValue();

            // Définir la valeur à 0 pour l'animation
            data.setPieValue(0);

            // Créer une KeyFrame pour cette donnée
            KeyValue keyValue = new KeyValue(data.pieValueProperty(), originalValue, Interpolator.EASE_OUT);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(maxTime), keyValue);
            timeline.getKeyFrames().add(keyFrame);
        }

        // Quand l'animation est terminée, afficher les labels
        timeline.setOnFinished(event -> {
            gouvernoratChart.setLabelsVisible(true);

            // Animation supplémentaire pour les labels
            for (Node node : gouvernoratChart.lookupAll(".chart-pie-label")) {
                node.setOpacity(0);
                FadeTransition ft = new FadeTransition(Duration.millis(500), node);
                ft.setToValue(1);
                ft.play();
            }
        });

        timeline.play();
    }
}