package services;

public class PowerBIService {

    // ➤ Ton lien Power BI ici
    private static final String REPORT_URL = "https://app.powerbi.com/reportEmbed?reportId=be817dad-be65-4261-ab10-4c865465e7dc&autoAuth=true&ctid=604f1a96-cbe8-43f8-abbf-f8eaf5d85730";

    public String getPublicReportUrl() {
        return REPORT_URL;
    }
}
