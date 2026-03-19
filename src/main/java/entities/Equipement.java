package entities;

import java.util.Objects;

public class Equipement {
    private int id;
    private Integer fournisseurId;
    private String nomEquipement;
    private int quantiteDisponible;
    private double prix;
    private String image;
    private String category;

    // Constructeur sans paramètre
    public Equipement() {
    }

    // Constructeur avec tous les paramètres
    public Equipement(int id, Integer fournisseurId, String nomEquipement, int quantiteDisponible, double prix, String image, String category) {
        this.id = id;
        this.fournisseurId = fournisseurId;
        this.nomEquipement = nomEquipement;
        this.quantiteDisponible = quantiteDisponible;
        this.prix = prix;
        this.image = image;
        this.category = category;
    }

    // Constructeur sans ID (cas où l'ID est généré automatiquement)
    public Equipement(Integer fournisseurId, String nomEquipement, int quantiteDisponible, double prix, String image, String category) {
        this.fournisseurId = fournisseurId;
        this.nomEquipement = nomEquipement;
        this.quantiteDisponible = quantiteDisponible;
        this.prix = prix;
        this.image = image;
        this.category = category;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(Integer fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    public String getNomEquipement() {
        return nomEquipement;
    }

    public void setNomEquipement(String nomEquipement) {
        this.nomEquipement = nomEquipement;
    }

    public int getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public void setQuantiteDisponible(int quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Equipement{" +
                "id=" + id +
                ", fournisseurId=" + fournisseurId +
                ", nomEquipement='" + nomEquipement + '\'' +
                ", quantiteDisponible=" + quantiteDisponible +
                ", prix=" + prix +
                ", image='" + image + '\'' +
                ", category='" + category + '\'' +
                '}';
    }

    // Méthodes equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipement that = (Equipement) o;
        return id == that.id &&
                quantiteDisponible == that.quantiteDisponible &&
                Double.compare(that.prix, prix) == 0 &&
                Objects.equals(fournisseurId, that.fournisseurId) &&
                Objects.equals(nomEquipement, that.nomEquipement) &&
                Objects.equals(image, that.image) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fournisseurId, nomEquipement, quantiteDisponible, prix, image, category);
    }
}
