package services;

import entities.Produit;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteService {
    private final ProduitService produitService;

    public FavoriteService() {
        this.produitService = new ProduitServiceImpl();
    }

    public void addToFavorites(int userId, int produitId) {
        String query = "INSERT INTO favorites (user_id, produit_id, created_at) VALUES (?, ?, NOW())";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            System.out.println("Adding to favorites - User ID: " + userId + ", Product ID: " + produitId);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, produitId);
            
            int result = pstmt.executeUpdate();
            System.out.println("Insert result: " + result + " row(s) affected");
            
            if (result != 1) {
                throw new SQLException("Failed to add to favorites");
            }
            System.out.println("Successfully added to favorites");
            
        } catch (SQLIntegrityConstraintViolationException e) {
            // Product is already in favorites - this is fine, just ignore
            System.out.println("Product already in favorites: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error adding to favorites: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error adding to favorites: " + e.getMessage());
        }
    }

    public void removeFromFavorites(int userId, int produitId) {
        String query = "DELETE FROM favorites WHERE user_id = ? AND produit_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, produitId);
            
            int result = pstmt.executeUpdate();
            if (result != 1) {
                System.out.println("No favorite found to remove");
            }
        } catch (SQLException e) {
            System.err.println("Error removing from favorites: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error removing from favorites: " + e.getMessage());
        }
    }

    public List<Produit> getFavoriteProducts(int userId) {
        List<Produit> favorites = new ArrayList<>();
        String query = "SELECT p.* FROM produit p " +
                      "INNER JOIN favorites f ON p.id = f.produit_id " +
                      "WHERE f.user_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            System.out.println("Getting favorites for User ID: " + userId);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nom"));
                produit.setQuantite(rs.getInt("quantite"));
                produit.setPrixTotal(rs.getDouble("prix_total"));
                produit.setDescription(rs.getString("description"));
                produit.setImage(rs.getString("image"));
                produit.setCategorie(rs.getString("categorie"));
                produit.setStockD(rs.getString("stock_d"));
                produit.setApproved(rs.getBoolean("approved"));
                produit.setUpdatedAt(rs.getTimestamp("updated_at"));
                favorites.add(produit);
                System.out.println("Found favorite product: " + produit.getNom());
            }
            
            System.out.println("Total favorites found: " + favorites.size());
            
        } catch (SQLException e) {
            System.err.println("Error getting favorite products: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error getting favorite products: " + e.getMessage());
        }
        
        return favorites;
    }

    public boolean isFavorite(int userId, int produitId) {
        String query = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND produit_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, produitId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking favorite status: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error checking favorite status: " + e.getMessage());
        }
        
        return false;
    }

    public void toggleFavorite(int userId, int produitId) {
        if (isFavorite(userId, produitId)) {
            removeFromFavorites(userId, produitId);
        } else {
            addToFavorites(userId, produitId);
        }
    }
} 