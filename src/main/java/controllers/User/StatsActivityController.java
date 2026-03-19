package controllers.User;
import entities.Activites;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import services.ActivitiesService;

import java.util.List;
public class StatsActivityController {

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private ActivitiesService activitiesService;

    public void initialize() {
        activitiesService = new ActivitiesService();
        afficherStats();
    }

    private void afficherStats() {
        // Récupérer les données d'activités par semaine
        List<Activites> activities = activitiesService.getActivitesByPeriod("semaine");

        // Organiser les données par type d'action
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Activités par action");

        for (Activites activity : activities) {
            series.getData().add(new XYChart.Data<>(activity.getAction(), getActionCount(activity.getAction())));
        }

        barChart.getData().add(series);
    }


    private int getActionCount(String action) {
        // Calculer le nombre d'activités pour une action donnée, pour simplifier, on peut filtrer par type
        return activitiesService.getAllActivites().stream()
                .filter(a -> a.getAction().equals(action))
                .mapToInt(a -> 1)
                .sum();
    }
}
