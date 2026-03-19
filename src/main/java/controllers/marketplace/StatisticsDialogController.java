package controllers.marketplace;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.StatisticsService;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class StatisticsDialogController implements Initializable {
    @FXML private ComboBox<String> periodCombo;
    @FXML private PieChart productsByCategoryChart;
    @FXML private PieChart stockStatusChart;
    @FXML private BarChart<String, Number> productValueChart;
    @FXML private LineChart<String, Number> monthlySalesChart;
    @FXML private BarChart<String, Number> salesByCategoryChart;
    
    private final StatisticsService statisticsService;
    
    public StatisticsDialogController() {
        this.statisticsService = new StatisticsService();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupPeriodComboBox();
        loadAllCharts();
    }
    
    private void setupPeriodComboBox() {
        periodCombo.setItems(FXCollections.observableArrayList(
            "7 derniers jours",
            "30 derniers jours",
            "Cette année",
            "Tout"
        ));
        periodCombo.setValue("30 derniers jours");
        periodCombo.setOnAction(e -> loadAllCharts());
    }
    
    private void loadAllCharts() {
        loadProductsByCategoryChart();
        loadStockStatusChart();
        loadProductValueChart();
        loadMonthlySalesChart();
        loadSalesByCategoryChart();
    }
    
    private void loadProductsByCategoryChart() {
        ObservableList<PieChart.Data> pieChartData = statisticsService.getProductsByCategory();
        productsByCategoryChart.setData(pieChartData);
    }
    
    private void loadStockStatusChart() {
        ObservableList<PieChart.Data> pieChartData = statisticsService.getStockStatus();
        stockStatusChart.setData(pieChartData);
    }
    
    private void loadProductValueChart() {
        XYChart.Series<String, Number> series = statisticsService.getProductValueByCategory();
        productValueChart.getData().clear();
        productValueChart.getData().add(series);
    }
    
    private void loadMonthlySalesChart() {
        XYChart.Series<String, Number> series = statisticsService.getMonthlySales(periodCombo.getValue());
        monthlySalesChart.getData().clear();
        monthlySalesChart.getData().add(series);
    }
    
    private void loadSalesByCategoryChart() {
        XYChart.Series<String, Number> series = statisticsService.getSalesByCategory(periodCombo.getValue());
        salesByCategoryChart.getData().clear();
        salesByCategoryChart.getData().add(series);
    }
    
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les statistiques");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Excel", "*.xlsx")
        );
        
        File file = fileChooser.showSaveDialog(periodCombo.getScene().getWindow());
        if (file != null) {
            try {
                if (file.getName().endsWith(".pdf")) {
                    //statisticsService.exportToPDF(file);
                } else {
                    statisticsService.exportToExcel(file);
                }
                showAlert("Succès", "Les statistiques ont été exportées avec succès.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de l'exportation: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadAllCharts();
    }
    
    @FXML
    private void handleClose() {
        ((Stage) periodCombo.getScene().getWindow()).close();
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 