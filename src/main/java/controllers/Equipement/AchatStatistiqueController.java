package controllers.Equipement;

import entities.Achat;
import entities.Equipement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.AchatService;
import services.AchatServiceImpl;
import services.EquipementService;
import services.EquipementServiceImpl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AchatStatistiqueController {

    @FXML
    private BarChart<String, Number> monthlyBarChart;

    @FXML
    private PieChart categoryPieChart;

    @FXML
    private Button retourButton;

    @FXML
    private Label statistiqueTitle;

    private AchatService achatService = new AchatServiceImpl();
    private EquipementService equipementService = new EquipementServiceImpl();
    private ObservableList<Achat> achatList = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        loadAchatsFromDB();
        initializeCharts();
    }

    private void loadAchatsFromDB() {
        achatList.clear();
        achatList.addAll(achatService.getAllAchats());
    }

    private void initializeCharts() {
        initializeMonthlyBarChart();
        initializeCategoryPieChart();
    }

    private void initializeMonthlyBarChart() {

        CategoryAxis xAxis = (CategoryAxis) monthlyBarChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) monthlyBarChart.getYAxis();
        xAxis.setLabel("Mois");
        yAxis.setLabel("Montant total (DT)");


        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes mensuelles");


        Map<String, Integer> monthlyTotals = new LinkedHashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM-yyyy");
        

        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            String monthKey = monthFormat.format(cal.getTime());
            monthlyTotals.put(monthKey, 0);
            cal.add(Calendar.MONTH, -1);
        }
        

        for (Achat achat : achatList) {
            String monthKey = monthFormat.format(achat.getDate());
            if (monthlyTotals.containsKey(monthKey)) {
                monthlyTotals.put(monthKey, monthlyTotals.get(monthKey) + achat.getTotal());
            }
        }


        List<String> sortedMonths = new ArrayList<>(monthlyTotals.keySet());
        Collections.sort(sortedMonths);
        for (String month : sortedMonths) {
            series.getData().add(new XYChart.Data<>(month, monthlyTotals.get(month)));
        }


        monthlyBarChart.getData().add(series);
        monthlyBarChart.setAnimated(true);
        monthlyBarChart.setTitle("Ventes mensuelles");
    }

    private void initializeCategoryPieChart() {
        // Préparation des données par catégorie
        Map<String, Integer> categoryTotals = new HashMap<>();
        categoryTotals.put("Machines", 0);
        categoryTotals.put("Outils", 0);
        categoryTotals.put("Accessoires", 0);
        categoryTotals.put("Autres", 0);


        for (Achat achat : achatList) {
            Equipement equipement = equipementService.getEquipementById(achat.getEquipementId());
            if (equipement != null) {
                String category = equipement.getCategory();
                if (categoryTotals.containsKey(category)) {
                    categoryTotals.put(category, categoryTotals.get(category) + achat.getTotal());
                } else {
                    categoryTotals.put("Autres", categoryTotals.get("Autres") + achat.getTotal());
                }
            }
        }


        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }


        categoryPieChart.setData(pieChartData);
        categoryPieChart.setTitle("Répartition des ventes par catégorie");
        categoryPieChart.setLabelsVisible(true);
        categoryPieChart.setAnimated(true);
        

        int total = categoryTotals.values().stream().mapToInt(Integer::intValue).sum();
        if (total > 0) {
            pieChartData.forEach(data -> {
                double percentage = (data.getPieValue() / total) * 100;
                String text = String.format("%s: %.1f%%", data.getName(), percentage);
                data.setName(text);
            });
        }
    }

    @FXML
    void retourEquipements(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Equipement/equipementfront.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Navigation");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de naviguer vers la page des équipements: " + e.getMessage());
            alert.showAndWait();
        }
    }
}