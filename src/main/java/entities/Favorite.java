package entities;

import java.util.Date;

public class Favorite {
    private int id;
    private int userId;
    private int produitId;
    private Date createdAt;

    // Default constructor
    public Favorite() {
    }

    // Constructor with all fields
    public Favorite(int id, int userId, int produitId, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.produitId = produitId;
        this.createdAt = createdAt;
    }

    // Constructor without id (for new favorites)
    public Favorite(int userId, int produitId, Date createdAt) {
        this.userId = userId;
        this.produitId = produitId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProduitId() {
        return produitId;
    }

    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Favorite{" +
                "id=" + id +
                ", userId=" + userId +
                ", produitId=" + produitId +
                ", createdAt=" + createdAt +
                '}';
    }
} 