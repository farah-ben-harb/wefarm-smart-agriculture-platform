package entities;

import java.util.Objects;

public class Terrain {
    private int id;
    private Integer ownerId; // Peut être NULL
    private String adresse;
    private double surface;
    private double prix;
    private String status;
    private String description;
    private double latitude;
    private double longitude;
    private String typeSol;
    private String photos; // Peut être NULL

    // Constructeur sans paramètre
    public Terrain() {
    }

    // Constructeur avec tous les paramètres sauf l'ID (car il est auto-incrémenté)
    public Terrain(Integer ownerId, String adresse, double surface, double prix, String status, String description,
                   double latitude, double longitude, String typeSol, String photos) {
        this.ownerId = ownerId;
        this.adresse = adresse;
        this.surface = surface;
        this.prix = prix;
        this.status = status;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.typeSol = typeSol;
        this.photos = photos;
    }

    // Constructeur avec tous les paramètres (y compris l'ID)
    public Terrain(int id, Integer ownerId, String adresse, double surface, double prix, String status, String description,
                   double latitude, double longitude, String typeSol, String photos) {
        this.id = id;
        this.ownerId = ownerId;
        this.adresse = adresse;
        this.surface = surface;
        this.prix = prix;
        this.status = status;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.typeSol = typeSol;
        this.photos = photos;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public double getSurface() {
        return surface;
    }

    public void setSurface(double surface) {
        this.surface = surface;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTypeSol() {
        return typeSol;
    }

    public void setTypeSol(String typeSol) {
        this.typeSol = typeSol;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Terrain{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", adresse='" + adresse + '\'' +
                ", surface=" + surface +
                ", prix=" + prix +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", typeSol='" + typeSol + '\'' +
                ", photos='" + photos + '\'' +
                '}';
    }

    // Méthodes equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Terrain terrain = (Terrain) o;
        return id == terrain.id &&
                Double.compare(terrain.surface, surface) == 0 &&
                Double.compare(terrain.prix, prix) == 0 &&
                Double.compare(terrain.latitude, latitude) == 0 &&
                Double.compare(terrain.longitude, longitude) == 0 &&
                Objects.equals(ownerId, terrain.ownerId) &&
                Objects.equals(adresse, terrain.adresse) &&
                Objects.equals(status, terrain.status) &&
                Objects.equals(description, terrain.description) &&
                Objects.equals(typeSol, terrain.typeSol) &&
                Objects.equals(photos, terrain.photos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerId, adresse, surface, prix, status, description, latitude, longitude, typeSol, photos);
    }
}

