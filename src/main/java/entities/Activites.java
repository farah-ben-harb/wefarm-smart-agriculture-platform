package entities;

import java.util.Date;
import java.util.Objects;

public class Activites {

    private int id;
    private int utilisateurId;
    private String action;
    private String metaData;
    private Date date;

    // Constructeur sans paramètre
    public Activites() {
    }

    // Constructeur avec tous les paramètres
    public Activites(int id, int utilisateurId, String action, String metaData, Date date) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.action = action;
        this.metaData = metaData;
        this.date = date;
    }

    // Constructeur sans ID
    public Activites(int utilisateurId, String action, String metaData, Date date) {
        this.utilisateurId = utilisateurId;
        this.action = action;
        this.metaData = metaData;
        this.date = date;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Activites{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", action='" + action + '\'' +
                ", metaData='" + metaData + '\'' +
                ", date=" + date +
                '}';
    }

    // Méthode equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activites activites = (Activites) o;
        return id == activites.id && utilisateurId == activites.utilisateurId && Objects.equals(action, activites.action) && Objects.equals(metaData, activites.metaData) && Objects.equals(date, activites.date);
    }

    // Méthode hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id, utilisateurId, action, metaData, date);
    }
}
