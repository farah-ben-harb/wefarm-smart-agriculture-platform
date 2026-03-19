package entities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class OffreEmploi {
    private int id;
    private Integer userId; // Peut être null
    private String titre;
    private String description;
    private int nombrePostes;
    private Date dateDebut;
    private Date dateFinEstimee;
    private String status;
    private String localisation;
    private Date datePublication;
    private String competencesRequises;
    private double salaire;

    // Constructeur sans paramètre
    public OffreEmploi() {
    }

    // Constructeur avec tous les paramètres
    public OffreEmploi(int id, Integer userId, String titre, String description, int nombrePostes, Date dateDebut,
                       Date dateFinEstimee, String status, String localisation, Date datePublication,
                       String competencesRequises, double salaire) {
        this.id = id;
        this.userId = userId;
        this.titre = titre;
        this.description = description;
        this.nombrePostes = nombrePostes;
        this.dateDebut = dateDebut;
        this.dateFinEstimee = dateFinEstimee;
        this.status = status;
        this.localisation = localisation;
        this.datePublication = datePublication;
        this.competencesRequises = competencesRequises;
        this.salaire = salaire;
    }

    // Constructeur sans ID
    public OffreEmploi(Integer userId, String titre, String description, int nombrePostes, Date dateDebut,
                       Date dateFinEstimee, String status, String localisation, Date datePublication,
                       String competencesRequises, double salaire) {
        this.userId = userId;
        this.titre = titre;
        this.description = description;
        this.nombrePostes = nombrePostes;
        this.dateDebut = dateDebut;
        this.dateFinEstimee = dateFinEstimee;
        this.status = status;
        this.localisation = localisation;
        this.datePublication = datePublication;
        this.competencesRequises = competencesRequises;
        this.salaire = salaire;
    }
    //Constructeur pour l'ajout
    public OffreEmploi(String titre, String description, int nombrePostes, Date dateDebut, Date dateFinEstimee,
                       String localisation, String competencesRequises, double salaire) {
        this.titre = titre;
        this.description = description;
        this.nombrePostes = nombrePostes;
        this.dateDebut = dateDebut;
        this.dateFinEstimee = dateFinEstimee;
        this.localisation = localisation;
        this.competencesRequises = competencesRequises;
        this.salaire = salaire;
        this.status = "actif";
        this.datePublication = new Date(System.currentTimeMillis());
        this.userId = 2; // ID utilisateur statique pour l'instant
    }
    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNombrePostes() {
        return nombrePostes;
    }

    public void setNombrePostes(int nombrePostes) {
        this.nombrePostes = nombrePostes;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFinEstimee() {
        return dateFinEstimee;
    }

    public void setDateFinEstimee(Date dateFinEstimee) {
        this.dateFinEstimee = dateFinEstimee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public Date getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(Date datePublication) {
        this.datePublication = datePublication;
    }

    public String getCompetencesRequises() {
        return competencesRequises;
    }

    public void setCompetencesRequises(String competencesRequises) {
        this.competencesRequises = competencesRequises;
    }

    public double getSalaire() {
        return salaire;
    }

    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "OffreEmploi{" +
                "id=" + id +
                ", userId=" + userId +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", nombrePostes=" + nombrePostes +
                ", dateDebut=" + dateDebut +
                ", dateFinEstimee=" + dateFinEstimee +
                ", status='" + status + '\'' +
                ", localisation='" + localisation + '\'' +
                ", datePublication=" + datePublication +
                ", competencesRequises='" + competencesRequises + '\'' +
                ", salaire=" + salaire +
                '}';
    }

    // Méthode equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OffreEmploi that = (OffreEmploi) o;
        return id == that.id && nombrePostes == that.nombrePostes && Double.compare(that.salaire, salaire) == 0 && Objects.equals(userId, that.userId) && Objects.equals(titre, that.titre) && Objects.equals(description, that.description) && Objects.equals(dateDebut, that.dateDebut) && Objects.equals(dateFinEstimee, that.dateFinEstimee) && Objects.equals(status, that.status) && Objects.equals(localisation, that.localisation) && Objects.equals(datePublication, that.datePublication) && Objects.equals(competencesRequises, that.competencesRequises);
    }

    // Méthode hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id, userId, titre, description, nombrePostes, dateDebut, dateFinEstimee, status, localisation, datePublication, competencesRequises, salaire);
    }
    //Partie Admin
    public String getDateDebutFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(dateDebut);
    }

    public String getDateFinFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(dateFinEstimee);
    }

    public String getStatusDisplay() {
        return "Actif".equalsIgnoreCase(status) ? "Actif" : "Inactif";
    }
}