package controllers.User;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import services.UtilisateurService;

import java.time.LocalDate;
import java.util.Map;

public class StatsCnxController {

    @FXML
    private Label labelNbConnexions;
    @FXML
    private Label labelNbInscriptions;
    @FXML
    private PieChart chartUserTypes;
    @FXML
    private PieChart chartUserStatus;

    private final UtilisateurService userService = new UtilisateurService();

    @FXML
    public void initialize() {
        chargerStatistiques();
    }

    private void chargerStatistiques() {
        // Statistiques des connexions
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = LocalDate.now();
        long totalConnexions = userService.getDailyConnections(debutMois, finMois)
                .values().stream().mapToLong(Long::longValue).sum();

        // Statistiques des inscriptions
        long totalInscriptions = userService.getMonthlyRegistrations(debutMois, finMois)
                .values().stream().mapToLong(Long::longValue).sum();

        // Répartition par type d'utilisateur
        Map<String, Long> statsTypes = userService.getUserTypesDistribution();
        chartUserTypes.getData().clear();

        statsTypes.forEach((type, count) -> {
            PieChart.Data slice = new PieChart.Data(
                    String.format("%s (%d)", type, count),
                    count
            );
            chartUserTypes.getData().add(slice);
        });

        // Personnalisation du premier graphique
        chartUserTypes.setLegendVisible(true);

        // Répartition par statut de compte
        Map<String, Long> statsStatus = userService.getUserStatusDistribution();
        chartUserStatus.getData().clear();

        statsStatus.forEach((status, count) -> {
            // Traduction des statuts pour un affichage plus clair
            String displayStatus = "";
            switch(status.toLowerCase()) {
                case "active": displayStatus = "Actif"; break;
                case "inactive": displayStatus = "Désactivé"; break;
                case "pending": displayStatus = "En attente"; break;
                default: displayStatus = status;
            }

            PieChart.Data slice = new PieChart.Data(
                    String.format("%s (%d)", displayStatus, count),
                    count
            );
            chartUserStatus.getData().add(slice);
        });

        // Personnalisation du deuxième graphique
        chartUserStatus.setLegendVisible(true);
    }
}