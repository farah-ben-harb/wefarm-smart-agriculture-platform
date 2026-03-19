package entities;

import java.util.Date;
import java.util.Objects;

public class Ad {
    private int id;
    private String imageUrl;
    private Date startDate;
    private Date endDate;
    private boolean isActive;
    private Date updatedAt;

    // Constructeur sans paramètre
    public Ad() {
    }

    // Constructeur avec tous les paramètres
    public Ad(int id, String imageUrl, Date startDate, Date endDate, boolean isActive, Date updatedAt) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.updatedAt = updatedAt;
    }

    // Constructeur sans ID (cas où l'ID est généré automatiquement)
    public Ad(String imageUrl, Date startDate, Date endDate, boolean isActive, Date updatedAt) {
        this.imageUrl = imageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.updatedAt = updatedAt;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Ad{" +
                "id=" + id +
                ", imageUrl='" + imageUrl + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isActive=" + isActive +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // Méthodes equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ad ad = (Ad) o;
        return id == ad.id &&
                isActive == ad.isActive &&
                Objects.equals(imageUrl, ad.imageUrl) &&
                Objects.equals(startDate, ad.startDate) &&
                Objects.equals(endDate, ad.endDate) &&
                Objects.equals(updatedAt, ad.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, imageUrl, startDate, endDate, isActive, updatedAt);
    }
}

