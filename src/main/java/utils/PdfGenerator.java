package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import entities.Terrain;
import entities.Transaction;
import entities.Utilisateur;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PdfGenerator {

    public static File generateContract(Transaction transaction, Terrain terrain,
                                        Utilisateur agriculteur, Utilisateur client) throws IOException, DocumentException {
        Document document = new Document();
        String fileName = "contrat_transaction_" + transaction.getId() + ".pdf";
        File file = new File(fileName);

        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Titre
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("CONTRAT DE " + transaction.getType().toUpperCase(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Informations de base
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        // Date
        Paragraph date = new Paragraph("Fait le : " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()), contentFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        document.add(date);

        // Espace
        document.add(new Paragraph(" "));

        // Tableau des parties
        PdfPTable partiesTable = new PdfPTable(2);
        partiesTable.setWidthPercentage(100);

        addPartyCell(partiesTable, "Vendeur/Loueur", agriculteur, headerFont, contentFont);
        addPartyCell(partiesTable, "Acheteur/Locataire", client, headerFont, contentFont);

        document.add(partiesTable);
        document.add(new Paragraph(" "));

        // Détails du terrain
        PdfPTable terrainTable = new PdfPTable(2);
        terrainTable.setWidthPercentage(100);

        addTerrainDetails(terrainTable, "Adresse", terrain.getAdresse(), headerFont, contentFont);
        addTerrainDetails(terrainTable, "Surface", terrain.getSurface() + " m²", headerFont, contentFont);
        addTerrainDetails(terrainTable, "Type de sol", terrain.getTypeSol(), headerFont, contentFont);
        addTerrainDetails(terrainTable, "Prix", terrain.getPrix() + " DT", headerFont, contentFont);

        document.add(terrainTable);
        document.add(new Paragraph(" "));

        // Détails de la transaction
        PdfPTable transactionTable = new PdfPTable(2);
        transactionTable.setWidthPercentage(100);

        addTransactionDetails(transactionTable, "Montant", transaction.getMontant() + " DT", headerFont, contentFont);
        addTransactionDetails(transactionTable, "Date", transaction.getDateTransaction().toString(), headerFont, contentFont);
        addTransactionDetails(transactionTable, "Type", transaction.getType(), headerFont, contentFont);

        document.add(transactionTable);
        document.add(new Paragraph(" "));

        // Clause de signature
        Paragraph signature = new Paragraph("En foi de quoi, les parties ont signé le présent contrat.", contentFont);
        signature.setSpacingBefore(30);
        document.add(signature);

        document.close();
        return file;
    }

    private static void addPartyCell(PdfPTable table, String role, Utilisateur user, Font headerFont, Font contentFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph rolePara = new Paragraph(role + ":", headerFont);
        Paragraph namePara = new Paragraph(user.getFirstName() + " " + user.getLastName(), contentFont);
        Paragraph emailPara = new Paragraph(user.getEmail(), contentFont);

        cell.addElement(rolePara);
        cell.addElement(namePara);
        cell.addElement(emailPara);

        table.addCell(cell);
    }

    private static void addTerrainDetails(PdfPTable table, String label, String value, Font headerFont, Font contentFont) {
        table.addCell(createCell(label + ":", headerFont));
        table.addCell(createCell(value, contentFont));
    }

    private static void addTransactionDetails(PdfPTable table, String label, String value, Font headerFont, Font contentFont) {
        table.addCell(createCell(label + ":", headerFont));
        table.addCell(createCell(value, contentFont));
    }

    private static PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }
}