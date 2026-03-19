package services;

import com.itextpdf.text.*;
import entities.Commande;
import entities.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import javax.swing.text.Document;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import java.util.Date;
import java.util.Map;

import javafx.scene.Node;  // ✅ Pour les éléments JavaFX de base
import javafx.scene.Node;// ✅ Alternative plus courante

public class StatisticsService {
    private final ProduitService produitService;
    private final CommandeService commandeService;

    public StatisticsService() {
        this.produitService = new ProduitServiceImpl();
        this.commandeService = new CommandeServiceImpl(this.produitService);
    }

    public ObservableList<PieChart.Data> getProductsByCategory() {
        Map<String, Long> categoryCount = produitService.getAllProduits().stream()
            .collect(Collectors.groupingBy(
                Produit::getCategorie,
                Collectors.counting()
            ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        categoryCount.forEach((category, count) -> 
            pieChartData.add(new PieChart.Data(category, count))
        );
        return pieChartData;
    }

    public ObservableList<PieChart.Data> getStockStatus() {
        Map<String, Long> stockStatus = produitService.getAllProduits().stream()
            .collect(Collectors.groupingBy(
                Produit::getStockD,
                Collectors.counting()
            ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        stockStatus.forEach((status, count) -> 
            pieChartData.add(new PieChart.Data(status, count))
        );
        return pieChartData;
    }

    public XYChart.Series<String, Number> getProductValueByCategory() {
        Map<String, Double> categoryValue = produitService.getAllProduits().stream()
            .collect(Collectors.groupingBy(
                Produit::getCategorie,
                Collectors.summingDouble(p -> p.getPrixTotal() * p.getQuantite())
            ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Valeur Totale");
        categoryValue.forEach((category, value) -> 
            series.getData().add(new XYChart.Data<>(category, value))
        );
        return series;
    }

    public XYChart.Series<String, Number> getMonthlySales(String period) {
        LocalDate startDate = getStartDate(period);
        List<Commande> commandes = commandeService.getAllCommandes().stream()
            .filter(c -> isAfterDate(c.getDateCommande(), startDate))
            .collect(Collectors.toList());

        Map<String, Double> monthlySales = commandes.stream()
            .collect(Collectors.groupingBy(
                c -> getMonthYear(c.getDateCommande()),
                Collectors.summingDouble(Commande::getPrixTotal)
            ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes Mensuelles");
        monthlySales.forEach((month, sales) -> 
            series.getData().add(new XYChart.Data<>(month, sales))
        );
        return series;
    }

    public XYChart.Series<String, Number> getSalesByCategory(String period) {
        LocalDate startDate = getStartDate(period);
        List<Commande> commandes = commandeService.getAllCommandes().stream()
            .filter(c -> isAfterDate(c.getDateCommande(), startDate))
            .collect(Collectors.toList());

        Map<String, Double> categorySales = new HashMap<>();
        for (Commande commande : commandes) {
            Produit produit = produitService.getProduitById(commande.getProduitId());
            if (produit != null) {
                categorySales.merge(
                    produit.getCategorie(),
                    commande.getPrixTotal(),
                    Double::sum
                );
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes par Catégorie");
        categorySales.forEach((category, sales) -> 
            series.getData().add(new XYChart.Data<>(category, sales))
        );
        return series;
    }

    private LocalDate getStartDate(String period) {
        LocalDate now = LocalDate.now();
        return switch (period) {
            case "7 derniers jours" -> now.minusDays(7);
            case "30 derniers jours" -> now.minusDays(30);
            case "Cette année" -> now.withDayOfYear(1);
            default -> now.minusYears(100); // "Tout"
        };
    }

    private boolean isAfterDate(Date date, LocalDate startDate) {
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .isAfter(startDate);
    }

    private String getMonthYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.format("%s %d",
            cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.FRANCE),
            cal.get(Calendar.YEAR)
        );
    }

    /*public void exportToPDF(File file) throws Exception {
        // Utilisez com.itextpdf.text.Document
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Rapport des Statistiques", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Add date
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
        Paragraph date = new Paragraph("Généré le: " + new Date(), normalFont);
        date.setSpacingAfter(20);
        document.add(date);

        // Products by Category
        document.add(new Paragraph("Produits par Catégorie:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        PdfPTable categoryTable = new PdfPTable(2);
        categoryTable.setWidthPercentage(100);
        categoryTable.addCell("Catégorie");
        categoryTable.addCell("Nombre de Produits");
        
        Map<String, Long> categoryCount = produitService.getAllProduits().stream()
            .collect(Collectors.groupingBy(
                Produit::getCategorie,
                Collectors.counting()
            ));
        
        categoryCount.forEach((category, count) -> {
            categoryTable.addCell(category);
            categoryTable.addCell(count.toString());
        });
        document.add(categoryTable);
        document.add(Chunk.NEWLINE);

        // Stock Status
        document.add(new Paragraph("État des Stocks:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        PdfPTable stockTable = new PdfPTable(2);
        stockTable.setWidthPercentage(100);
        stockTable.addCell("État");
        stockTable.addCell("Nombre de Produits");
        
        Map<String, Long> stockStatus = produitService.getAllProduits().stream()
            .collect(Collectors.groupingBy(
                Produit::getStockD,
                Collectors.counting()
            ));
        
        stockStatus.forEach((status, count) -> {
            stockTable.addCell(status);
            stockTable.addCell(count.toString());
        });
        document.add(stockTable);
        document.add(Chunk.NEWLINE);

        // Product Values
        document.add(new Paragraph("Valeur des Produits par Catégorie:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        PdfPTable valueTable = new PdfPTable(2);
        valueTable.setWidthPercentage(100);
        valueTable.addCell("Catégorie");
        valueTable.addCell("Valeur Totale (DT)");
        
        Map<String, Double> categoryValue = produitService.getAllProduits().stream()
            .collect(Collectors.groupingBy(
                Produit::getCategorie,
                Collectors.summingDouble(p -> p.getPrixTotal() * p.getQuantite())
            ));
        
        categoryValue.forEach((category, value) -> {
            valueTable.addCell(category);
            valueTable.addCell(String.format("%.2f", value));
        });
        document.add(valueTable);
        document.add(Chunk.NEWLINE);

        // Monthly Sales
        document.add(new Paragraph("Ventes Mensuelles:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        PdfPTable salesTable = new PdfPTable(2);
        salesTable.setWidthPercentage(100);
        salesTable.addCell("Mois");
        salesTable.addCell("Ventes (DT)");
        
        List<Commande> commandes = commandeService.getAllCommandes();
        Map<String, Double> monthlySales = commandes.stream()
            .collect(Collectors.groupingBy(
                c -> getMonthYear(c.getDateCommande()),
                Collectors.summingDouble(Commande::getPrixTotal)
            ));
        
        monthlySales.forEach((month, sales) -> {
            salesTable.addCell(month);
            salesTable.addCell(String.format("%.2f", sales));
        });
        document.add(salesTable);

        document.close();
    }*/

    public void exportToExcel(File file) {
        // TODO: Implement Excel export
        throw new UnsupportedOperationException("Export Excel pas encore implémenté");
    }
} 