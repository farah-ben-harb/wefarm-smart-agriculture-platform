package entities;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

public class Evenement {
    private int id;
    private Integer agriculteurId;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private String typeVisite;
    private String localisation;
    private double prix;

    // Constructeur sans paramètre
    public Evenement() {
    }

    // Constructeur avec tous les paramètres
    public Evenement(int id, Integer agriculteurId, String titre, String description, LocalDateTime dateDebut, String typeVisite, String localisation, double prix) {
        this.id = id;
        this.agriculteurId = agriculteurId;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.typeVisite = typeVisite;
        this.localisation = localisation;
        this.prix = prix;
    }

    // Constructeur sans ID (cas où l'ID est généré automatiquement)
    public Evenement(Integer agriculteurId, String titre, String description, LocalDateTime dateDebut, String typeVisite, String localisation, double prix) {
        this.agriculteurId = agriculteurId;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.typeVisite = typeVisite;
        this.localisation = localisation;
        this.prix = prix;
    }

    public Evenement(int id, int agriculteurId, String titre, String description, LocalDateTime dateDebut, String typeVisite, String localisation, double prix) {
        this.id = id;
        this.agriculteurId = agriculteurId;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.typeVisite = typeVisite;
        this.localisation = localisation;
        this.prix = prix;
    }



    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getAgriculteurId() {
        return agriculteurId;
    }

    public void setAgriculteurId(Integer agriculteurId) {
        this.agriculteurId = agriculteurId;
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

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getTypeVisite() {
        return typeVisite;
    }

    public void setTypeVisite(String typeVisite) {
        this.typeVisite = typeVisite;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", agriculteurId=" + agriculteurId +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", dateDebut=" + dateDebut +
                ", typeVisite='" + typeVisite + '\'' +
                ", localisation='" + localisation + '\'' +
                ", prix=" + prix +
                '}';
    }

    // Méthodes equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evenement evenement = (Evenement) o;
        return id == evenement.id &&
                Double.compare(evenement.prix, prix) == 0 &&
                Objects.equals(agriculteurId, evenement.agriculteurId) &&
                Objects.equals(titre, evenement.titre) &&
                Objects.equals(description, evenement.description) &&
                Objects.equals(dateDebut, evenement.dateDebut) &&
                Objects.equals(typeVisite, evenement.typeVisite) &&
                Objects.equals(localisation, evenement.localisation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, agriculteurId, titre, description, dateDebut, typeVisite, localisation, prix);
    }
}

