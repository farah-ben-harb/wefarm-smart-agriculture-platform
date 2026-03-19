package controllers.Event;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import services.ReservationService;
import utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class StatistiquesReservationsController implements Initializable {

    @FXML private BorderPane mainContainer;
    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final ReservationService reservationService = new ReservationService();
    private final int currentUserId = SessionManager.getInstance().getId();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), mainContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        try {
            chargerStatistiques();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chargerStatistiques() throws SQLException {
        Map<String, Integer> stats = reservationService.getNombreReservationsParEvenementPourAgriculteur(currentUserId);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations");

        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: #2d6a4f; -fx-border-color: #1b4332;");
                    animateBar(newNode);
                }
            });
        }

        barChart.getData().clear();
        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        barChart.setCategoryGap(20);
    }

    private void animateBar(Node node) {
        node.setScaleY(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(1000), node);
        st.setFromY(0);
        st.setToY(1);
        st.play();
    }
}
